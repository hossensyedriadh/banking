package io.github.hossensyedriadh.cbs.eod.internal.repository;

import io.github.hossensyedriadh.cbs.eod.internal.domain.BusinessDate;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessDateRepository extends JpaRepository<BusinessDate, Short> {
    @Override
    @NullMarked
    default <S extends BusinessDate> S save(S entity) {
        if (entity.getId() == null || !existsById(entity.getId())) {
            throw new IllegalArgumentException("Business date can not be inserted");
        }

        return saveAndFlush(entity);
    }

    @Query("select b from BusinessDate b where b.id = 1")
    Optional<BusinessDate> findCurrent();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BusinessDate b where b.id = 1")
    Optional<BusinessDate> findCurrentForUpdate();
}
