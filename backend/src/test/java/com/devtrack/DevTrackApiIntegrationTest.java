package com.devtrack;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
    "server.servlet.context-path=",
    "spring.datasource.url=jdbc:h2:mem:devtrack-it;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "devtrack.security.jwt-secret=devtrack-integration-test-secret-0123456789abcdef",
    "devtrack.security.cookie-secure=false",
    "devtrack.security.allowed-origins=http://localhost:5173"
})
@AutoConfigureMockMvc
class DevTrackApiIntegrationTest {
  @Autowired private MockMvc mockMvc;

  @Test
  void registrationPersistsUserAndIssuesHttpOnlyCookie() throws Exception {
    MvcResult registration = register(
        "Member " + UUID.randomUUID(), uniqueEmail(), "SecurePassphrase123");
    Cookie cookie = accessCookie(registration);
    org.junit.jupiter.api.Assertions.assertEquals("/api/v1", cookie.getPath());
    org.junit.jupiter.api.Assertions.assertTrue(
        registration.getResponse().getHeader("Set-Cookie").contains("HttpOnly"));

    mockMvc.perform(get("/v1/auth/me").cookie(cookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(containsString("@example.test")));
  }

  @Test
  void duplicateRegistrationIsRejectedAndPasswordValidationRuns() throws Exception {
    String email = uniqueEmail();
    register("First Member", email, "SecurePassphrase123");

    mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody("Second Member", email, "SecurePassphrase123")))
        .andExpect(status().isConflict());

    mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody("New Member", uniqueEmail(), "short")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors.password").exists());
  }

  @Test
  void privateRecordsRequireAuthenticationAndAreScopedToOwner() throws Exception {
    mockMvc.perform(get("/v1/problems")).andExpect(status().isUnauthorized());
    Cookie owner = accessCookie(register("Owner", uniqueEmail(), "SecurePassphrase123"));
    Cookie other = accessCookie(register("Other", uniqueEmail(), "SecurePassphrase123"));
    String problem = """
        {"title":"Two Sum","topic":"Arrays","difficulty":"EASY","platform":"LeetCode","status":"TODO"}
        """;
    MvcResult created = mockMvc.perform(post("/v1/problems")
            .cookie(owner)
            .contentType(MediaType.APPLICATION_JSON)
            .content(problem))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Two Sum"))
        .andReturn();
    String body = created.getResponse().getContentAsString(StandardCharsets.UTF_8);
    String problemId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
        .readTree(body).get("id").asText();

    mockMvc.perform(get("/v1/problems/" + problemId).cookie(other))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/v1/problems/" + problemId).cookie(owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("TODO"));
    mockMvc.perform(put("/v1/problems/" + problemId)
            .cookie(owner)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Two Sum","topic":"Arrays","difficulty":"EASY","platform":"LeetCode","status":"SOLVED"}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SOLVED"));
    mockMvc.perform(delete("/v1/problems/" + problemId).cookie(owner))
        .andExpect(status().isNoContent());
    mockMvc.perform(get("/v1/problems/" + problemId).cookie(owner))
        .andExpect(status().isNotFound());

    mockMvc.perform(get("/v1/dashboard/summary").cookie(owner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.problemTotals.total").value(0))
        .andExpect(jsonPath("$.recentActivity.length()").value(3));
  }

  @Test
  void projectAndSkillCreateOperationsPersistUserOwnedRecords() throws Exception {
    Cookie user = accessCookie(register("Builder", uniqueEmail(), "SecurePassphrase123"));
    mockMvc.perform(post("/v1/projects")
            .cookie(user)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"title":"Portfolio","technologies":["React","Spring"],"status":"IN_PROGRESS"}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.technologies.length()").value(2));
    mockMvc.perform(post("/v1/skills")
            .cookie(user)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"name":"Java","category":"Programming languages","proficiencyLevel":2,"targetLevel":4}
                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.targetLevel").value(4));
    mockMvc.perform(get("/v1/dashboard/summary").cookie(user))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectTotals.inProgress").value(1))
        .andExpect(jsonPath("$.skillTotals.total").value(1))
        .andExpect(jsonPath("$.goals[0].name").value("Java"));
  }

  private MvcResult register(String name, String email, String password) throws Exception {
    return mockMvc.perform(post("/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerBody(name, email, password)))
        .andExpect(status().isCreated())
        .andReturn();
  }

  private String registerBody(String name, String email, String password) {
    return """
        {"name":"%s","email":"%s","password":"%s"}
        """.formatted(name, email, password);
  }

  private String uniqueEmail() {
    return "member-" + UUID.randomUUID() + "@example.test";
  }

  private Cookie accessCookie(MvcResult result) {
    String header = result.getResponse().getHeader("Set-Cookie");
    org.junit.jupiter.api.Assertions.assertNotNull(header);
    String pair = java.util.Arrays.stream(header.split(";"))
        .map(String::trim)
        .filter(value -> value.startsWith("DEVTRACK_ACCESS="))
        .findFirst()
        .orElseThrow();
    Cookie cookie = new Cookie("DEVTRACK_ACCESS", pair.substring("DEVTRACK_ACCESS=".length()));
    cookie.setPath("/api/v1");
    return cookie;
  }
}
