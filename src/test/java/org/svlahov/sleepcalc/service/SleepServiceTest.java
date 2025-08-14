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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SleepServiceTest {

    @Mock
    private SleepDataRepository sleepDataRepository;

    @InjectMocks
    private SleepServiceImpl sleepService;

    @Test
    @DisplayName("recordSleep should throw exception for invalid time format")
    void recordSleep_withInvalidFromat_throwsException() {
       assertThrows(IllegalArgumentException.class, () -> {
           sleepService.recordSleep("test-user", "invalid-time");
       });
    }

    @Test
    @DisplayName("recordSleep should throw exception for out of bounds time values")
    void recordSleep_withOutOfBoundsTime_throwsException() {
        assertThrows(IllegalArgumentException.class,  () -> {
            sleepService.recordSleep("test-user", "8:60");
        }, "Minutes should not be 60 or greater.");

        assertThrows(IllegalArgumentException.class, () -> {
            sleepService.recordSleep("test-user", "24:00");
        }, "Hours should be less than 24");
    }

    @Test
    @DisplayName("recordSleep: Extra sleep (as string) with zero debt should increase surplus")
    void recordSleep_withExtraSleepAsStringAndNoDebt_increaseSurplus() {
       String userId = "rested-user";
       when(sleepDataRepository.findByUserId(userId)).thenReturn(Optional.empty());

       when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

       SleepService.SleepState newState = sleepService.recordSleep(userId, "9:30");

       assertEquals(0.0, newState.sleepDebt(), "Debt should remain zero");
       assertEquals(2.0, newState.sleepSurplus(), 0.01, "Surplus should increase by 2.0");
    }

    @Test
    @DisplayName("recordSleep: Shortfall (as string) with surplus should decrease surplus first")
    void recordSleep_withShortFallAsStringAndSurplus_decreasesSurplus() {
        String userId = "surplus-user";
        SleepData existingData = new SleepData(userId);
        existingData.setSleepSurplus(new BigDecimal("3.0"));
        when(sleepDataRepository.findByUserId(userId)).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep(userId, "6:30");

        assertEquals(0.0, newState.sleepDebt(), "Debt should remain zero");
        assertEquals(2.0, newState.sleepSurplus(), 0.01, "Surplus should decrease by 1.0");
    }

    @Test
    @DisplayName("recordSleep: Large shortfall (as string) should deplete surplus and increase debt")
    void recordSleep_withLargeShortfallAsString_depletesSurplusAndIncreasesDebt() {
        String userId = "user-in-trouble";
        SleepData existingData = new SleepData(userId);
        existingData.setSleepSurplus(new BigDecimal("1.0"));
        when(sleepDataRepository.findByUserId(userId)).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep(userId, "4:30");

        assertEquals(2.0, newState.sleepDebt(), 0.01, "Debt should increase by the remaining shortfall");
        assertEquals(0.0, newState.sleepSurplus(), "Surplus should be depleted to zero");
    }

    @Test
    @DisplayName("recordSleep: Extra sleep (as string) should pay down debt")
    void recordSleep_withExtraSleepString_PayDownDebtBeforeSurplus() {
        String userId = "paying-debt";
        SleepData existingData = new SleepData(userId);
        existingData.setSleepDebt(new BigDecimal("1.0"));
        when(sleepDataRepository.findByUserId(userId)).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep(userId, "8:30");

        assertEquals(0.0, newState.sleepDebt(), "Debt should be paid off to zero");
        assertEquals(0.0, newState.sleepSurplus(), 0.01, "Surplus should be zero after paying off 1.0 debt");
    }

    @Test
    @DisplayName("getCurrentSleepState for an existing user should return the stored debt")
    void getCurrentSleepState_forExistingUser_returnsDebt() {
        SleepData existingData = new SleepData("test-user");
        existingData.setSleepDebt(new BigDecimal("10.5"));
        when(sleepDataRepository.findByUserId("test-user")).thenReturn(Optional.of(existingData));

        SleepService.SleepState state  = sleepService.getCurrentSleepState("test-user");

        assertNotNull(state);
        assertEquals(10.5, state.sleepDebt(), 0.01);
        assertEquals(0.0, state.sleepSurplus());
    }

    @Test
    @DisplayName("getCurrentSleepState for a new user should return 0 for both fields")
    void getCurrentSleepState_forNewUser_returnsZeroState() {
        when(sleepDataRepository.findByUserId("new-user")).thenReturn(Optional.empty());

        SleepService.SleepState state  = sleepService.getCurrentSleepState("new-user");

        assertNotNull(state);
        assertEquals(0.0, state.sleepDebt());
        assertEquals(0.0, state.sleepSurplus());
    }

    @Test
    @DisplayName("recordSleep: NEW user should create new data and calculate debt")
    void recordSleep_forNewUser_createsAndCalculatesDebt() {
        when(sleepDataRepository.findByUserId("new-user")).thenReturn(Optional.empty());

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("new-user", "6:00");

        assertEquals(1.5, newState.sleepDebt(), 0.01);

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals("new-user", savedData.getUserId());
        assertEquals(0, new BigDecimal("1.5").compareTo(savedData.getSleepDebt()));
    }

    @Test
    @DisplayName("recordSleep: High debt (as string) with extra sleep should apply diminished recovery")
    void recordSleep_forExistingUser_updatesAndCalculatesDebt() {
        String userId = "high-debt-user";
        SleepData existingData = new SleepData(userId);
        existingData.setSleepDebt(new BigDecimal("5.0"));

        when(sleepDataRepository.findByUserId(userId)).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep(userId, "9:30");

        assertEquals(3.35, newState.sleepDebt(), 0.01, "Debt should be reduced by the diminished recovery amount");
        assertEquals(0.35, newState.sleepSurplus(), "Surplus should be the extra sleep minus the debt that was paid down");

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals(0, new BigDecimal("3.35").compareTo(savedData.getSleepDebt()));
        assertEquals(0, new BigDecimal("0.35").compareTo(savedData.getSleepSurplus()));
    }
}
