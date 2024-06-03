package io.dudsthekid3756.schema_validator.common.servlet;

import io.dudsthekid3756.schema_validator.common.utils.SchemaValidation;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class SchemaValidationFilter implements Filter {

    private final SchemaValidation schemaValidation;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        ServletRequest requestToFilter = servletRequest;
        if (Objects.equals(servletRequest.getContentType(), "application/json")) {
            RequestWrapper wrappedRequest = new RequestWrapper((HttpServletRequest) servletRequest);

            if (!schemaValidation.schemaIsOASValid(wrappedRequest, (HttpServletResponse) servletResponse)) return;

            requestToFilter = wrappedRequest;
        }

        filterChain.doFilter(requestToFilter, servletResponse);
    }
}
