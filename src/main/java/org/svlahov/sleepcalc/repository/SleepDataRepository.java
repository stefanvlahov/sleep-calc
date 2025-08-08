package org.svlahov.sleepcalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.svlahov.sleepcalc.entity.SleepData;

import java.util.Optional;

public interface SleepDataRepository extends JpaRepository<SleepData, Long> {

    Optional<SleepData> findByUserId(String userId);
}
