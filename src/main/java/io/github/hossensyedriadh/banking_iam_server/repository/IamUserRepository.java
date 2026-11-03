package io.github.hossensyedriadh.banking_iam_server.repository;

import io.github.hossensyedriadh.banking_iam_server.entity.IamUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IamUserRepository extends JpaRepository<IamUser, UUID> {
    Optional<IamUser> findByUsername(String username);
}
