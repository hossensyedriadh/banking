package io.github.hossensyedriadh.banking_iam_server.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "iam_user_authority", schema = "core")
public class IamUserAuthority {
    @EmbeddedId
    private IamUserAuthorityId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private IamUser user;

    @MapsId("authorityId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authority_id", nullable = false)
    private IamAuthority authority;
}