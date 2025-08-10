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
import static org.mockito.ArgumentMatchers.anyDouble;
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
    @DisplayName("POST /api/sleep should call service and return its result")
    void recordSleep_shouldCallServiceAndReturnResult() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(8.0);
        when(sleepService.recordSleep("default-user",8.0)).thenReturn(new SleepState(0.0, 0.5));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebt", is(0.0)))
                .andExpect(jsonPath("$.sleepSurplus", is(0.5)));

        Mockito.verify(sleepService).recordSleep("default-user",8.0);
    }

    @Test
    @DisplayName("POST /api/sleep should return 400 if service throws exception")
    void recordSleep_whenInputNegative_thenReturnBadRequest() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(-1.0);
        when(sleepService.recordSleep(anyString(), anyDouble())).thenThrow(new IllegalArgumentException("Hours slept cannot be negative."));

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isBadRequest());
    }

}