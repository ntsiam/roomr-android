package com.app.ariadne.tumrfmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.app.ariadne.tumrfmap.geojson.BoundingBox;
import com.app.ariadne.tumrfmap.geojson.GeoJSONDijkstra;
import com.app.ariadne.tumrfmap.geojson.GeoJsonMap;
import com.app.ariadne.tumrfmap.geojson.IndoorBuildingBoundsAndFloors;
import com.app.ariadne.tumrfmap.geojson.LatLngWithTags;
import com.app.ariadne.tumrfmap.listeners.ButtonClickListener;
import com.app.ariadne.tumrfmap.listeners.CircleClickListener;
import com.app.ariadne.tumrfmap.listeners.ItemClickListener;
import com.app.ariadne.tumrfmap.listeners.LocationButtonClickListener;
import com.app.ariadne.tumrfmap.listeners.MapClickListener;
import com.app.ariadne.tumrfmap.listeners.MapLocationListener;
import com.app.ariadne.tumrfmap.map.MapUIElementsManager;
import com.app.ariadne.tumrfmap.tileProvider.CustomMapTileProvider;
import com.app.ariadne.tumrfmap.util.SpaceTokenizer;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.SphericalUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.app.ariadne.tumrfmap.geojson.GeoJsonHelper.ListToArrayList;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.findDestinationFromId;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.routablePath;
import static com.app.ariadne.tumrfmap.map.MapUIElementsManager.MAX_FLOOR;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MultiAutoCompleteTextView roomDestination;
    private static final String TAG = "MainActivity";
    public static boolean isFirstTime = true;
    TileOverlay tileOverlay;
    MultiAutoCompleteTextView autoCompleteDestination;
    GeoJSONDijkstra dijkstra;
    Handler routeHandler = new Handler();
    MapUIElementsManager mapUIElementsManager;
    ButtonClickListener buttonClickListener;
    MapClickListener mapClickListener;
    ItemClickListener itemClickListener;

    public final String TILESERVER_IP = "ec2-18-191-35-229.us-east-2.compute.amazonaws.com";
    GeoJsonMap geoJsonMap;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
//        roomDestination = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
//        autoCompleteDestination = findViewById(R.id.multiAutoCompleteTextView);
//        autoCompleteDestination.setTokenizer(new SpaceTokenizer());
        buttonClickListener = new ButtonClickListener(this);

    }

    public void addTileProvider(final String level) {
        TileProvider tileProvider;
        Log.i(TAG, "Add tiles, level: " + level);
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
            Log.i(TAG, "Remove tiles");
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
            System.out.println("No permission to access location!");
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        LatLngBounds MUNICH = new LatLngBounds(
                new LatLng(47.8173, 11.063), new LatLng(48.5361, 12.062));
        mMap.setLatLngBoundsForCameraTarget(MUNICH);
        mMap.setOnCircleClickListener(new CircleClickListener());

        // Add a marker in Sydney and move the camera
        LatLng munich = new LatLng(48.137, 11.574);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(munich));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(munich, 14, mMap.getCameraPosition().tilt,
                mMap.getCameraPosition().bearing)));

        mMap.setOnCameraIdleListener(buttonClickListener);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(MUNICH, 0));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionToAccessLocation();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "No permissions!", Toast.LENGTH_SHORT).show();
            return;
        }

        geoJsonMap = new GeoJsonMap(mMap);
        geoJsonMap.loadIndoorTopology(this);
        dijkstra = new GeoJSONDijkstra(routablePath);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, geoJsonMap.targetPointsIds);
//        autoCompleteDestination.setAdapter(adapter);
//        autoCompleteDestination.setOnItemClickListener(this);

        mMap.setMyLocationEnabled(true);
        mMap.setPadding(0,150,0,0);
        Toast.makeText(this, "Camera position: " + mMap.getCameraPosition(), Toast.LENGTH_SHORT).show();

        mMap.setOnMyLocationButtonClickListener(new LocationButtonClickListener(this, mMap));
        mapUIElementsManager = new MapUIElementsManager(this, buttonClickListener, mMap);
        buttonClickListener.setMapUIElementsManager(mapUIElementsManager);
        mapClickListener = new MapClickListener(this, buttonClickListener, mapUIElementsManager);
        mMap.setOnMapClickListener(mapClickListener);
        itemClickListener = new ItemClickListener(this, mapUIElementsManager, buttonClickListener);
    }

    public void cancelTarget(View view) {
//        autoCompleteDestination.setText("");
        mapUIElementsManager.cancelTarget();
        buttonClickListener.removeButtons();
        mapUIElementsManager.removeDestinationDescription();
    }

    public void onFindOriginDestinationClick() {
        Intent getNameScreenIntent = new Intent(this, FindOriginDestinationActivity.class);
        // We ask for the Activity to start and don't expect a result to be sent back
        // startActivity(getNameScreenIntent);
        // We use startActivityForResult when we expect a result to be sent back
        final int result = 1;

        // To send data use putExtra with a String name followed by its value
        getNameScreenIntent.putExtra("callingActivity", "MapsActivity");
        getNameScreenIntent.putExtra("destination", mapUIElementsManager.target.getId());
        startActivityForResult(getNameScreenIntent, result);
    }

    public void onFindDestinationClick(View view) {
        Intent findDestinationIntent = new Intent(this, FindDestinationActivity.class);
        // We ask for the Activity to start and don't expect a result to be sent back
        // startActivity(getNameScreenIntent);
        // We use startActivityForResult when we expect a result to be sent back
        final int result = 1;
        // To send data use putExtra with a String name followed by its value
        findDestinationIntent.putExtra("callingActivity", "MapsActivity");
//        getDestinationScreenIntent.putExtra("destination", target.getId());
        startActivityForResult(findDestinationIntent, result);
    }


    private static boolean isGarchingMIId(String id) {
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            Log.i(TAG, "MORE tokens");
        }
        return index == 3;
    }

    public static int findLevelFromId(String id) {
        int level = 0;
        if (isGarchingMIId(id)) {
            level = Integer.valueOf(id.substring(1,2));
            Log.i(TAG, "id: " + id + " is from Garching MI, level: " + level);
        } else {
            Log.i(TAG, "id: " + id + " is not from Garching MI");
        }
        return level;
    }

    private void handleRouteRequest(final String sourceName, final String targetName) {
        if (!sourceName.equals("Building entrance") & !sourceName.equals("My Location")) {
            mapUIElementsManager.source = findDestinationFromId(sourceName);
            mapUIElementsManager.sourceLevel = findLevelFromId(sourceName);
            Log.i(TAG, "Source level = " + mapUIElementsManager.sourceLevel);
            mapUIElementsManager.destinationLevel = findLevelFromId(targetName);
            Log.i(TAG, "Destination level = " + mapUIElementsManager.destinationLevel);
            if (mapUIElementsManager.source != null) {
                Log.i(TAG, "Source found!");
                mapUIElementsManager.target = findDestinationFromId(targetName);
                if (mapUIElementsManager.target != null) {
                    Log.i(TAG, "Destination found!");
                    dijkstra.startDijkstra(mapUIElementsManager.source);
                    mapUIElementsManager.routePolylineOptionsInLevels = null;
//                    routePolylineOptionsGray = new PolylineOptions().width(15).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 20);
                    mapUIElementsManager.routePolylineOptionsInLevels = dijkstra.getPath(mapUIElementsManager.target);
                    mapUIElementsManager.handleRoutePolyline(routeHandler);
                } else {
                    Looper.prepare();
                    Toast.makeText(this, "Destination could not be found", Toast.LENGTH_SHORT).show();
                    cancelTarget(findViewById(R.id.targetDescriptionLayout));
                }
            } else {
                Looper.prepare();
                Toast.makeText(this, "Starting point could not be found", Toast.LENGTH_SHORT).show();
                cancelTarget(findViewById(R.id.targetDescriptionLayout));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Return from Activity, resultCode: " + resultCode);

        if (resultCode == RESULT_OK) {
            // Get the users name from the previous Activity
            String activityWeReturnedFrom = data.getStringExtra("Activity Name");
            if (activityWeReturnedFrom.equals("FindDestinationActivity")) {
                Log.i(TAG, "Returned from Destination activity");
                String destination = data.getStringExtra("Destination");
                if (destination != null) {
                    Log.i(TAG, "Destination: " + destination);
                    mapUIElementsManager.setDestinationOnMap(destination);
                }
            } else {
                mapUIElementsManager.sourceName = data.getStringExtra("Starting point");
                mapUIElementsManager.targetName = data.getStringExtra("Destination");
                Log.i(TAG, "Source: " + mapUIElementsManager.sourceName + ", Destination: " + mapUIElementsManager.targetName);
                buttonClickListener.showDirectionsButtons();
                ProgressBar progressBar = findViewById(R.id.progressBar2);
                progressBar.setVisibility(ProgressBar.VISIBLE);
                AsyncTask.execute(new Runnable() {
                    @Override
                    public void run() {
                        handleRouteRequest(mapUIElementsManager.sourceName, mapUIElementsManager.targetName);
                        //TODO your background code
                    }
                });

            }
        }
    }
}
