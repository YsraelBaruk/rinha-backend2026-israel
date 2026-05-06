package com.api.rinha.service;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.api.rinha.model.TransactionRequest;
import com.api.rinha.vector.VPTree;

import java.io.*;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FraudDetectionService {

    private static final int K = 2_000;
    private static final int DIMS = 14;
    private static final int MAX_SIZE = 3_000_000;

    private VPTree vpTree;
    private VectorizerService vectorizer;

    public void loadDataset() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        Map<String, Double> mccRisk;
        try (InputStream is = getClass().getResourceAsStream("/mcc_risk.json")) {
            mccRisk = mapper.readValue(is, new TypeReference<>() {});
        }
        this.vectorizer = new VectorizerService(mccRisk);

        float[] vectors = new float[MAX_SIZE * DIMS];
        byte[] labels = new byte[MAX_SIZE];
        int size = 0;

        try (
            InputStream raw = getClass().getResourceAsStream("/references.json.gz");
            GZIPInputStream gz = new GZIPInputStream(new BufferedInputStream(raw, 65536));
            JsonParser parser = new JsonFactory().createParser(gz)
        ) {
            parser.nextToken();

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                int base = size * DIMS;

                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String field = parser.getCurrentName();
                    parser.nextToken();

                    if ("vector".equals(field)) {
                        for (int i = 0; i < DIMS; i++) {
                            parser.nextToken();
                            vectors[base + i] = parser.getFloatValue();
                        }
                        parser.nextToken();

                    } else if ("label".equals(field)) {
                        labels[size] = "fraud".equals(parser.getText())
                            ? (byte) 1 : (byte) 0;
                    } else {
                        parser.skipChildren();
                    }
                }
                size++;
            }
        }

        System.out.printf("Dataset carregado: %d vetores %n", size);
        this.vpTree = new VPTree(vectors, labels, size);
    }

    public double score(TransactionRequest req) {
        double[] query = vectorizer.vectorize(req);
        return vpTree.knnScore(query, K);
    }
}