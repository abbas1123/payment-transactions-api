package io.github.abbas1123.payments.dao;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Map;

/**
 * Delegates the actual transfer to the Oracle package PKG_TRANSFERS.
 * Balance checks, commission calculation and the ledger insert all happen
 * atomically inside the database, in a single procedure call.
 */
@Repository
public class OracleTransferDao implements TransferDao {

    private final JdbcTemplate jdbcTemplate;
    private SimpleJdbcCall transferCall;

    public OracleTransferDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void init() {
        this.transferCall = new SimpleJdbcCall(jdbcTemplate)
                .withCatalogName("PKG_TRANSFERS")
                .withProcedureName("TRANSFER_FUNDS")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_from_account", Types.NUMERIC),
                        new SqlParameter("p_to_account", Types.NUMERIC),
                        new SqlParameter("p_amount", Types.NUMERIC),
                        new SqlOutParameter("o_tx_id", Types.NUMERIC),
                        new SqlOutParameter("o_status", Types.VARCHAR));
    }

    @Override
    public TransferOutcome transferFunds(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("p_from_account", fromAccountId)
                .addValue("p_to_account", toAccountId)
                .addValue("p_amount", amount);

        Map<String, Object> result = transferCall.execute(params);

        Number txId = (Number) result.get("o_tx_id");
        String status = (String) result.get("o_status");
        return new TransferOutcome(txId == null ? null : txId.longValue(), status);
    }
}
