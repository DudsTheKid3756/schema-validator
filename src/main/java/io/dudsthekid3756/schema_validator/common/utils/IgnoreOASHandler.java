package io.dudsthekid3756.schema_validator.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dudsthekid3756.schema_validator.common.annotation.IgnoreOAS;
import io.dudsthekid3756.schema_validator.common.exception.ServerError;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Getter
@RequiredArgsConstructor
@Slf4j
public class IgnoreOASHandler {

    @NonNull
    private final AnnotationScanner annotationScanner;

    private final List<String> endpointsMarkedForExclusion = new ArrayList<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Scans the controller package for methods that have the IgnoreOAS annotation applied
     */
    @PostConstruct
    private void scanForIgnoreOASAnnotations() {
        List<Method> methods = annotationScanner.getMethodsByAnnotation(IgnoreOAS.class);
        for (Method method : methods) {
            List<Annotation> mappingAnnotations = Arrays.stream(method.getDeclaredAnnotations())
                    .filter(annotation -> annotation.annotationType().getSimpleName().endsWith("Mapping"))
                    .toList();
            if (mappingAnnotations.isEmpty()) {
                log.error("Could not find methods with IgnoreOAS annotation that match the expected criteria. " +
                        "Annotation should be applied along side RequestMapping annotations---");
                throw new ServerError();
            }

            Annotation mappingClass = mappingAnnotations.get(0);
            Map<String, Object> mappingAnnotation = AnnotationUtils.getAnnotationAttributes(mappingClass);
            JsonNode mappingNode = objectMapper.valueToTree(mappingAnnotation.get("value"));
            if (mappingNode.isNull()) {
                log.error("Unable to map request mapping value to JsonNode---");
                throw new ServerError();
            }
            if (mappingNode.isEmpty()) {
                mappingNode = objectMapper.valueToTree(mappingAnnotation.get("path"));
                if (mappingNode.isNull() || mappingNode.isEmpty()) {
                    log.error("Unable to find default path/value for provided {}---", mappingClass);
                    throw new ServerError();
                }
            }

            String mappedPathValue = mappingClass.toString().replace("\"", "");
            endpointsMarkedForExclusion.add(mappedPathValue);
        }

        int markedEndpointsListSize = endpointsMarkedForExclusion.size();
        if (!endpointsMarkedForExclusion.isEmpty()) {
            log.info("Found {} endpoint{} to exclude: [{}]",
                    markedEndpointsListSize,
                    markedEndpointsListSize > 1 ? "s" : "",
                    String.join(", ", endpointsMarkedForExclusion)
            );
        } else {
            log.info("No endpoints found to exclude form validation");
        }
    }
}
