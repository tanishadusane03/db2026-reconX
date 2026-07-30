package com.dbtraining.reconx.service;

import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Trade;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TradeLookupServiceTest {

    @Test
    void resolvesCounterpartyForKnownTradeRef() {
        TradeRepository tradeRepo = mock(TradeRepository.class);
        CounterpartyRepository cpRepo = mock(CounterpartyRepository.class);
        TradeLookupService service = new TradeLookupService(tradeRepo, cpRepo);

        Counterparty expected = new Counterparty();
        expected.setName("Acme");
        expected.setLeiCode("LEI123");
        expected.setRegion("EU");
        ReflectionTestUtils.setField(expected, "id", 42L);

        Trade trade = new Trade();
        trade.setTradeRef("T-100");
        trade.setCounterparty(expected);

        when(tradeRepo.findByTradeRef("T-100")).thenReturn(Optional.of(trade));
        when(cpRepo.findById(42L)).thenReturn(Optional.of(expected));

        Counterparty actual = service.counterpartyForTradeRef("T-100");

        assertEquals(expected, actual);
    }

    @Test
    void throwsWhenNoMatchingCounterpartyCanBeResolved() {
        TradeRepository tradeRepo = mock(TradeRepository.class);
        CounterpartyRepository cpRepo = mock(CounterpartyRepository.class);
        TradeLookupService service = new TradeLookupService(tradeRepo, cpRepo);

        when(tradeRepo.findByTradeRef("missing")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.counterpartyForTradeRef("missing"));
    }
}
