package io.github.hossensyedriadh.cbs.account.internal.domain;

import io.github.hossensyedriadh.cbs.account.api.enumerator.AccountStatus;
import io.github.hossensyedriadh.cbs.account.internal.util.AccountNumberGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
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
@Table(name = "accounts", schema = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter(AccessLevel.NONE)
    @Column(name = "account_number", updatable = false, unique = true, nullable = false, length = 30)
    private String accountNumber;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<AccountHolder> accountHolders;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "opened_at", updatable = false, nullable = false)
    private OffsetDateTime openedAt;

    @Column(name = "closed_at", insertable = false)
    private OffsetDateTime closedAt;

    @Column(name = "closure_reason", insertable = false)
    private String closureReason;

    @PrePersist
    private void pre() {
        this.accountNumber = AccountNumberGenerator.generate(this.branchId, this.productId);
    }
}