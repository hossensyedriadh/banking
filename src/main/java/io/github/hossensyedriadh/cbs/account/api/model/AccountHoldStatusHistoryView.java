package io.github.hossensyedriadh.cbs.account.api.model;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountHoldStatusHistoryView implements Serializable {
    @Serial
    private static final long serialVersionUID = -3873798308378497032L;

    private Long id;

    private AccountHoldStatus status;

    private String reason;

    private OffsetDateTime createdAt;
}