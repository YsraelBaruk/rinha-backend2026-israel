package com.api.rinha.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class ReadyHandler implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        ctx.response()
            .setStatusCode(200)
            .putHeader("Content-Type", "application/json")
            .end("{\"status\":\"ok\"}");
    }
}