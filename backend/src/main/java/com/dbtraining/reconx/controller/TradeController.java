package com.dbtraining.reconx.controller;
import java.net.URI;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dbtraining.reconx.dto.PagedResponse;
import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;
import com.dbtraining.reconx.sse.TradeStreamService;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * ============================================================================
 * TICKET-ADV063-ADV067 — TradeController (full CRUD + filterable list)
 * TICKET-ADV080 — API versioning: every endpoint under /v1/
 *
 * Combined with the /api context-path from application.yml, full URLs are
 * /api/v1/trades, /api/v1/trades/{id} etc.
 * ============================================================================
 */
@RestController
@RequestMapping("/v1/trades")
@Tag(name = "trades", description = "Trade CRUD and search")
@SecurityRequirement(name = "bearerAuth")

public class TradeController {

    private final TradeService service;
    private final TradeMapper mapper;
    private final TradeStreamService stream;

    public TradeController(TradeService service, TradeMapper mapper, TradeStreamService stream) {
        this.service = service;
        this.mapper = mapper;
        this.stream = stream;
    }

    // TICKET-ADV104 — Day 7 SSE live feed. Must be declared before any GET
    // mapping that could otherwise shadow the "stream" path segment.
    @GetMapping(value = "/stream", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Live SSE feed of trade create/update/status-change events")
    public SseEmitter stream() {
        return stream.subscribe();
    }

    @GetMapping
@Operation(summary = "List trades — paginated, filterable, sortable")
public PagedResponse<TradeResponse> list(
        @RequestParam(required = false) LocalDate from,
        @RequestParam(required = false) LocalDate to,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Long counterpartyId,
        @PageableDefault(size = 20, sort = "tradeDate", direction = Sort.Direction.DESC)
        Pageable pageable) {

    Page<Trade> page = service.list(
            from,
            to,
            status,
            counterpartyId,
            pageable
    );

    return PagedResponse.from(page, mapper::toResponse);
}
    @PostMapping
    @Operation(summary = "Create a trade")
    public ResponseEntity<TradeResponse> create(@Valid @RequestBody TradeRequest req,
                                                @AuthenticationPrincipal Object principal) {
        // TODO(TICKET-ADV064): call service.create(req, actor), build a Location
        //   header at /api/v1/trades/{id}, and return 201 Created with the
        //   mapped TradeResponse body.
         String actor = String.valueOf(principal);
    Trade saved = service.create(req, actor);
    TradeResponse response = mapper.toResponse(saved);
    stream.broadcast(response);
    return ResponseEntity
            .created(URI.create("/api/v1/trades/" + saved.getId()))
            .body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Full update of a trade")
    public TradeResponse update(@PathVariable Long id, @Valid @RequestBody TradeRequest req,
                                @AuthenticationPrincipal Object principal) {
        // TODO(TICKET-ADV065): delegate to service.update(id, req, actor) and
        //   map the updated entity through mapper.toResponse.
        TradeResponse response = mapper.toResponse(service.update(id, req, String.valueOf(principal)));
        stream.broadcast(response);
        return response;
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update only the status field")
    public TradeResponse updateStatus(@PathVariable Long id,
                                      @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal Object principal) {
        // TODO(TICKET-ADV066): read body.get("status") and call
        //   service.updateStatus(id, status, actor). Return mapper.toResponse(saved).
        String status = body.get("status");
        TradeResponse response = mapper.toResponse(service.updateStatus(id, status, String.valueOf(principal)));
        stream.broadcast(response);
        return response;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete (sets deleted_at)")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal Object principal) {
        // TODO(TICKET-ADV067): service.softDelete(id, actor); return 204 No Content.
        service.softDelete(id, String.valueOf(principal));
    return ResponseEntity.noContent().build();
    }
}
