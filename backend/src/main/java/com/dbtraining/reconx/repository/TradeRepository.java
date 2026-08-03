package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.repository.entity.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

/**
 * ============================================================================
 * TICKET-ADV055 — Custom JPQL filter query
 * TICKET-ADV056 — Specification-based dynamic queries (JpaSpecificationExecutor)
 * TICKET-ADV057 — Pageable / Page<T> for paginated list endpoints
 * ============================================================================
 */
public interface TradeRepository
        extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    /**
     * TradeController maps the returned entities through TradeMapper *after* the
     * service transaction has closed, and spring.jpa.open-in-view is false. The
     * LAZY counterparty/instrument proxies therefore have no session and mapping
     * blew up with LazyInitializationException — fetch them with the page.
     */
    @Override
    @EntityGraph(attributePaths = {"counterparty", "instrument"})
    Page<Trade> findAll(Specification<Trade> spec, Pageable pageable);

    /** Same reason as findAll — PUT/PATCH map the entity after the transaction ends. */
    @Override
    @EntityGraph(attributePaths = {"counterparty", "instrument"})
    Optional<Trade> findById(Long id);

    @EntityGraph(attributePaths = {"counterparty", "instrument"})
    Optional<Trade> findByTradeRef(String tradeRef);

boolean existsByTradeRef(String tradeRef);

    @Query("""
        SELECT t FROM Trade t
        WHERE t.tradeDate BETWEEN :from AND :to
          AND (:status IS NULL OR t.status = :status)
          AND (:counterpartyId IS NULL OR t.counterparty.id = :counterpartyId)
        """)
    Page<Trade> findByFilters(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("status") TradeStatus status,
            @Param("counterpartyId") Long counterpartyId,
            Pageable pageable
    );

    long countByStatus(TradeStatus status);
}