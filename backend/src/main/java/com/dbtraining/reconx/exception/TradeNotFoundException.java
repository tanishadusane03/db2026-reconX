package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 404 Not Found: tradeRef has no row in trades. */
public class TradeNotFoundException extends ReconException {
    /**
 * Represents an error caused by a requested trade not being found.
 *
 * WHAT:
 * Signals that a trade identified by a trade reference does not exist.
 *
 * HOW:
 * Extends {@link ReconException} and is created using the missing trade
 * reference value.
 *
 * WHY:
 * Provides a specific error type for missing trade scenarios, allowing the
 * application to distinguish lookup failures from other reconciliation
 * errors.
 */
    public TradeNotFoundException(String tradeRef) {
        super("Trade not found: " + tradeRef);
    }
}
