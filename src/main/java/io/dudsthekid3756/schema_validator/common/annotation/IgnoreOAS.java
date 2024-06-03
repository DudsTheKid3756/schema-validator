package io.dudsthekid3756.schema_validator.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A marker indicating if a controller method should ignore OAS schema validation
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface IgnoreOAS {
}
