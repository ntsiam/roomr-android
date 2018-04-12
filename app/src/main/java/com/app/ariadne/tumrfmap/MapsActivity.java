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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import com.app.ariadne.tumrfmap.listeners.MapLocationListener;
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

import static com.app.ariadne.tumrfmap.geojson.GeoJsonHelper.ListToArrayList;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.findDestinationFromId;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.routablePath;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener, View.OnClickListener, AdapterView.OnItemClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnCircleClickListener {

    private GoogleMap mMap;
    private MultiAutoCompleteTextView roomDestination;
    LocationManager locationManager;
    MapLocationListener mapLocationListerner;
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
    LatLngWithTags target;
    LatLngWithTags source;
    int sourceLevel;
    int destinationLevel;
    String level;
    ArrayList<Polyline> routeLines;
    Handler routeHandler = new Handler();
    PolylineOptions routePolylineOptions;
    PolylineOptions routePolylineOptionsGray;
    ArrayList<PolylineOptions> routePolylineOptionsInLevels;
    String sourceName;
    String targetName;
    ArrayList<ToggleButton> floorButtonList;
    private final int MIN_FLOOR = -4;
    private final int MAX_FLOOR = 3;



    public final String TILESERVER_IP = "131.159.218.107";
//    public final String TILESERVER_IP = "10.19.1.52";
    GeoJsonMap geoJsonMap;
//    public final String TILESERVER_IP = "192.168.1.83";


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
        roomDestination = (MultiAutoCompleteTextView) findViewById(R.id.multiAutoCompleteTextView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        autoCompleteDestination = findViewById(R.id.multiAutoCompleteTextView);
        autoCompleteDestination.setTokenizer(new SpaceTokenizer());
        initFloorButtonList();
        directionsButton = findViewById(R.id.directions);
        revertButton = findViewById(R.id.revert);
        directionsButton.setOnClickListener(this);
        revertButton.setOnClickListener(this);

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

    void addTileProvider(final String ipAddress, final String level) {


        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public URL getTileUrl(int x, int y, int zoom) {

                /* Define the URL pattern for the tile images */
//                String s = String.format("http://10.19.1.52/hot/%d/%d/%d.png",
                String s = String.format("http://" + ipAddress + "/hot" + level + "/%d/%d/%d.png",
                        zoom, x, y);
                Log.i(TAG, "Server: " + s);
                if (!checkTileExists(x, y, zoom)) {
                    return null;
                }

                try {
                    return new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
            }

            /*
             * Check that the tile server supports the requested x, y and zoom.
             * Complete this stub according to the tile range you support.
             * If you support a limited range of tiles at different zoom levels, then you
             * need to define the supported x, y range at each zoom level.
             */
            private boolean checkTileExists(int x, int y, int zoom) {
                int minZoom = 4;
                int maxZoom = 25;

                if ((zoom < minZoom || zoom > maxZoom)) {
                    return false;
                }

                return true;
            }
        };
        if (tileOverlay != null) {
            tileOverlay.clearTileCache();
            tileOverlay.remove();
        }
        tileOverlay = mMap.addTileOverlay(new TileOverlayOptions()
                .tileProvider(tileProvider)
                .zIndex(Float.MAX_VALUE)
                .fadeIn(false)
                .transparency(0.0f));
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
        autoCompleteDestination.setAdapter(adapter);
        autoCompleteDestination.setOnItemClickListener(this);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setPadding(0,150,0,0);
        Toast.makeText(this, "Camera position: " + mMap.getCameraPosition(), Toast.LENGTH_SHORT).show();
        addTileProvider(TILESERVER_IP, "0");

//        mMap.setOnMyLocationClickListener(this);

    }


    public void cancelTarget(View view) {
        autoCompleteDestination.setText("");
        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.INVISIBLE);
        target = null;
        destinationMarker.remove();
        destinationMarker = null;
        directionsButton.setVisibility(Button.GONE);
        revertButton.setVisibility(ImageButton.GONE);
        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.GONE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText("");
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("");

//        if (sourceMarker != null) {
//            sourceMarker.remove();
//            sourceMarker = null;
//        }
        removeSourceCircle();
        if (routeLines != null) {
//            for (int i = 0; i < routeLines.size(); i++) {
//                routeLines.get(i).remove();
//            }
            removeRouteLine();
        }
        routePolylineOptionsInLevels = null;
        routePolylineOptions = null;

    }

    public void onGetNameClick() {

        Intent getNameScreenIntent = new Intent(this, FindDestinationActivity.class);

        // We ask for the Activity to start and don't expect a result to be sent back
        // startActivity(getNameScreenIntent);

        // We use startActivityForResult when we expect a result to be sent back

        final int result = 1;

        // To send data use putExtra with a String name followed by its value

        getNameScreenIntent.putExtra("callingActivity", "MapsActivity");
        getNameScreenIntent.putExtra("destination", target.getId());


        startActivityForResult(getNameScreenIntent, result);
    }

    private boolean isGarchingMIId(String id) {
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        String value = "";
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            Log.i(TAG, "MORE tokens");
        }
        if (index == 3) {
            return true;
        }
        return false;
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
            source = findDestinationFromId(sourceName);
            sourceLevel = findLevelFromId(sourceName);
            Log.i(TAG, "Source level = " + sourceLevel);
            destinationLevel = findLevelFromId(targetName);
            Log.i(TAG, "Destination level = " + destinationLevel);
            if (source != null) {
                Log.i(TAG, "Source found!");
                target = findDestinationFromId(targetName);
                if (target != null) {
                    Log.i(TAG, "Destination found!");
                    dijkstra.startDijkstra(source);
                    routePolylineOptionsInLevels = null;
//                    routePolylineOptionsGray = new PolylineOptions().width(15).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 20);
                    routePolylineOptionsInLevels = dijkstra.getPath(target);
                    routePolylineOptions = routePolylineOptionsInLevels.get(0);
//                    for (PolylineOptions polylineOptions: routePolylineOptionsInLevels) {
//                        for (LatLng latLng: polylineOptions.getPoints()) {
//                            routePolylineOptionsGray.add(latLng);
//                        }
//                    }
                    routeHandler.post(new Runnable() {
                        public void run() {
                            ArrayList<ArrayList<LatLng>> route = new ArrayList<>();
                            if (routePolylineOptions != null && routePolylineOptions.getPoints().size() > 0) {
                                route.add(ListToArrayList(routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
                                moveCameraToStartingPosition();
//                                if (routeLines != null) {
//                                    routeLines.remove();
//                                }
                                removeRouteLine();
                                routeLines = new ArrayList<>();
                                routeLines.add(mMap.addPolyline(routePolylineOptions));
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
                            destinationMarker = mMap.addMarker(new MarkerOptions()
                                    .position(target.getLatlng())
                                    .title(target.getId()));
//                            sourceMarker = mMap.addMarker(new MarkerOptions()
//                                    .position(source.getLatlng())
//                                    .title(source.getId()));
                            addSourceCircle();
                            setFloorAsChecked(sourceLevel);

                            ProgressBar progressBar = findViewById(R.id.progressBar2);
                            progressBar.setVisibility(ProgressBar.GONE);

                            //back on UI thread...
                        }
                    });
                } else {
                    Toast.makeText(this, "Destination could not be found", Toast.LENGTH_SHORT).show();
                    cancelTarget(findViewById(R.id.targetDescriptionLayout));
                }
            } else {
                Toast.makeText(this, "Starting point could not be found", Toast.LENGTH_SHORT).show();
                cancelTarget(findViewById(R.id.targetDescriptionLayout));
            }
        }
    }

    public void moveCameraToStartingPosition() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source.getLatlng(), 18));
    }


    public void setFloorAsChecked(int level) {
        int indexOfLevelInButtonList = MAX_FLOOR - level;
        floorButtonList.get(indexOfLevelInButtonList).setChecked(true);
        clickFloor(level);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Return from Activity");
        // Get the users name from the previous Activity
        sourceName = data.getStringExtra("Starting point");
        targetName = data.getStringExtra("Destination");
        Log.i(TAG, "Source: " + sourceName + ", Destination: " + targetName);
        directionsButton.setVisibility(Button.GONE);
        revertButton.setVisibility(ImageButton.VISIBLE);
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                handleRouteRequest(sourceName, targetName);
                //TODO your background code
            }
        });

    }

    void initLocationUpdates() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        requestPermissionToAccessLocation();
        if (isFirstTime) {
            setLocationUpdates(locationManager); // Used for the GPS/Network localization
        }
    }

    private void setLocationUpdates(LocationManager locationManager) {
        mapLocationListerner = new MapLocationListener(getApplicationContext(), this);
        mapLocationListerner.setMap(mMap);
        String provider;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Log.i(TAG, "Provider: Network Provider");
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.i(TAG, "Provider: GPS Provider");
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.PASSIVE_PROVIDER;
            Log.i(TAG, "Provider: Passive Provider");
        }
        Log.i(TAG, "onCreate");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, mapLocationListerner);
    }

    @Override
    public boolean onMyLocationButtonClick() {
//        Toast.makeText(this, "MyLocation button clicked, location: " + mMap.getMyLocation().toString(), Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        initLocationUpdates();
        return false;
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

    public void removeRouteLine() {
        if (routeLines != null) {
            for (int i = 0; i < routeLines.size(); i++) {
                if (routeLines.get(i) != null) {
                    routeLines.get(i).remove();
                }
            }
        }
        routeLines = null;
    }

    public void addRouteLine(PolylineOptions polylineOptions) {
        if (routeLines == null) {
            routeLines = new ArrayList<>();
        }
        routeLines.add(mMap.addPolyline(polylineOptions));

    }

    public int findPolyLineIndex(int level) {
        int index = Integer.MIN_VALUE;
        if (sourceLevel == level) {
            index = 0;
        } else if (destinationLevel == level) {
            index = routePolylineOptionsInLevels.size() - 1;
        } else if (sourceLevel < level && destinationLevel > level) {
            index = Math.abs(level - sourceLevel);
        }
        Log.i(TAG, "Polyline index returned: " + index + ", for level: " + level + ". Levels: " + routePolylineOptionsInLevels.size());
        return index;
    }

    public void addRouteLineFromPolyLineOptions(int index) {
        removeRouteLine();
        int currentIndex = 0;
        for (PolylineOptions polylineOptions: routePolylineOptionsInLevels) {
            routePolylineOptionsGray = new PolylineOptions().width(15).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 20);
            if (currentIndex != index) {
                for (LatLng point: polylineOptions.getPoints()) {
                    routePolylineOptionsGray.add(point);
                }
            }
            addRouteLine(routePolylineOptionsGray);
            currentIndex++;
        }
        if (index != Integer.MIN_VALUE) {
            addRouteLine(routePolylineOptionsInLevels.get(index));
        }

        removeSourceCircle();
        addSourceCircle();
    }

    public void removeSourceCircle() {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }
    }

    public void addSourceCircle() {
        CircleOptions circleOptions = new CircleOptions()
                .center(source.getLatlng())
                .radius(3.0) // radius in meters
                .fillColor(0xBB00CCFF) //this is a half transparent blue, change "88" for the transparency
                .strokeColor(Color.BLUE) //The stroke (border) is blue
                .strokeWidth(2) // The width is in pixel, so try it!
                .clickable(true)
                .zIndex(Integer.MAX_VALUE);
        sourceMarker = mMap.addCircle(circleOptions);
        sourceMarker.setClickable(true);
        sourceMarker.setTag(sourceName);
    }

    public void clickFloor(int requestedLevel) {
        level = String.valueOf(requestedLevel);
        if (level.equals("0")) {
            level = "";
        }
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
                    level = "0"; //no tiles for "0" - ground floor is ""
                    if (routePolylineOptionsInLevels != null) {
                        addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
                    }
                } else {
                    if (routePolylineOptionsInLevels != null) {
                        int polyLineIndex = findPolyLineIndex(requestedLevel);
                        addRouteLineFromPolyLineOptions(polyLineIndex);
                    }
                }
            }
            currentIndexInButtonList++;
        }
    }

    @Override
    public void onClick(View view) {
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
            onGetNameClick();
        } else if (view.getId() == R.id.revert) {
            onGetNameClick();
        }
        addTileProvider(TILESERVER_IP, level);
    }

    private LatLngWithTags getDestination() {
        String destinationName = autoCompleteDestination.getText().toString();
        destinationName = destinationName.substring(0, destinationName.length() - 1);
//        Toast.makeText(this, "Destination: " + destinationName, Toast.LENGTH_LONG).show();
        target = findDestinationFromId(destinationName);
        if (target != null) {
            return target;
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

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(this, "i = " + i + ", l = " + l + ", view: " + view.toString(), Toast.LENGTH_LONG).show();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        LatLngWithTags destination = getDestination();
        addMarkerAndZoomCameraOnTarget(destination);
        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.VISIBLE);
        revertButton.setVisibility(ImageButton.GONE);
        directionsButton.setVisibility(Button.VISIBLE);

        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.VISIBLE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText(String.format("%s, Garching MI Builiding", target.getId()));
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("Bolzmanstrasse 3");
        int level = findLevelFromId(target.getId());
        Log.i(TAG, "Set floor as checked: " + level);
        setFloorAsChecked(level);

    }

    @Override
    public void onMapClick(LatLng latLng) {
//        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//        descriptionLayout.setVisibility(LinearLayout.GONE);
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
