package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.exception.DuplicateTradeRefException;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.kafka.TradeEventProducer;
import com.dbtraining.reconx.observability.TradeMetrics;
import com.dbtraining.reconx.repository.CounterpartyRepository;
import com.dbtraining.reconx.repository.InstrumentRepository;
import com.dbtraining.reconx.repository.TradeRepository;
import com.dbtraining.reconx.repository.entity.Trade;

import com.dbtraining.reconx.dto.TradeEvent;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.dbtraining.reconx.repository.entity.TradeStatus;

import static com.dbtraining.reconx.repository.TradeSpecifications.*;

/**
 * ============================================================================
 * TICKET-ADV064 — TradeService.create (POST endpoint backing)
 * TICKET-ADV065 — update
 * TICKET-ADV066 — updateStatus (PATCH)
 * TICKET-ADV067 — softDelete
 * TICKET-ADV083 — increments trade_created_total Counter on create
 * TICKET-ADV129 — publishes TradeEvent on every state change
 * TICKET-ADV055/ADV056 — list() uses Specifications + filter query
 * ============================================================================
 */
@Service
@Transactional
public class TradeService {

    private final TradeRepository tradeRepo;
    private final CounterpartyRepository cpRepo;
    private final InstrumentRepository instRepo;
    private final TradeEventProducer events;
    private final TradeMetrics metrics;


    public TradeService(TradeRepository tradeRepo,
                        CounterpartyRepository cpRepo,
                        InstrumentRepository instRepo,
                        TradeEventProducer events,
                        TradeMetrics metrics) {

        this.tradeRepo = tradeRepo;
        this.cpRepo = cpRepo;
        this.instRepo = instRepo;
        this.events = events;
        this.metrics = metrics;
    }


    public Trade create(TradeRequest req, String actor) {

    if (tradeRepo.existsByTradeRef(req.tradeRef())) {
        throw new DuplicateTradeRefException(req.tradeRef());
    }

    Trade trade = new Trade();

    trade.setTradeRef(req.tradeRef());

    trade.setInstrument(
            instRepo.findById(req.instrumentId())
                    .orElseThrow(() ->
                            new TradeNotFoundException("instrument=" + req.instrumentId()))
    );

    trade.setCounterparty(
            cpRepo.findById(req.counterpartyId())
                    .orElseThrow(() ->
                            new TradeNotFoundException("counterparty=" + req.counterpartyId()))
    );

trade.setQuantity(req.quantity());
trade.setPrice(req.price());
trade.setTradeDate(req.tradeDate());
trade.setStatus(TradeStatus.PENDING);

    Trade saved = tradeRepo.save(trade);

    metrics.incrementTradeCreated();

    metrics.recordTradeValue(
            saved.getQuantity().multiply(saved.getPrice()).doubleValue()
    );

    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_CREATED,
                    Instant.now(),
                    actor,
                    null,
                    null
            )
    );

    return saved;
}

    public Trade update(Long id, TradeRequest req, String actor) {

    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException("id=" + id));

    trade.setTradeRef(req.tradeRef());

    trade.setInstrument(
            instRepo.findById(req.instrumentId())
                    .orElseThrow(() ->
                            new TradeNotFoundException("instrument=" + req.instrumentId()))
    );

    trade.setCounterparty(
            cpRepo.findById(req.counterpartyId())
                    .orElseThrow(() ->
                            new TradeNotFoundException("counterparty=" + req.counterpartyId()))
    );



    trade.setQuantity(req.quantity());
    trade.setPrice(req.price());
    trade.setTradeDate(req.tradeDate());

    Trade saved = tradeRepo.save(trade);

    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_UPDATED,
                    Instant.now(),
                    actor,
                    null,
                    null
            )
    );

    return saved;
}
    public Trade updateStatus(Long id, String status, String actor) {

    Trade trade = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException("id=" + id));

 trade.setStatus(TradeStatus.valueOf(status));

    Trade saved = tradeRepo.save(trade);

    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    saved.getTradeRef(),
                    TradeEvent.EventType.TRADE_UPDATED,
                    Instant.now(),
                    actor,
                    null,
                    status
            )
    );

    return saved;
}

    
public void softDelete(Long id, String actor) {

    Trade t = tradeRepo.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException("id=" + id));

    t.setDeletedAt(Instant.now());

    tradeRepo.save(t);

    events.publish(
            new TradeEvent(
                    UUID.randomUUID(),
                    t.getTradeRef(),
                    TradeEvent.EventType.TRADE_CANCELLED,
                    Instant.now(),
                    actor,
                    null,
                    null
            )
    );
}


    /**
     * =========================================================================
     * TICKET-ADV055 + TICKET-ADV056
     *
     * Dynamic search using JPA Specifications.
     *
     * Instead of:
     *
     * WHERE (:status IS NULL OR status = :status)
     *
     * we compose independent specifications:
     *
     * date range
     * status
     * counterparty
     * reference pattern
     *
     * =========================================================================
     */
    @Transactional(readOnly = true)
    public Page<Trade> list(LocalDate from,
                            LocalDate to,
                            String status,
                            Long counterpartyId,
                            Pageable pageable) {


        Specification<Trade> spec =
        Specification
                .where(tradeDateBetween(from, to))
                .and(hasStatus(status))
                .and(hasCounterparty(counterpartyId));
        return tradeRepo.findAll(spec, pageable);
    }

}