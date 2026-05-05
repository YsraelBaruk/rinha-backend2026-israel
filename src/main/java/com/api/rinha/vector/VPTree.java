package com.rinha.vector;

import java.util.*;

public class VPTree {

    private final Node root;

    public VPTree(ReferenceVector[] vectors) {
        List<ReferenceVector> list = new ArrayList<>(Arrays.asList(vectors));
        this.root = build(list);
    }

    private Node build(List<ReferenceVector> points) {
        if (points.isEmpty()) return null;

        Node node = new Node();

        node.vantage = points.remove(points.size() - 1);

        if (points.isEmpty()) return node;

        double[] dists = new double[points.size()];
        double[] vec   = node.vantage.getVector();
        for (int i = 0; i < points.size(); i++) {
            dists[i] = node.vantage.dist(points.get(i).getVector());
        }

        node.radius = median(dists.clone());

        List<ReferenceVector> inner = new ArrayList<>();
        List<ReferenceVector> outer = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (dists[i] < node.radius) inner.add(points.get(i));
            else                         outer.add(points.get(i));
        }

        node.left  = build(inner);
        node.right = build(outer);
        return node;
    }

    private double median(double[] arr) {
        Arrays.sort(arr);
        int mid = arr.length / 2;
        return (arr.length % 2 == 0)
            ? (arr[mid - 1] + arr[mid]) / 2.0
            : arr[mid];
    }

    public List<ReferenceVector> knn(double[] query, int k) {
        PriorityQueue<ReferenceVector> heap = new PriorityQueue<>(
            k + 1,
            (a, b) -> Double.compare(b.dist(query), a.dist(query))
        );

        search(root, query, k, heap);

        return new ArrayList<>(heap);
    }

    private void search(Node node, double[] query, int k,
                        PriorityQueue<ReferenceVector> heap) {
        if (node == null) return;

        double d = node.vantage.dist(query);

        heap.offer(node.vantage);
        if (heap.size() > k) heap.poll();

        double tau = heap.size() == k
            ? heap.peek().dist(query)
            : Double.MAX_VALUE;

        if (d < node.radius) {
            if (d - tau < node.radius) search(node.left,  query, k, heap);
            if (d + tau >= node.radius) search(node.right, query, k, heap);
        } else {
            if (d + tau >= node.radius) search(node.right, query, k, heap);
            if (d - tau < node.radius)  search(node.left,  query, k, heap);
        }
    }

    private static class Node {
        ReferenceVector vantage;
        double radius;
        Node left;
        Node right;
    }
}