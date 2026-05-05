package com.api.rinha.service;

import com.api.rinha.model.TransactionRequest;
import java.time.*;
import java.util.Map;

public class VectorizerService {
    private static final double MAX_AMOUNT = 10_000.0;
    private static final double MAX_INSTALLMENTS = 12.0;
    private static final double AMOUNT_VS_AVG_RATIO = 10.0;
    private static final double MAX_MINUTES = 1_440.0;
    private static final double MAX_KM = 1_000.0;
    private static final double MAX_TX_COUNT_24H = 20.0;
    private static final double MAX_MERCHANT_AVG_AMOUNT = 10_000.0;

    private final Map<String, Double> mccRisk;

    public VectorizerService(Map<String, Double> mccRisk) {
        this.mccRisk = mccRisk;
    }

    public double[] vectorize(TransactionRequest req) {
        double[] v = new double[14];

        v[0] = clamp(req.transaction.amount / MAX_AMOUNT);
        v[1] = clamp(req.transaction.installments / MAX_INSTALLMENTS);
        v[2] = clamp(
            (req.transaction.amount / req.customer.avgAmount) / AMOUNT_VS_AVG_RATIO
        );

        ZonedDateTime dt = ZonedDateTime.parse(req.transaction.requestedAt)
            .withZoneSameInstant(ZoneOffset.UTC);
        
        v[3] = dt.getHour() / 23.0;
        
        int dow = dt.getDayOfWeek().getValue() - 1;
        
        v[4] = dow / 6.0;
        
        if (req.lastTransaction == null) {
            v[5] = -1.0;
            v[6] = -1.0;
        } else {
            ZonedDateTime lastDt = ZonedDateTime.parse(req.lastTransaction.timestamp)
                .withZoneSameInstant(ZoneOffset.UTC);
            long minutes = Duration.between(lastDt, dt).toMinutes();
            v[5] = clamp(minutes / MAX_MINUTES);
            v[6] = clamp(req.lastTransaction.kmFromCurrent / MAX_KM);
        }
        
        v[7] = clamp(req.terminal.kmFromHome / MAX_KM);
        v[8] = clamp(req.customer.txCount24h / MAX_TX_COUNT_24H);
        v[9] = req.terminal.isOnline ? 1.0 : 0.0;
        v[10] = req.terminal.cardPresent ? 1.0 : 0.0;

        boolean known = req.customer.knownMerchants != null
            && req.customer.knownMerchants.contains(req.merchant.id);
        
        v[11] = known ? 0.0 : 1.0;
        v[12] = mccRisk.getOrDefault(req.merchant.mcc, 0.5);
        v[13] = clamp(req.merchant.avgAmount / MAX_MERCHANT_AVG_AMOUNT);

        return v;
    }

    private static double clamp(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}