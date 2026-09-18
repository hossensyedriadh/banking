package io.github.hossensyedriadh.cbs.cheque.internal.domain;

import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeBookStatus;
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

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "cheque_books", schema = "cheque", indexes = {
        @Index(name = "idx_cheque_books_account_id",
                columnList = "account_id"),
        @Index(name = "idx_cheque_books_cheque_product_id",
                columnList = "cheque_product_id"),
        @Index(name = "idx_cheque_books_status",
                columnList = "status")}, uniqueConstraints = {@UniqueConstraint(name = "uk_cheque_books_book_number",
        columnNames = {"book_number"})})
public class ChequeBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "account_id", updatable = false, nullable = false)
    private Long accountId;

    @Column(name = "cheque_product_id", updatable = false, nullable = false)
    private Long chequeProductId;

    @Column(name = "book_number", updatable = false, nullable = false, length = 50)
    private String bookNumber;

    @Column(name = "first_cheque_number", updatable = false, nullable = false)
    private Long firstChequeNumber;

    @Column(name = "last_cheque_number", updatable = false, nullable = false)
    private Long lastChequeNumber;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private ChequeBookStatus status;

    @Column(name = "issued_at", updatable = false, nullable = false)
    private OffsetDateTime issuedAt;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}