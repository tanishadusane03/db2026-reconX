package com.dbtraining.reconx.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import java.time.Instant;
import java.net.URI;

import java.util.stream.Collectors;

/**
 * ============================================================================
 * TICKET-ADV062 — RFC 7807 ProblemDetail for every ReconException
 *
 * Maps each domain exception subtype to the right HTTP status, with a
 * structured ProblemDetail body so clients don't have to parse free text.
 * ============================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TradeNotFoundException.class)
public ProblemDetail notFound(TradeNotFoundException ex) {

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
    );

    pd.setTitle("Trade Not Found");
    pd.setType(URI.create("/trade-not-found"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}


@ExceptionHandler(DuplicateTradeRefException.class)
public ProblemDetail duplicate(DuplicateTradeRefException ex) {

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            ex.getMessage()
    );

    pd.setTitle("Duplicate Trade Reference");
    pd.setType(URI.create("/duplicate-trade-ref"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}


@ExceptionHandler(InvalidTradeException.class)
public ProblemDetail invalid(InvalidTradeException ex) {

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
    );

    pd.setTitle("Invalid Trade");
    pd.setType(URI.create("/invalid-trade"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}


@ExceptionHandler(ReconciliationMismatchException.class)
public ProblemDetail mismatch(ReconciliationMismatchException ex) {

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY,
            ex.getMessage()
    );

    pd.setTitle("Reconciliation Mismatch");
    pd.setType(URI.create("/reconciliation-mismatch"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}


@ExceptionHandler(MethodArgumentNotValidException.class)
public ProblemDetail validation(MethodArgumentNotValidException ex) {

    String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));


    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            errors
    );

    pd.setTitle("Validation Failed");
    pd.setType(URI.create("/validation-error"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}


@ExceptionHandler(ConstraintViolationException.class)
public ProblemDetail constraint(ConstraintViolationException ex) {

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
    );

    pd.setTitle("Constraint Violation");
    pd.setType(URI.create("/constraint-violation"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}
@ExceptionHandler(Exception.class)
public ProblemDetail generic(Exception ex) {

    log.error("Unexpected server error", ex);

    ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred"
    );

    pd.setTitle("Internal Server Error");
    pd.setType(URI.create("/internal-error"));
    pd.setProperty("timestamp", Instant.now());

    return pd;
}
}
