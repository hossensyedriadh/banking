package io.github.hossensyedriadh.banking_iam_server.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "iam_user_profile", schema = "core")
public class IamUserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_iam_user_profile_user"))
    private IamUser user;

    @Column(name = "first_name", nullable = false, length = 75)
    private String firstName;

    @Column(name = "middle_name", length = 75)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 75)
    private String lastName;

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "phone_no", unique = true, nullable = false, length = 16)
    private String phoneNo;

    @ColumnDefault("false")
    @Column(name = "is_email_verified", nullable = false)
    private boolean emailVerified;

    @ColumnDefault("false")
    @Column(name = "is_phone_no_verified", nullable = false)
    private boolean phoneNoVerified;

    @Setter(AccessLevel.NONE)
    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public void changeEmail(String email) {
        this.email = email;
        this.emailVerified = false;
    }

    public void changePhone(String phoneNo) {
        this.phoneNo = phoneNo;
        this.phoneNoVerified = false;
    }
}