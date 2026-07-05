package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.domain.Account;
import io.github.abbas1123.payments.dto.AccountResponse;
import io.github.abbas1123.payments.dto.CreateAccountRequest;
import io.github.abbas1123.payments.exception.AccountNotFoundException;
import io.github.abbas1123.payments.repository.AccountRepository;
import io.github.abbas1123.payments.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository, transactionRepository);
    }

    @Test
    void createAccountMapsRequestAndDefaults() {
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountResponse response = accountService.createAccount(
                new CreateAccountRequest("Abbas Ramazanov", "AZN", new BigDecimal("1000.00")));

        assertThat(response.ownerName()).isEqualTo("Abbas Ramazanov");
        assertThat(response.currency()).isEqualTo("AZN");
        assertThat(response.balance()).isEqualByComparingTo("1000.00");
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void getAccountReturnsMappedDto() {
        Account account = new Account("Test Merchant", "AZN", new BigDecimal("250.00"));
        when(accountRepository.findById(5L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(5L);

        assertThat(response.ownerName()).isEqualTo("Test Merchant");
        assertThat(response.balance()).isEqualByComparingTo("250.00");
    }

    @Test
    void getAccountThrowsWhenMissing() {
        when(accountRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getAccount(404L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("404");
    }

    @Test
    void transactionListingRequiresExistingAccount() {
        when(accountRepository.existsById(7L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.getAccountTransactions(7L))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
