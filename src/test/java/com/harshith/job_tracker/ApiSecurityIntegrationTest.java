package com.harshith.job_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiSecurityIntegrationTest {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":(\\d+)");

    @Autowired
    private MockMvc mockMvc;

    // ---------- helpers ----------

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }

    private String credentialsJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void register(String email, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, password)))
                .andExpect(status().isCreated());
    }

    private String login(String email, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = TOKEN_PATTERN.matcher(body);
        assertTrue(matcher.find(), "login response should contain an accessToken");
        return matcher.group(1);
    }

    private String newUserToken() throws Exception {
        String email = uniqueEmail();
        register(email, "StrongPass123");
        return login(email, "StrongPass123");
    }

    private String applicationJson(String company) {
        return "{\"companyName\":\"" + company + "\",\"jobTitle\":\"Java Developer\",\"appliedDate\":\"2026-10-10\"}";
    }

    private long createApplication(String token, String company) throws Exception {
        String body = mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson(company)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = ID_PATTERN.matcher(body);
        assertTrue(matcher.find(), "create response should contain an id");
        return Long.parseLong(matcher.group(1));
    }

    private String listBody(String token, String query) throws Exception {
        return mockMvc.perform(get("/api/applications" + query)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    // ---------- tests ----------

    @Test
    void applications_requireAToken() throws Exception {
        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_rejectsDuplicateEmail() throws Exception {
        String email = uniqueEmail();
        register(email, "StrongPass123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, "StrongPass123")))
                .andExpect(status().isConflict());
    }

    @Test
    void register_rejectsBadEmailAndShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson("not-an-email", "123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withWrongPassword_isUnauthorized() throws Exception {
        String email = uniqueEmail();
        register(email, "StrongPass123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentialsJson(email, "WrongPassword")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgedToken_isRejected() throws Exception {
        String token = newUserToken();

        mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_returnsTheLoggedInUsersEmail() throws Exception {
        String email = uniqueEmail();
        register(email, "StrongPass123");
        String token = login(email, "StrongPass123");

        String body = mockMvc.perform(get("/api/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(body.contains(email));
    }

    @Test
    void users_cannotSeeOrChangeEachOthersApplications() throws Exception {
        String tokenA = newUserToken();
        String tokenB = newUserToken();
        long applicationId = createApplication(tokenA, "SecretCo");

        // user B sees an empty list
        assertTrue(listBody(tokenB, "").contains("\"totalElements\":0"));

        // user B cannot open or delete user A's application: 404, not the data
        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // user A still has it
        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    @Test
    void createApplication_withEmptyCompany_isRejected() throws Exception {
        String token = newUserToken();

        mockMvc.perform(post("/api/applications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(applicationJson("")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusFilter_returnsOnlyMatchingApplications() throws Exception {
        String token = newUserToken();
        long firstId = createApplication(token, "Amazon");
        createApplication(token, "Netflix");

        mockMvc.perform(patch("/api/applications/" + firstId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"INTERVIEW\"}"))
                .andExpect(status().isOk());

        String body = listBody(token, "?status=INTERVIEW");
        assertTrue(body.contains("\"totalElements\":1"));
        assertTrue(body.contains("Amazon"));
    }

    @Test
    void pagination_splitsResultsIntoPages() throws Exception {
        String token = newUserToken();
        createApplication(token, "CompanyOne");
        createApplication(token, "CompanyTwo");
        createApplication(token, "CompanyThree");

        String body = listBody(token, "?page=0&size=2");

        assertTrue(body.contains("\"totalElements\":3"));
        assertTrue(body.contains("\"totalPages\":2"));
    }
}
