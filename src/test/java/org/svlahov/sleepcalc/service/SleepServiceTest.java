package org.svlahov.sleepcalc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.repository.SleepDataRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SleepServiceTest {

    @Mock
    private SleepDataRepository sleepDataRepository;

    @InjectMocks
    private SleepServiceImpl sleepService;

    @Test
    @DisplayName("recordSleep for a NEW user should create new data and calculate debt")
    void recordSleep_forNewUser_createsAndCalculatesDebt() {
        when(sleepDataRepository.findByUserId("new-user")).thenReturn(Optional.empty());

        double newDebt = sleepService.recordSleep("new-user", 6.0);

        assertEquals(1.5, newDebt, 0.01);

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals("new-user", savedData.getUserId());
        assertEquals(0, new BigDecimal("1.5").compareTo(savedData.getSleepDebt()));
    }

    @Test
    @DisplayName("recordSleep for an EXISTING user should update data and apply diminished recovery")
    void recordSleep_forExistingUser_updatesAndCalculatesDebt() {

        SleepData existingData = new SleepData("existing-user");
        existingData.setSleepDebt(new BigDecimal("5.0"));

        when(sleepDataRepository.findByUserId("existing-user")).thenReturn(Optional.of(existingData));

        double newDebt = sleepService.recordSleep("existing-user", 9.5);

        assertEquals(3.35, newDebt, 0.01);

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals("existing-user", savedData.getUserId());
        assertEquals(0, new BigDecimal("3.35").compareTo(savedData.getSleepDebt()));
    }

    @Test
    @DisplayName("getCurrentSleepDebt for an existing user should return the stored debt")
    void getCurrentSleepDebt_forExistingUser_returnsDebt() {
        SleepData existingData = new SleepData("test-user");
        existingData.setSleepDebt(new BigDecimal("10.5"));
        when(sleepDataRepository.findByUserId("test-user")).thenReturn(Optional.of(existingData));

        double currentDebt = sleepService.getCurrentSleepDebt("test-user");

        assertEquals(10.5, currentDebt, 0.01);
    }

    @Test
    @DisplayName("getCurrentSLeepDebt for a new user should return 0.0")
    void getCurrentSleepDebt_forNewUser_returnsZero() {
        when(sleepDataRepository.findByUserId("new-user")).thenReturn(Optional.empty());

        double currentDebt = sleepService.getCurrentSleepDebt("new-user");

        assertEquals(0.0, currentDebt, 0.01);
    }
}
