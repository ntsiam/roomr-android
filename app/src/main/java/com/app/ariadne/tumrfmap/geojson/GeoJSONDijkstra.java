package com.app.ariadne.tumrfmap.geojson;

import android.graphics.Color;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.LineString;
import com.app.ariadne.tumrfmap.dijkstra.DijkstraAlgorithm;
import com.app.ariadne.tumrfmap.dijkstra.model.Edge;
import com.app.ariadne.tumrfmap.dijkstra.model.Graph;
import com.app.ariadne.tumrfmap.dijkstra.model.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

public class GeoJSONDijkstra {
    private DijkstraAlgorithm dijkstraAlgorithm;
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    public String level;


    public GeoJSONDijkstra(ArrayList<ArrayList<LatLngWithTags>> paths) {
        fromGeoJSONToGraph(paths);
    }

    private Vertex newVertexFromPoint(LatLngWithTags point) {
        String level = point.getLevel();
        String id = point.getLatlng().toString();// + level;
//        System.out.println("Add point with level: " + level);
        return new Vertex(id, id, level);
    }

    private void addNewVertex(ArrayList<Vertex> vertices, Set<String> nameSet, Vertex vertex) {
        if (nameSet.add(vertex.getId())) {
            vertices.add(vertex);
//            System.out.println("Adding vertex with id: " + vertex.getId());
        }
    }

    private void addNewEdge(ArrayList<Edge> edges, Vertex source, Vertex destination, double distance, int level) {
        int weight = (int) Math.round(distance);
        String id = source.getId() + "," + destination.getId() + String.valueOf(level);
        edges.add(new Edge(id, source, destination, weight, level));

//        System.out.println("Adding edge: source: " + source.toString() + ", destination: " + destination.toString());
    }

    private void fromGeoJSONToGraph(ArrayList<ArrayList<LatLngWithTags>> routablePaths) {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        Graph routableGraph;
        Set<String> s = new HashSet<String>(64);
        for (ArrayList<LatLngWithTags> line : routablePaths) {
            for (int i = 0; i < line.size() - 1; i++) {
                Vertex source = newVertexFromPoint(line.get(i));
                Vertex destination = newVertexFromPoint(line.get(i+1));
                addNewVertex(vertices, s, source);
                double distance = SphericalUtil.computeDistanceBetween(line.get(i).getLatlng(), line.get(i+1).getLatlng());
                addNewEdge(edges, source, destination, distance, Integer.valueOf(line.get(i).getLevel()));
                addNewEdge(edges, destination, source, distance, Integer.valueOf(line.get(i).getLevel()));
                if (i == line.size() - 2) {
                    addNewVertex(vertices, s, destination);
                }
            }
        }
//        for (Vertex vertex : vertices) {
//            System.out.println("Vertex: " + vertex.getId());
//        }
        routableGraph = new Graph(vertices, edges);
        dijkstraAlgorithm = new DijkstraAlgorithm(routableGraph);
    }

    private int findVertexIndex(LatLngWithTags source) {
        String startName = source.getLatlng().toString();// + String.valueOf(source.getLevel());
        int index = -1;
        for (int i = 0; i < vertices.size(); i++) {
//            System.out.println("point: " + vertices.get(i).getId() + ", start: " + startName);
            if (vertices.get(i).getId().equals(startName)) {
                System.out.println("Target found: " + vertices.get(i).getId().equals(startName));
                this.level = vertices.get(i).getLevel();
                index = i;
                break;
            }
        }
        return index;
    }

    public void startDijkstra(LatLngWithTags source) {
        int index = findVertexIndex(source);
        if (index >= 0) {
            dijkstraAlgorithm.execute(vertices.get(index));
            ArrayList<Vertex> nodesWithoutpredecessors = dijkstraAlgorithm.getNodesWithoutPredecessors();
//            System.out.println("Start dijkstra from: " + vertices.get(index).getId());
        } else {
            System.out.println("Dijkstra starting point not found!!!!!!");
        }
    }

    private LatLng stringToLatLng(String pointName) {
        StringTokenizer multiTokenizer = new StringTokenizer(pointName, "(),");
        LatLng point;
        int index = 0;
        double latitude = 0.0;
        double longitude = 0.0;
        while (multiTokenizer.hasMoreTokens()) {
            if (index == 1) {
                latitude = Double.valueOf(multiTokenizer.nextToken());
            } else if (index == 2) {
                longitude = Double.valueOf(multiTokenizer.nextToken());
            } else {
                multiTokenizer.nextToken();
            }
            index++;
        }
        point = new LatLng(latitude, longitude);
        return point;
    }

    private LinkedList<Vertex> getGraphPath(LatLngWithTags destination) {
        int index = findVertexIndex(destination);
        LinkedList<Vertex> path = new LinkedList<>();
        LineString pathLineString;
        ArrayList<LatLng> pathArrayList = new ArrayList<>();
        if (index > 0) {
            path = dijkstraAlgorithm.getPath(vertices.get(index));
        } else {
            System.out.println("Destination not found!");
        }
        return path;
    }

    public ArrayList<PolylineOptions> getPath(LatLngWithTags destination) {
        LinkedList<Vertex> path;
        LineString pathLineString;
        ArrayList<PolylineOptions> polylineOptionsInLevels = new ArrayList<>();
        PolylineOptions polylineOptions = new PolylineOptions().width(15).color(Color.RED).zIndex(Integer.MAX_VALUE - 10);
        ArrayList<LatLng> pathArrayList = new ArrayList<>();
//        System.out.println("Target: " + vertices.get(index).toString());
            path = getGraphPath(destination);
            Log.i("GEOJSONDIJKSTRA", "Path found");
            if (path != null) {
                int prevLevel = Integer.MIN_VALUE;
                for (Vertex point : path) {
                    int level = Integer.valueOf(point.getLevel());
                    LatLng nextPoint;
//                System.out.println("Point: " + point.getId());
                    nextPoint = stringToLatLng(point.getId());
                    if (prevLevel == level) {
                        System.out.println("Next point level: " + level);

                        pathArrayList.add(nextPoint);
                        polylineOptions.add(nextPoint);
                    } else {
                        if (prevLevel != Integer.MIN_VALUE) {
                            polylineOptions.add(nextPoint);
                            polylineOptionsInLevels.add(polylineOptions);
                        }
                        polylineOptions = new PolylineOptions().width(15).color(Color.RED).zIndex(Integer.MAX_VALUE - 10);
                        pathArrayList.add(nextPoint);
                        polylineOptions.add(nextPoint);
                        prevLevel = level;
                    }
                }
                polylineOptionsInLevels.add(polylineOptions);
            } else {
                System.out.println("Path not found!");
            }
        return polylineOptionsInLevels;
    }

    public double getPathLength(LatLngWithTags destination) {
        System.out.println("Get Path for dest: " + destination.toString());
        LinkedList<Vertex> path;
        double distance = 0.0;
        LatLng currentPoint = null;
//        System.out.println("Target: " + vertices.get(index).toString());
            path = getGraphPath(destination);
            if (path != null) {
                for (Vertex point : path) {
                    if (currentPoint != null) {
                        LatLng nextPoint;
                        nextPoint = stringToLatLng(point.getId());
                        distance += SphericalUtil.computeDistanceBetween(currentPoint, nextPoint);
                    } else {
                        currentPoint = stringToLatLng(point.getId());
                    }
                }
            } else {
                System.out.println("Path not found!");
                return -1.0;
            }
        return distance;
    }



}
