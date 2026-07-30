package com.dbtraining.reconx.service;

import com.dbtraining.reconx.dto.ReconResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class ReconSummaryCollectorTest {

    @Test
    void serialAndParallelStreamsProduceSameSummary() {
        List<ReconResult> results = IntStream.rangeClosed(1, 10_000)
                .mapToObj(i -> {
                    String ref = "EQU-20260603-" + String.format("%04d", i);
                    return i % 3 == 0
                            ? ReconResult.matched(ref)
                            : ReconResult.breakResult(ref, "VALUE_MISMATCH", "dummy");
                })
                .collect(Collectors.toList());

        ReconSummary expected = results.stream()
                .collect(new ReconSummaryCollector());

        ReconSummary actual = results.parallelStream()
                .collect(new ReconSummaryCollector());

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.total()).isEqualTo(10_000);
        assertThat(actual.matched()).isEqualTo(10_000 / 3);
        assertThat(actual.broken()).isEqualTo(10_000 - actual.matched());
    }

    @Test
    void emptyStreamReturnsEmptySummary() {
        ReconSummary actual = List.<ReconResult>of().stream()
                .collect(new ReconSummaryCollector());

        assertThat(actual).isEqualTo(ReconSummary.empty());
    }
}
