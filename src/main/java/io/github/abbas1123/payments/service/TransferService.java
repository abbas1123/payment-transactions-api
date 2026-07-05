package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.dao.TransferDao;
import io.github.abbas1123.payments.domain.Transaction;
import io.github.abbas1123.payments.dto.TransferRequest;
import io.github.abbas1123.payments.dto.TransferResponse;
import io.github.abbas1123.payments.exception.AccountNotFoundException;
import io.github.abbas1123.payments.exception.TransferRejectedException;
import io.github.abbas1123.payments.repository.TransactionRepository;
import org.springframework.cache.CacheManager;
import org.springframework.cache.Cache;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransferService {

    private final TransferDao transferDao;
    private final TransactionRepository transactionRepository;
    private final TransactionEventPublisher eventPublisher;
    private final CacheManager cacheManager;

    public TransferService(TransferDao transferDao,
                           TransactionRepository transactionRepository,
                           TransactionEventPublisher eventPublisher,
                           CacheManager cacheManager) {
        this.transferDao = transferDao;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.cacheManager = cacheManager;
    }

    public TransferResponse transfer(TransferRequest request) {
        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new TransferRejectedException("SAME_ACCOUNT", "Cannot transfer to the same account");
        }

        TransferDao.TransferOutcome outcome =
                transferDao.transferFunds(request.fromAccountId(), request.toAccountId(), request.amount());

        if (!outcome.isOk()) {
            throw switch (outcome.status()) {
                case "ACCOUNT_NOT_FOUND" -> new AccountNotFoundException("One of the accounts does not exist");
                case "ACCOUNT_INACTIVE" -> new TransferRejectedException(outcome.status(), "Account is not active");
                case "INSUFFICIENT_FUNDS" -> new TransferRejectedException(outcome.status(),
                        "Insufficient funds to cover amount plus commission");
                default -> new TransferRejectedException(outcome.status(), "Transfer rejected: " + outcome.status());
            };
        }

        evictAccountCache(request.fromAccountId());
        evictAccountCache(request.toAccountId());

        BigDecimal commission = transactionRepository.findById(outcome.transactionId())
                .map(Transaction::getCommission)
                .orElse(null);

        TransferResponse response = new TransferResponse(
                outcome.transactionId(),
                request.fromAccountId(),
                request.toAccountId(),
                request.amount(),
                commission,
                "COMPLETED");

        eventPublisher.publishCompleted(response);
        return response;
    }

    private void evictAccountCache(Long accountId) {
        Cache cache = cacheManager.getCache("accounts");
        if (cache != null) {
            cache.evict(accountId);
        }
    }
}
