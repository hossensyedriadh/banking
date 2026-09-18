package io.github.hossensyedriadh.cbs.cheque.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequePresentationStatus;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeReturnReason;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ChequePresentationView implements Serializable {
    @Serial
    private static final long serialVersionUID = 1553528180063865632L;

    private Long id;

    private ChequeView cheque;

    @JsonProperty("presenting_bank_code")
    private String presentingBankCode;

    @JsonProperty("presenting_account_no")
    private String presentingAccountNo;

    @JsonProperty("presented_amount")
    private BigDecimal presentedAmount;

    private ChequePresentationStatus status;

    @JsonProperty("presented_at")
    private OffsetDateTime presentedAt;

    @JsonProperty("accepted_at")
    private OffsetDateTime acceptedAt;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @JsonProperty("returned_at")
    private OffsetDateTime returnedAt;

    @JsonProperty("return_reason")
    private ChequeReturnReason returnReason;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
}