package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 400 Bad Request: a trade failed business validation. */
public class InvalidTradeException extends ReconException {
    /**
 * Represents an error caused by invalid trade data.
 *
 * WHAT:
 * Signals that a trade does not satisfy the required domain rules or
 * validation constraints.
 *
 * HOW:
 * Extends {@link ReconException} and carries a message describing the
 * validation failure.
 *
 * WHY:
 * Provides a specific exception type for invalid trade scenarios so that
 * trade validation failures can be distinguished from other application
 * errors.
 */

    
    public InvalidTradeException(String message) { super(message); }
}
