package com.app.ariadne.tumrfmap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import static com.app.ariadne.tumrfmap.geojson.GeoJsonHelper.ListToArrayList;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.findDestinationFromId;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.routablePath;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.targetPointsTagged;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener, View.OnClickListener, AdapterView.OnItemClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private MultiAutoCompleteTextView roomDestination;
    LocationManager locationManager;
    MapLocationListener mapLocationListerner;
    private static final String TAG = "MainActivity";
    public static boolean isFirstTime = true;
    ToggleButton level2;
    ToggleButton level1;
    ToggleButton level0;
    ToggleButton leveln1;
    ToggleButton leveln2;
    ToggleButton leveln3;
    ToggleButton leveln4;
    Button directionsButton;
    TileOverlay tileOverlay;
    MultiAutoCompleteTextView autoCompleteDestination;
    GeoJSONDijkstra dijkstra;
    Marker sourceMarker;
    Marker destinationMarker;
    LatLngWithTags target;
    LatLngWithTags source;
    Polyline routeLine;
    Handler routeHandler = new Handler();
    PolylineOptions routePolylineOptions;



    public final String TILESERVER_IP = "10.19.1.52";
    GeoJsonMap geoJsonMap;
//    public final String TILESERVER_IP = "192.168.1.83";


    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    private static final String[] COUNTRIES = new String[]{
            "Belgium", "France", "Italy", "Itastria", "Germany", "Spain"
    };

//    double maxLat = 48.162697;
//    double minLat = 48.160468;
//    double minLng = 11.584937;
//    double maxLng = 11.587385;

    private static final BoundingBox MUENCHNER_FREIHEIT_BB = new BoundingBox(new LatLng(48.162697, 11.587385), new LatLng(48.160468, 11.584937));
    private static final BoundingBox HAUPTBAHNHOF_BB = new BoundingBox(new LatLng(48.143000, 11.565000), new LatLng(48.137300, 11.554600));
    private static final BoundingBox GARCHING_MI_BB = new BoundingBox(new LatLng(48.2636, 11.6705), new LatLng(48.2613, 11.6653));
    private static final IndoorBuildingBoundsAndFloors MUENCHNER_FREIHEIT = new IndoorBuildingBoundsAndFloors(MUENCHNER_FREIHEIT_BB, -2, 0);
    private static final IndoorBuildingBoundsAndFloors HAUPTBAHNHOF = new IndoorBuildingBoundsAndFloors(HAUPTBAHNHOF_BB, -4, 0);
    private static final IndoorBuildingBoundsAndFloors GARCHING_MI = new IndoorBuildingBoundsAndFloors(GARCHING_MI_BB, 0, 2);
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
        level2 = findViewById(R.id.button2);
        level1 = findViewById(R.id.button1);
        level0 = findViewById(R.id.button0);
        leveln1 = findViewById(R.id.buttonn1);
        leveln2 = findViewById(R.id.buttonn2);
        leveln3 = findViewById(R.id.buttonn3);
        leveln4 = findViewById(R.id.buttonn4);
        directionsButton = findViewById(R.id.directions);
        level2.setOnClickListener(this);
        level1.setOnClickListener(this);
        level0.setOnClickListener(this);
        leveln1.setOnClickListener(this);
        leveln2.setOnClickListener(this);
        leveln3.setOnClickListener(this);
        leveln4.setOnClickListener(this);
        directionsButton.setOnClickListener(this);

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
//        directionsButton.setVisibility(Button.INVISIBLE);

        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.GONE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText("");
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("");

        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }
        if (routeLine != null) {
            routeLine.remove();
        }

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

    private void handleRouteRequest(String sourceName, final String targetName) {
        if (!sourceName.equals("Building entrance") & !sourceName.equals("My Location")) {
            source = findDestinationFromId(sourceName);
            if (source != null) {
                Log.i(TAG, "Source found!");
                target = findDestinationFromId(targetName);
                if (target != null) {
                    Log.i(TAG, "Destination found!");
                    dijkstra.startDijkstra(source);
                    routePolylineOptions = dijkstra.getPath(target);
                    routeHandler.post(new Runnable() {
                        public void run() {
                            ArrayList<ArrayList<LatLng>> route = new ArrayList<>();
                            if (routePolylineOptions != null && routePolylineOptions.getPoints().size() > 0) {
                                route.add(ListToArrayList(routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
//                        moveCameraToStartingPosition();
                                if (routeLine != null) {
                                    routeLine.remove();
                                }
                                routeLine = mMap.addPolyline(routePolylineOptions);
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
                            sourceMarker = mMap.addMarker(new MarkerOptions()
                                    .position(source.getLatlng())
                                    .title(source.getId()));

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "Return from Activity");
        // Get the users name from the previous Activity
        final String sourceName = data.getStringExtra("Starting point");
        final String targetName = data.getStringExtra("Destination");
        Log.i(TAG, "Source: " + sourceName + ", Destination: " + targetName);
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
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= -4 && indoorBuildingBoundsAndFloors.getMaxFloor() >= -4) {
                leveln4.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= -3 && indoorBuildingBoundsAndFloors.getMaxFloor() >= -3) {
                leveln3.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= -2 && indoorBuildingBoundsAndFloors.getMaxFloor() >= -2) {
                leveln2.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= -1 && indoorBuildingBoundsAndFloors.getMaxFloor() >= -1) {
                leveln1.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= 0 && indoorBuildingBoundsAndFloors.getMaxFloor() >= 0) {
                level0.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= 1 && indoorBuildingBoundsAndFloors.getMaxFloor() >= 1) {
                level1.setVisibility(Button.VISIBLE);
            }
            if (indoorBuildingBoundsAndFloors.getMinFloor() <= 2 && indoorBuildingBoundsAndFloors.getMaxFloor() >= 2) {
                level2.setVisibility(Button.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View view) {
        String level = "";
        if (view.getId() == R.id.button2) {
            level = "2";
            level1.setChecked(false);
            level0.setChecked(false);
            leveln1.setChecked(false);
            leveln2.setChecked(false);
            leveln3.setChecked(false);
            leveln4.setChecked(false);
            if (!level2.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.button1) {
            level = "1";
            level2.setChecked(false);
            level0.setChecked(false);
            leveln1.setChecked(false);
            leveln2.setChecked(false);
            leveln3.setChecked(false);
            leveln4.setChecked(false);
            if (!level1.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.button0) {
            level = "";
            level1.setChecked(false);
            level2.setChecked(false);
            leveln1.setChecked(false);
            leveln2.setChecked(false);
            leveln3.setChecked(false);
            leveln4.setChecked(false);
            if (!level0.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.buttonn1) {
            level = "n1";
            level1.setChecked(false);
            level0.setChecked(false);
            level2.setChecked(false);
            leveln2.setChecked(false);
            leveln3.setChecked(false);
            leveln4.setChecked(false);
            if (!leveln1.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.buttonn2) {
            level = "n2";
            level1.setChecked(false);
            level0.setChecked(false);
            leveln1.setChecked(false);
            level2.setChecked(false);
            leveln3.setChecked(false);
            leveln4.setChecked(false);
            if (!leveln2.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.buttonn3) {
            level = "n3";
            Log.i(TAG, "level -3 clicked");
            level1.setChecked(false);
            level0.setChecked(false);
            leveln1.setChecked(false);
            level2.setChecked(false);
            leveln2.setChecked(false);
            leveln4.setChecked(false);
            if (!leveln3.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.buttonn4) {
            level = "n4";
            level1.setChecked(false);
            level0.setChecked(false);
            leveln1.setChecked(false);
            level2.setChecked(false);
            leveln3.setChecked(false);
            leveln2.setChecked(false);
            if (!leveln4.isChecked()) {
                level = "0"; //no tiles for "0" - ground floor is ""
            }
        } else if (view.getId() == R.id.directions) {
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
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(16 - mMap.getCameraPosition().zoom, mapPoint));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(target.getLatlng()));
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

//        directionsButton.setVisibility(Button.VISIBLE);
        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.VISIBLE);
        TextView descriptionText = findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText(String.format("%s, Garching MI Builiding", target.getId()));
        TextView descriptionTextBody = findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("Bolzmanstrasse 3");

    }

    @Override
    public void onMapClick(LatLng latLng) {
//        LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//        descriptionLayout.setVisibility(LinearLayout.GONE);
    }
}
