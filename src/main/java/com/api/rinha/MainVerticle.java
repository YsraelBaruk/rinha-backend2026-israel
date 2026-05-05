package com.api.rinha;

import io.vertx.core.Future;
import io.vertx.core.VerticleBase;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        int port = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080")
        );

        // Carrega dataset em thread worker (não bloqueia o event loop)
        vertx.executeBlocking(promise -> {
            try {
                FraudDetectionService service = new FraudDetectionService();
                service.loadDataset();   // carrega references.json.gz + índice
                promise.complete(service);
            } catch (Exception e) {
                promise.fail(e);
            }
        }, result -> {
            if (result.failed()) {
                startPromise.fail(result.cause());
                return;
            }

            FraudDetectionService service = (FraudDetectionService) result.result();

            // Configura rotas
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            router.get("/ready").handler(new ReadyHandler());
            router.post("/fraud-score").handler(new FraudScoreHandler(service));

            // Inicia servidor
            vertx.createHttpServer()
                .requestHandler(router)
                .listen(port, http -> {
                    if (http.succeeded()) {
                        System.out.println("🚀 Servidor na porta " + port);
                        startPromise.complete();
                    } else {
                        startPromise.fail(http.cause());
                    }
                });
        });
    }
}