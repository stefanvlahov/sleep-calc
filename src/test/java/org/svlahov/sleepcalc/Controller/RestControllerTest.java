package org.svlahov.sleepcalc.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SleepController.class)
public class RestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAddSleepEntry_whenSleepLessThanIdeal_shouldIncreaseSleepDebt() throws Exception {
        // Test that when a user logs 6.5 hours of sleep (less than ideal 7.5),
        // the sleep debt increases by 1.0 hour
        mockMvc.perform(post("/api/sleep")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hoursSlept\": 6.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebtChange").value(1.0))
                .andExpect(jsonPath("$.message").value("Sleep debt increased by 1.0 hours"));
    }
    
    @Test
    public void testAddSleepEntry_whenSleepMoreThanIdeal_shouldDecreaseSleepDebt() throws Exception {
        // Test that when a user logs 9.0 hours of sleep (more than ideal 7.5),
        // the sleep debt decreases by 1.5 hours
        mockMvc.perform(post("/api/sleep")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hoursSlept\": 9.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebtChange").value(-1.5))
                .andExpect(jsonPath("$.message").value("Sleep debt decreased by 1.5 hours"));
    }
    
    @Test
    public void testAddSleepEntry_whenSleepEqualsIdeal_shouldNotChangeSleepDebt() throws Exception {
        // Test that when a user logs exactly 7.5 hours of sleep (equal to ideal),
        // the sleep debt change is 0
        mockMvc.perform(post("/api/sleep")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"hoursSlept\": 7.5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sleepDebtChange").value(0.0))
                .andExpect(jsonPath("$.message").value("Sleep debt decreased by 0.0 hours"));
    }
}
