package io.dudsthekid3756.schema_validator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

import static io.dudsthekid3756.schema_validator.common.constants.Paths.API_RESOURCE_DEFINITION;

@Configuration
@Getter
@Setter
public class ScannerConfig {

    private String controllerPackagePath = API_RESOURCE_DEFINITION;
}
