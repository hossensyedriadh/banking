package io.github.hossensyedriadh.cbs.product.internal.repository;

import io.github.hossensyedriadh.cbs.product.internal.domain.AccountProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountProductRepository extends JpaRepository<AccountProduct, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from AccountProduct p where p.id = :id")
    Optional<AccountProduct> findByIdForUpdate(@Param("id") Long id);

    Optional<AccountProduct> findByProductCode(String productCode);

    Boolean existsByProductCode(String productCode);
}
