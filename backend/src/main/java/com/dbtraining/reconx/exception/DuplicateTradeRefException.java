package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 409 Conflict: tradeRef already exists. */
public class DuplicateTradeRefException extends ReconException {

    /**
 * Represents an error caused by attempting to create or process a duplicate
 * trade reference.
 *
 * WHAT:
 * Signals that a trade reference already exists and cannot be accepted as
 * a unique identifier.
 *
 * HOW:
 * Extends {@link ReconException} and is created with the duplicate trade
 * reference value that caused the failure.
 *
 * WHY:
 * Prevents multiple trades from sharing the same business identifier,
 * which could lead to incorrect reconciliation results.
 */
    public DuplicateTradeRefException(String tradeRef) {
        /**
 * Creates an exception for a duplicate trade reference.
 *
 * @param tradeRef the duplicate trade reference value.
 */
        super("Duplicate tradeRef: " + tradeRef);
    }
}
