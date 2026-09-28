package io.github.hossensyedriadh.cbs.eod.internal.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "business_date", schema = "eod")
public class BusinessDate {
    @Id
    @ColumnDefault("1")
    @Column(name = "id", updatable = false, nullable = false)
    private Short id;

    @Column(name = "current_business_date", nullable = false)
    private LocalDate currentBusinessDate;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}