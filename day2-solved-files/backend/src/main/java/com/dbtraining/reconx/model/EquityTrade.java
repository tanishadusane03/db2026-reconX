package com.dbtraining.reconx.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Currency;

import org.junit.jupiter.api.Test;

class EquityTradeTest {


    @Test
    void builder_buildsWhenAllRequiredPresent() {

        EquityTrade trade = EquityTrade.builder()
                .tradeRef(new TradeRef("TRD-001"))
                .instrumentSymbol("AAPL")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("150"))
                .currency(Currency.getInstance("USD"))
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 4, 1))
                .counterpartyId(100)
                .build();


        assertNotNull(trade);
        assertEquals("AAPL", trade.instrumentSymbol());
        assertEquals(new BigDecimal("10"), trade.quantity());
        assertEquals(new BigDecimal("150"), trade.price());
    }



    @Test
    void builder_missingPrice_throws() {


        assertThrows(
                IllegalStateException.class,
                () -> EquityTrade.builder()
                        .tradeRef(new TradeRef("TRD-001"))
                        .instrumentSymbol("AAPL")
                        .quantity(new BigDecimal("10"))
                        .currency(Currency.getInstance("USD"))
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 4, 1))
                        .counterpartyId(100)
                        .build()
        );
    }



    @Test
    void equality_byTradeRef() {


        EquityTrade first =
                EquityTrade.builder()
                        .tradeRef(new TradeRef("TRD-001"))
                        .instrumentSymbol("AAPL")
                        .quantity(new BigDecimal("10"))
                        .price(new BigDecimal("150"))
                        .currency(Currency.getInstance("USD"))
                        .side(Side.BUY)
                        .tradeDate(LocalDate.of(2026, 4, 1))
                        .counterpartyId(100)
                        .build();



        EquityTrade second =
                EquityTrade.builder()
                        .tradeRef(new TradeRef("TRD-001"))
                        .instrumentSymbol("MSFT")
                        .quantity(new BigDecimal("20"))
                        .price(new BigDecimal("200"))
                        .currency(Currency.getInstance("USD"))
                        .side(Side.SELL)
                        .tradeDate(LocalDate.of(2026, 4, 2))
                        .counterpartyId(200)
                        .build();



        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
