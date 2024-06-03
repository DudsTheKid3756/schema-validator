package io.dudsthekid3756.schema_validator.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.*;
import io.dudsthekid3756.schema_validator.common.exception.BadRequest;
import io.dudsthekid3756.schema_validator.common.exception.ExceptionUtils;
import io.dudsthekid3756.schema_validator.common.exception.ServerError;
import io.dudsthekid3756.schema_validator.common.servlet.RequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static io.dudsthekid3756.schema_validator.common.constants.Paths.BASE_PATH;

@Component
@AllArgsConstructor
@Slf4j
public class SchemaValidation {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ResourceLoader resourceLoader;
    private final IgnoreOASHandler ignoreOASHandler;
    private final ExceptionUtils exceptionUtils;

    public boolean schemaIsOASValid(RequestWrapper wrappedRequest, HttpServletResponse response) throws IOException {
        String endpoint = wrappedRequest.getServletPath();
        List<String> pathParts = new ArrayList<>(List.of(endpoint.split(Pattern.quote(BASE_PATH))[1].split("/")));
        pathParts.removeIf(String::isBlank);
        String specPrefix = pathParts.get(0);

        List<String> endpointsMarkedForExclusion = ignoreOASHandler.getEndpointsMarkedForExclusion()
                .stream().map(markedEndpoint -> specPrefix + markedEndpoint).toList();

        if (!endpointsMarkedForExclusion.contains(String.join("/", pathParts))) {
            String schemaPrefix = pathParts.get(1);

            try {
                validateRequestSchema(wrappedRequest.getBody(), specPrefix, schemaPrefix);
            } catch (BadRequest badRequest) {
                exceptionUtils.handleException(response, badRequest, wrappedRequest);
                return false;
            } catch (ServerError serverError) {
                exceptionUtils.handleException(response, serverError, wrappedRequest);
                return false;
            }
        }
        return true;
    }

    private <T> void validateRequestSchema(T request, String specPrefix, String schemaPrefix) throws IOException {

        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(
                SpecVersion.VersionFlag.V202012,
                builder -> builder.addMetaSchema(JsonMetaSchema.getV202012())
        );

        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);

        String fileName = specPrefix + "-openapi.json";
        Resource resource = resourceLoader.getResource(String.format("classpath:readonly-%s", fileName));
        JsonNode openApiNode = objectMapper.readTree(resource.getInputStream());
        String schemaName = StringUtils.capitalize(schemaPrefix) + "Request";

        String componentsKey = "components";
        JsonNode componentNode = openApiNode.get(componentsKey);
        JsonNode schemaNode = componentNode.get("schemas");
        JsonNode requestSchemaNode = schemaNode.get(schemaName);

        if (requestSchemaNode == null) {
            log.error("No schema was found for {} in the {} file", schemaName, fileName);
            throw new BadRequest();
        }

        ((ObjectNode) requestSchemaNode).set(componentsKey, componentNode);

        JsonSchema schema = jsonSchemaFactory.getSchema(requestSchemaNode, config);

        String requestString = request.toString();

        Set<ValidationMessage> validationErrors = schema.validate(
                requestString,
                InputFormat.JSON,
                ExecutionContext::getExecutionConfig
        );

        if (!validationErrors.isEmpty()) {
            for (ValidationMessage error : validationErrors) {
                log.error(error.getMessage());
            }
            throw new BadRequest();
        }

        log.info("Payload structure validated against OAS schema---");
    }
}
