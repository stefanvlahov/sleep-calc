package org.svlahov.sleepcalc.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.entity.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SleepDataRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SleepDataRepository sleepDataRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = new User("testuser", "password");
        entityManager.persistAndFlush(testUser);
    }

    private SleepData persistSleepData(LocalDate date, BigDecimal hoursSlept, BigDecimal debt, BigDecimal surplus) {
        SleepData data = new SleepData(testUser, date, hoursSlept, debt, surplus);
        return entityManager.persistAndFlush(data);
    }

    @Test
    @DisplayName("findTopByUser_UsernameOrderBySleepDateDesc should return the latest entry")
    void findTopByUser_UsernameOrderBySleepDateDesc_shouldReturnLatest() {
        // Arrange
        persistSleepData(LocalDate.now().minusDays(2), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("0.5"));
        SleepData latestEntry = persistSleepData(LocalDate.now().minusDays(1), new BigDecimal("7.0"), new BigDecimal("0.5"), BigDecimal.ZERO);
        persistSleepData(LocalDate.now().minusDays(3), new BigDecimal("6.0"), new BigDecimal("1.5"), BigDecimal.ZERO);

        // Act
        Optional<SleepData> foundData = sleepDataRepository.findTopByUser_UsernameOrderBySleepDateDesc("testuser");

        // Assert
        assertTrue(foundData.isPresent(), "Should find the latest sleep data");
        assertEquals(latestEntry.getId(), foundData.get().getId());
        assertEquals(new BigDecimal("7.0"), foundData.get().getHoursSlept());
    }

    @Test
    @DisplayName("findTopByUser_UsernameOrderBySleepDateDesc should return empty Optional when user does not exist")
    void findTopByUser_Username_whenUserDoesNotExist_returnEmpty() {
        // Act
        Optional<SleepData> foundData = sleepDataRepository.findTopByUser_UsernameOrderBySleepDateDesc("non-existing-user");

        // Assert
        assertFalse(foundData.isPresent(), "Optional should be empty for a non-existing user");
    }

    @Test
    @DisplayName("findTop5ByUser_UsernameOrderBySleepDateDesc should return recent entries in correct order")
    void findTop5ByUser_UsernameOrderBySleepDateDesc_shouldReturnRecentEntries() {
        // Arrange
        persistSleepData(LocalDate.now().minusDays(5), new BigDecimal("8.0"), BigDecimal.ZERO, new BigDecimal("0.5"));
        persistSleepData(LocalDate.now().minusDays(4), new BigDecimal("7.0"), new BigDecimal("0.5"), BigDecimal.ZERO);
        SleepData entry3 = persistSleepData(LocalDate.now().minusDays(3), new BigDecimal("6.0"), new BigDecimal("1.5"), BigDecimal.ZERO);
        SleepData entry2 = persistSleepData(LocalDate.now().minusDays(2), new BigDecimal("8.0"), new BigDecimal("1.0"), new BigDecimal("0.5"));
        SleepData entry1 = persistSleepData(LocalDate.now().minusDays(1), new BigDecimal("9.0"), BigDecimal.ZERO, new BigDecimal("2.0"));
        persistSleepData(LocalDate.now().minusDays(6), new BigDecimal("7.5"), BigDecimal.ZERO, new BigDecimal("0.5"));

        // Act
        List<SleepData> recentEntries = sleepDataRepository.findTop5ByUser_UsernameOrderBySleepDateDesc("testuser");

        // Assert
        assertEquals(5, recentEntries.size(), "Should return exactly 5 entries");
        assertEquals(entry1.getId(), recentEntries.get(0).getId(), "First entry should be the latest date");
        assertEquals(entry2.getId(), recentEntries.get(1).getId());
        assertEquals(entry3.getId(), recentEntries.get(2).getId());
        assertEquals(LocalDate.now().minusDays(1), recentEntries.get(0).getSleepDate());
        assertEquals(LocalDate.now().minusDays(5), recentEntries.get(4).getSleepDate(), "Last entry should be the 5th latest date");
    }
}