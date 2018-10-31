package com.app.ariadne.tumaps.geojson;

import android.content.Context;
import android.graphics.Color;

import com.app.ariadne.tumaps.models.Entrance;
import com.app.ariadne.tumaps.models.MapSources;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.collect.Lists;
import com.google.maps.android.data.Layer;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

//import static com.roomr.ariadne.tumnavigator.MapLocationListener.gpsLocation;

public class GeoJsonMap {
    ArrayList<GeoJsonLayer> indoorTopologyLayer;
    private GoogleMap mMap;
    private final String TAG = "GeoJsonMap";

    //Indoor Layout variables
    public static ArrayList<ArrayList<ArrayList<LatLngWithTags>>> corridorsInLevels;
    public static ArrayList<ArrayList<ArrayList<LatLngWithTags>>> stepsInLevels;
    ArrayList<ArrayList<LatLng>> corridors;
    public static ArrayList<ArrayList<LatLngWithTags>> routablePath;
    ArrayList<ArrayList<LatLng>> steps;
    public static ArrayList<LatLngWithTags> targetPointsTagged; //This stores the possible targets for routing, including their ids and their Latlng coordinates.
    public static ArrayList<String> targetPointsIds; //This stores the destination ids for routing that are displayed in the dropdown list on the screen
    public static ArrayList<String> sourcePointsIds; //This stores the source ids for routing. They may be redundant.
    private MapSources mapSources;
    HashMap<String, Entrance> entranceHashMap;
    public static ArrayList<ArrayList<LatLngWithTags>> targetPointsTaggedForEachBuilding;
    public static ArrayList<ArrayList<String>> targetPointsIdsForEachBuilding;
    public static ArrayList<ArrayList<String>> sourcePointsIdsForEachBuilding;
    public static ArrayList<ArrayList<ArrayList<LatLngWithTags>>> routablePathForEachBuilding;
    public static HashMap<String, Integer> buildingIndexes;
    public static HashMap<String, String> buildingIdToName;
    public static HashMap<String, String> buildingNameToId;




    public GeoJsonMap(GoogleMap mMap) {
        this.mMap = mMap;
        entranceHashMap = new HashMap<>();
        buildingIdToName = new HashMap<>();
        buildingNameToId = new HashMap<>();
        buildingIdToName.put("mi", "Mathematics Informatics");
        buildingIdToName.put("mw", "Mechanical Engineering");
        buildingIdToName.put("mc", "Main Campus");
        buildingNameToId.put("Mathematics Informatics", "mi");
        buildingNameToId.put("Mechanical Engineering", "mw");
        buildingNameToId.put("Main Campus", "mc");
        mapSources = new MapSources();
    }

    public void setMapSources(MapSources mapSources) {
        this.mapSources = mapSources;
    }

    /**
     * Loads the indoor paths that are going to be used for routing by
     * parsing the geojson files containing the paths and storing them as ArrayLists of LatLngWithTags objects.
     * @param appContext
     */
    public void loadIndoorTopology(Context appContext) {
        resetGlobalPathVariables();
        try {
//            Log.i(TAG, "IndoorTopology: Starting time: " + System.currentTimeMillis());
            addAllBuildingTopologies(appContext);
            //Log.i(TAG, "IndoorTopology: Ending time: " + System.currentTimeMillis());
        } catch (JSONException | IOException e) {
//            Log.i(TAG, "Error while loading Geojson data!!");
            e.printStackTrace();
        }
    }

    private void addAllBuildingTopologies(Context appContext) throws IOException, JSONException {
        indoorTopologyLayer = new ArrayList<>();
        for (int mapSource: mapSources.getMapSources()) {
            GeoJsonLayer newLayer = new GeoJsonLayer(mMap, mapSource, appContext);
            indoorTopologyLayer.add(newLayer);
            addNewBuildingTopology(newLayer);
        }

    }

    private void addNewBuildingTopology(GeoJsonLayer buildingLayer) {
        processIndoorPathFeatures(buildingLayer);
        targetPointsTaggedForEachBuilding.add(targetPointsTagged);
        targetPointsIdsForEachBuilding.add(targetPointsIds);
        sourcePointsIdsForEachBuilding.add(sourcePointsIds);
        routablePathForEachBuilding.add(routablePath);
        resetTempVariables();
    }

    private void resetGlobalPathVariables() {
        resetTempVariables();
        corridorsInLevels = new ArrayList<>();
        corridors = new ArrayList<>();
        steps = new ArrayList<>();
        stepsInLevels = new ArrayList<>();
        targetPointsTagged = new ArrayList<>();
        targetPointsIds = new ArrayList<>();
        sourcePointsIds = new ArrayList<>();
        targetPointsTaggedForEachBuilding = new ArrayList<>();
        targetPointsIdsForEachBuilding = new ArrayList<>();
        sourcePointsIdsForEachBuilding = new ArrayList<>();
        routablePathForEachBuilding = new ArrayList<>();
        buildingIndexes = new HashMap<>();
    }

    private void resetTempVariables() {
        routablePath = new ArrayList<>();
    }

    /**
     * This function parses each feature in the geojson and stores it to the corresponding ArrayList object:
     * CorridorsInLevels for a corridor
     * StepsInLevels for a stair
     * TargetPoints Tagged for a point that can be the source or destination of a routing request
     * @param indoorPathLayer
     */
    private void processIndoorPathFeatures(GeoJsonLayer indoorPathLayer) {
        Iterable<GeoJsonFeature> features = indoorPathLayer.getFeatures();
        ArrayList<GeoJsonFeature> featureArrayList = Lists.newArrayList(features);
        for (GeoJsonFeature feature : featureArrayList) {
            processIndoorPathFeature(feature);
        }
    }

    private void processIndoorPathFeature(GeoJsonFeature feature) {
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

    private void handleTargetPoint(GeoJsonFeature feature) {
        LatLng latLng = ((LatLng) feature.getGeometry().getGeometryObject());
        ArrayList<Integer> levels = new ArrayList<>();
        String id = "";
        String ref = "";
        String address = "";
        String building = "";
        String plz = "";
        String city = "";
        boolean isEntrance = false;
        String buildingId = "";
        Iterable properties = feature.getProperties();
        for (Object prop : properties) {
            String property = prop.toString();
            if (property.contains("level")) {
                ArrayList<Integer> tempLevels = GeoJsonHelper.getLevelOfElement(property);
                for (Integer tempLevel : tempLevels) {
                    levels.add(tempLevel);
                }
            }
            if (property.contains("building_id=")) {
//                //Log.i(TAG, "building_id: " + GeoJsonHelper.getValueOfProperty(property));
                buildingId = GeoJsonHelper.getValueOfProperty(property);
                if (!buildingIndexes.containsKey(buildingId)) {
                    buildingIndexes.put(buildingId, routablePathForEachBuilding.size());
                }

            } else if (property.contains("id=")) {
                id = GeoJsonHelper.getValueOfProperty(property);
//                Log.i(TAG, "New point, id: " + id);
                if (id.equals("entrance")) {
                    isEntrance = true;
                }
            }
            if (property.contains("entrance=")) {
                isEntrance = true;
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
        }

        if (levels.size() > 0 && !id.equals("")) {
            addNewTargetPoint(buildingId, latLng, levels, id);
        }
        if (isEntrance) {
            addEntrance(buildingId, latLng, address, plz, city, building, id);
        }
    }

    private void addEntrance(String buildingId, LatLng latLng, String address, String plz, String city, String building, String id) {
        id += ", " + buildingIdToName.get(buildingId);
        LatLngWithTags taggedPoint = new LatLngWithTags(latLng, "0", id, buildingId); // Assume entrance is on ground floor
        if (!building.equals("")) {
            Entrance newEntrance = new Entrance(address, building, city, plz, taggedPoint);
            //Log.i(TAG, "Add new entrance: " + newEntrance.getAddress());
            addEntranceToEntranceHashMap(buildingId, newEntrance);
        }
    }

    public void addEntranceToEntranceHashMap(String buildingId, Entrance newEntrance) {
        entranceHashMap.put(buildingId, newEntrance);
    }

    private void addNewTargetPoint(String buildingId, LatLng latLng, ArrayList<Integer> levels, String id) {
        id += ", " + buildingIdToName.get(buildingId);
        LatLngWithTags taggedPoint = new LatLngWithTags(latLng, String.valueOf(levels.get(0)), id, buildingId);
        targetPointsTagged.add(taggedPoint);
        targetPointsIds.add(taggedPoint.getId());
        sourcePointsIds.add(taggedPoint.getId());
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
//                printRoutablePathLatLng(latLngList);
                for (ArrayList<LatLng> latLngs: latLngList) { // A polygon contains >=1 lines
                    targetLatLngList.add(latLngs);
                    addLineStringWithLevel(property, latLngs, targetLatLngListWithLevels);
                }
            }
        }
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
            if (levels.size() > 1) { // TODO: Assume even more than 2 levels - necessary?
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
//        Log.i(TAG, "Taggedpoints: " + targetPointsTagged.size());
        for (LatLngWithTags candidate : targetPointsTagged) {
//            Log.i(TAG, "Candidate: " + candidate.getId() + ", destination: " + destinationId);
            if (candidate.getId().equals(destinationId)) {
                break;
            }
            targetIndex++;
        }
        if (targetIndex < targetPointsTagged.size()) {
            //Log.i("findDestinationFromId", "Point is: " + targetPointsTagged.get(targetIndex).getId());
            return targetPointsTagged.get(targetIndex);
        } else {
            //Log.i("findDestinationFromId", "Point not found!");
            return null;
        }
    }

    public static boolean isSourceAndDestinationInTheSameBuilding(LatLngWithTags source, LatLngWithTags destination) {
        return getBuildingIdFromRoomName(source.getId()).equals(getBuildingIdFromRoomName(destination.getId()));
    }

    public static String getRoomIdFromBuildingName(String name) {
        StringTokenizer multiTokenizer = new StringTokenizer(name, ",");
        int index = 0;
        ArrayList<String> nameParts = new ArrayList<>();
        while (multiTokenizer.hasMoreTokens()) {
            nameParts.add(multiTokenizer.nextToken());
            index++;
        }
        return nameParts.get(0);
    }

    public static String getBuildingIdFromRoomName(String name) {
        StringTokenizer multiTokenizer = new StringTokenizer(name, ",");
        int index = 0;
        String buildingName = "";
        ArrayList<String> nameParts = new ArrayList<>();
        while (multiTokenizer.hasMoreTokens()) {
            nameParts.add(multiTokenizer.nextToken());
            index++;
        }
        //Log.i(TAG, "number of tokens: " + index);
        if (index > 1) {
            buildingName = nameParts.get(1).substring(1);
            //Log.i(TAG, "buildingName: " + buildingName);
            return GeoJsonMap.buildingNameToId.get(buildingName);
        }
        return buildingName;
    }



    /**
     * entranceHashMap maps the building id to the entrance details of the building. Each building is (wrongly) assumed to have one entrance though.
     * @param destination
     * @return
     */
    public Entrance findEntranceForDestination(LatLngWithTags destination) {
        return entranceHashMap.get(destination.getBuildingId());
    }
}

