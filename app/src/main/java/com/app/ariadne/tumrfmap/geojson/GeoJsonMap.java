package com.app.ariadne.tumrfmap.geojson;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.app.ariadne.tumrfmap.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

//import static com.roomr.ariadne.tumnavigator.MapLocationListener.gpsLocation;

public class GeoJsonMap {
    GeoJsonLayer indoorGeometryLayer;
    ArrayList<GeoJsonLayer> indoorTopologyLayer;
    private GoogleMap mMap;
    private ArrayList<Circle> particleMarkers;
    public static LatLng weightedAverage;
    private final String TAG = "GeoJsonMap";

    //Indoor Layout variables
    public static ArrayList<ArrayList<ArrayList<LatLngWithTags>>> corridorsInLevels;
    public static ArrayList<ArrayList<ArrayList<LatLngWithTags>>> stepsInLevels;
    ArrayList<ArrayList<LatLng>> corridors;
    public static ArrayList<ArrayList<LatLngWithTags>> routablePath;
    ArrayList<ArrayList<LatLng>> steps;
    public static ArrayList<LatLng> targetPoints;
    public static ArrayList<LatLngWithTags> targetPointsTagged;
    public static ArrayList<String> targetPointsIds;
    public static ArrayList<String> sourcePointsIds;
    public static LatLngWithTags currentLatLngWithTags;
    ArrayList<ArrayList<LatLng>> interpolatedLines;
    ArrayList<ArrayList<LatLng>> interpolatedCorridors;
    ArrayList<ArrayList<LatLng>> interpolatedStaircases;
    public static LatLngWithTags startingPoint;
    public ArrayList<String> mapLevels = new ArrayList<>();
    public ArrayList<ArrayList<GeoJsonFeature>> mapInLevels = new ArrayList<>();
    public static final int[] mapSources = {R.raw.indoor_path_ga_mi};
    HashMap<String, Entrance> entranceHashMap;



    public GeoJsonMap(GoogleMap mMap) {
        this.mMap = mMap;
        entranceHashMap = new HashMap<>();
    }

    public void loadIndoorGeometry(Context appContext, int currentLevel) {
        removeLayer(indoorGeometryLayer);
        try {
            //TODO: add polyline instead -> Maybe can click anywhere on the map and get the position
            indoorGeometryLayer = new GeoJsonLayer(mMap, findGeometryMapToLoad(currentLevel),
                    appContext);
        } catch (JSONException | IOException e) {
            System.out.println("Error while loading Geojson data!!!");
            e.printStackTrace();
            return;
        }
        removePointsFromMap(indoorGeometryLayer);
        setGeometryLayerStyle(indoorGeometryLayer, currentLevel);
//        divideMapInLevels(indoorGeometryLayer);
//        int index = currentLevel;
//        if (index < 0) {
//            index = -index;
//        }
//        GeoJsonLayer indoorLayerWithLevels = new GeoJsonLayer(mMap, new JSONObject());
//        for (GeoJsonFeature feature: mapInLevels.get(index)) {
//            indoorLayerWithLevels.addFeature(feature);
//        }
//        indoorGeometryLayer = indoorLayerWithLevels;
//        indoorLayerWithLevels.addLayerToMap();
        indoorGeometryLayer.addLayerToMap();
    }

    private void setGeometryLayerStyle(GeoJsonLayer indoorGeometryLayer, int currentLevel) {
        indoorGeometryLayer.getDefaultLineStringStyle().setColor(Color.BLACK);
        indoorGeometryLayer.getDefaultLineStringStyle().setWidth(5);
        int fillColor = findFillColorForGeometryLayer(currentLevel);
        indoorGeometryLayer.getDefaultPolygonStyle().setFillColor(fillColor);
    }

    private int findGeometryMapToLoad(int currentLevel) {
        switch (currentLevel) {
//            case 0: return R.raw.indoor_map_ga_mi_0;
//            case 1: return R.raw.indoor_map_ga_mi_1;
//            case 2: return R.raw.indoor_map_ga_mi_2;
//            case 0: return R.raw.munchner_freiheit;
//            case 1: return R.raw.munchner_freiheit;
//            case 2: return R.raw.munchner_freiheit;
//            case 3: return R.raw.munchner_freiheit;
//            case 3: return R.raw.indoor_map_ga_mi_3;
            default: return R.raw.indoor_path_ga_mi;
        }
    }

    private void findMapSourceBasedOnLocation() {
//        if (gpsLocation != null) {
//
//        }
    }

    private int findFillColorForGeometryLayer(int currentLevel) {
        switch (currentLevel) {
//            case 1: return Color.LTGRAY;
//            case 0: return Color.YELLOW;
//            case 2: return Color.GREEN;
//            case 3: return Color.CYAN;
            case 1: return Color.LTGRAY;
            case 0: return Color.LTGRAY;
            case 2: return Color.LTGRAY;
            case 3: return Color.LTGRAY;
            default: return Color.LTGRAY;
        }
    }

    private void removeLayer(GeoJsonLayer geoJsonLayer) {
        if (geoJsonLayer != null) {
            geoJsonLayer.removeLayerFromMap();
            geoJsonLayer = null;
        }
    }

    private void removePointsFromMap(GeoJsonLayer indoorMapLayer) {
        Iterable<GeoJsonFeature> features = indoorMapLayer.getFeatures();
        ArrayList<GeoJsonFeature> featureArrayList = Lists.newArrayList(features);
        for (GeoJsonFeature feature: featureArrayList) {
            if (feature.getGeometry() != null) {
                if (feature.getGeometry().getGeometryType() != null && feature.getGeometry().getGeometryType().equals("Point")) {
                    indoorMapLayer.removeFeature(feature);
                }
            }
        }
    }

    private void divideMapInLevels(GeoJsonLayer indoorMapLayer) {
        mapLevels = new ArrayList<>();
        mapInLevels = new ArrayList<>();
        Iterable<GeoJsonFeature> features = indoorMapLayer.getFeatures();
        ArrayList<GeoJsonFeature> featureArrayList = Lists.newArrayList(features);
        for (GeoJsonFeature feature: featureArrayList) {
            if (feature.getGeometry() != null) {
//                if (feature.getGeometry().getGeometryType() != null && feature.getGeometry().getGeometryType().equals("Point")) {
                Iterable props = feature.getProperties();
                for (Object prop : props) {
                    String property = prop.toString();
//            System.out.println(property);
                    if (property.contains("level")) {
                        ArrayList<Integer> tempLevels = GeoJsonHelper.getLevelOfElement(property);

                        if (tempLevels.size() > 0) {
                            if (tempLevels.get(0) < 0 || tempLevels.get(tempLevels.size() - 1) < 0) {
                                for (int i = 0; i < tempLevels.size(); i++) {
                                    if (tempLevels.get(i) < 0) {
                                        tempLevels.set(i, -tempLevels.get(i));
                                    }
                                }
                            }
                            while (mapInLevels.size() <= tempLevels.get(0) || mapInLevels.size() <= tempLevels.get(tempLevels.size() - 1)) {
                                mapInLevels.add(new ArrayList<GeoJsonFeature>());
                            }
                        }

                        for (int i = 0; i < tempLevels.size(); i++) {
                            mapInLevels.get(i).add(feature);
                        }
                    }
                }
            }
        }
    }


    public void addIndoorTopologyToMap(Layer.OnFeatureClickListener listener) {
        for (GeoJsonLayer indoorLayer: indoorTopologyLayer) {
            indoorLayer.getDefaultLineStringStyle().setVisible(false);
            indoorLayer.getDefaultPointStyle().setVisible(false);
            indoorLayer.setOnFeatureClickListener(listener);
            indoorLayer.addLayerToMap();
        }
    }

    public void loadIndoorTopology(Context appContext) {
        resetGlobalPathVariables();
//        sourcePointsIds.add("My Location");
//        sourcePointsIds.add("Entrance of building");
        indoorTopologyLayer = new ArrayList<>();
        ArrayList<JSONObject> geojsonMaps = new ArrayList<>();
        try {
            for (int mapSource: mapSources) {
                indoorTopologyLayer.add(new GeoJsonLayer(mMap, mapSource,
                        appContext));
//            indoorTopologyLayer = new GeoJsonLayer(mMap, R.raw.munchner_freiheit,
//                    appContext);
                Log.d(TAG, "indoorTopology: " + indoorTopologyLayer);
            }
        } catch (JSONException | IOException e) {
            System.out.println("Error while loading Geojson data!!");
            e.printStackTrace();
            return;
        }
        for (GeoJsonLayer indoorLayer: indoorTopologyLayer) {
            processIndoorPathFeatures(indoorLayer);
        }

        interpolatedCorridors = GeoJsonHelper.interpolateLinesLatLng(corridors);
//            interpolatedLines = interpolateLines(latLngArrayList);
        interpolatedStaircases = GeoJsonHelper.interpolateLinesLatLng(steps);
        interpolatedLines = new ArrayList<>();
        for (ArrayList<LatLng> line : interpolatedStaircases) {
            interpolatedLines.add(line);
        }
        for (ArrayList<LatLng> line : interpolatedCorridors) {
            interpolatedLines.add(line);
        }
    }

    private void resetGlobalPathVariables() {
        targetPoints = new ArrayList<>();
        targetPointsTagged = new ArrayList<>();
        targetPointsIds = new ArrayList<>();
        sourcePointsIds = new ArrayList<>();
        corridorsInLevels = new ArrayList<>();
        corridors = new ArrayList<>();
        steps = new ArrayList<>();
        stepsInLevels = new ArrayList<>();
        routablePath = new ArrayList<>();
    }

    private void processIndoorPathFeatures(GeoJsonLayer indoorPathLayer) {
        System.out.println("process!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Iterable<GeoJsonFeature> features = indoorPathLayer.getFeatures();
        ArrayList<GeoJsonFeature> featureArrayList = Lists.newArrayList(features);
        for (GeoJsonFeature feature : featureArrayList) {
            if (feature.getGeometry() != null) {
                if (feature.getGeometry().getGeometryType() != null && feature.getGeometry().getGeometryType().equals("LineString")) {
                    String elementType = GeoJsonHelper.findElementType(feature);
                    if (elementType.equals("corridor") || elementType.equals("footway")) {
                        handleLineString(feature, corridors, corridorsInLevels);
                    } else if (elementType.equals("steps")) {
                        handleLineString(feature, steps, stepsInLevels);
                    }
                } else if (feature.getGeometry().getGeometryType() != null && feature.getGeometry().getGeometryType().equals("Point")) {
                    handleTargetPoint(feature);
                } else if (feature.getGeometry().getGeometryType() != null && feature.getGeometry().getGeometryType().equals("Polygon")) {
                    handlePolygon(feature, corridors, corridorsInLevels);
                }
            }
        }
//        printRoutablePath(routablePath);
        ArrayList<ArrayList<ArrayList<LatLngWithTags>>> corridorsStraightLines = new ArrayList<>();
        for (ArrayList<ArrayList<LatLngWithTags>> level: corridorsInLevels) {
            corridorsStraightLines.add(GeoJsonHelper.findStraightLines(level));
        }
        corridorsInLevels = corridorsStraightLines;
        routablePath = GeoJsonHelper.findStraightLines(routablePath);
    }

    public static void printRoutablePath(ArrayList<ArrayList<LatLngWithTags>> routablePath) {
        for (int i = 0; i < routablePath.size(); i++) {
            for (int j = 0; j < routablePath.get(i).size(); j++) {
                System.out.println("Path: " + routablePath.get(i).get(j).getLatlng() + ", level: " + routablePath.get(i).get(j).getLevel());
            }
        }
    }

    public static void printRoutablePathLatLng(ArrayList<ArrayList<LatLng>> routablePath) {
        for (int i = 0; i < routablePath.size(); i++) {
            for (int j = 0; j < routablePath.get(i).size(); j++) {
                System.out.println("Path: " + routablePath.get(i).get(j) + ", level: " + routablePath.get(i).get(j));
            }
        }
    }

    private void handleTargetPoint(GeoJsonFeature feature) {
        LatLng latLng = ((LatLng) feature.getGeometry().getGeometryObject());
        targetPoints.add(latLng);
        ArrayList<Integer> levels = new ArrayList<>();
        int level = 0;
        String id = "";
        String ref = "";
        String address = "";
        String building = "";
        String plz = "";
        String city = "";
        boolean isEntrance = false;
        String buildingId = "";
        Iterable props = feature.getProperties();
        for (Object prop : props) {
            String property = prop.toString();
            //            System.out.println(property);
            if (property.contains("level")) {
                ArrayList<Integer> tempLevels = GeoJsonHelper.getLevelOfElement(property);
                for (Integer tempLevel : tempLevels) {
                    levels.add(tempLevel);
                }
            }
            if (property.contains("building_id=")) {
                Log.i(TAG, "building_id: " + GeoJsonHelper.getValueOfProperty(property));
                buildingId = GeoJsonHelper.getValueOfProperty(property);

            } else if (property.contains("id=")) {
                id = GeoJsonHelper.getValueOfProperty(property);
                System.out.println("New point, id: " + id);
                if (id.equals("entrance")) {
                    isEntrance = true;
                }
            }
            if (property.contains("entrance=")) {
                isEntrance = true;
                Log.i(TAG, "Entrance: ");
            }
            if (property.contains("address=")) {
                address = GeoJsonHelper.getValueOfProperty(property);
            }
            if (property.contains("city=")) {
                city = GeoJsonHelper.getValueOfProperty(property);
            }
            if (property.contains("plz=")) {
                plz = GeoJsonHelper.getValueOfProperty(property);
            }
            if (property.contains("building=")) {
                building = GeoJsonHelper.getValueOfProperty(property);
            }
            if (property.contains("ref=")) {
                Log.i(TAG, "Has ref: " + GeoJsonHelper.getValueOfProperty(property));
                ref = GeoJsonHelper.getValueOfProperty(property);
            }
        }


        if (!isEntrance && levels.size() > 0 && !id.equals("") && !id.equals("entrance")) {
            LatLngWithTags taggedPoint = new LatLngWithTags(latLng, String.valueOf(levels.get(0)), id, buildingId);
            id = "";
//            if (!buildingId.equals("")) {
//                taggedPoint.setBuildingId(buildingId);
//            }
            System.out.println("TaggedPoint: " + taggedPoint);
            targetPointsTagged.add(taggedPoint);
            targetPointsIds.add(taggedPoint.getId());
            sourcePointsIds.add(taggedPoint.getId());
        } else if (isEntrance) {
            id += " " + buildingId;
            LatLngWithTags taggedPoint = new LatLngWithTags(latLng, "0", id, buildingId);
            targetPointsTagged.add(taggedPoint);
            targetPointsIds.add(taggedPoint.getId());
            sourcePointsIds.add(taggedPoint.getId());
            Log.i(TAG, "There is an entrance");
            if (!building.equals("") && !address.equals("") && !city.equals("") && !plz.equals("")) {
                Entrance newEntrance = new Entrance(address, building, city, plz, taggedPoint);
                Log.i(TAG, "Add new entrance: " + newEntrance.getAddress());
                address = "";
                building = "";
                plz = "";
                city = "";
                entranceHashMap.put(buildingId, newEntrance);
            }
//            System.out.println("No tagged points!");
        }
    }

    private void handleLineString(GeoJsonFeature feature, ArrayList<ArrayList<LatLng>> targetLatLngList, ArrayList<ArrayList<ArrayList<LatLngWithTags>>> targetLatLngListWithLevels) {
        ArrayList<LatLng> latLngs = ((ArrayList<LatLng>) feature.getGeometry().getGeometryObject());
        targetLatLngList.add(latLngs);
        Iterable props = feature.getProperties();
        for (Object prop : props) {
            String property = prop.toString();
            if (property.contains("level")) {
                addLineStringWithLevel(property, latLngs, targetLatLngListWithLevels); //TODO: Store destination level too
            }
        }
    }

    private void handlePolygon(GeoJsonFeature feature, ArrayList<ArrayList<LatLng>> targetLatLngList, ArrayList<ArrayList<ArrayList<LatLngWithTags>>> targetLatLngListWithLevels) {
        ArrayList<ArrayList<LatLng>> latLngList = ((ArrayList<ArrayList<LatLng>>) feature.getGeometry().getGeometryObject());
        Iterable props = feature.getProperties();
        for (Object prop : props) {
            String property = prop.toString();
            if (property.contains("level")) {
//                System.out.println("!!!!!!!!!Adding platform!!!!!!!");
                printRoutablePathLatLng(latLngList);
                for (ArrayList<LatLng> latLngs: latLngList) {
                    targetLatLngList.add(latLngs);
                    addLineStringWithLevel(property, latLngs, targetLatLngListWithLevels);
                }
            }
        }
//        System.out.println("Routable path: ");
//        printRoutablePath(routablePath);
    }

    private void addLineStringWithLevel(String property, ArrayList<LatLng> targetLatLngList, ArrayList<ArrayList<ArrayList<LatLngWithTags>>> targetLatLngListWithLevels) {
        ArrayList<Integer> levels;
        levels = GeoJsonHelper.getLevelOfElement(property);
        if (levels.size() > 0) {
            if (levels.get(0) < 0 || levels.get(levels.size() - 1) < 0) {
                for (int i = 0; i < levels.size(); i++) {
                    if (levels.get(i) < 0) {
                        levels.set(i, -levels.get(i));
                    }
                }
            }
            while (targetLatLngListWithLevels.size() <= levels.get(0) || targetLatLngListWithLevels.size() <= levels.get(levels.size() - 1)) {
                targetLatLngListWithLevels.add(new ArrayList<ArrayList<LatLngWithTags>>());
            }
            ArrayList<LatLngWithTags> latLngWithTags;
            if (levels.size() > 1) { // TODO: Assume even more than 2 levels
                latLngWithTags = LatLngWithTags.fromLatLngToLatLngWithTagsArrayList(targetLatLngList, String.valueOf(levels.get(0)));
                targetLatLngListWithLevels.get(levels.get(1)).add(latLngWithTags);
                ArrayList<LatLngWithTags> latLngWithTags1 = LatLngWithTags.fromLatLngToLatLngWithTagsArrayList(targetLatLngList, String.valueOf(levels.get(1)));
                targetLatLngListWithLevels.get(levels.get(0)).add(latLngWithTags1);
            } else {
                latLngWithTags = LatLngWithTags.fromLatLngToLatLngWithTagsArrayList(targetLatLngList, String.valueOf(levels.get(0)));
                targetLatLngListWithLevels.get(levels.get(0)).add(latLngWithTags);
            }
            routablePath.add(latLngWithTags);
        }
    }

    public static LatLngWithTags findDestinationFromId(String destinationId) {
        int targetIndex = 0;
//        System.out.println("Taggedpoints: " + targetPointsTagged.size());
        for (LatLngWithTags candidate : targetPointsTagged) {
//            System.out.println("Candidate: " + candidate.getId() + ", destination: " + destinationId);
            if (candidate.getId().equals(destinationId)) {
                break;
            }
            targetIndex++;
        }
        if (targetIndex < targetPointsTagged.size()) {
            return targetPointsTagged.get(targetIndex);
        } else {
            return null;
        }
    }

    public Entrance findEntranceForDestination(LatLngWithTags destination) {
        return entranceHashMap.get(destination.getBuildingId());
    }


//    public void addParticlesOnMap(ArrayList<Particle> particles, ParticleDeadReckoning deadReckoning) {
////        System.out.println("Add particles on Map, size: " + particles.size());
//        double radius;
//        double cumLat = 0.0;
//        double cumLong = 0.0;
//        double cumWeight = 0.0;
//        if (particleMarkers != null) {
//            for (Circle particleMarker : particleMarkers) {
//                if (particleMarker != null) {
////                    System.out.println("Removing particle marker");
//                    particleMarker.remove();
//                    particleMarker = null;
//                }
//            }
//        }
//        particleMarkers = new ArrayList<>();
//        for (int i = 0; i < particles.size(); i++) {
//            radius = particles.get(i).getWeight();
//            cumLat += particles.get(i).getLatlng().latitude * particles.get(i).getWeight();
//            cumLong += particles.get(i).getLatlng().longitude * particles.get(i).getWeight();
//            cumWeight += particles.get(i).getWeight();
//            if (i == 0) {
//                CircleOptions circleOptions = new CircleOptions()
//                        .center(particles.get(i).getLatlng())
//                        .radius(radius) // radius in meters
//                        .fillColor(0xBB00CCFF) //this is a half transparent blue, change "88" for the transparency
//                        .strokeColor(Color.BLUE) //The stroke (border) is blue
//                        .strokeWidth(2) // The width is in pixel, so try it!
//                        .zIndex(Integer.MAX_VALUE);
//                particleMarkers.add(mMap.addCircle(circleOptions));
////                circleOptions = new CircleOptions()
////                        .center(particles.get(i).getLatlng())
////                        .radius(deadReckoning.uncertaintyRadius) // radius in meters
////                        .fillColor(0x3300CCFF)
////                        .strokeColor(Color.BLUE) //The stroke (border) is blue
////                        .strokeWidth(2) // The width is in pixel, so try it!
////                        .zIndex(Integer.MAX_VALUE - 10);
//
////            System.out.println("adding particle marker");
////                particleMarkers.add(mMap.addCircle(circleOptions));
//
//            } else {
//                CircleOptions circleOptions = new CircleOptions()
//                        .center(particles.get(i).getLatlng())
//                        .radius(radius) // radius in meters
//                        .fillColor(0x88CC00FF)
//                        .strokeColor(Color.RED) //The stroke (border) is blue
//                        .strokeWidth(2) // The width is in pixel, so try it!
//                        .zIndex(Integer.MAX_VALUE);
//
////            System.out.println("adding particle marker");
//                particleMarkers.add(mMap.addCircle(circleOptions));
//            }
//        }
//        cumLat = cumLat / (cumWeight);
//        cumLong = cumLong / (cumWeight);
//        weightedAverage = new LatLng(cumLat, cumLong);
////        System.out.println("Weighted Average: " + weightedAverage.toString());
//        CircleOptions circleOptions = new CircleOptions()
//                .center(weightedAverage)
//                .radius(1.5) // radius in meters
//                .fillColor(0x88CCFF00)
//                .strokeColor(Color.GREEN) //The stroke (border) is blue
//                .strokeWidth(2) // The width is in pixel, so try it!
//                .zIndex(Integer.MAX_VALUE);
//
//
////            System.out.println("adding particle marker");
//        particleMarkers.add(mMap.addCircle(circleOptions));
//
//        circleOptions = new CircleOptions()
//                .center(weightedAverage)
//                .radius(deadReckoning.uncertaintyRadius) // radius in meters
//                .fillColor(0x3300CCFF)
//                .strokeColor(Color.BLUE) //The stroke (border) is blue
//                .strokeWidth(2) // The width is in pixel, so try it!
//                .zIndex(Integer.MAX_VALUE - 10);
//        particleMarkers.add(mMap.addCircle(circleOptions));
//
////        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(particles.get(0).getLatlng(), 20));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(weightedAverage, 20));
//    }

}

