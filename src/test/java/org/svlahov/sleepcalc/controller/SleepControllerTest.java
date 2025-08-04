package org.svlahov.sleepcalc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SleepController.class)
class SleepControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("GET /debt should return initial debt of 0.0")
    void getInitialDebt() throws Exception {
        mockMvc.perform(get("/api/sleep/debt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", closeTo(0.0, 0.01)));
    }

    @Test
    @DisplayName("POST should increase debt when sleep is less than target")
    void recordSleep_whenLessThanTarget_thenIncreaseDebt() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(6.0);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(sleepInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", closeTo(1.5, 0.01)));
    }

    @Test
    @DisplayName("POST should decrease debt when sleep is more that target and debt is zero")
    void recordSleep_whenMoreThanTarget_thenDecreaseDebt() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(8.5);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", closeTo(-1.0, 0.01)));
    }

    @Test
    @DisplayName("POST should have no change when sleep is exactly the target")
    void recordSleep_whenExactlyTarget_thenNoChange() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(7.5);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", closeTo(0.0, 0.01)));
    }

    @Test
    @DisplayName("POST should apply diminshed recovery when debt is already high")
    void recordSleep_whenDebtIsHigh_thenApplyDiminishedRecovery() throws Exception {
        SleepController.SleepInput initialSleep = new SleepController.SleepInput();
        initialSleep.setHoursSlept(2.5);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(initialSleep)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", closeTo(5.0, 0.01)));

        SleepController.SleepInput recoverySleep = new SleepController.SleepInput();
        recoverySleep.setHoursSlept(9.5);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recoverySleep)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", closeTo(3.35, 0.01)));
    }

    @Test
    @DisplayName("POST should return 400 Bad Request for negative sleep hours")
    void recordSleep_whenInputNegative_thenReturnBadRequest() throws Exception {
        SleepController.SleepInput sleepInput = new SleepController.SleepInput();
        sleepInput.setHoursSlept(-1.0);

        mockMvc.perform(post("/api/sleep")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sleepInput)))
                .andExpect(status().isBadRequest());
    }

}