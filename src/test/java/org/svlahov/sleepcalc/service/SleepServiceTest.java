package org.svlahov.sleepcalc.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.SleepDataRepository;
import org.svlahov.sleepcalc.repository.UserRepository;
import org.svlahov.sleepcalc.support.TestJwtDynamicProps;

import java.math.BigDecimal;
import java.util.Optional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class SleepServiceTest extends TestJwtDynamicProps {

    @MockitoBean
    private SleepDataRepository sleepDataRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private SleepServiceImpl sleepService;

    private final LocalDate testDate = LocalDate.now();

    private SleepData createSleepData(User user) {
        return new SleepData(user, testDate);
    }

    private SleepData createSleepData(User user, String debt, String surplus) {
        SleepData data = new SleepData(user, testDate);
        data.setSleepDebt(new BigDecimal(debt));
        data.setSleepSurplus(new BigDecimal(surplus));
        return data;
    }

    @Test
    @DisplayName("recordSleep should throw exception for invalid time format")
    @WithMockUser(username = "invalid-time-user")
    void recordSleep_withInvalidFromat_throwsException() {
        User user = new User("invalid-time-user", "password");
        when(userRepository.findByUsername("invalid-time-user")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> {
            sleepService.recordSleep("invalid-time", testDate);
        });
    }

    @Test
    @DisplayName("recordSleep should throw exception for out of bounds time values")
    @WithMockUser(username = "out-of-bounds-user")
    void recordSleep_withOutOfBoundsTime_throwsException() {
        User user = new User("out-of-bounds-user", "password");
        when(userRepository.findByUsername("out-of-bounds-user")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> {
            sleepService.recordSleep("8:60", testDate);
        }, "Minutes should not be 60 or greater.");

        assertThrows(IllegalArgumentException.class, () -> {
            sleepService.recordSleep("24:00", testDate);
        }, "Hours should be less than 24");
    }

    @Test
    @DisplayName("recordSleep: Extra sleep (as string) with zero debt should increase surplus")
    @WithMockUser(username = "rested-user")
    void recordSleep_withExtraSleepAsStringAndNoDebt_increaseSurplus() {
        User user = new User("rested-user", "password");
        when(userRepository.findByUsername("rested-user")).thenReturn(Optional.of(user));
        when(sleepDataRepository.findByUser_Username("rested-user")).thenReturn(Optional.empty());

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("9:30", testDate);

        assertEquals(0.0, newState.sleepDebt(), "Debt should remain zero");
        assertEquals(2.0, newState.sleepSurplus(), 0.01, "Surplus should increase by 2.0");

        ArgumentCaptor<SleepData> captor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(captor.capture());
        assertEquals(testDate, captor.getValue().getSleepDate());
    }

    @Test
    @DisplayName("recordSleep: Shortfall (as string) with surplus should decrease surplus first")
    @WithMockUser(username = "surplus-user")
    void recordSleep_withShortFallAsStringAndSurplus_decreasesSurplus() {
        User user = new User("surplus-user", "password");
        when(userRepository.findByUsername("surplus-user")).thenReturn(Optional.of(user));
        SleepData existingData = createSleepData(user,"0.0", "3.0");
        existingData.setSleepSurplus(new BigDecimal("3.0"));
        when(sleepDataRepository.findByUser_Username("surplus-user")).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("6:30", testDate);

        assertEquals(0.0, newState.sleepDebt(), "Debt should remain zero");
        assertEquals(2.0, newState.sleepSurplus(), 0.01, "Surplus should decrease by 1.0");
    }

    @Test
    @DisplayName("recordSleep: Large shortfall (as string) should deplete surplus and increase debt")
    @WithMockUser(username = "user-in-trouble")
    void recordSleep_withLargeShortfallAsString_depletesSurplusAndIncreasesDebt() {
        User user = new User("user-in-trouble", "password");
        when(userRepository.findByUsername("user-in-trouble")).thenReturn(Optional.of(user));
        SleepData existingData = createSleepData(user, "0.0", "1.0");
        when(sleepDataRepository.findByUser_Username("user-in-trouble")).thenReturn(Optional.of(existingData));
        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("4:30", testDate);

        assertEquals(2.0, newState.sleepDebt(), 0.01, "Debt should increase by the remaining shortfall");
        assertEquals(0.0, newState.sleepSurplus(), "Surplus should be depleted to zero");
    }

    @Test
    @DisplayName("recordSleep: Extra sleep (as string) should pay down debt")
    @WithMockUser(username = "paying-debt")
    void recordSleep_withExtraSleepString_PayDownDebtBeforeSurplus() {
        User user = new User("paying-debt", "password");
        when(userRepository.findByUsername("paying-debt")).thenReturn(Optional.of(user));
        SleepData existingData = createSleepData(user, "1.0", "0.0");
        when(sleepDataRepository.findByUser_Username("paying-debt")).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("8:30", testDate);

        assertEquals(0.0, newState.sleepDebt(), "Debt should be paid off to zero");
        assertEquals(0.0, newState.sleepSurplus(), 0.01, "Surplus should be zero after paying off 1.0 debt");
    }

    @Test
    @DisplayName("getCurrentSleepState for an existing user should return the stored debt")
    @WithMockUser(username = "test-user")
    void getCurrentSleepState_forExistingUser_returnsDebt() {
        User user = new User("test-user", "password");
        when(userRepository.findByUsername("test-user")).thenReturn(Optional.of(user));
        SleepData existingData = createSleepData(user, "10.5", "0.0");

        when(sleepDataRepository.findByUser_Username("test-user")).thenReturn(Optional.of(existingData));

        SleepService.SleepState state = sleepService.getCurrentSleepState();

        assertNotNull(state);
        assertEquals(10.5, state.sleepDebt(), 0.01);
        assertEquals(0.0, state.sleepSurplus());
    }

    @Test
    @DisplayName("getCurrentSleepState for a new user should return 0 for both fields")
    @WithMockUser(username = "new-user")
    void getCurrentSleepState_forNewUser_returnsZeroState() {
        User user = new User("new-user", "password");
        when(userRepository.findByUsername("new-user")).thenReturn(Optional.of(user));
        when(sleepDataRepository.findByUser_Username("new-user")).thenReturn(Optional.empty());

        SleepService.SleepState state = sleepService.getCurrentSleepState();

        assertNotNull(state);
        assertEquals(0.0, state.sleepDebt());
        assertEquals(0.0, state.sleepSurplus());
    }

    @Test
    @DisplayName("recordSleep: NEW user should create new data and calculate debt")
    @WithMockUser(username = "new-user")
    void recordSleep_forNewUser_createsAndCalculatesDebt() {
        User user = new User("new-user", "password");
        when(userRepository.findByUsername("new-user")).thenReturn(Optional.of(user));
        when(sleepDataRepository.findByUser_Username("new-user")).thenReturn(Optional.empty());

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("6:00", testDate);

        assertEquals(1.5, newState.sleepDebt(), 0.01);

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals("new-user", sleepDataCaptor.getValue().getUser().getUsername());
        assertEquals(0, new BigDecimal("1.5").compareTo(savedData.getSleepDebt()));
    }

    @Test
    @DisplayName("recordSleep: High debt (as string) with extra sleep should apply diminished recovery")
    @WithMockUser(username = "high-debt-user")
    void recordSleep_forExistingUser_updatesAndCalculatesDebt() {
        User user = new User("high-debt-user", "password");
        when(userRepository.findByUsername("high-debt-user")).thenReturn(Optional.of(user));
        SleepData existingData = createSleepData(user, "5.0", "0.0");

        when(sleepDataRepository.findByUser_Username("high-debt-user")).thenReturn(Optional.of(existingData));

        when(sleepDataRepository.save(any(SleepData.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SleepService.SleepState newState = sleepService.recordSleep("9:30", testDate);

        assertEquals(3.35, newState.sleepDebt(), 0.01, "Debt should be reduced by the diminished recovery amount");
        assertEquals(0.0, newState.sleepSurplus(), 0.01, "Surplus should be the extra sleep minus the debt that was paid down");

        ArgumentCaptor<SleepData> sleepDataCaptor = ArgumentCaptor.forClass(SleepData.class);
        verify(sleepDataRepository).save(sleepDataCaptor.capture());

        SleepData savedData = sleepDataCaptor.getValue();
        assertEquals(0, new BigDecimal("3.35").compareTo(savedData.getSleepDebt()));
        assertEquals(0, BigDecimal.ZERO.compareTo(savedData.getSleepSurplus()));
    }
}
