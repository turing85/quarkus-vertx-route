package de.turing85.quarkus.verx.route;

import java.util.Optional;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;

import io.quarkus.runtime.ShutdownEvent;
import io.smallrye.common.annotation.Identifier;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Config {
  public static final String VERTX_ROUTER_SIMULATOR = "simulator";

  private HttpServer server;

  @Produces
  @Identifier(VERTX_ROUTER_SIMULATOR)
  Router simulator() {
    Vertx vertx = Vertx.vertx();
    Router router = Router.router(vertx);
    server = vertx.createHttpServer().requestHandler(router);
    server.listen(8081);
    return router;
  }

  void shutdown(@Observes ShutdownEvent event) {
    log.info("Shutting down");
    Optional.ofNullable(server).map(HttpServer::close).ifPresent(Future::result);
    log.info("Shutdown done");
  }
}
