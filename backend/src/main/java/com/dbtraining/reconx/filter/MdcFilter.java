package com.dbtraining.reconx.filter;


import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;


@Component
@Order(1)
public class MdcFilter implements Filter {


    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {


        HttpServletRequest httpRequest =
                (HttpServletRequest) request;


        String correlationId =
                httpRequest.getHeader("X-Correlation-Id");


        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }


        MDC.put("correlationId", correlationId);


        String tradeRef =
                httpRequest.getHeader("X-Trade-Ref");


        if (tradeRef != null) {
            MDC.put("tradeRef", tradeRef);
        }


        try {

            chain.doFilter(request, response);

        } finally {

            MDC.clear();

        }
    }
}
