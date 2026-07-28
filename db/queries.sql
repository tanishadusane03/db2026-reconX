-- ============================================================================
-- TICKET-ADV011 — Recursive CTE: trade lifecycle rollup
-- (execution -> confirmation -> settlement -> recon_break -> resolution)
-- ============================================================================

WITH RECURSIVE trade_lifecycle AS (

    -- Anchor: every trade starts at EXECUTION
    SELECT
        t.id AS trade_id,
        t.trade_ref,
        1 AS stage,
        'EXECUTION' AS stage_name,
        t.created_at AS event_at,
        COALESCE(t.status, 'EXECUTED') AS event_status
    FROM trades t
    WHERE t.deleted_at IS NULL

    UNION ALL

    SELECT
        tl.trade_id,
        tl.trade_ref,
        tl.stage + 1,
        next_event.stage_name,
        next_event.event_at,
        next_event.event_status
    FROM trade_lifecycle tl

    JOIN LATERAL (

        -- Stage 2: Confirmation
        SELECT
            'CONFIRMATION' AS stage_name,
            COALESCE(t2.modified_at, t2.created_at) AS event_at,
            CASE
                WHEN t2.status IN ('CONFIRMED','SETTLED','MATCHED','BROKEN','RESOLVED')
                    THEN 'CONFIRMED'
                ELSE 'PENDING_CONFIRMATION'
            END AS event_status
        FROM trades t2
        WHERE tl.stage = 1
          AND t2.id = tl.trade_id

        UNION ALL

        -- Stage 3: Settlement
        SELECT
            'SETTLEMENT',
            COALESCE(s.settlement_date::timestamp, now()),
            COALESCE(s.status, 'PENDING')
        FROM (
            SELECT *
            FROM settlements
            WHERE trade_id = tl.trade_id
            ORDER BY settlement_date DESC, id DESC
            LIMIT 1
        ) s
        WHERE tl.stage = 2

        UNION ALL

        -- Stage 4: Recon Break
        SELECT
            'RECON_BREAK',
            COALESCE(rb.detected_at, now()),
            COALESCE(rb.status, 'NO_BREAK')
        FROM (
            SELECT *
            FROM recon_breaks
            WHERE trade_id = tl.trade_id
            ORDER BY detected_at DESC NULLS LAST, id DESC
            LIMIT 1
        ) rb
        WHERE tl.stage = 3

        UNION ALL

        -- Stage 5: Resolution
        SELECT
            'RESOLUTION',
            COALESCE(rb.resolved_at, rb.detected_at, now()),
            CASE
                WHEN rb.resolved_at IS NOT NULL
                    THEN COALESCE(rb.status, 'RESOLVED')
                ELSE 'UNRESOLVED'
            END
        FROM (
            SELECT *
            FROM recon_breaks
            WHERE trade_id = tl.trade_id
            ORDER BY resolved_at DESC NULLS LAST,
                     detected_at DESC NULLS LAST,
                     id DESC
            LIMIT 1
        ) rb
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
