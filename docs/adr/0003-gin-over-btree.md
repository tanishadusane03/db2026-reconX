# ADR-0003 — Use GIN index for JSONB metadata search

## Status

Accepted

Date: 2026-07-29

## Context

ReconX frequently searches instrument metadata.

Example:

Find instruments where:

{
 "currency":"USD"
}

B-tree indexes work well for fixed columns but not JSON structures.

Alternatives considered:

- B-tree index
- No index
- GIN jsonb_path_ops index

## Decision

Use PostgreSQL GIN jsonb_path_ops index on metadata column.

## Consequences

Positive:
- Faster JSON searches.
- Supports flexible metadata queries.

Negative:
- Additional storage usage.
- Index updates add write overhead.