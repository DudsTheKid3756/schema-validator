package io.dudsthekid3756.schema_validator.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dudsthekid3756.schema_validator.SchemaValidatorApplication;
import io.dudsthekid3756.schema_validator.common.exception.ServerError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static io.dudsthekid3756.schema_validator.common.constants.Paths.API_RESOURCE_DEFINITION;

@Component
@Slf4j
public class OASSchemaBundler implements ApplicationRunner {

    private static final String RESOURCE_PREFIX = "classpath:openapi";
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        new OASSchemaBundler().run(null);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!isRunningFromJar()) {
            log.info("Application is running from file---");
            bundleClasspathResources();
        } else log.info("Application is running from jar---");
    }

    private boolean isRunningFromJar() {
        String className = SchemaValidatorApplication.class.getName().replace(".", "/");
        String classJar = Objects.requireNonNull(SchemaValidatorApplication.class.getResource("/" + className + ".class")).toString();
        return classJar.startsWith("jar:");
    }

    private void bundleClasspathResources() throws IOException {
        String oasRootFileName = API_RESOURCE_DEFINITION + "-openapi.json";
        Resource[] oasSchemaResource = resolver.getResources(RESOURCE_PREFIX + "/**/*.json");

        ObjectNode bundledOASNode = objectMapper.createObjectNode();
        List<Resource> resourceList = Arrays.stream(oasSchemaResource).toList();
        List<Resource> rootSchemaResource = resourceList
                .stream().filter(resource -> Objects.equals(resource.getFilename(), oasRootFileName)).toList();
        ObjectNode rootSchemaNode = (ObjectNode) objectMapper.readTree(rootSchemaResource.get(0).getFile());
        handleRefs(rootSchemaNode, this::updateInternal);
        bundledOASNode.setAll(rootSchemaNode);


        for (Resource resource : oasSchemaResource) {
            modifyOASResource(resource, bundledOASNode, oasRootFileName);
        }

        File bundledOASFile = new File("target/classes/readonly-" + oasRootFileName);
        String accessError = "Could not make file read only";
        boolean canChangeAccess = bundledOASFile.setWritable(true);
        if (!canChangeAccess) log.error(accessError);

        writeToFile(bundledOASFile, bundledOASNode, exception -> log.error(exception.getMessage()));
        Path shareableResource = Paths.get("docs/readonly-" + oasRootFileName);
        if (Files.exists(shareableResource)) {
            canChangeAccess = shareableResource.toFile().setWritable(true);
            if (!canChangeAccess) log.error(accessError);
        }
        Files.copy(
                bundledOASFile.toPath(),
                shareableResource,
                StandardCopyOption.REPLACE_EXISTING);

        canChangeAccess = shareableResource.toFile().setWritable(false);
        if (!canChangeAccess) log.error(accessError);

        canChangeAccess = bundledOASFile.setWritable(false);
        if (!canChangeAccess) log.error(accessError);
    }

    private void modifyOASResource(Resource resource, ObjectNode bundledOASNode, String oasRootFileName) {
        if (resource.isFile()) {
            ObjectNode fileNode;
            try {
                fileNode = (ObjectNode) objectMapper.readTree(resource.getFile());
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new ServerError();
            }

            handleRefs(fileNode, this::updateInternal);
            ObjectNode schemasNode = (ObjectNode) bundledOASNode.get("components").get("schemas");
            Iterator<Map.Entry<String, JsonNode>> fields = fileNode.fields();
            fields.forEachRemaining(field -> {
                if (!Objects.equals(resource.getFilename(), oasRootFileName)) {
                    schemasNode.set(field.getKey(), field.getValue());
                }
            });
        }
    }

    private void writeToFile(File file, ObjectNode fileNode, Consumer<IOException> exceptionConsumer) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(fileNode.toPrettyString());
        } catch (IOException e) {
            exceptionConsumer.accept(e);
        }
    }

    private void handleRefs(JsonNode fileNode, BiConsumer<String, ObjectNode> updatePattern) {
        if (fileNode.isObject()) {
            ObjectNode objectNode = (ObjectNode) fileNode;
            if (objectNode.has("$ref")) {
                JsonNode refValue = objectNode.get("$ref");
                String refValueStringified = refValue.toString().replace("\"", "");
                updatePattern.accept(refValueStringified, objectNode);
            }
            objectNode.elements().forEachRemaining(element -> handleRefs(element, updatePattern));
        } else if (fileNode.isArray()) {
            fileNode.forEach(element -> handleRefs(element, updatePattern));
        }
    }

    private void updateInternal(String ref, ObjectNode node) {
        if (!ref.matches("#/components.*")) {
            String keyPath = "#/";
            int startingIndex = ref.indexOf(keyPath);
            if (startingIndex != -1) {
                String newValue = ref.substring(startingIndex + 1);
                node.put("$ref", "#/components/schemas" + newValue);
            }
        }
    }
}
