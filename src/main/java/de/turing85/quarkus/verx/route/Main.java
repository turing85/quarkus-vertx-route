package de.turing85.quarkus.verx.route;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain
public class Main implements QuarkusApplication {
  public static void main(String... args) {
    Quarkus.run(Main.class, args);
  }

  @Override
  public int run(String... args) throws Exception {
    Quarkus.waitForExit();
    return 0;
  }
}
