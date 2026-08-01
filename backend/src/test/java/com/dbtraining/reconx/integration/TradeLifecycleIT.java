package com.dbtraining.reconx.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.HashMap;

import com.dbtraining.reconx.dto.TradeRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TradeLifecycleIT {
    static String token;
    static Long createdId;
    static Long reconJobId;
    static Long breakId;
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    void loginAsAdmin() throws Exception {
        Map<String, String> request = Map.of(
            "email", "admin@db.com",
            "password", "password"
        );
        ResponseEntity<String> response =
            restTemplate.postForEntity(
                    "/api/auth/login",
                    request,
                    String.class
            );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode json = objectMapper.readTree(response.getBody());

        token = json.get("token").asText();

        assertNotNull(token);
    }
    private HttpHeaders authHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
    
        return headers;
    }
    @Test
    @Order(2)
    void createTrade() throws Exception {
        TradeRequest request = new TradeRequest(
            "ABC-20260801-0001",
            1L,
            1L,
            "EQUITY",
            "BUY",
            new BigDecimal("100"),
            new BigDecimal("150.50"),
            LocalDate.now()
        );


        HttpEntity<TradeRequest> entity =
            new HttpEntity<>(request, authHeaders());


        ResponseEntity<String> response =
            restTemplate.postForEntity(
                    "/v1/trades",
                    entity,
                    String.class
            );


        assertEquals(
            201,
            response.getStatusCode().value()
        );


        JsonNode json =
            objectMapper.readTree(response.getBody());


        createdId = json.get("id").asLong();


        assertNotNull(createdId);
    }
    @Test
    @Order(3)
    void getTrades() {

        HttpEntity<Void> entity =
            new HttpEntity<>(authHeaders());


        ResponseEntity<String> response =
            restTemplate.exchange(
                    "/v1/trades",
                    HttpMethod.GET,
                    entity,
                    String.class
            );


        assertEquals(
            200,
            response.getStatusCode().value()
        );


        assertNotNull(response.getBody());
    }
    @Test
    @Order(4)
    void updateStatus() {
        Map<String,String> body = new HashMap<>();
        body.put("status", "MATCHED");

        HttpHeaders headers = authHeaders();

        HttpEntity<Map<String,String>> entity =
            new HttpEntity<>(body, headers);


        ResponseEntity<String> response =
            restTemplate.exchange(
                    "/v1/trades/" + createdId + "/status",
                    HttpMethod.PATCH,
                    entity,
                    String.class
            );


        assertEquals(
            HttpStatus.OK,
            response.getStatusCode()
        );
    }
    @Test
    @Order(5)
    void runRecon() throws Exception{

        HttpEntity<Void> entity =
            new HttpEntity<>(authHeaders());

        ResponseEntity<String> response =
            restTemplate.exchange(
                    "/v1/recon/run",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

        assertEquals(
            HttpStatus.ACCEPTED,
            response.getStatusCode()
        );
        JsonNode json =
            objectMapper.readTree(response.getBody());

        reconJobId =
            json.get("jobId").asLong();

    }
    @Test
    @Order(6)
    void resolveBreak() {

        HttpEntity<Void> entity =
            new HttpEntity<>(authHeaders());

        ResponseEntity<String> response =
            restTemplate.exchange(
                    "/v1/recon/results/" + breakId + "/resolve",
                    HttpMethod.PUT,
                    entity,
                    String.class
            );

        assertEquals(
            HttpStatus.OK,
            response.getStatusCode()
        );
    }

}