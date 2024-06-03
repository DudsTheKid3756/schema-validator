package io.dudsthekid3756.schema_validator.common.servlet;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.UUID;

/**
 * A filter that uses MDC (Mapped Diagnostic Context) to generate a unique correlation ID to logs for each HTTP request
 */
@Component
@Slf4j
public class Slf4jMDCFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(final @NonNull HttpServletRequest request,
                                    final @NonNull HttpServletResponse response,
                                    final @NonNull FilterChain filterChain) {

        String correlationId = request.getHeader(CORRELATION_ID) == null ?
                UUID.randomUUID().toString() :
                request.getHeader(CORRELATION_ID);
        MDC.put("correlation.id", correlationId);
        response.addHeader(CORRELATION_ID, correlationId);
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("ERROR {}", "Exception occurred in filter while setting UUID for logs", ex);
        } finally {
            MDC.remove("correlation.id");
        }
    }

    @Override
    protected boolean isAsyncDispatch(final @NonNull HttpServletRequest request) {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }
}
