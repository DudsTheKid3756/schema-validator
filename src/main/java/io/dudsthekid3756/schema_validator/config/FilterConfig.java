package io.dudsthekid3756.schema_validator.config;

import io.dudsthekid3756.schema_validator.common.servlet.SchemaValidationFilter;
import io.dudsthekid3756.schema_validator.common.servlet.Slf4jMDCFilter;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class FilterConfig {

    private final Slf4jMDCFilter loggingFilter;
    private final SchemaValidationFilter schemaValidationFilter;

    @Bean
    public FilterRegistrationBean<Slf4jMDCFilter> loggingRegistrationBean() {
        final FilterRegistrationBean<Slf4jMDCFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(loggingFilter);
        filterRegistrationBean.addUrlPatterns("/v1/*");
        return filterRegistrationBean;
    }

    @Bean
    public FilterRegistrationBean<SchemaValidationFilter> validationRegistrationBean() {
        final FilterRegistrationBean<SchemaValidationFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(schemaValidationFilter);
        filterRegistrationBean.addUrlPatterns("/v1/*");
        return filterRegistrationBean;
    }
}
