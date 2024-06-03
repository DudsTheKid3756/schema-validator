package io.dudsthekid3756.schema_validator.common.exception;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class ExceptionResponse {

    private int code;
    private String status;
    private String error;
    private String path;
    private Timestamp timestamp;

}
