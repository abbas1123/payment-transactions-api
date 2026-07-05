package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.domain.Account;
import io.github.abbas1123.payments.dto.AccountResponse;
import io.github.abbas1123.payments.dto.CreateAccountRequest;
import io.github.abbas1123.payments.dto.TransactionResponse;
import io.github.abbas1123.payments.exception.AccountNotFoundException;
import io.github.abbas1123.payments.repository.AccountRepository;
import io.github.abbas1123.payments.repository.TransactionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        Account account = new Account(request.ownerName(), request.currency(), request.initialBalance());
        return AccountResponse.from(accountRepository.save(account));
    }

    /**
     * Hot lookup — cached in Redis. Entries expire by TTL and are evicted
     * explicitly after a transfer touches the account.
     */
    @Cacheable(cacheNames = "accounts", key = "#accountId")
    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .map(AccountResponse::from)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAccountTransactions(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return transactionRepository
                .findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }
}
