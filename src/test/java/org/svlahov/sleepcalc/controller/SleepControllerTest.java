package org.svlahov.sleepcalc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
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

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(sleepController)
                .setControllerAdvice(new RestExceptionHandler()).build();
    }

    @Test
    @DisplayName("GET /api/sleep/state should return full state from service")
    void getDebt_shouldReturnValueFromService() throws Exception {
        when(sleepService.getCurrentSleepState("default-user")).thenReturn(new SleepState(5.0, 2.0));

        mockMvc.perform(get("/api/sleep/state"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sleepDebt", is(5.0)))
                .andExpect(jsonPath("$.sleepSurplus", is(2.0)));
    }

    @Test
    @DisplayName("POST /api/sleep should accept time string and call service")
    void recordSleep_shouldCallServiceAndReturnResult() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setTimeSlept("8:30");
        when(sleepService.recordSleep("default-user","8:30")).thenReturn(new SleepState(0.0, 1.0));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebt", is(0.0)))
                .andExpect(jsonPath("$.sleepSurplus", is(1.0)));

        Mockito.verify(sleepService).recordSleep("default-user","8:30");
    }

    @Test
    @DisplayName("POST /api/sleep should return 400 if service throws exception")
    void recordSleep_whenInputNegative_thenReturnBadRequest() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setTimeSlept("-1:00");
        when(sleepService.recordSleep(anyString(), anyString())).thenThrow(new IllegalArgumentException("Hours slept cannot be negative."));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isBadRequest());
    }

}