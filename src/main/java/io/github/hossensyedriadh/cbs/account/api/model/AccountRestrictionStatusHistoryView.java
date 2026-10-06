package io.github.hossensyedriadh.cbs.account.api.model;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Getter
@Setter
public class AccountRestrictionStatusHistoryView implements Serializable {
    @Serial
    private static final long serialVersionUID = -7269706483567641788L;

    private Long id;

    private AccountRestrictionStatus status;

    private String reason;

    private OffsetDateTime createdAt;
}