package io.github.hossensyedriadh.cbs.product.internal.repository;

import io.github.hossensyedriadh.cbs.product.internal.domain.CardProduct;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from CardProduct p where p.id = :id")
    Optional<CardProduct> findByIdForUpdate(@Param("id") Long id);

    Optional<CardProduct> findByProductCode(String productCode);

    Boolean existsByProductCode(String productCode);
}
