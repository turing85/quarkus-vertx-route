package de.turing85.quarkus.verx.route;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Qualifier;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;

public class QuarkusTestExtension implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    if (type == TestInfo.class || type == RepetitionInfo.class || type == TestReporter.class) {
      return false;
    }
    return !parameterContext.isAnnotated(TempDir.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext,
      ExtensionContext extensionContext) throws ParameterResolutionException {
    // @formatter:off
    Set<Annotation> qualifierAnnotations =
        Arrays.stream(parameterContext.getParameter().getAnnotations())
            .filter(QuarkusTestExtension::isQualifier)
            .collect(Collectors.toSet());
    Optional<ConfigProperty> maybeConfigProperty = qualifierAnnotations.stream()
        .filter(qualifierAnnotation ->
            qualifierAnnotation.annotationType().equals(ConfigProperty.class))
        .map(ConfigProperty.class::cast)
        .findFirst();
    if (maybeConfigProperty.isPresent()) {
      ConfigProperty configProperty = maybeConfigProperty.get();
      try {
      return ConfigProvider.getConfig()
          .getValue(configProperty.name(), parameterContext.getParameter().getType());
      } catch (NoSuchElementException e) {
          return Integer.parseInt(configProperty.defaultValue());
      }
    } else {
      return CDI.current()
          .select(
              parameterContext.getParameter().getType(),
              qualifierAnnotations.toArray(new Annotation[0])).get();
    }
    // @formatter:on
  }

  private static boolean isQualifier(Annotation annotation) {
    if (annotation.annotationType().equals(Documented.class)) {
      return false;
    }
    if (annotation.annotationType().equals(Qualifier.class)) {
      return true;
    }
    for (Annotation qualifierAnnotation : annotation.annotationType().getAnnotations()) {
      if (annotationHasAnnotation(qualifierAnnotation, Qualifier.class)) {
        return true;
      }
    }
    return false;
  }

  private static boolean annotationHasAnnotation(Annotation annotation,
      Class<? extends Annotation> annotationToHave) {
    if (Set.of(Documented.class, Retention.class, Target.class)
        .contains(annotation.annotationType())) {
      return false;
    }
    if (annotation.annotationType().equals(annotationToHave)) {
      return true;
    }
    for (Annotation qualifierAnnotation : annotation.annotationType().getAnnotations()) {
      if (annotationHasAnnotation(qualifierAnnotation, annotationToHave)) {
        return true;
      }
    }
    return false;
  }
}
