package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
class TradeControllerWebMvcTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private TradeService tradeService;
    // TradeController also injects the MapStruct-generated TradeMapper, which is not
    // part of the @WebMvcTest slice — mock it so the context can start.
    @MockBean  private TradeMapper tradeMapper;

    private TradeRequest validRequest() {
        // Field order matches the current TradeRequest record:
        // (tradeRef, instrumentId, counterpartyId, assetClass, side, quantity, price, tradeDate).
        // tradeRef regex: ^[A-Z]{3}-\d{8}-\d{4}$. Status is NOT a request field — it is set server-side.
        return new TradeRequest(
                "TRD-20260315-9999",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now(),
                "PENDING");
    }

    @Test
    @WithMockUser(roles = "TRADER")
    void testCreateTrade_authenticated_returns201() throws Exception {
        // TradeController.create returns service.create(req, actor) -> Trade entity,
        // then maps it through TradeMapper. Mock both hops.
        Instant now = Instant.now();

        Trade saved = mock(Trade.class);
        when(saved.getId()).thenReturn(42L);
        when(tradeService.create(any(), any())).thenReturn(saved);

        // Field order matches the current TradeResponse record:
        // (id, tradeRef, counterpartyId, counterpartyName, instrumentId, instrumentSymbol,
        //  quantity, price, tradeDate, status, createdAt, modifiedAt).
        when(tradeMapper.toResponse(any())).thenReturn(
                new TradeResponse(
                        42L,
                        "TRD-20260315-9999",
                        1L,
                        "Apex Brokers Inc",
                        1L,
                        "SAP.DE",
                        new BigDecimal("100.0000"),
                        new BigDecimal("245.50"),
                        LocalDate.now(),
                        "PENDING",
                        now,
                        now));

        mockMvc.perform(post("/api/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest()))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/trades/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
    }
    @Test
    void testCreateTrade_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/trades")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "tradeRef": "ABC-20260101-0001",
                    "instrumentId": 1,
                    "counterpartyId": 1,
                    "assetClass": "EQUITY",
                    "side": "BUY",
                    "quantity": 10,
                    "price": 100
                }
                """))
            .andExpect(status().isUnauthorized());
    }
    @Test
    @WithMockUser(roles = "VIEWER")
    void testCreateTrade_viewerRole_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/trades")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRequest())))
            .andExpect(status().isForbidden());
        }
}
