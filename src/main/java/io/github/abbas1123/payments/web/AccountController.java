package io.github.abbas1123.payments.web;

import io.github.abbas1123.payments.dto.AccountResponse;
import io.github.abbas1123.payments.dto.CreateAccountRequest;
import io.github.abbas1123.payments.dto.TransactionResponse;
import io.github.abbas1123.payments.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management and lookups")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Open a new account")
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an account by id (Redis-cached)")
    public AccountResponse getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "List transactions where the account is sender or receiver")
    public List<TransactionResponse> getAccountTransactions(@PathVariable Long id) {
        return accountService.getAccountTransactions(id);
    }
}
