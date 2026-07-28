package com.dbtraining.reconx.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class EquityTradeTest {

    private EquityTrade buildTrade(String ref) {

        return EquityTrade.builder()
                .tradeRef(new TradeRef(ref))
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("150.00"))
                .currency(Currency.getInstance("USD"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 7, 28))
                .counterpartyId(1L)
                .build();
    }


    @Test
    void builder_buildsWhenAllRequiredPresent() {

        EquityTrade trade = buildTrade("TRD-001");

        assertNotNull(trade);
        assertEquals("AAPL", trade.instrumentSymbol());
        assertEquals(new BigDecimal("10"), trade.quantity());
        assertEquals(new BigDecimal("150.00"), trade.price());
    }


    @Test
    void builder_missingPrice_throws() {

        assertThrows(NullPointerException.class, () -> {

            EquityTrade.builder()
                    .tradeRef(new TradeRef("TRD-002"))
                    .instrumentSymbol("AAPL")
                    .quantity(new BigDecimal("10"))
                    .currency(Currency.getInstance("USD"))
                    .side(Side.BUY)
                    .tradeDate(LocalDate.of(2026, 7, 28))
                    .counterpartyId(1L)
                    .build();

        });
    }


    @Test
    void equality_byTradeRef() {

        EquityTrade first = buildTrade("TRD-100");
        EquityTrade second = buildTrade("TRD-100");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
