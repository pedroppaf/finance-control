package com.pedro.finance_control;

import com.pedro.finance_control.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUp() {
        // MockMvc é construído sob demanda para evitar detecção de autowiring pelo IDE.
    }

    private org.springframework.test.web.servlet.MockMvc mockMvc() {
        Filter springSecurityFilterChain = webApplicationContext.getBean("springSecurityFilterChain", Filter.class);
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        String email = "pedro-" + UUID.randomUUID() + "@email.com";
        RegisterRequest request = new RegisterRequest(
                "Pedro",
                email,
                "123456");

        mockMvc().perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(request.name(), request.email(), request.password())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldFailWhenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Pedro",
                "pedro@email.com",
                "123456");

        // primeiro cadastro
        mockMvc().perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(request.name(), request.email(), request.password())));

        // segundo cadastro (deve falhar)
        mockMvc().perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(request.name(), request.email(), request.password())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void shouldFailWhenInvalidData() throws Exception {
        String invalidJson = """
                    {
                        "name": "",
                        "email": "email-invalido",
                        "password": "123"
                    }
                """;

        mockMvc().perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        String email = "pedro-" + UUID.randomUUID() + "@email.com";
        // registra usuário
        RegisterRequest register = new RegisterRequest(
                "Pedro",
                email,
                "123456");

        mockMvc().perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(register.name(), register.email(), register.password())));

        String loginJson = """
                    {
                        "email": "%s",
                        "password": "123456"
                    }
                """.formatted(email);

        mockMvc().perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void shouldRefreshAndLogoutSuccessfully() throws Exception {
        String email = "pedro-" + UUID.randomUUID() + "@email.com";

        MvcResult register = mockMvc().perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Pedro", email, "123456")))
                .andExpect(status().isCreated())
                .andReturn();

        String accessToken = extractValue(register.getResponse().getContentAsString(), "token");
        String refreshToken = extractValue(register.getResponse().getContentAsString(), "refreshToken");

        String refreshJson = """
                    {
                        "refreshToken": "%s"
                    }
                """.formatted(refreshToken);

        MvcResult refreshed = mockMvc().perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        String newRefreshToken = extractValue(refreshed.getResponse().getContentAsString(), "refreshToken");

        String logoutJson = """
                    {
                        "refreshToken": "%s"
                    }
                """.formatted(newRefreshToken);

        mockMvc().perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(logoutJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailWhenNoToken() throws Exception {
        mockMvc().perform(get("/transactions"))
                .andExpect(status().isUnauthorized());
    }

    private String registerJson(String name, String email, String password) {
        return """
                {
                    "name": "%s",
                    "email": "%s",
                    "password": "%s"
                }
                """.formatted(name, email, password);
    }

    private String extractValue(String json, String field) {
        Pattern pattern = Pattern.compile("\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Field not found in JSON response: " + field);
    }


}
