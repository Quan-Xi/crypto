package com.flow.binance;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SpotMarkerOrderDetail {
    private String symbol;
    private BigDecimal executedQty;
    private BigDecimal executedU;
}
