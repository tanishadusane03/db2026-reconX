package com.dbtraining.reconx.exception;

/** TICKET-ADV025 — 422 Unprocessable: internal vs external trade do not match. */
public class ReconciliationMismatchException extends ReconException {
    /**
 * Represents an error caused by a mismatch between trades during
 * reconciliation.
 *
 * WHAT:
 * Signals that two trades could not be reconciled because their details
 * do not satisfy the configured matching rules.
 *
 * HOW:
 * Extends {@link ReconException} and stores a message describing the
 * reconciliation mismatch.
 *
 * WHY:
 * Provides a specific exception type for reconciliation failures so that
 * mismatch scenarios can be identified separately from other application
 * errors.
 */

    public ReconciliationMismatchException(String message) { super(message); }
}
