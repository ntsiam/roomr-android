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
import com.app.ariadne.tumrfmap.listeners.LocationButtonClickListener;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnClickListener, AdapterView.OnItemClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener {

    private GoogleMap mMap;
    private MultiAutoCompleteTextView roomDestination;
    private static final String TAG = "MainActivity";
    public static boolean isFirstTime = true;
    ToggleButton level3;
    ToggleButton level2;
    ToggleButton level1;
    ToggleButton level0;
    ToggleButton leveln1;
    ToggleButton leveln2;
    ToggleButton leveln3;
    ToggleButton leveln4;
    Button directionsButton;
    ImageButton revertButton;
    TileOverlay tileOverlay;
    MultiAutoCompleteTextView autoCompleteDestination;
    GeoJSONDijkstra dijkstra;
    Circle sourceMarker;
    Marker destinationMarker;
    Handler routeHandler = new Handler();
    ArrayList<ToggleButton> floorButtonList;
    EditText destinationEditText;
    MapUIElementsManager mapUIElementsManager;

    public final String TILESERVER_IP = "ec2-18-191-35-229.us-east-2.compute.amazonaws.com";
    GeoJsonMap geoJsonMap;

    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private static final String[] COUNTRIES = new String[]{
            "Belgium", "France", "Italy", "Itastria", "Germany", "Spain"
    };

    private static final BoundingBox MUENCHNER_FREIHEIT_BB = new BoundingBox(new LatLng(48.162697, 11.587385), new LatLng(48.160468, 11.584937));
    private static final BoundingBox HAUPTBAHNHOF_BB = new BoundingBox(new LatLng(48.143000, 11.565000), new LatLng(48.137300, 11.554600));
    private static final BoundingBox GARCHING_MI_BB = new BoundingBox(new LatLng(48.2636, 11.6705), new LatLng(48.2613, 11.6653));
    private static final IndoorBuildingBoundsAndFloors MUENCHNER_FREIHEIT = new IndoorBuildingBoundsAndFloors(MUENCHNER_FREIHEIT_BB, -2, 0);
    private static final IndoorBuildingBoundsAndFloors HAUPTBAHNHOF = new IndoorBuildingBoundsAndFloors(HAUPTBAHNHOF_BB, -4, 0);
    private static final IndoorBuildingBoundsAndFloors GARCHING_MI = new IndoorBuildingBoundsAndFloors(GARCHING_MI_BB, 0, 3);
    private static final IndoorBuildingBoundsAndFloors[] BOUNDS_FOR_INDOOR_BUTTONS = new IndoorBuildingBoundsAndFloors[] {
            MUENCHNER_FREIHEIT, HAUPTBAHNHOF, GARCHING_MI
    };

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
//        editText = findViewById(R.id.editText);
//        editText.setOnClickListener(this);
        initFloorButtonList();
        directionsButton = findViewById(R.id.directions);
        revertButton = findViewById(R.id.revert);
        directionsButton.setOnClickListener(this);
        revertButton.setOnClickListener(this);
        destinationEditText = findViewById(R.id.findDestination);

    }

    private void initFloorButtonList() {
        level3 = findViewById(R.id.button3);
        level2 = findViewById(R.id.button2);
        level1 = findViewById(R.id.button1);
        level0 = findViewById(R.id.button0);
        leveln1 = findViewById(R.id.buttonn1);
        leveln2 = findViewById(R.id.buttonn2);
        leveln3 = findViewById(R.id.buttonn3);
        leveln4 = findViewById(R.id.buttonn4);
        floorButtonList = new ArrayList<>();
        floorButtonList.add(level3);
        floorButtonList.add(level2);
        floorButtonList.add(level1);
        floorButtonList.add(level0);
        floorButtonList.add(leveln1);
        floorButtonList.add(leveln2);
        floorButtonList.add(leveln3);
        floorButtonList.add(leveln4);
        for (ToggleButton button: floorButtonList) {
            button.setOnClickListener(this);
        }
    }

    void addTileProvider(final String level) {
        TileProvider tileProvider;
        Log.i(TAG, "Add tiles, level: " + level);
        if (!level.equals("")) {
//            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(getResources().getAssets(), this);
            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(this);
//            String usedLevel = "0";
//            if (!level.equals("")) {
              String usedLevel = level;
//            }
            customMapTileProvider.setLevel(usedLevel);
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("No permission to access location!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
        mMap.setOnMapClickListener(this);
        mMap.setLatLngBoundsForCameraTarget(MUNICH);
        mMap.setOnCircleClickListener(this);

        // Add a marker in Sydney and move the camera
        LatLng munich = new LatLng(48.137, 11.574);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(munich));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(munich, 14, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing)));

        mMap.setOnCameraIdleListener(this);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(MUNICH, 0));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        mapUIElementsManager = new MapUIElementsManager(this, floorButtonList, mMap);

    }

    private void removeDestinationDescription() {
        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.GONE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText("");
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("");
    }


    public void cancelTarget(View view) {
//        autoCompleteDestination.setText("");
        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.INVISIBLE);
        mapUIElementsManager.target = null;
        if (destinationMarker!= null) {
            destinationMarker.remove();
        }
        destinationMarker = null;
        directionsButton.setVisibility(Button.GONE);
        revertButton.setVisibility(ImageButton.GONE);
        destinationEditText.setText("");
        removeDestinationDescription();

        mapUIElementsManager.removeSourceCircle();
        if (mapUIElementsManager.routeLines != null) {
            mapUIElementsManager.removeRouteLine();
        }
        mapUIElementsManager.routePolylineOptionsInLevels = null;
        mapUIElementsManager.routePolylineOptions = null;

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


    private boolean isGarchingMIId(String id) {
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            Log.i(TAG, "MORE tokens");
        }
        return index == 3;
    }

    private int findLevelFromId(String id) {
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
                    if (mapUIElementsManager.routePolylineOptionsInLevels != null && mapUIElementsManager.routePolylineOptionsInLevels.size() > 0) {
                        mapUIElementsManager.routePolylineOptions = mapUIElementsManager.routePolylineOptionsInLevels.get(0);
                        routeHandler.post(new Runnable() {
                            public void run() {
                                ArrayList<ArrayList<LatLng>> route = new ArrayList<>();
                                if (mapUIElementsManager.routePolylineOptions != null && mapUIElementsManager.routePolylineOptions.getPoints().size() > 0) {
                                    route.add(ListToArrayList(mapUIElementsManager.routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
                                    moveCameraToStartingPosition();
//                                if (routeLines != null) {
//                                    routeLines.remove();
//                                }
                                    mapUIElementsManager.removeRouteLine();
                                    mapUIElementsManager.routeLines = new ArrayList<>();
                                    mapUIElementsManager.routeLines.add(mMap.addPolyline(mapUIElementsManager.routePolylineOptions));
                                }
//                    System.out.println("Number of points: " + route.get(0).size());
                                if (destinationMarker != null) {
                                    destinationMarker.remove();
                                    destinationMarker = null;
                                }
                                if (sourceMarker != null) {
                                    sourceMarker.remove();
                                    sourceMarker = null;
                                }
                                mapUIElementsManager.addSourceCircle();
                                setFloorAsChecked(mapUIElementsManager.sourceLevel);

                                ProgressBar progressBar = findViewById(R.id.progressBar2);
                                progressBar.setVisibility(ProgressBar.GONE);

                                //back on UI thread...
                            }
                        });
                    }
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

    public void moveCameraToStartingPosition() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mapUIElementsManager.source.getLatlng(), 18));
    }


    public void setFloorAsChecked(int level) {
        int indexOfLevelInButtonList = MAX_FLOOR - level;
        floorButtonList.get(indexOfLevelInButtonList).setChecked(true);
        addTileProvider(String.valueOf(level));
        clickFloor(level);
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
                    setDestinationOnMap(destination);
                }
            } else {
                mapUIElementsManager.sourceName = data.getStringExtra("Starting point");
                mapUIElementsManager.targetName = data.getStringExtra("Destination");
                Log.i(TAG, "Source: " + mapUIElementsManager.sourceName + ", Destination: " + mapUIElementsManager.targetName);
                directionsButton.setVisibility(Button.GONE);
                revertButton.setVisibility(ImageButton.VISIBLE);
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

    @Override
    public void onCameraIdle() {
        setButtonVisibilityBasedOnCameraPosition();
    }

    private void setButtonVisibilityBasedOnCameraPosition() {
        level3.setVisibility(Button.GONE);
        level2.setVisibility(Button.GONE);
        level1.setVisibility(Button.GONE);
        level0.setVisibility(Button.GONE);
        leveln1.setVisibility(Button.GONE);
        leveln2.setVisibility(Button.GONE);
        leveln3.setVisibility(Button.GONE);
        leveln4.setVisibility(Button.GONE);
        for (IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors: BOUNDS_FOR_INDOOR_BUTTONS) {
            setButtonVisibilityForSingleLocation(indoorBuildingBoundsAndFloors);
        }
    }

    private void setButtonVisibilityForSingleLocation(IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors) {
        int minZoom = 15;
        LatLng cameraPosition = mMap.getCameraPosition().target;
        BoundingBox boundingBox = indoorBuildingBoundsAndFloors.getBoundingBox();
        double maxLat = boundingBox.getMaxLat();
        double maxLng = boundingBox.getMaxLng();
        double minLat = boundingBox.getMinLat();
        double minLng = boundingBox.getMinLng();
        ScrollView sv = (ScrollView)findViewById(R.id.scrollViewButtons);
        if (cameraPosition.latitude < maxLat && cameraPosition.latitude > minLat && cameraPosition.longitude < maxLng &&
                cameraPosition.longitude > minLng && mMap.getCameraPosition().zoom > minZoom) {
            int floor = MAX_FLOOR;
            for (ToggleButton button: floorButtonList) {
                // Here set the corresponding floor button to visible, if the current map includes a layer for the button's floor
                // The first item in the button list refers to the minimum floor
                if (indoorBuildingBoundsAndFloors.getMinFloor() <= floor && indoorBuildingBoundsAndFloors.getMaxFloor() >= floor) {
                    button.setVisibility(ToggleButton.VISIBLE);
                }
                floor--;
            }
        }
    }

    public void clickFloor(int requestedLevel) {
        String level;
        level = String.valueOf(requestedLevel);
        Log.i(TAG, "Requested level: " + level);
        level = level.replace("-", "n");
        Log.i(TAG, "Requested level after replace: " + level);
        int indexOfLevelInButtonList = MAX_FLOOR - requestedLevel;
        int currentIndexInButtonList = 0;
        for (ToggleButton levelButton: floorButtonList) {
            if (currentIndexInButtonList != indexOfLevelInButtonList) {
                levelButton.setChecked(false);
            } else {
                if (!levelButton.isChecked()) {
                    Log.i(TAG, "Entered here");
                    level = ""; //no tiles for ""
                    if (mapUIElementsManager.routePolylineOptionsInLevels != null) {
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
                    }
                } else {
                    if (mapUIElementsManager.routePolylineOptionsInLevels != null) {
                        int polyLineIndex = mapUIElementsManager.findPolyLineIndex(requestedLevel);
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(polyLineIndex);
                    }
                }
            }
            currentIndexInButtonList++;
        }
        mapUIElementsManager.level = level;
    }

    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick");
        if (view.getId() == R.id.button3) {
            clickFloor(3);
        } else if (view.getId() == R.id.button2) {
            clickFloor(2);
        } else if (view.getId() == R.id.button1) {
            clickFloor(1);
        } else if (view.getId() == R.id.button0) {
            clickFloor(0);
        } else if (view.getId() == R.id.buttonn1) {
            clickFloor(-1);
        } else if (view.getId() == R.id.buttonn2) {
            clickFloor(-2);
        } else if (view.getId() == R.id.buttonn3) {
            clickFloor(-3);
        } else if (view.getId() == R.id.buttonn4) {
            clickFloor(-4);
        } else if (view.getId() == R.id.directions) {
            onFindOriginDestinationClick();
        } else if (view.getId() == R.id.revert) {
            onFindOriginDestinationClick();
        } else if (view.getId() == R.id.targetDescriptionLayout) {
//            LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//            descriptionLayout.setOnClickListener(null);
//            ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
//            params.height = 200;
//            descriptionLayout.setLayoutParams(params);

        }
        addTileProvider(mapUIElementsManager.level);
    }

    private LatLngWithTags getDestination() {
//        String destinationName = autoCompleteDestination.getText().toString();
//        destinationName = destinationName.substring(0, destinationName.length() - 1);
//        Toast.makeText(this, "Destination: " + destinationName, Toast.LENGTH_LONG).show();
//        target = findDestinationFromId(destinationName);
        if (mapUIElementsManager.target != null) {
            return mapUIElementsManager.target;
        } else {
            return null;
        }
    }

    private void addMarkerAndZoomCameraOnTarget(LatLngWithTags target) {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
        Projection projection = mMap.getProjection();
        Point mapPoint = projection.toScreenLocation(target.getLatlng());
        destinationMarker = mMap.addMarker(new MarkerOptions().position(target.getLatlng())
                .title(target.getId()));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(target.getLatlng(), 18, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing)));
    }

    private void setDestinationOnMap(String destinationId) {
        mapUIElementsManager.target = findDestinationFromId(destinationId);
        if (mapUIElementsManager.target != null) {
//            destinationEditText = findViewById(R.id.findDestination);
            destinationEditText.setText(destinationId);
            addMarkerAndZoomCameraOnTarget(mapUIElementsManager.target);
            ImageButton cancelButton = findViewById(R.id.cancel_button);
            cancelButton.setVisibility(Button.VISIBLE);
            revertButton.setVisibility(ImageButton.GONE);
            directionsButton.setVisibility(Button.VISIBLE);
            addDestinationDescription();

            int level = findLevelFromId(mapUIElementsManager.target.getId());
            Log.i(TAG, "Set floor as checked: " + level);
            setFloorAsChecked(level);
        }
    }

    private void addDestinationDescription() {
        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.VISIBLE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText(String.format("%s, Garching MI Builiding", mapUIElementsManager.target.getId()));
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("Bolzmanstrasse 3");
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(this, "i = " + i + ", l = " + l + ", view: " + view.toString(), Toast.LENGTH_LONG).show();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //Hide keyboard
        in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        LatLngWithTags destination = getDestination();
        addMarkerAndZoomCameraOnTarget(destination);
        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.VISIBLE);
        revertButton.setVisibility(ImageButton.GONE);
        directionsButton.setVisibility(Button.VISIBLE);

        addDestinationDescription();
//        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//        descriptionLayout.setVisibility(LinearLayout.VISIBLE);
//        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
//        descriptionText.setText(String.format("%s, Garching MI Builiding", target.getId()));
//        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
//        descriptionTextBody.setText("Bolzmanstrasse 3");
        int level = findLevelFromId(mapUIElementsManager.target.getId());
        Log.i(TAG, "Set floor as checked: " + level);
        setFloorAsChecked(level);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        double minDistance = 100.0;
        //TODO: handle negative levels
        int levelToShow = Integer.valueOf(mapUIElementsManager.level);
        if (mapUIElementsManager.routePolylineOptionsInLevels != null) {
            int currLevel = 0;
            for (PolylineOptions routeLevel : mapUIElementsManager.routePolylineOptionsInLevels) {
                for (LatLng point : routeLevel.getPoints()) {
                    double tmpDistance = SphericalUtil.computeDistanceBetween(point, latLng);
                    if (tmpDistance < minDistance) {
                        minDistance = tmpDistance;
                        levelToShow = currLevel;
                    }
                }
                currLevel++;
            }
            if (minDistance < 5.0) {
                setFloorAsChecked(levelToShow);
            }
        }
//        if (descriptionLayout.getVisibility() == LinearLayout.VISIBLE) {
//            ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
//            params.height = 50;
//            descriptionLayout.setLayoutParams(params);
//            Log.i(TAG, "clicked on map: " + latLng.toString());
//            descriptionLayout.setOnClickListener(this);
//            descriptionLayout.setVisibility(LinearLayout.GONE);
//        }
    }

    @Override
    public void onCircleClick(Circle circle) {
        Log.i(TAG, "Clicked on circle: " + circle.getTag());
//        Marker source = mMap.addMarker(new MarkerOptions()
//                .position(circle.getCenter())
//                .title(circle.getTag().toString()));
//        source.setVisible(false);
//        source.showInfoWindow();

    }
}
