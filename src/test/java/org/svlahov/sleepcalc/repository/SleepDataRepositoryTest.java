package org.svlahov.sleepcalc.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.svlahov.sleepcalc.entity.SleepData;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SleepDataRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SleepDataRepository sleepDataRepository;

    @Test
    @DisplayName("findByUserId should return SleepData when user exists")
    void findByUserId_whenUserExists_returnSleepData() {
        SleepData newSleepData = new SleepData("test-user");
        newSleepData.setSleepDebt(new BigDecimal("5.0"));

        entityManager.persistAndFlush(newSleepData);

        Optional<SleepData> foundData = sleepDataRepository.findByUserId("test-user");

        assertTrue(foundData.isPresent(), "SleepData should be found for the existing user");
        assertEquals("test-user", foundData.get().getUserId());
        assertEquals(0, new BigDecimal("5.0").compareTo(foundData.get().getSleepDebt()));
    }

    @Test
    @DisplayName("findByUserId should return empty Optional when user does not exist")
    void findByUserId_whenUserDoesNotExist_returnEmpty() {
        Optional<SleepData> foundData = sleepDataRepository.findByUserId("non-existing-user");

        assertFalse(foundData.isPresent(), "Optional should be empty got a non-existing user");
    }
}
