package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BondTradeTest {

    @Test
    void builder_buildsWhenMaturityIsAfterTradeDate() {
        BondTrade trade = sampleBond(LocalDate.of(2031, 6, 3));

        assertThat(trade.tradeRef()).isEqualTo(TradeRef.of("BND-20260603-0001"));
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.BOND);
        assertThat(trade.notional()).isEqualTo(Money.of("1000000", "USD"));
    }

    @Test
    void builder_maturityBeforeTradeDate_throws() {
        assertThatThrownBy(() -> sampleBond(LocalDate.of(2026, 6, 2)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("maturityDate cannot be before tradeDate");
    }

    @Test
    void builder_missingFaceValue_throws() {
        assertThatThrownBy(() -> BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260603-0001"))
                .isin("US0378331005")
                .couponRate(new BigDecimal("0.045"))
                .maturityDate(LocalDate.of(2031, 6, 3))
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessage("faceValue");
    }

    private BondTrade sampleBond(LocalDate maturityDate) {
        return BondTrade.builder()
                .tradeRef(TradeRef.of("BND-20260603-0001"))
                .isin("US0378331005")
                .faceValue(new BigDecimal("1000000"))
                .couponRate(new BigDecimal("0.045"))
                .maturityDate(maturityDate)
                .currency("USD")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
