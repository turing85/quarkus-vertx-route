package de.turing85.quarkus.verx.route;

import jakarta.ws.rs.core.Response;

import com.google.common.truth.Truth;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.CoreMatchers.is;

@ExtendWith(QuarkusTestExtension.class)
@Slf4j
public class DummyTest {
  @BeforeEach
  void setup(@ConfigProperty(name = "quarkus.http.port", defaultValue = "8080") int port) {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;
  }

  @Test
  void dummyTest() {
    // @formatter:off
    RestAssured
        .when().get("/q/health")
        .then().statusCode(is(Response.Status.OK.getStatusCode()));
    Truth.assertThat(true).isTrue();
    // @formatter:on
  }
}
