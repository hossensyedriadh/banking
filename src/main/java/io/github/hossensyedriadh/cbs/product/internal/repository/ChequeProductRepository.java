package io.github.hossensyedriadh.cbs.product.internal.repository;

import io.github.hossensyedriadh.cbs.product.internal.domain.ChequeProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChequeProductRepository extends JpaRepository<ChequeProduct, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ChequeProduct p where p.id = :id")
    Optional<ChequeProduct> findByIdForUpdate(@Param("id") Long id);

    Optional<ChequeProduct> findByProductCode(String productCode);

    Boolean existsByProductCode(String productCode);
}
