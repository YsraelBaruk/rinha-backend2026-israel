package com.api.rinha.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.rinha.model.TransactionRequest;
import com.api.rinha.vector.VPTree;
import com.api.rinha.vector.ReferenceVector;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FraudDetectionService {

    private static final int K = 5;
    private static final double THRESHOLD = 0.6;

    private VPTree vpTree;
    private VectorizerService vectorizer;

    public void loadDataset() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Double> mccRisk;
        try (InputStream is = getClass().getResourceAsStream("/mcc_risk.json")) {
            mccRisk = mapper.readValue(is, new TypeReference<>() {});
        }

        this.vectorizer = new VectorizerService(mccRisk);

        List<ReferenceEntry> entries;
        try (InputStream raw = getClass().getResourceAsStream("/references.json.gz");
             GZIPInputStream gz = new GZIPInputStream(raw)) {
            entries = mapper.readValue(gz, new TypeReference<>() {});
        }

        ReferenceVector[] vectors = new ReferenceVector[entries.size()];
        for (int i = 0; i < entries.size(); i++) {
            ReferenceEntry e = entries.get(i);
            vectors[i] = new ReferenceVector(e.vector, "fraud".equals(e.label));
        }

        System.out.printf("Dataset carregado: %d vetores %n", vectors.length);

        this.vpTree = new VPTree(vectors);
        System.out.println("P-Tree construído");
    }

    public double score(TransactionRequest req) {
        double[] queryVector = vectorizer.vectorize(req);
        List<ReferenceVector> neighbors = vpTree.knn(queryVector, K);

        long fraudCount = neighbors.stream()
            .filter(ReferenceVector::isFraud)
            .count();

        return (double) fraudCount / K;
    }

    public static class ReferenceEntry {
        public double[] vector;
        public String   label;
    }
}