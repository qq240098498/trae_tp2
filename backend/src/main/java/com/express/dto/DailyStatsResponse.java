package com.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyStatsResponse {
    private LocalDate date;
    private Long count;
}
