package io.dudsthekid3756.schema_validator.common.utils;

import io.dudsthekid3756.schema_validator.common.exception.ServerError;
import io.dudsthekid3756.schema_validator.config.ScannerConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
@Slf4j
public class AnnotationScanner {

    private ScannerConfig scannerConfig;

    public List<Method> getMethodsByAnnotation(Class<? extends Annotation> annotationClass) {

        TypeFilter typeFilter = new AnnotationTypeFilter(annotationClass);

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.addIncludeFilter(typeFilter);

        String controllerPackage = "io.dudsthekid3756" + scannerConfig.getControllerPackagePath() + ".controller";

        log.info("Scanning base package {} for methods with {} annotations---", controllerPackage, annotationClass.getSimpleName());

        List<Method> methodsWithAnnotation = new ArrayList<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(controllerPackage)) {
            Class<?> clazz;
            try {
                clazz = ClassUtils.forName(Objects.requireNonNull(bd.getBeanClassName()), ClassUtils.getDefaultClassLoader());
            } catch (ClassNotFoundException ex) {
                log.error(ex.getMessage());
                throw new ServerError();
            }

            for (Method method : clazz.getMethods()) {
                Annotation annotation = AnnotationUtils.findAnnotation(method, annotationClass);
                if (annotation != null) {
                    methodsWithAnnotation.add(method);
                }
            }
        }

        return methodsWithAnnotation;
    }

    public void overrideScannerConfigControllerPackage(String controllerPackagePath) {
        scannerConfig.setControllerPackagePath(controllerPackagePath);
    }
}
