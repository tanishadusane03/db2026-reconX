package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DerivativeTradeTest {

    @Test
    void builder_acceptsHistoricalOptionWhenExpiryIsAfterTradeDate() {
        DerivativeTrade trade = sampleDerivative(
                new BigDecimal("125.50"),
                LocalDate.of(2025, 6, 3));

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("DRV-20240603-0001"));
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.DERIVATIVE);
        assertThat(trade.optionType()).isEqualTo(DerivativeTrade.OptionType.CALL);
        assertThat(trade.notional()).isEqualTo(Money.of("12550.00", "USD"));
    }

    @Test
    void builder_expiryBeforeTradeDate_throws() {
        assertThatThrownBy(() -> sampleDerivative(
                new BigDecimal("125.50"),
                LocalDate.of(2024, 6, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("expiry cannot be before tradeDate");
    }

    @Test
    void builder_nonPositiveStrike_throws() {
        assertThatThrownBy(() -> sampleDerivative(
                BigDecimal.ZERO,
                LocalDate.of(2025, 6, 3)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("strike must be > 0");
    }

    private DerivativeTrade sampleDerivative(BigDecimal strike, LocalDate expiry) {
        return DerivativeTrade.builder()
                .tradeRef(TradeRef.of("DRV-20240603-0001"))
                .underlying("AAPL")
                .strike(strike)
                .quantity(new BigDecimal("100"))
                .expiry(expiry)
                .optionType(DerivativeTrade.OptionType.CALL)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2024, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
