package com.api.rinha.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.rinha.model.TransactionRequest;
import com.api.rinha.service.FraudDetectionService;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class FraudScoreHandler implements Handler<RoutingContext> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final FraudDetectionService service;

    public FraudScoreHandler(FraudDetectionService service) {
        this.service = service;
    }

    @Override
    public void handle(RoutingContext ctx) {
        try {
            TransactionRequest req = MAPPER.readValue(
                ctx.body().asString(),
                TransactionRequest.class
            );

            double fraudScore = service.score(req);
            boolean approved  = fraudScore < 0.6;

            String json = String.format(
                "{\"approved\":%b,\"fraud_score\":%.1f}",
                approved, fraudScore
            );

            ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end(json);

        } catch (Exception e) {
            ctx.response()
                .setStatusCode(200)
                .putHeader("Content-Type", "application/json")
                .end("{\"approved\":true,\"fraud_score\":0.0}");
        }
    }
}