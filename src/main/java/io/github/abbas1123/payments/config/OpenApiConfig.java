package io.github.abbas1123.payments.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentApiSpec() {
        return new OpenAPI().info(new Info()
                .title("Payment Transactions API")
                .description("Banking-style transfer API. Business rules live in Oracle PL/SQL packages; "
                        + "completed transfers are streamed to Kafka and account lookups are cached in Redis.")
                .version("v0.1.0")
                .contact(new Contact()
                        .name("Abbas Ramazanov")
                        .email("aramazanov107@gmail.com")
                        .url("https://github.com/abbas1123")));
    }
}
