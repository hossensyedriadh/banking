package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountRestrictionType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "account_restrictions", schema = "account", indexes = {
        @Index(name = "idx_account_restrictions_account_status", columnList = "account_id,status"),
        @Index(name = "idx_account_restrictions_account_type_status", columnList = "account_id,restriction_type,status")
})
public class AccountRestriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", updatable = false, nullable = false, length = 30)
    private AccountRestrictionType restrictionType;

    @OneToMany(mappedBy = "accountRestriction", fetch = FetchType.LAZY)
    private List<AccountRestrictionStatusHistory> history;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}