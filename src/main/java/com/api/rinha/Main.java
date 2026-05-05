package com.api.rinha;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class Main {
  public static void main(String[] args) {
    VertxOptions options = new VertxOptions()
      .setEventLoopPoolSize(2)
      .setWorkerPoolSize(4);

    Vertx vertx = Vertx.vertx(options);

    vertx.deployVerticle(new MainVerticle())
      .onComplete(result -> {
        if (result.succeeded()) {
          System.out.println("API iniciada");
        } else {
          System.err.println("Falha ao iniciar: " + result.cause());
          System.exit(1);
        }
      });
  }
}
