package de.turing85.quarkus.verx.route;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Identifier;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.LoggingListener;

@ApplicationScoped
@Slf4j
public class Builder {
  private final Router router;

  public Builder(@Identifier(Config.VERTX_ROUTER_SIMULATOR) Router router) {
    this.router = router;
  }

  public void onStartup(@Observes StartupEvent event) {
    // @formatter:off
    QuarkusStatus.isRunning = true;
    router.errorHandler(
        Response.Status.NOT_FOUND.getStatusCode(),
        context -> context
            .response()
                .setStatusCode(Response.Status.NOT_FOUND.getStatusCode())
                .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .end("Not Found"));
    router.post("/run")
        .produces(MediaType.TEXT_PLAIN)
        .handler(Builder::runTest);
  }

  private static void runTest(RoutingContext context) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    LoggingListener listener = constructLoggingListener(outputStream);
    LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(DiscoverySelectors.selectClass(DummyTest.class))
        .build();
    Launcher launcher = LauncherFactory.create();
    launcher.registerLauncherDiscoveryListeners(LauncherDiscoveryListener.NOOP);
    launcher.registerTestExecutionListeners(listener);
    launcher.discover(request);
    launcher.execute(request);

    String summary = outputStream.toString();
    log.info(summary);
    context
        .response()
            .setStatusCode(Response.Status.OK.getStatusCode())
            .putHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .end(summary);
  }

  private static LoggingListener constructLoggingListener(ByteArrayOutputStream outputStream) {
    PrintStream writer = new PrintStream(outputStream);
    return LoggingListener.forBiConsumer((throwable, messageSupplier) -> {
      String message = messageSupplier.get();
      if (throwable == null) {
        log.error(message, throwable);
        try {
        writer.write("%s%n".formatted(message).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
          // NOOP
        }
      } else {
        log.info(message);
        try {
          writer.write("%s: %s".formatted(message, throwable).getBytes(StandardCharsets.UTF_8));
          throwable.printStackTrace(writer);
          writer.write('\n');
        } catch (IOException e) {
          // NOOP
        }
      }
    });
  }
}
