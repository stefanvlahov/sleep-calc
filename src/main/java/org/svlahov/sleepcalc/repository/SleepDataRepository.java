package org.svlahov.sleepcalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.svlahov.sleepcalc.entity.SleepData;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SleepDataRepository extends JpaRepository<SleepData, Long> {

    Optional<SleepData> findTopByUser_UsernameOrderBySleepDateDesc(String username);

    Optional<SleepData> findTopByUser_UsernameAndSleepDateLessThanOrderBySleepDateDesc(String username, LocalDate date);

    List<SleepData> findByUser_UsernameAndSleepDateGreaterThanOrderBySleepDateAsc(String username, LocalDate date);

    Optional<SleepData> findByUser_UsernameAndSleepDate(String username, LocalDate date);

    List<SleepData> findTop5ByUser_UsernameOrderBySleepDateDesc(String username);

    List<SleepData> findByUser_UsernameAndSleepDateBetween(String username, LocalDate startDate, LocalDate endDate);

}
