package com.api.rinha;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import com.api.rinha.handler.FraudScoreHandler;
import com.api.rinha.handler.ReadyHandler;
import com.api.rinha.service.FraudDetectionService;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Promise<Void> startPromise) {
    int port = Integer.parseInt(
      System.getenv().getOrDefault("PORT", "8080")
    );

    vertx.executeBlocking(() -> {
      FraudDetectionService service = new FraudDetectionService();
      service.loadDataset();
      return service;
    }).onComplete(result -> {
      if (result.failed()) {
        startPromise.fail(result.cause());
        return;
      }

      FraudDetectionService service = result.result();

      Router router = Router.router(vertx);
      router.route().handler(BodyHandler.create());

      router.get("/ready").handler(new ReadyHandler());
      router.post("/fraud-score").handler(new FraudScoreHandler(service));

      vertx.createHttpServer()
        .requestHandler(router)
        .listen(port)
        .onComplete(http -> {
          if (http.succeeded()) {
            System.out.println("Servidor na porta " + port);
            startPromise.complete();
          } else {
            startPromise.fail(http.cause());
          }
        });
    });
  }
}