package org.svlahov.sleepcalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.svlahov.sleepcalc.entity.SleepData;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SleepDataRepository extends JpaRepository<SleepData, Long> {

    Optional<SleepData> findTopByUser_UsernameOrderBySleepDateDesc(String username);

    List<SleepData> findTop5ByUser_UsernameOrderBySleepDateDesc(String username);

    List<SleepData> findByUser_UsernameAndSleepDateBetween(String username, LocalDate startDate, LocalDate endDate);

}
