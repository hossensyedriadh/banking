package io.github.hossensyedriadh.cbs.customer.internal.domain;

import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerIdentityStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "customer_identities", schema = "customer", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_identity_iam_subject",
                columnNames = {"iam_subject"}),
        @UniqueConstraint(name = "customer_identities_customer_id_key",
                columnNames = {"customer_id"})})
public class CustomerIdentity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "iam_subject", updatable = false, nullable = false)
    private String iamSubject;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", updatable = false, nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private CustomerIdentityStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private OffsetDateTime updatedAt;
}