package org.svlahov.sleepcalc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // 1. Import JavaTimeModule
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.svlahov.sleepcalc.exception.RestExceptionHandler;
import org.svlahov.sleepcalc.service.SleepService;
import org.svlahov.sleepcalc.service.SleepService.SleepState;
import org.svlahov.sleepcalc.service.SleepService.SleepHistoryEntry;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SleepControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SleepService sleepService;

    @InjectMocks
    private SleepController sleepController;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LocalDate testDate = LocalDate.now();

    @BeforeEach
    public void setup() {
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(sleepController)
                .setControllerAdvice(new RestExceptionHandler()).build();
    }

    @Test
    @DisplayName("GET /api/sleep/state should return full state from service")
    void getState_shouldReturnFullStateFromService() throws Exception {
        when(sleepService.getCurrentSleepState()).thenReturn(new SleepState(5.0, 2.0));

        mockMvc.perform(get("/api/sleep/state"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sleepDebt", is(5.0)))
                .andExpect(jsonPath("$.sleepSurplus", is(2.0)));
    }

    @Test
    @DisplayName("POST /api/sleep should accept time string and date, and call service")
    void recordSleep_withTimeStringAndDate_shouldCallServiceAndReturnResult() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setTimeSlept("8:30");
        sleepInput.setDate(testDate); // Add the date

        when(sleepService.recordSleep(eq("8:30"), eq(testDate))).thenReturn(new SleepState(0.0, 1.0));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput))) // Send the new object
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebt", is(0.0)))
                .andExpect(jsonPath("$.sleepSurplus", is(1.0)));

        Mockito.verify(sleepService).recordSleep(eq("8:30"), eq(testDate));
    }

    @Test
    @DisplayName("POST /api/sleep should return 400 if service throws error")
    void recordSleep_whenServiceThrowsError_thenReturnBadRequest() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setTimeSlept("-1:00");
        sleepInput.setDate(testDate);

        when(sleepService.recordSleep(anyString(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Hours slept cannot be negative."));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Hours slept cannot be negative.")));
    }

    @Test
    @DisplayName("GET /api/sleep/history should return a list of sleep entries")
    void getSleephistory_shouldReturnHistoryList() throws Exception {

        List<SleepHistoryEntry> historyList = List.of(
                new SleepHistoryEntry(LocalDate.now(), 8.0, 0.0, 0.5),
                new SleepHistoryEntry(LocalDate.now().minusDays(1), 7.0, 0.5, 0.0)
        );
        when(sleepService.getSleepHistory()).thenReturn(historyList);

        mockMvc.perform(get("/api/sleep/history"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sleepDate", is(LocalDate.now().toString())))
                .andExpect(jsonPath("$[0].hoursSlept", is(8.0)))
                .andExpect(jsonPath("$[1].sleepDate", is(LocalDate.now().minusDays(1).toString())))
                .andExpect(jsonPath("$[1].hoursSlept", is(7.0)));
    }

    @Test
    @DisplayName("GET /api/sleep/history/range should return entries within date range")
    void getSleepHistoryRange_shouldReturnEntries() throws Exception {
        // Arrange
        LocalDate from = LocalDate.now().minusDays(5);
        LocalDate to = LocalDate.now();

        List<SleepHistoryEntry> historyList = List.of(
                new SleepHistoryEntry(to, 8.0, 0.0, 0.5),
                new SleepHistoryEntry(from, 7.0, 0.5, 0.0)
        );

        when(sleepService.getSleepHistory(eq(from), eq(to))).thenReturn(historyList);

        // Act & Assert
        mockMvc.perform(get("/api/sleep/history/range")
                .param("from", from.toString())
                .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].sleepDate", is(to.toString())))
                .andExpect(jsonPath("$[1].sleepDate", is(from.toString())));

        Mockito.verify(sleepService).getSleepHistory(eq(from), eq(to));
    }
}