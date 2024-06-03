package io.dudsthekid3756.schema_validator.controller;

import io.dudsthekid3756.schema_validator.common.annotation.IgnoreOAS;
import io.dudsthekid3756.schema_validator.domain.application.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static io.dudsthekid3756.schema_validator.common.constants.Paths.*;

@RestController
@RequestMapping(BASE_PATH + "/application")
@AllArgsConstructor
@Slf4j
public class ApplicationController {

    @IgnoreOAS
    @PostMapping(CREATE_APPLICATION)
    public ResponseEntity<CreateResponse> create(@RequestBody CreateRequest request) {
        log.info("Request received for createApplication---");
        CreateResponse response = new CreateResponse();
        response.setId(UUID.randomUUID().toString());
        response.setStatus("In Progress");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @IgnoreOAS
    @PostMapping(RETRIEVE_APPLICATION)
    public ResponseEntity<RetrieveResponse> retrieve(@RequestBody RetrieveRequest request) {
        log.info("Request received for retrieveApplication---");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping(UPDATE_APPLICATION)
    public ResponseEntity<UpdateResponse> update(@RequestBody UpdateRequest request) {
        log.info("Request received for updateApplication---");
        UpdateResponse response = new UpdateResponse();
        response.setApplication(request.getApplication());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
