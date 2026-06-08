package com.toilet.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ChartData {
    private String name;
    private BigDecimal value;
}
