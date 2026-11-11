package io.github.hossensyedriadh.banking_iam_server.repository;

import io.github.hossensyedriadh.banking_iam_server.entity.IamUserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IamUserProfileRepository extends JpaRepository<IamUserProfile, Long> {
}
