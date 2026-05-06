package com.api.rinha.vector;

import java.util.Arrays;
import java.util.PriorityQueue;

public class VPTree {
    private static final int DIMS = 14;
    private final float[] vectors;
    private final byte[] labels;
    private final int size;

    private final int[] nodeIndex;
    private final float[] nodeRadius;
    private final int[] nodeLeft;
    private final int[] nodeRight;
    private final int root;

    private int[] tempIndexes;
    private int nodeCount = 0;

    public VPTree(float[] vectors, byte[] labels, int size) {
        this.vectors = vectors;
        this.labels  = labels;
        this.size    = size;

        this.nodeIndex  = new int[size];
        this.nodeRadius = new float[size];
        this.nodeLeft   = new int[size];
        this.nodeRight  = new int[size];
        
        Arrays.fill(nodeLeft,  -1);
        Arrays.fill(nodeRight, -1);
        
        this.tempIndexes = new int[size];

        for (int i = 0; i < size; i++) tempIndexes[i] = i;
        this.root = build(0, size);
        this.tempIndexes = null;
        System.out.printf("VPTree construído: %d nós%n", nodeCount);
    }

    private int build(int from, int to) {
        if (from >= to) return -1;

        int vp      = tempIndexes[to - 1];
        int nodeId  = nodeCount++;
        nodeIndex[nodeId] = vp;

        if (to - from == 1) return nodeId;

        int count = to - from - 1;

        double[] dists = new double[count];
        for (int i = 0; i < count; i++) {
            dists[i] = dist(vp, tempIndexes[from + i]);
        }

        double[] sortedDists = dists.clone();
        Arrays.sort(sortedDists);
        int mid = count / 2;
        double radius = (count % 2 == 0)
            ? (sortedDists[mid - 1] + sortedDists[mid]) / 2.0
            : sortedDists[mid];

        nodeRadius[nodeId] = (float) radius;

        int[] inner = new int[count];
        int[] outer = new int[count];
        int   ni = 0, no = 0;

        for (int i = 0; i < count; i++) {
            if (dists[i] < radius) inner[ni++] = tempIndexes[from + i];
            else                   outer[no++] = tempIndexes[from + i];
        }

        System.arraycopy(inner, 0, tempIndexes, from,      ni);
        System.arraycopy(outer, 0, tempIndexes, from + ni, no);

        nodeLeft[nodeId]  = build(from,      from + ni);
        nodeRight[nodeId] = build(from + ni, to - 1);

        return nodeId;
    }

    private double dist(int a, int b) {
        int baseA = a * DIMS;
        int baseB = b * DIMS;
        double sum = 0.0;
        for (int i = 0; i < DIMS; i++) {
            double d = vectors[baseA + i] - vectors[baseB + i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    private double distToQuery(int idx, double[] query) {
        int base = idx * DIMS;
        double sum = 0.0;
        for (int i = 0; i < DIMS; i++) {
            double d = vectors[base + i] - query[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    public double knnScore(double[] query, int k) {
        PriorityQueue<long[]> heap = new PriorityQueue<>(
            k + 1,
            (a, b) -> Double.compare(
                Double.longBitsToDouble(b[1]),
                Double.longBitsToDouble(a[1])
            )
        );

        search(root, query, k, heap);

        long fraudCount = 0;
        for (long[] entry : heap) {
            int idx = (int) entry[0];
            if (labels[idx] == 1) fraudCount++;
        }

        return (double) fraudCount / k;
    }

    private void search(int nodeId, double[] query, int k,
                        PriorityQueue<long[]> heap) {
        if (nodeId == -1) return;

        int vp = nodeIndex[nodeId];
        double d = distToQuery(vp, query);

        heap.offer(new long[]{vp, Double.doubleToLongBits(d)});
        if (heap.size() > k) heap.poll();

        double tau = heap.size() == k
            ? Double.longBitsToDouble(heap.peek()[1])
            : Double.MAX_VALUE;

        double radius = nodeRadius[nodeId];

        if (d < radius) {
            if (d - tau < radius)  search(nodeLeft[nodeId],  query, k, heap);
            if (d + tau >= radius) search(nodeRight[nodeId], query, k, heap);
        } else {
            if (d + tau >= radius) search(nodeRight[nodeId], query, k, heap);
            if (d - tau < radius)  search(nodeLeft[nodeId],  query, k, heap);
        }
    }
}