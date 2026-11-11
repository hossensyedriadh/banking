package io.github.hossensyedriadh.cbs.eod.internal.repository;

import io.github.hossensyedriadh.cbs.eod.internal.domain.EodRunStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EodRunStepRepository extends JpaRepository<EodRunStep, Long> {
}
