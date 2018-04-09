package com.app.ariadne.tumrfmap.dijkstra.model;

public class Edge  {
    private final String id;
    private final Vertex source;
    private final Vertex destination;
    private final int weight;
    private final int level;

    public Edge(String id, Vertex source, Vertex destination, int weight, int level) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.weight = weight;
        this.level = level;
    }

    public String getId() {
        return id;
    }
    public Vertex getDestination() {
        return destination;
    }
    public int getLevel() {
        return level;
    }

    public Vertex getSource() {
        return source;
    }
    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return source + " " + destination;
    }


}