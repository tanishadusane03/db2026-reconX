Write an ADR for ReconX.

Decision:
Partition trades table by trade_date.

Alternatives:
1. Single table
2. Hash partitioning
3. Range partitioning

Constraints:
- PostgreSQL 16
- 50k trades/day
- 5 year retention