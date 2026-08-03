package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.entity.TradeStatus;

import com.dbtraining.reconx.repository.TradeRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradesByStatusGauge {

    public TradesByStatusGauge(MeterRegistry registry, TradeRepository repo) {

        // Was hardcoded and included "CANCELLED", which is not a TradeStatus
        // constant — every /actuator/prometheus scrape logged
        // "No enum constant ...TradeStatus.CANCELLED" and the gauge returned NaN.
        for (String status : List.of(
        "PENDING",
        "MATCHED",
        "UNMATCHED",
        "DISPUTED")) {

    Gauge.builder("trades_by_status",
            repo,
            r -> r.countByStatus(TradeStatus.valueOf(status)))
            .tag("status", status)
            .description("Trades currently in a given status")
            .register(registry);
}
    }
}