package com.api.rinha.vector;

public class ReferenceVector {

    private final double[]  vector;
    private final boolean   fraud;

    public ReferenceVector(double[] vector, boolean fraud) {
        this.vector = vector;
        this.fraud  = fraud;
    }

    public double[] getVector() { return vector; }
    public boolean  isFraud()   { return fraud;  }

    /** Distância euclidiana ao quadrado (evita sqrt desnecessário na busca). */
    public double distSq(double[] other) {
        double sum = 0.0;
        for (int i = 0; i < vector.length; i++) {
            double d = vector[i] - other[i];
            sum += d * d;
        }
        return sum;
    }

    /** Distância euclidiana real (usada para ordenação final dos K vizinhos). */
    public double dist(double[] other) {
        return Math.sqrt(distSq(other));
    }
}