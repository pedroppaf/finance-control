package com.pedro.finance_control;

import com.pedro.finance_control.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
        RegisterRequest request = new RegisterRequest(
                "Pedro",
                "pedro@email.com",
                "123456");

        mockMvc().perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(request.name(), request.email(), request.password())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists());
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
        // registra usuário
        RegisterRequest register = new RegisterRequest(
                "Pedro",
                "pedro@email.com",
                "123456");

        mockMvc().perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson(register.name(), register.email(), register.password())));

        String loginJson = """
                    {
                        "email": "pedro@email.com",
                        "password": "123456"
                    }
                """;

        mockMvc().perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
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


}
