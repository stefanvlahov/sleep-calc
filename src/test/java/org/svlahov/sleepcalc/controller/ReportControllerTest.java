package org.svlahov.sleepcalc.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.svlahov.sleepcalc.dto.MonthlyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportItem;
import org.svlahov.sleepcalc.service.ReportService;
import org.svlahov.sleepcalc.support.TestJwtDynamicProps;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportControllerTest extends TestJwtDynamicProps {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    @WithMockUser
    void getWeeklyReport_shouldReturnOk() throws Exception {
        WeeklyReportDTO mockReport = new WeeklyReportDTO(0, 0, 0, Collections.emptyList());
        when(reportService.getWeeklyReport(any(LocalDate.class))).thenReturn(mockReport);

        mockMvc.perform(get("/api/reports/weekly"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(Objects.requireNonNull(org.springframework.http.MediaType.APPLICATION_JSON)));
    }

    @Test
    @WithMockUser
    void getMonthlyReport_shouldReturnOk() throws Exception {
        MonthlyReportDTO mockReport = new MonthlyReportDTO(0, 0, List.of(new WeeklyReportItem("Week 1", 0)));
        when(reportService.getMonthlyReport(any(LocalDate.class))).thenReturn(mockReport);

        mockMvc.perform(get("/api/reports/monthly"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .contentType(Objects.requireNonNull(org.springframework.http.MediaType.APPLICATION_JSON)));
    }

    @Test
    @WithMockUser
    void exportReport_shouldReturnCsv() throws Exception {
        when(reportService.exportReport(any(LocalDate.class), any(LocalDate.class))).thenReturn("header\ndata");

        mockMvc.perform(get("/api/reports/export")
                .param("from", "2023-10-01")
                .param("to", "2023-10-31"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=sleep_report.csv"))
                .andExpect(content().contentType("text/csv"));
    }
}
