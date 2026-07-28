# ADR-0002 — Use JSONB for instrument metadata

## Status

Accepted

Date: 2026-07-29

## Context

ReconX stores instrument information.

Different instruments contain different attributes.

Example:

Stocks:
- exchange
- sector

Bonds:
- coupon
- maturity_date


Alternatives considered:
- Separate metadata tables
- Fixed columns
- JSON storage

## Decision

Use PostgreSQL JSONB column for instrument metadata.

## Consequences

Positive:
- Flexible schema.
- New attributes can be added without migrations.

Negative:
- Validation becomes application responsibility.
- Complex queries need careful indexing.