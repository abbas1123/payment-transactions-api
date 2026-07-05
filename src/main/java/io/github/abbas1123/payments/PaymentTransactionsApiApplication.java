package io.github.abbas1123.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PaymentTransactionsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentTransactionsApiApplication.class, args);
    }
}
