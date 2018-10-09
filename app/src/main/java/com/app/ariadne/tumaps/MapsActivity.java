package com.app.ariadne.tumaps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.ariadne.tumaps.geojson.GeoJSONDijkstra;
import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.listeners.ButtonClickListener;
import com.app.ariadne.tumaps.listeners.CircleClickListener;
import com.app.ariadne.tumaps.listeners.LocationButtonClickListener;
import com.app.ariadne.tumaps.listeners.MapClickListener;
import com.app.ariadne.tumaps.map.MapUIElementsManager;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumaps.listeners.ItemClickListener;
import com.app.ariadne.tumaps.tileProvider.CustomMapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MainActivity";
    public static boolean isFirstTime = true;
    TileOverlay tileOverlay;
    ArrayList<GeoJSONDijkstra> dijkstra;
    Handler routeHandler = new Handler();
    MapUIElementsManager mapUIElementsManager;
    ButtonClickListener buttonClickListener;
    MapClickListener mapClickListener;
    ItemClickListener itemClickListener;
    GeoJsonMap geoJsonMap;
    String previousDestination;
    int targetBuildingIndex;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    /**
     * Starting point of the program
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//
        geoJsonMap = new GeoJsonMap(mMap);
        geoJsonMap.loadIndoorTopology(this);

    }

    public void addTileProvider(final String level) {
        TileProvider tileProvider;
        //Log.i(TAG, "Add tiles, level: " + level);
        if (!level.equals("")) {
//            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(getResources().getAssets(), this);
            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(this);
            customMapTileProvider.setLevel(level);
            tileProvider = customMapTileProvider;
            if (tileOverlay != null) {
//            tileOverlay.clearTileCache();
                tileOverlay.remove();
            }
            tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                    .tileProvider(tileProvider)
                    .zIndex(100)
                    .fadeIn(false)
                    .transparency(0.0f));

        } else {
            //Log.i(TAG, "Remove tiles");
            if (tileOverlay != null) {
//            tileOverlay.clearTileCache();
                tileOverlay.remove();
            }
        }
    }

    void requestPermissionToAccessLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "No permission to access location!");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        } else {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        boolean isLocationAccepted = false;
        switch (permsRequestCode) {
            case 200:
                isLocationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                boolean cameraAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                break;

        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        buttonClickListener = new ButtonClickListener(this);
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        LatLngBounds MUNICH = new LatLngBounds(
                new LatLng(47.8173, 11.063), new LatLng(48.5361, 12.062));
        mMap.setLatLngBoundsForCameraTarget(MUNICH);
        mMap.setOnCircleClickListener(new CircleClickListener());
        mMap.setIndoorEnabled(false);

//        LatLng munich = new LatLng(48.137, 11.574);
        LatLng garching = new LatLng(48.262534, 11.667992);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(munich));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(garching, 14, mMap.getCameraPosition().tilt,
                mMap.getCameraPosition().bearing)));

        mMap.setOnCameraIdleListener(buttonClickListener);
        dijkstra = new ArrayList<>();
        for (int i = 0; i < GeoJsonMap.routablePathForEachBuilding.size(); i++) {
            dijkstra.add(new GeoJSONDijkstra(GeoJsonMap.routablePathForEachBuilding.get(i)));
        }
        ArrayList<String> targetPointsIds = GeoJsonMap.targetPointsIds;
        targetPointsIds.remove("Entrance of building");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, targetPointsIds);

        mMap.setPadding(0,150,0,0);

        mMap.setOnMyLocationButtonClickListener(new LocationButtonClickListener(this, mMap));
        mapUIElementsManager = new MapUIElementsManager(this, buttonClickListener, mMap, geoJsonMap);
        buttonClickListener.setMapUIElementsManager(mapUIElementsManager);
        mapClickListener = new MapClickListener(this, buttonClickListener, mapUIElementsManager);
        mMap.setOnMapClickListener(mapClickListener);
        itemClickListener = new ItemClickListener(this, mapUIElementsManager, buttonClickListener);
        requestPermissionToAccessLocation();

    }

    public void cancelTarget(View view) {
//        autoCompleteDestination.setText("");
        mapUIElementsManager.cancelTarget();
        buttonClickListener.removeButtons();
        mapUIElementsManager.removeDestinationDescription();
    }

    public void onFindOriginDestinationClick() {
        startActivityForResults(FindOriginDestinationActivity.class);
    }

    public void onFindDestinationClick(View view) {
        cancelTarget(view);
        startActivityForResults(FindDestinationActivity.class);
    }

    public void startActivityForResults(Class activityClass) {
        Intent getNameScreenIntent = new Intent(this, activityClass);
        // We ask for the Activity to start and don't expect a result to be sent back
        // startActivity(getNameScreenIntent);
        // We use startActivityForResult when we expect a result to be sent back
        final int result = 1;

        // To send data use putExtra with a String name followed by its value
        getNameScreenIntent.putExtra("callingActivity", "MapsActivity");
        previousDestination = "";

        if (mapUIElementsManager.target != null) {
            previousDestination = mapUIElementsManager.target.getId();
            getNameScreenIntent.putExtra("destination", mapUIElementsManager.target.getId());
        }
        if (mapUIElementsManager.source != null && mapUIElementsManager.target != null) {
            Log.i(TAG, "Source building: " + getBuildingIdFromRoomName(mapUIElementsManager.source.getId()) + ", Destination building: " +
                    getBuildingIdFromRoomName(mapUIElementsManager.target.getId()));
        }
        if (mapUIElementsManager.source != null && mapUIElementsManager.target != null &&
                getBuildingIdFromRoomName(mapUIElementsManager.source.getId()).equals(getBuildingIdFromRoomName(mapUIElementsManager.target.getId()))) {
            getNameScreenIntent.putExtra("source", mapUIElementsManager.source.getId());
        }
        startActivityForResult(getNameScreenIntent, result);

    }


    private static boolean isGarchingMIId(String id) {
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            //Log.i(TAG, "MORE tokens");
        }
        return index == 3;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private static boolean isMWId(String name) {
        String id = getRoomIdFromBuildingName(name);
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            //Log.i(TAG, "MORE tokens");
        }
        return index == 1 && isInteger(id);
    }

    public static int findLevelFromId(String id) {
        int level = 0;
        if (isGarchingMIId(id)) {
            level = Integer.valueOf(id.substring(1,2));
            //Log.i(TAG, "id: " + id + " is from Garching MI, level: " + level);
        } else if (isMWId(id)) {
            level = Integer.valueOf(id.substring(0,1));
        } else {
            //Log.i(TAG, "id: " + id + " is not from Garching MI");
        }
        return level;
    }

    public static String getRoomIdFromBuildingName(String name) {
        StringTokenizer multiTokenizer = new StringTokenizer(name, ",");
        int index = 0;
        ArrayList<String> nameParts = new ArrayList<>();
        while (multiTokenizer.hasMoreTokens()) {
            nameParts.add(multiTokenizer.nextToken());

            index++;
            //Log.i(TAG, "MORE tokens");
        }
        return nameParts.get(0);
    }

    public String getBuildingIdFromRoomName(String name) {
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
            String id = GeoJsonMap.buildingNameToId.get(buildingName);
            //Log.i(TAG, "building id: " + id);
            return id;
        }
        return buildingName;
    }

    private void handleRouteRequest(final String sourceName, final String targetName) {
//        long unixTime = System.currentTimeMillis();
        //Log.i("getPath", "HandleRouteRequest, start: " + unixTime);

        if (isRoutableSourceDestination()) {
            //Log.i(TAG, "Source: " + source);
            setSourceAndDestination(sourceName, targetName);
            //Log.i(TAG, "Destination level = " + mapUIElementsManager.destinationLevel);
            if (mapUIElementsManager.source != null && mapUIElementsManager.target != null) {
                mapUIElementsManager.routePolylineOptionsInLevels = null;
                mapUIElementsManager.route = dijkstra.get(targetBuildingIndex).getPath(mapUIElementsManager.source);
                LatLng minPoint = new LatLng(dijkstra.get(targetBuildingIndex).minRouteLat, dijkstra.get(targetBuildingIndex).minRouteLng);
                LatLng maxPoint = new LatLng(dijkstra.get(targetBuildingIndex).maxRouteLat, dijkstra.get(targetBuildingIndex).maxRouteLng);
                mapUIElementsManager.handleRoutePolyline(routeHandler, mapUIElementsManager.target, minPoint, maxPoint);
            } else {
                final View view = findViewById(R.id.targetDescriptionLayout);
                routeHandler.post(new Runnable() {
                    public void run() {
                        cancelTarget(view);
                    }
                });
            }
        }
    }

    private void setSourceAndDestination(final String sourceName, final String targetName) {
        if (sourceName.equals("Entrance of building")){
//                mapUIElementsManager.target = findDestinationFromId(targetName);
            mapUIElementsManager.source = geoJsonMap.findEntranceForDestination(mapUIElementsManager.target).getEntranceLatLngWithTags();
        } else {
            mapUIElementsManager.source = GeoJsonMap.findDestinationFromId(sourceName);
        }
        if (targetName.equals("Entrance of building")) {
            mapUIElementsManager.target = geoJsonMap.findEntranceForDestination(mapUIElementsManager.source).getEntranceLatLngWithTags();
        } else {
            mapUIElementsManager.target = GeoJsonMap.findDestinationFromId(targetName);
        }
        mapUIElementsManager.sourceLevel = findLevelFromId(sourceName);
        //Log.i(TAG, "Source level = " + mapUIElementsManager.sourceLevel);
        mapUIElementsManager.destinationLevel = findLevelFromId(targetName);

    }

    private void startAsyncTaskToHandleRouteRequest(final String targetBuildingId) {
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (!previousDestination.equals(mapUIElementsManager.targetName)) {
                    previousDestination = mapUIElementsManager.targetName;
                    targetBuildingIndex = GeoJsonMap.buildingIndexes.get(targetBuildingId);
                    dijkstra.get(targetBuildingIndex).startDijkstra(GeoJsonMap.findDestinationFromId(mapUIElementsManager.targetName));
                }
                handleRouteRequest(mapUIElementsManager.sourceName, mapUIElementsManager.targetName);
            }
        });
    }

    private void handleNewDestinationFromFindDestinationActivity(Intent data) {
        String destination = data.getStringExtra("Destination");
        if (destination != null && !destination.equals("")) {
            //Log.i(TAG, "Destination: " + destination);
            mapUIElementsManager.setDestinationOnMap(destination);
            startAsyncDijkstraForSelectedTarget();
        }
    }

    private void startAsyncDijkstraForSelectedTarget() {
        if (!previousDestination.equals(mapUIElementsManager.target.getId())) { // If destination changed start new dijkstra
            previousDestination = mapUIElementsManager.target.getId();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    targetBuildingIndex = GeoJsonMap.buildingIndexes.get(mapUIElementsManager.target.getBuildingId());
                    dijkstra.get(targetBuildingIndex).startDijkstra(mapUIElementsManager.target);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i(TAG, "Return from Activity, resultCode: " + resultCode);
        if (resultCode == RESULT_OK) {
            // Get the name of the previous Activity
            String activityWeReturnedFrom = data.getStringExtra("Activity Name");
            if (activityWeReturnedFrom.equals("FindDestinationActivity")) {
                handleNewDestinationFromFindDestinationActivity(data);
            } else {
                handleSourceDestinationFromActivity(data);
            }
        }
    }

    private void handleSourceDestinationFromActivity(Intent data) {
        mapUIElementsManager.sourceName = data.getStringExtra("Starting point");
        mapUIElementsManager.targetName = data.getStringExtra("Destination");
        //Log.i(TAG, "Source: " + mapUIElementsManager.sourceName + ", Destination: " + mapUIElementsManager.targetName);
        buttonClickListener.showDirectionsButtons();
        if (isRoutableSourceDestination()) {
            final String targetBuildingId = getBuildingIdFromRoomName(mapUIElementsManager.targetName);
            final String sourceBuildingId = getBuildingIdFromRoomName(mapUIElementsManager.sourceName);

            if (targetBuildingId.equals(sourceBuildingId) || mapUIElementsManager.sourceName.equals("Entrance of building")) {
                startAsyncTaskToHandleRouteRequest(targetBuildingId);
            } else {
                Toast.makeText(this, "Inter-building routing is not supported yet", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isRoutableSourceDestination() {
        return !(mapUIElementsManager.sourceName.equals("My Location") || mapUIElementsManager.sourceName.equals(mapUIElementsManager.targetName)
                || mapUIElementsManager.sourceName.contains("ntrance")
                && mapUIElementsManager.targetName.contains("ntrance") || mapUIElementsManager.targetName.contains("ntrance")
                && mapUIElementsManager.sourceName.contains("ntrance"));
    }

    /**
     * This is used for loading the settings. A new activity should be created and this should be placed in the
     * onCreate() method.
     * The settings are stored inside the res/xml/preferences.xml file
     * @param view nothing necessary at all, it's just there because the onClick() callback for the relevant imagebutton
     *             (imageButton2) is specified in the activity_maps.xml - and therefore a View parameter is passed by default
     */
    public void startPreferencesFragment(View view) {
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
