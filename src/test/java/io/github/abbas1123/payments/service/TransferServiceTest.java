package io.github.abbas1123.payments.service;

import io.github.abbas1123.payments.dao.TransferDao;
import io.github.abbas1123.payments.domain.Transaction;
import io.github.abbas1123.payments.dto.TransferRequest;
import io.github.abbas1123.payments.dto.TransferResponse;
import io.github.abbas1123.payments.exception.AccountNotFoundException;
import io.github.abbas1123.payments.exception.TransferRejectedException;
import io.github.abbas1123.payments.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferDao transferDao;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionEventPublisher eventPublisher;

    @Mock
    private CacheManager cacheManager;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(transferDao, transactionRepository, eventPublisher, cacheManager);
    }

    @Test
    void rejectsTransferToSameAccount() {
        TransferRequest request = new TransferRequest(1L, 1L, new BigDecimal("50.00"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(TransferRejectedException.class)
                .hasMessageContaining("same account");

        verifyNoInteractions(transferDao, eventPublisher);
    }

    @Test
    void mapsInsufficientFundsToRejection() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("999.00"));
        when(transferDao.transferFunds(1L, 2L, new BigDecimal("999.00")))
                .thenReturn(new TransferDao.TransferOutcome(null, "INSUFFICIENT_FUNDS"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(TransferRejectedException.class)
                .satisfies(ex -> assertThat(((TransferRejectedException) ex).getCode())
                        .isEqualTo("INSUFFICIENT_FUNDS"));

        verify(eventPublisher, never()).publishCompleted(any());
    }

    @Test
    void mapsUnknownAccountToNotFound() {
        TransferRequest request = new TransferRequest(1L, 42L, new BigDecimal("10.00"));
        when(transferDao.transferFunds(1L, 42L, new BigDecimal("10.00")))
                .thenReturn(new TransferDao.TransferOutcome(null, "ACCOUNT_NOT_FOUND"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void mapsInactiveAccountToRejection() {
        TransferRequest request = new TransferRequest(1L, 3L, new BigDecimal("10.00"));
        when(transferDao.transferFunds(1L, 3L, new BigDecimal("10.00")))
                .thenReturn(new TransferDao.TransferOutcome(null, "ACCOUNT_INACTIVE"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(TransferRejectedException.class)
                .satisfies(ex -> assertThat(((TransferRejectedException) ex).getCode())
                        .isEqualTo("ACCOUNT_INACTIVE"));
    }

    @Test
    void successfulTransferPublishesEventAndEvictsCache() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));
        when(transferDao.transferFunds(1L, 2L, new BigDecimal("100.00")))
                .thenReturn(new TransferDao.TransferOutcome(1001L, "OK"));

        Transaction ledgerRow = mock(Transaction.class);
        when(ledgerRow.getCommission()).thenReturn(new BigDecimal("0.50"));
        when(transactionRepository.findById(1001L)).thenReturn(Optional.of(ledgerRow));

        Cache accountsCache = mock(Cache.class);
        when(cacheManager.getCache("accounts")).thenReturn(accountsCache);

        TransferResponse response = transferService.transfer(request);

        assertThat(response.transactionId()).isEqualTo(1001L);
        assertThat(response.commission()).isEqualByComparingTo("0.50");
        assertThat(response.status()).isEqualTo("COMPLETED");

        verify(accountsCache).evict(1L);
        verify(accountsCache).evict(2L);
        verify(eventPublisher).publishCompleted(response);
    }
}
