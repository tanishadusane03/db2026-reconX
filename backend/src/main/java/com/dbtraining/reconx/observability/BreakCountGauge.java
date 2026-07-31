package com.dbtraining.reconx.observability;

import com.dbtraining.reconx.repository.ReconBreakRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class BreakCountGauge {

    private final ReconBreakRepository breakRepository;

    public BreakCountGauge(
            MeterRegistry registry,
            ReconBreakRepository breakRepository) {

        this.breakRepository = breakRepository;

        Gauge.builder(
                "recon_break_count",
                breakRepository,
                repo -> repo.countByStatus("OPEN")
        )
        .description("Open recon breaks")
        .register(registry);
    }
}