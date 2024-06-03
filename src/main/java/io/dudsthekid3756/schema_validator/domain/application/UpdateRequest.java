package io.dudsthekid3756.schema_validator.domain.application;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateRequest {

    private String practiceType;
    private String section;
    private ApplicationBase application;
}
