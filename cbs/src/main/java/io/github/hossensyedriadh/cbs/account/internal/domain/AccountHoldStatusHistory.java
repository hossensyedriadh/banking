package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountHoldStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@Entity
@Table(name = "account_hold_status_history", schema = "account")
public class AccountHoldStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", updatable = false, nullable = false, length = 20)
    private AccountHoldStatus status;

    @Column(name = "reason", updatable = false, nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_hold_id", updatable = false, nullable = false)
    private AccountHold accountHold;

    //intentionally put no audit timestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}