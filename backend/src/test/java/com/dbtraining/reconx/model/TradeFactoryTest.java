package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeFactoryTest {

    @Test
    void create_equityPayload_returnsEquityTrade() {
        TradeType trade = TradeFactory.create("EQUITY", equityPayload());

        assertThat(trade).isInstanceOf(EquityTrade.class);
        assertThat(trade.assetClass()).isEqualTo(TradeType.AssetClass.EQUITY);
    }

    @Test
    void create_unknownAssetClass_throws() {
        assertThatThrownBy(() -> TradeFactory.create("FOO", Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void create_equityPayloadMissingPrice_throws() {
        Map<String, Object> payload = new HashMap<>(equityPayload());
        payload.remove("price");

        assertThatThrownBy(() -> TradeFactory.create("EQUITY", payload))
                .isInstanceOf(NullPointerException.class);
    }

    private Map<String, Object> equityPayload() {
        return Map.of(
                "tradeRef", "EQT-20260603-0001",
                "symbol", "SAP.DE",
                "quantity", new BigDecimal("100"),
                "price", new BigDecimal("125.50"),
                "currency", "EUR",
                "side", "BUY",
                "tradeDate", "2026-06-03",
                "counterpartyId", 1L);
    }
}
