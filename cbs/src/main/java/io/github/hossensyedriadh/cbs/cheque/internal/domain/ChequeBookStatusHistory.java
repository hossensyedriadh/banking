package io.github.hossensyedriadh.cbs.cheque.internal.domain;

import io.github.hossensyedriadh.cbs.cheque.api.enumerator.ChequeBookStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "cheque_book_status_history", schema = "cheque")
public class ChequeBookStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "cheque_book_id", updatable = false, nullable = false)
    private Long chequeBookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", updatable = false, nullable = false, length = 20)
    private ChequeBookStatus status;

    @Setter(AccessLevel.NONE)
    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private OffsetDateTime createdAt;
}