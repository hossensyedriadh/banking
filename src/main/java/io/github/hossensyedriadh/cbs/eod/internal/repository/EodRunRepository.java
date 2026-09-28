package io.github.hossensyedriadh.cbs.eod.internal.repository;

import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EodRunRepository extends JpaRepository<EodRun, Long> {
}
