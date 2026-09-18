package io.github.hossensyedriadh.cbs.cheque.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeBookStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class ChequeBookView implements Serializable {
    @Serial
    private static final long serialVersionUID = 6576026271523987404L;

    private Long id;

    @JsonProperty("account_id")
    private Long accountId;

    @JsonProperty("cheque_product_id")
    private Long chequeProductId;

    @JsonProperty("book_no")
    private String bookNumber;

    @JsonProperty("first_cheque_no")
    private Long firstChequeNumber;

    @JsonProperty("last_cheque_no")
    private Long lastChequeNumber;

    private ChequeBookStatus status;

    @JsonProperty("issued_at")
    private OffsetDateTime issuedAt;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;
}
