package com.dbtraining.reconx.model;

/**
 * BUY (we acquire) / SELL (we dispose). Used across all TradeType impls.
 * Kept as a tiny enum rather than a String so a typo can't survive compile.
 */

 /**
 * Represents the direction of a trade.
 *
 * WHAT:
 * Defines whether a trade represents a purchase or a sale.
 *
 * HOW:
 * Implemented as an enum to restrict trade direction to the supported
 * values: BUY and SELL.
 *
 * WHY:
 * Provides a type-safe representation of trade direction across the
 * reconciliation domain.
 */
public enum Side {
    BUY, SELL
}