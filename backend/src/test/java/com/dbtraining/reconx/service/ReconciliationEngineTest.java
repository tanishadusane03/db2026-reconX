package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import com.dbtraining.reconx.model.*;
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

    private final ReconciliationEngine engine = new ReconciliationEngine();

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
    void testReconcile_missingCounterpartyTrade_returnsBreak() {
        // TODO(TICKET-ADV042): internal trade with no external counterpart -> status BREAK,
        //                     discrepancyType = "MISSING_EXTERNAL".
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV042 not implemented yet");
    }

    @Test
    void testReconcile_emptyInternal_returnsEmpty() {
        // TODO(TICKET-ADV040): empty internal + empty external -> reconcile returns an empty list.
        org.junit.jupiter.api.Assertions.fail("TICKET-ADV040 not implemented yet");
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
