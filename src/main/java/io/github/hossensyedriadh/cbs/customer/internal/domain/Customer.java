package io.github.hossensyedriadh.cbs.customer.internal.domain;

import io.github.hossensyedriadh.cbs.customer.api.enumerator.CustomerStatus;
import io.github.hossensyedriadh.cbs.customer.internal.util.CustomerNumberGenerator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "customers", schema = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter(AccessLevel.NONE)
    @Column(name = "customer_number", updatable = false, nullable = false, length = 30)
    private String customerNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "email")
    private String email;

    @Column(name = "phone", length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(name = "last_updated_at", insertable = false)
    private OffsetDateTime lastUpdatedAt;

    @Column(name = "closed_at", insertable = false)
    private OffsetDateTime closedAt;

    @PrePersist
    private void pre() {
        this.customerNumber = CustomerNumberGenerator.generate();
    }
}