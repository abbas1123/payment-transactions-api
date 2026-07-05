package io.github.abbas1123.payments.repository;

import io.github.abbas1123.payments.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
