package io.github.hossensyedriadh.cbs.product.internal.repository;

import io.github.hossensyedriadh.cbs.product.internal.domain.AccountProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountProductRepository extends JpaRepository<AccountProduct, Long> {
    Optional<AccountProduct> findByProductCode(String productCode);

    Boolean existsByProductCode(String productCode);
}
