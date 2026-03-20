package dev.ilkersahin.java.spring.gym.workload.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class TransactionIdFilter extends OncePerRequestFilter {
    public static final String TRANSACTION_ID_HEADER = "X-Transaction-ID";
    public static final String MDC_TRANSACTION_ID = "transactionId";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String transactionId = request.getHeader(TRANSACTION_ID_HEADER);
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put(MDC_TRANSACTION_ID, transactionId);
        response.setHeader(TRANSACTION_ID_HEADER, transactionId);

        try {
            log.debug("Request started: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            log.debug("Request completed: {} {}, Status: {}",
                    request.getMethod(), request.getRequestURI(), response.getStatus());
            MDC.remove(MDC_TRANSACTION_ID);
        }

    }
}
