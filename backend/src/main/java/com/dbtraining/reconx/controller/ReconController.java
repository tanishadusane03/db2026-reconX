package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.ReconRunRequest;
import com.dbtraining.reconx.exception.TradeNotFoundException;
import com.dbtraining.reconx.repository.ReconBreakRepository;
import com.dbtraining.reconx.repository.entity.ReconBreak;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.dbtraining.reconx.dto.ResolutionRequest;

@RestController
@RequestMapping("/v1/recon")
@Tag(name = "recon", description = "Reconciliation operations")
@SecurityRequirement(name = "bearerAuth")
public class ReconController {

    private final ReconBreakRepository breaks;

    public ReconController(ReconBreakRepository breaks) {
        this.breaks = breaks;
    }

    /**
     * TICKET-ADV068
     */
    @PostMapping("/run")
    @Operation(summary = "Trigger a reconciliation job (async)")
    public ResponseEntity<Map<String, String>> runRecon(
            @Valid @RequestBody ReconRunRequest req) {

        String jobId = UUID.randomUUID().toString();

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(Map.of(
                        "jobId", jobId,
                        "status", "QUEUED"
                ));
    }

    /**
     * TICKET-ADV069
     */
    @GetMapping("/jobs/{jobId}/results")
    @Operation(summary = "Get results for a recon job")
    public List<ReconBreak> results(@PathVariable String jobId) {

        // Trainer stub
        return breaks.findAll();
    }

    /**
     * TICKET-ADV070
     */
    @PutMapping("/results/{id}/resolve")
@Operation(summary = "Mark a recon break as RESOLVED with a note")
public ResponseEntity<ReconBreak> resolve(
        @PathVariable Long id,
        @Valid @RequestBody ResolutionRequest request) {

    ReconBreak rb = breaks.findById(id)
            .orElseThrow(() ->
                    new TradeNotFoundException("recon_break " + id));

    rb.resolve(request.note());

    return ResponseEntity.ok(breaks.save(rb));
}
}