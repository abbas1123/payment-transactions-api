package io.github.abbas1123.payments.web;

import io.github.abbas1123.payments.dto.TransferRequest;
import io.github.abbas1123.payments.dto.TransferResponse;
import io.github.abbas1123.payments.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Money movement between accounts")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Transfer funds between two accounts",
            description = "Executed atomically by the Oracle PL/SQL package PKG_TRANSFERS "
                    + "(balance check, commission, ledger insert). "
                    + "On success a TransactionEvent is published to Kafka.")
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request);
    }
}
