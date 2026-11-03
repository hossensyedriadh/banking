package io.github.hossensyedriadh.banking_iam_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@Embeddable
public class IamUserAuthorityId implements Serializable {
    @Serial
    private static final long serialVersionUID = 5505633227719125957L;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "authority_id", nullable = false)
    private UUID authorityId;
}