package com.app.ariadne.tumaps.geojson;

import android.graphics.Color;
import android.util.Log;

import com.app.ariadne.tumaps.dijkstra.DijkstraAlgorithm;
import com.app.ariadne.tumaps.dijkstra.model.Edge;
import com.app.ariadne.tumaps.dijkstra.model.Graph;
import com.app.ariadne.tumaps.dijkstra.model.Vertex;
import com.app.ariadne.tumaps.models.Route;
import com.app.ariadne.tumaps.models.RouteInstruction;
import com.app.ariadne.tumrfmap.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.data.LineString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;

public class GeoJSONDijkstra {
    private DijkstraAlgorithm dijkstraAlgorithm;
    private ArrayList<Vertex> vertices;
    private ArrayList<Edge> edges;
    private ArrayList<ArrayList<Vertex>> verticesForEachBuilding;
    private ArrayList<ArrayList<Edge>> edgesForEachBuilding;
    public String level;
    public double maxRouteLat;
    public double maxRouteLng;
    public double minRouteLat;
    public double minRouteLng;
    private HashMap<String, Edge> edgeHashMap;
    public int minRouteLevel;
    public int maxRoutelevel;
    public int sourceLevel;
    public ArrayList<String> routeInstructions;
    public ArrayList<LatLng> routeInstructionPoints;
    public Queue<RouteInstruction> instructionQueue;


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
        Edge edge = new Edge(id, source, destination, weight, level);
        edges.add(edge);
        edgeHashMap.put(source.getId() + destination.getId(), edge);
        edgeHashMap.put(destination.getId() + source.getId(), edge);

//        System.out.println("Adding edge: source: " + source.toString() + ", destination: " + destination.toString());
    }

    private void fromGeoJSONToGraph(ArrayList<ArrayList<LatLngWithTags>> routablePaths) {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        edgeHashMap = new HashMap<>();
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
        dijkstraAlgorithm = new DijkstraAlgorithm(routableGraph, edgeHashMap);
    }

    private int findVertexIndex(LatLngWithTags source) {
        String startName = source.getLatlng().toString();// + String.valueOf(source.getLevel());
        int index = -1;
        for (int i = 0; i < vertices.size(); i++) {
//            System.out.println("point: " + vertices.get(i).getId() + ", start: " + startName);
            if (vertices.get(i).getId().equals(startName)) {
//                System.out.println("Target found: " + vertices.get(i).getId().equals(startName));
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
//            ArrayList<Vertex> nodesWithoutpredecessors = dijkstraAlgorithm.getNodesWithoutPredecessors();
//            System.out.println("Start dijkstra from: " + vertices.get(index).getId());
//        } else {
//            System.out.println("Dijkstra starting point not found!!!!!!");
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
//        } else {
//            System.out.println("Destination not found!");
        }
        return path;
    }

    public Route getPath(LatLngWithTags destination) {
        long unixTime = System.currentTimeMillis();
//        Log.i("getPath", "HandleRouteRequest, getPath, Start: " + unixTime);
        initRouteMinMax();
        LinkedList<Vertex> path;
        LineString pathLineString;
        HashMap<Integer, ArrayList<MarkerOptions>> pathStairMarkers = new HashMap<>();
        HashMap<Integer, ArrayList<PolylineOptions>> pathHashMapForEachLevel = new HashMap<>();
        ArrayList<PolylineOptions> polylineOptionsInLevels = new ArrayList<>();
        PolylineOptions polylineOptions = new PolylineOptions().width(10).color(Color.RED).zIndex(Integer.MAX_VALUE - 1000);
        ArrayList<LatLng> pathArrayList = new ArrayList<>();

        routeInstructions = new ArrayList<>();
        routeInstructionPoints = new ArrayList<>();
        String instruction;
        double currentHeading;
        double prevHeading = -1000.0;
        double currentDistance = 0.0;
        LatLng prevInstructionPoint = null;
        instructionQueue = new LinkedList<>();
        RouteInstruction routeInstruction;


//        System.out.println("Target: " + vertices.get(index).toString());
        path = getGraphPath(destination);
        path = removePathDuplicates(path);
//        Log.i("GEOJSONDIJKSTRA", "Path found");
        if (path != null) {
            int prevLevel = Integer.MIN_VALUE;
            LatLng prevPoint = null;
            minRouteLevel = maxRoutelevel = sourceLevel = Integer.valueOf(path.get(0).getLevel());
            for (Vertex point : path) {
                int level = Integer.valueOf(point.getLevel());
                if (level < minRouteLevel) {
                    minRouteLevel = level;
                }
                if (level > maxRoutelevel) {
                    maxRoutelevel = level;
                }
                LatLng nextPoint;
//                System.out.println("Point: " + point.getId());
                nextPoint = stringToLatLng(point.getId());
                if (nextPoint.latitude > maxRouteLat) {
                    maxRouteLat = nextPoint.latitude;
                }
                if (nextPoint.longitude > maxRouteLng) {
                    maxRouteLng = nextPoint.longitude;
                }
                if (nextPoint.latitude < minRouteLat) {
                    minRouteLat = nextPoint.latitude;
                }
                if (nextPoint.longitude < minRouteLng) {
                    minRouteLng = nextPoint.longitude;
                }
                if (prevLevel == level) {
//                    System.out.println("Next point level: " + level);

                    pathArrayList.add(nextPoint);
                    polylineOptions.add(nextPoint);
                    double tempDistance = SphericalUtil.computeDistanceBetween(prevPoint, nextPoint);
                    currentHeading = SphericalUtil.computeHeading(prevPoint, nextPoint);
                    if (currentHeading < 0) {
                        currentHeading = 360 + currentHeading;
                    }
                    if (Math.abs(currentHeading - prevHeading) > 20.0) { // If it is not the first point in the path and there is a turn
                        Log.i("Instuctions", "Current distance = " + currentDistance);
                        if (currentDistance > 0.2) { // If there has been some distance travelled
                            instruction = "Walk straight on level " + prevLevel + " for " + ((int) (currentDistance * 10)) / 10.0 + " meters.";
                            routeInstructions.add(instruction);
                            routeInstructionPoints.add(prevInstructionPoint); // Way point is the one that started the straight path that ended at nextPoint
                            routeInstruction = new RouteInstruction(instruction, prevInstructionPoint, level);
                            instructionQueue.add(routeInstruction);
                            if (currentHeading - prevHeading < 0) { // Turned left
                                if (currentHeading - prevHeading > -180) {
                                    if (currentHeading - prevHeading < -50) {
                                        instruction = "Turn left";
                                    } else {
                                        instruction = "Turn slightly left";
                                    }
                                } else {
                                    if (360 - (prevHeading - currentHeading) > 50) {
                                        instruction = "Turn right";
                                    } else {
                                        instruction = "Turn slightly right";
                                    }
                                }
                                routeInstructions.add(instruction);
                                Log.i("TAG", "Prevpoint3:" + prevPoint);
                                routeInstructionPoints.add(prevPoint); // Add the direction for turning at the prevPoint
                                routeInstruction = new RouteInstruction(instruction, prevPoint, level);
                                instructionQueue.add(routeInstruction);

                                prevInstructionPoint = prevPoint; // New straight path started from prevPoint
                            } else if (currentHeading - prevHeading > 0) {
                                if (currentHeading - prevHeading < 180) {
                                    if (currentHeading - prevHeading > 50) {
                                        instruction = "Turn right. ";
                                    } else {
                                        instruction = "Turn slightly right. ";
                                    }
                                } else {
                                    if (360 - (currentHeading - prevHeading) > 50) {
                                        instruction = "Turn left. ";
                                    } else {
                                        instruction = "Turn slightly left. ";
                                    }
                                }
                                routeInstructions.add(instruction);
                                Log.i("TAG", "Prevpoint2:" + prevPoint);
                                routeInstructionPoints.add(prevPoint);
                                prevInstructionPoint = prevPoint;
                                routeInstruction = new RouteInstruction(instruction, prevPoint, level);
                                instructionQueue.add(routeInstruction);

                            }
                        }
                        currentDistance = tempDistance;
                    } else { // If there is no turn
                        Log.i("Instructions", "Heading difference: " + Math.abs(currentHeading - prevHeading));
                        currentDistance+=tempDistance;
                    }
                    prevHeading = currentHeading;
                } else { // Level changed
                    if (prevLevel != Integer.MIN_VALUE) {
//                            polylineOptions.add(nextPoint);

                        instruction = "Walk straight for " + ((int) (currentDistance * 10)) / 10.0 + " meters. ";
                        routeInstructions.add(instruction);
                        routeInstructionPoints.add(prevInstructionPoint); // Way point is the one that started the straight path that ended at nextPoint
                        routeInstruction = new RouteInstruction(instruction, prevInstructionPoint, level);
                        instructionQueue.add(routeInstruction);



                        instruction = "Go " + (prevLevel < level ? "up " : "down ") + String.valueOf(Math.abs(prevLevel - level)) + " levels";
                        currentDistance = SphericalUtil.computeDistanceBetween(prevPoint, nextPoint);
                        prevHeading = SphericalUtil.computeHeading(prevPoint, nextPoint);


                        routeInstructions.add(instruction);
                        Log.i("TAG", "Prevpoint1:" + prevPoint);
                        routeInstructionPoints.add(prevInstructionPoint);
                        routeInstruction = new RouteInstruction(instruction, prevInstructionPoint, level);
                        instructionQueue.add(routeInstruction);

                        prevInstructionPoint = prevPoint;

                        polylineOptionsInLevels.add(polylineOptions);
                        addPolylineToHashMap(pathHashMapForEachLevel, prevLevel, polylineOptions);

//                        if (!pathHashMapForEachLevel.containsKey(prevLevel)) {
//                            pathHashMapForEachLevel.put(level, new ArrayList<PolylineOptions>());
//                            Log.i("GeoJSONDijkstra:getPath", "Adding new level: " + prevLevel);
//                        }
//                        ArrayList<PolylineOptions> polyline = pathHashMapForEachLevel.get(prevLevel);
//                        polyline.add(polylineOptions);
//                        Log.i("GeoJSONDijkstra:getPath", "Adding to level: " + prevLevel + ", new size: " + pathHashMapForEachLevel.get(prevLevel).size());
//                        pathHashMapForEachLevel.put(level, polyline);
                    }
                    polylineOptions = new PolylineOptions().width(10).color(Color.RED).zIndex(Integer.MAX_VALUE - 1000);
                    if (prevPoint != null) {
                        pathArrayList.add(prevPoint);
                        polylineOptions.add(prevPoint);
                        addPathStairMarker(pathStairMarkers, prevPoint, prevLevel, level);


                    }
                    pathArrayList.add(nextPoint);
                    polylineOptions.add(nextPoint);
                    prevLevel = level;
                }
                if (prevPoint == null) {
                    prevInstructionPoint = nextPoint;
                }
                prevPoint = nextPoint;
            }
            polylineOptionsInLevels.add(polylineOptions);
            addPolylineToHashMap(pathHashMapForEachLevel, prevLevel, polylineOptions);
//        } else {
//            System.out.println("Path not found!");
        }
        unixTime = System.currentTimeMillis();
//        Log.i("GeoJSONDijkstra:getPath", "HandleRouteRequest, getPath, End: " + unixTime);

        Route routeForEachLevel = new Route(pathHashMapForEachLevel, maxRoutelevel, minRouteLevel, sourceLevel, pathStairMarkers);

        printInstructions(routeInstructions);
        return routeForEachLevel;
    }

    private void printInstructions(ArrayList<String> instructions) {
        for (String instruction: instructions) {
            Log.i("Instructions", instruction);
        }

    }

    private void addPathStairMarker(HashMap<Integer, ArrayList<MarkerOptions>> pathStairMarkers, LatLng point, int prevLevel, int newLevel) {
        MarkerOptions stair;
        ArrayList<MarkerOptions> oldList;
        if (prevLevel - newLevel < 0) {
            stair = new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_up)).title("Take the stairs up one level");
        } else {
            stair = new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_down)).title("Take the stairs down one level");
        }
        if (pathStairMarkers.containsKey(prevLevel)) {
            oldList = pathStairMarkers.get(prevLevel);
        } else {
            oldList = new ArrayList<>();
        }
        oldList.add(stair);
        pathStairMarkers.put(prevLevel, oldList);
    }

    private void addPolylineToHashMap(HashMap<Integer, ArrayList<PolylineOptions>> pathHashMapForEachLevel, int level,
                                      PolylineOptions polylineOptions) {
        if (!pathHashMapForEachLevel.containsKey(level)) {
            pathHashMapForEachLevel.put(level, new ArrayList<PolylineOptions>());
//            Log.i("GeoJSONDijkstra:getPath", "Adding new level: " + level);
        }
        ArrayList<PolylineOptions> polyline = pathHashMapForEachLevel.get(level);
        polyline.add(polylineOptions);
//        Log.i("GeoJSONDijkstra:getPath", "Adding to level: " + level + ", new size: " + pathHashMapForEachLevel.get(level).size());
        pathHashMapForEachLevel.put(level, polyline);
    }

    private LinkedList<Vertex> removePathDuplicates(LinkedList<Vertex> path) {
        LinkedList<Vertex> pathWithoutDuplicates = new LinkedList<>();
        HashMap<String, Integer> hashMap = new HashMap<>();
          for (Vertex point : path) {
            if (!hashMap.containsKey(point.getId())) {
                hashMap.put(point.getId(), 1);
                pathWithoutDuplicates.add(point);
            }
        }
        return pathWithoutDuplicates;
    }

    private void initRouteMinMax() {
        maxRouteLat = -100000;
        maxRouteLng = -100000;
        minRouteLat = 100000;
        minRouteLng = 100000;
    }

    public double getPathLength(LatLngWithTags destination) {
//        Log.i("getPathLength", "Get Path for dest: " + destination.toString());
        LinkedList<Vertex> path;
        double distance = 0.0;
        LatLng currentPoint = null;
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
                return -1.0;
            }
        return distance;
    }



}
