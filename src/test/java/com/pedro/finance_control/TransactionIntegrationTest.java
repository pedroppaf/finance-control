package com.pedro.finance_control;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class TransactionIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        Filter springSecurityFilterChain = webApplicationContext.getBean("springSecurityFilterChain", Filter.class);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateTransactionSuccessfully() throws Exception {
        String token = registerAndGetToken();

        String request = transactionJson("Salário", "Pagamento mensal", "5000.00", "RECEITA", "SALARIO", "2026-01-10");

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction created successfully"))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.title").value("Salário"))
                .andExpect(jsonPath("$.data.type").value("RECEITA"))
                .andExpect(jsonPath("$.data.category").value("SALARIO"));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionPayloadIsInvalid() throws Exception {
        String token = registerAndGetToken();

        String invalidJson = "{}";

        mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.path").value("/transactions"))
                .andExpect(jsonPath("$.message", containsString("required")));
    }

    @Test
    void shouldFindTransactionByIdSuccessfully() throws Exception {
        String token = registerAndGetToken();

        String request = transactionJson("Freelance", "Projeto extra", "1200.00", "RECEITA", "OUTROS", "2026-02-05");

        long id = createTransactionAndGetId(token, request);

        mockMvc.perform(get("/transactions/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value((int) id))
                .andExpect(jsonPath("$.data.title").value("Freelance"))
                .andExpect(jsonPath("$.data.amount").value(1200.00));
    }

    @Test
    void shouldReturnBadRequestWhenTransactionDoesNotExist() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(get("/transactions/{id}", 999999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    void shouldUpdateTransactionSuccessfully() throws Exception {
        String token = registerAndGetToken();

        String createRequest = transactionJson("Mercado", "Compra do mês", "300.00", "DESPESA", "ALIMENTACAO", "2026-03-01");

        long id = createTransactionAndGetId(token, createRequest);

        String updateRequest = transactionJson("Mercado Atualizado", "Compra do mês atualizada", "350.00", "DESPESA", "ALIMENTACAO", "2026-03-02");

        mockMvc.perform(put("/transactions/{id}", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction updated successfully"))
                .andExpect(jsonPath("$.data.id").value((int) id))
                .andExpect(jsonPath("$.data.title").value("Mercado Atualizado"))
                .andExpect(jsonPath("$.data.amount").value(350.00))
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    @Test
    void shouldDeleteTransactionSuccessfully() throws Exception {
        String token = registerAndGetToken();

        String request = transactionJson("Cinema", "Filme do fim de semana", "80.00", "DESPESA", "LAZER", "2026-04-10");

        long id = createTransactionAndGetId(token, request);

        mockMvc.perform(delete("/transactions/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));

        mockMvc.perform(get("/transactions/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Transaction not found"));
    }

    @Test
    void shouldListTransactionsWithFilters() throws Exception {
        String token = registerAndGetToken();

        String januaryIncome = transactionJson("Salário Janeiro", "Receita do mês", "5000.00", "RECEITA", "SALARIO", "2026-01-10");

        String marchIncome = transactionJson("Salário Março", "Receita fora do filtro", "5200.00", "RECEITA", "SALARIO", "2026-03-10");

        createTransactionAndGetId(token, januaryIncome);
        createTransactionAndGetId(token, marchIncome);

        mockMvc.perform(get("/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "RECEITA")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Salário Janeiro"))
                .andExpect(jsonPath("$.data.content[0].type").value("RECEITA"));
    }

    @Test
    void shouldReturnSummarySuccessfully() throws Exception {
        String token = registerAndGetToken();

        String income = transactionJson("Salário", "Receita principal", "5000.00", "RECEITA", "SALARIO", "2026-01-10");

        String expense = transactionJson("Aluguel", "Despesa fixa", "1200.00", "DESPESA", "MORADIA", "2026-01-11");

        createTransactionAndGetId(token, income);
        createTransactionAndGetId(token, expense);

        mockMvc.perform(get("/transactions/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receita").value(5000.0))
                .andExpect(jsonPath("$.despesa").value(1200.0))
                .andExpect(jsonPath("$.balance").value(3800.0));
    }

    private String registerAndGetToken() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@email.com";

        String registerRequest = registerJson("Pedro", email, "123456");

        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        return extractValue(result.getResponse().getContentAsString(), "token");
    }

    private long createTransactionAndGetId(String token, String request) throws Exception {
        MvcResult result = mockMvc.perform(post("/transactions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn();

        return Long.parseLong(extractValue(result.getResponse().getContentAsString(), "id"));
    }

    private String extractValue(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\":(?:\"([^\"]+)\"|(\\d+))");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
        }
        throw new IllegalStateException("Field not found in JSON response: " + field);
    }

    private String transactionJson(String title, String description, String amount, String type, String category, String date) {
        return """
                {
                    "title": "%s",
                    "description": "%s",
                    "amount": %s,
                    "type": "%s",
                    "category": "%s",
                    "date": "%s"
                }
                """.formatted(title, description, amount, type, category, date);
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