-- ============================================================================
-- TICKET-ADV010 — VWAP per instrument per day (window function)
-- ============================================================================
SELECT DISTINCT
    t.instrument_id,
    t.trade_date,
    SUM(t.price * t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date)
        / NULLIF(SUM(t.quantity) OVER (PARTITION BY t.instrument_id, t.trade_date), 0)
            AS vwap
FROM trades t
WHERE t.deleted_at IS NULL
  AND t.asset_class = 'EQUITY'
ORDER BY t.trade_date DESC, t.instrument_id;


-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup
--                 (execution -> confirmation -> settlement -> recon_break
--                  -> resolution)
-- ============================================================================
WITH RECURSIVE trade_lifecycle AS (
    -- anchor: every trade starts at EXECUTION
    SELECT
        t.id           AS trade_id,
        t.trade_ref,
        1              AS stage,
        'EXECUTION'    AS stage_name,
        t.created_at   AS event_at,
        COALESCE(t.status, 'EXECUTED')::text AS event_status
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    -- recursive: advance by exactly one stage per iteration
    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        next_event.stage_name,
        next_event.event_at,
        next_event.event_status
    FROM trade_lifecycle tl
    JOIN LATERAL (
        -- stage 2: confirmation
        SELECT
            'CONFIRMATION'::text AS stage_name,
            COALESCE(t2.modified_at, t2.created_at) AS event_at,
            CASE
                WHEN t2.status IN ('CONFIRMED', 'SETTLED', 'MATCHED', 'BROKEN', 'RESOLVED') THEN 'CONFIRMED'
                ELSE 'PENDING_CONFIRMATION'
            END::text AS event_status
        FROM trades t2
        WHERE tl.stage = 1
          AND t2.id = tl.trade_id

        UNION ALL

        -- stage 3: settlement
        SELECT
            'SETTLEMENT'::text AS stage_name,
            COALESCE(s.settlement_date::timestamp, now()) AS event_at,
            COALESCE(s.status, 'PENDING')::text AS event_status
        FROM (
            SELECT s1.*
            FROM settlements s1
            WHERE s1.trade_id = tl.trade_id
            ORDER BY s1.settlement_date DESC, s1.id DESC
            LIMIT 1
        ) s
        WHERE tl.stage = 2

        UNION ALL

        -- stage 4: recon break
        SELECT
            'RECON_BREAK'::text AS stage_name,
            COALESCE(rb.detected_at, now()) AS event_at,
            COALESCE(rb.status, 'NO_BREAK')::text AS event_status
        FROM (
            SELECT rb1.*
            FROM recon_breaks rb1
            WHERE rb1.trade_id = tl.trade_id
            ORDER BY rb1.detected_at DESC NULLS LAST, rb1.id DESC
            LIMIT 1
        ) rb
        WHERE tl.stage = 3

        UNION ALL

        -- stage 5: resolution
        SELECT
            'RESOLUTION'::text AS stage_name,
            COALESCE(rb_resolved.resolved_at, rb_resolved.detected_at, now()) AS event_at,
            CASE
                WHEN rb_resolved.resolved_at IS NOT NULL THEN COALESCE(rb_resolved.status, 'RESOLVED')
                ELSE 'UNRESOLVED'
            END::text AS event_status
        FROM (
            SELECT rb2.*
            FROM recon_breaks rb2
            WHERE rb2.trade_id = tl.trade_id
            ORDER BY rb2.resolved_at DESC NULLS LAST, rb2.detected_at DESC NULLS LAST, rb2.id DESC
            LIMIT 1
        ) rb_resolved
        WHERE tl.stage = 4
    ) AS next_event ON TRUE
    WHERE tl.stage < 5
)
SELECT
    trade_id,
    trade_ref,
    stage,
    stage_name,
    event_at,
    event_status
FROM trade_lifecycle
ORDER BY trade_id, stage;


-- ============================================================================
-- TICKET-ADV008 — REFRESH the daily-summary materialised view (concurrent so it
--                 can run while the dashboard is reading it)
-- ============================================================================
REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_recon_summary;


-- ============================================================================
-- ADV009 — JSONB lookup: which instruments have sector = 'Banking'?
-- ============================================================================
SELECT id, symbol, metadata
FROM instruments
WHERE metadata @> '{"sector":"Banking"}'::jsonb;
