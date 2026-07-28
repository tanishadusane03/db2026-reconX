# ADR-0001 — Partition the trades table by trade_date

## Status
Accepted

Date: 2026-07-29

## Context

ReconX processes around 50,000 trades/day.
With 5 years retention, the trades table can grow to around 91 million rows.

Most reconciliation queries filter trades using trade_date.

A single large table would make:
- date searches slower
- archival difficult
- maintenance expensive

Alternatives considered:
- Single unpartitioned table
- Hash partitioning by trade_id
- Range partitioning by trade_date

## Decision

We will partition the trades table using PostgreSQL RANGE partitioning on trade_date.

Each month will have a separate partition.

Example:

trades_2026_01
trades_2026_02

## Consequences

Positive:
- Faster date-based queries due to partition pruning.
- Easier archival.
- Smaller indexes.

Negative:
- More database maintenance.
- Composite primary keys become more complex.