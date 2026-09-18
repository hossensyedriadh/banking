package io.github.hossensyedriadh.cbs.cheque.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ChequeView implements Serializable {
    @Serial
    private static final long serialVersionUID = -5846875825892325828L;

    private Long id;

    @JsonProperty("cheque_book")
    private ChequeBookView chequeBook;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("cheque_number")
    private Long chequeNumber;

    private ChequeStatus status;

    @JsonProperty("issued_at")
    private OffsetDateTime issuedAt;

    @JsonProperty("expiry_date")
    private LocalDate expiryDate;

    @JsonProperty("presented_at")
    private OffsetDateTime presentedAt;

    @JsonProperty("paid_at")
    private OffsetDateTime paidAt;

    @JsonProperty("bounced_at")
    private OffsetDateTime bouncedAt;

    @JsonProperty("stopped_at")
    private OffsetDateTime stoppedAt;

    @JsonProperty("cancelled_at")
    private OffsetDateTime cancelledAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
