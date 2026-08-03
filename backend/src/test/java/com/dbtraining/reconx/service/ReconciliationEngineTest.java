package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
import com.dbtraining.reconx.observability.ReconMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TICKET-ADV040 / ADV041 / ADV042 — TDD: write the test FIRST, then the impl.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine(
            new ReconMetrics(new SimpleMeterRegistry()));

    @Test
@DisplayName("Exact match on price and quantity returns MATCHED")
void testReconcile_exactMatch_returnsMatched() {

    // given
    EquityTrade internal =
            equity("EQU-20260603-0001", "100.00", "1000");

    EquityTrade external =
            equity("EQU-20260603-0001", "100.00", "1000");

    // when
    List<ReconResult> out = engine.reconcile(
            List.of(internal),
            List.of(external),
            ReconciliationRule.EXACT
    );

    // then
    assertThat(out).hasSize(1);
    assertThat(out.get(0).status())
            .isEqualTo(ReconResult.Status.MATCHED);
}

    @ParameterizedTest(name = "price diff {0} stays within 1% tolerance -> MATCHED")
@ValueSource(strings = {"0.10", "0.50", "0.99"})
void testReconcile_priceTolerance_withinThreshold(String diff) {

    BigDecimal basePrice = new BigDecimal("100.00");

    EquityTrade internal = equity(
            "EQU-20260603-0002",
            basePrice.toString(),
            "1000"
    );

    EquityTrade external = equity(
            "EQU-20260603-0002",
            basePrice.add(new BigDecimal(diff)).toString(),
            "1000"
    );

    List<ReconResult> out = engine.reconcile(
            List.of(internal),
            List.of(external),
            ReconciliationRule.PRICE_TOLERANCE_1PCT
    );

    assertThat(out.get(0).status())
            .isEqualTo(ReconResult.Status.MATCHED);
}
    @Test
@DisplayName("Missing counterparty trade returns BREAK")
void testReconcile_missingCounterpartyTrade_returnsBreak() {

    // given
    EquityTrade internal =
            equity("EQU-20260603-0003", "100.00", "1000");

    // when
    List<ReconResult> out = engine.reconcile(
            List.of(internal),
            List.of(),
            ReconciliationRule.EXACT
    );

    // then
    assertThat(out.get(0).status())
            .isEqualTo(ReconResult.Status.BREAK);

    assertThat(out.get(0).discrepancyType())
            .isEqualTo("MISSING_EXTERNAL");
}

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        assertThat(engine.reconcile(List.of(), List.of(), ReconciliationRule.EXACT)).isEmpty();
    }

    @Test
    void testReconcileByCounterparty_exactMatch_returnsMatched() {
        Map<Long, List<TradeType>> internalByCp = Map.of(1L,
                List.of((TradeType) equity("EQU-20260603-0001", "100.00", "10")));
        Map<Long, List<TradeType>> externalByCp = Map.of(1L,
                List.of((TradeType) equity("EQU-20260603-0001", "100.00", "10")));

        List<ReconResult> results = engine.reconcileByCounterparty(
                internalByCp,
                externalByCp,
                ReconciliationRule.EXACT)
                .join();

        assertThat(results).containsExactly(ReconResult.matched("EQU-20260603-0001"));
    }

    private EquityTrade equity(String ref, String price, String qty) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .price(new BigDecimal(price))
                .quantity(new BigDecimal(qty))
                .currency("EUR").side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
