package io.github.abbas1123.payments.web;

import io.github.abbas1123.payments.dto.AccountResponse;
import io.github.abbas1123.payments.exception.AccountNotFoundException;
import io.github.abbas1123.payments.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AccountController(accountService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAccountReturnsJson() throws Exception {
        when(accountService.getAccount(100L)).thenReturn(new AccountResponse(
                100L, "Abbas Ramazanov", "AZN", new BigDecimal("1000.00"), "ACTIVE", Instant.now()));

        mockMvc.perform(get("/api/accounts/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.ownerName").value("Abbas Ramazanov"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void missingAccountProducesProblemDetail404() throws Exception {
        when(accountService.getAccount(404L)).thenThrow(new AccountNotFoundException(404L));

        mockMvc.perform(get("/api/accounts/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }
}
