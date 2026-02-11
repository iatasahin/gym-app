package dev.ilkersahin.java.spring.gym.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class TransactionIdFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TransactionIdFilter.class);

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-ID";
    public static final String MDC_TRANSACTION_ID = "transactionId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain
    ) throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_TRANSACTION_ID, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        try {
            log.debug("Request started: {} {}", request.getMethod(), request.getRequestURI());
            chain.doFilter(request, response);
        } finally {
            log.debug("Request completed: {} {} , Status: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus());
            MDC.remove(MDC_TRANSACTION_ID);
        }
    }
}
