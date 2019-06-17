package com.app.ariadne.tumaps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.*;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.ariadne.tumaps.db.models.WifiAPDetails;
import com.app.ariadne.tumaps.geojson.GeoJSONDijkstra;
import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.listeners.ButtonClickListener;
import com.app.ariadne.tumaps.listeners.MapClickListener;
import com.app.ariadne.tumaps.map.MapManager;
import com.app.ariadne.tumaps.map.MapUIElementsManager;
import com.app.ariadne.tumaps.models.LocalizedWifiAP;
import com.app.ariadne.tumaps.models.Route;
import com.app.ariadne.tumaps.models.RouteInstruction;
import com.app.ariadne.tumaps.wifi.WifiScanner;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumaps.listeners.ItemClickListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.hermes.IHermesService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static android.content.pm.PackageManager.*;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MainActivity";
    public static boolean isFirstTime = true;
    public static boolean isStreamingToTangleEnabled = false;
    public static boolean isStreamingToTanglePossible = false;
    ArrayList<GeoJSONDijkstra> dijkstraForEachBuilding;
    Handler routeHandler = new Handler();
    MapUIElementsManager mapUIElementsManager;
    ButtonClickListener buttonClickListener;
    GeoJsonMap geoJsonMap;
    String previousDestination;
    int targetBuildingIndex;
    MapManager mapManager;
    MapClickListener mapClickListener;
    ItemClickListener itemClickListener;


    public FirebaseApp dynamicMLDB;
    public FirebaseOptions options = new FirebaseOptions.Builder()
            .setApplicationId("dynamicml-86ae8") // Required for Analytics.
            .setApiKey("AIzaSyCgU01k54OtOz5Y_kz0IsmrzBUEooT6HzI ") // Required for Auth.
            .setDatabaseUrl("https://dynamicml-86ae8.firebaseio.com/") // Required for RTDB.
            .build();


    public static ArrayList<String> instructions;
    public static ArrayList<LatLng> waypoints;
    public static Queue<RouteInstruction> routeInstructionQueue;

    public static Polyline predictionRectanglePolyline;
    public WifiManager wifi;
    public WifiScanner wifiScanner;
    public static ArrayList<WifiAPDetails> wifiAPDetailsList = new ArrayList<>();


    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    IHermesService iHermesService;
    String uuid = null;
    MessageDigest md;


    private final String DATA = "{" +
            "\"deviceMacAddress\" : \"ec:9b:f3:44:9b:6a\"," +
            "\"lat\" : 48.263077," +
            "\"level\" : 1," +
            "\"lng\" : 11.667316," +
            "\"timestamp\" : 1552372456538," +
            "\"wifiAPDetailsArrayList\" : [ {" +
            "\"frequency\" : 2412," +
            "\"macAddress\" : \"18:bd:27:42:5e:e0\"," +
            "\"signalStrength\" : 89" +
            "}, {" +
            "\"frequency\" : 2412," +
            "\"macAddress\" : \"34:c3:46:60:ca:a9\"," +
            "\"signalStrength\" : 48" +
            "} ]" +
            "}";


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
        mapManager = new MapManager(this);
        geoJsonMap = new GeoJsonMap(mapManager.getMap());
        geoJsonMap.loadIndoorTopology(this);

        dynamicMLDB = FirebaseApp.initializeApp(this /* Context */, options, "DynamicML");

        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        connectService();
//        Timer myTimer = new Timer();
//        TimerTask streamTimerTask = new TimerTask() {
//            @Override
//            public void run() {
//                streamWifiToTangle();
//            }
//        };
//        myTimer.schedule(streamTimerTask, 5000);

    }

    private void writeSignalStrengthsToFirebaseDynamicML(LocalizedWifiAP localizedWifiAP) {
        // Firebase Database paths must not contain '.', '#', '$', '[', or ']'
//        mDatabase.child("wifis").child(localizedWifiAP.toString()).setValue(localizedWifiAP);
//        System.out.println("Writing signal strengths to firebase");
        FirebaseDatabase dynamicMLDatabase = FirebaseDatabase.getInstance(dynamicMLDB);
        DatabaseReference ref = dynamicMLDatabase.getReference();
        String key = ref.child("route").push().getKey();
        Map<String, Object> childUpdates = new HashMap<>();
        int pathCounter = 1;
//        childUpdates.put("/route/route-" + pathCounter + "/" + key, localizedWifiAP);
//        childUpdates.put("/route/route-1/" + key, localizedWifiAP);
//
//        ref.updateChildren(childUpdates);

        if (localizedWifiAP.getWifiAPDetailsArrayList().size() > 0) {
            key = ref.child("wifis").push().getKey();
            childUpdates = new HashMap<>();
            childUpdates.put("/wifis/route-" + pathCounter + "/" + key, localizedWifiAP);
            childUpdates.put("/wifis/route-1/" + key, localizedWifiAP);

            ref.updateChildren(childUpdates);
        }
    }


    void connectService() {
        Log.i(TAG, "Trying to connect to ledger service...");
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                iHermesService = IHermesService.Stub.asInterface((IBinder) service);
                Log.i(TAG,"Connected to service");
                try {
                    uuid = iHermesService.register("android.maps1", "string", "random_source", null, null);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (uuid == null) {
                    Log.e(TAG, "There was an error while trying to connect to the service");
                    return;
                }

                Log.i(TAG, "Sending data with uuid = " + uuid);

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                iHermesService = null;
                Log.i("MapsActivity","Disconnected from service");

            }
        };

        PackageInfo otherApp;
        Log.i(TAG, "Registering...");
        try {
            otherApp = getBaseContext().getPackageManager().getPackageInfo("org.hermes", GET_SERVICES);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Hermes application is not installed in the system!");
            return;
        }
        Log.i(TAG, "Other app: " + otherApp.packageName);
        ServiceInfo hermesService = null;
        for (int i=0 ; i<otherApp.services.length ; i++) {
            ServiceInfo serviceInfo = otherApp.services[i];
            if (serviceInfo.name.equals("org.hermes.LedgerService")) {
                hermesService = serviceInfo;
                break;
            }
        }
        if (hermesService == null) {
            Log.e(TAG, "Could not find Hermes service in the package info of the Hermes App!");
        } else {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(hermesService.packageName, hermesService.name));
            if (getBaseContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                Log.i(TAG, "Initiated binding to Hermes Service!");
            } else {
                Log.e(TAG, "Could not bind to Hermes Service");
            }
        }
    }

    public void endStreamingToTangle() {
        String res = null;
        try {
            res = iHermesService.sendDataString(uuid, "END OF DATA", null, null,
                    null, null, null, null, -1, null);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "uuid: " + uuid + ", res: " + res + ", length of res = " + res.length());
    }

    public void streamWifiToTangle(JSONArray wifiAPDetailsListJSON) {
        if (mapUIElementsManager.getPositionManager() != null) {
            LatLng currentPosition = mapUIElementsManager.getPositionManager().currentPosition;
            long unixTime = System.currentTimeMillis();
            if (uuid != null && wifiAPDetailsListJSON != null && wifiAPDetailsListJSON.length() > 0
                    && currentPosition != null && isStreamingToTangleEnabled) {
                try {
                    JSONObject localizedDataJSON = new JSONObject();
                    localizedDataJSON.put("lat", currentPosition.latitude);
                    localizedDataJSON.put("lng", currentPosition.longitude);
                    localizedDataJSON.put("wifiAPList", wifiAPDetailsListJSON);
                    localizedDataJSON.put("timestamp", unixTime);
                    String json = localizedDataJSON.toString();
                    Log.i(TAG, json);
                    String res = iHermesService.sendDataString(uuid, json, null, null,
                            null, null, null, null, -1, null);
                    Log.i(TAG, "uuid: " + uuid + ", res: " + res + ", length of res = " + res.length());
                } catch (RemoteException | JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i(TAG, "uuid or wifi list is null");
            }
            LocalizedWifiAP localizedWifiAPArrayList = new LocalizedWifiAP(md, wifiAPDetailsList, currentPosition, unixTime);
            writeSignalStrengthsToFirebaseDynamicML(localizedWifiAPArrayList);
        } else {
            Log.i(TAG, "Position manager is null");
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
            mapManager.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        mapManager.setMyLocationEnabled(true);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapManager.setUpMap(googleMap);
        requestPermissionToAccessLocation();
        dijkstraForEachBuilding = new ArrayList<>();
        for (int i = 0; i < GeoJsonMap.routablePathForEachBuilding.size(); i++) {
            dijkstraForEachBuilding.add(new GeoJSONDijkstra(GeoJsonMap.routablePathForEachBuilding.get(i)));
        }
        buttonClickListener = new ButtonClickListener(this, mapManager);
        mapManager.setOnCameraIdleListener(buttonClickListener);
        mapUIElementsManager = new MapUIElementsManager(this, buttonClickListener, mapManager.getMap(), geoJsonMap);
        buttonClickListener.setMapUIElementsManager(mapUIElementsManager);
        mapClickListener = new MapClickListener(mapUIElementsManager);
        mapManager.setOnMapClickListener(mapClickListener);
        itemClickListener = new ItemClickListener(this, mapUIElementsManager, buttonClickListener);

        initWifiScanner();

    }


    public void cancelTarget(View view) {
        mapUIElementsManager.removeAllDestinationElementsFromMap();
        buttonClickListener.removeButtons();
        isStreamingToTanglePossible = false;
        findViewById(R.id.streamToggleButton).setVisibility(Button.GONE);
    }

    /**
     * This method is called when the user has pressed the "Get directions"
     * button that launches the second screen of the application where both
     * the origin and the destination can be set.
     */
    public void onFindOriginDestinationClick() {
        startActivityForResults(FindOriginDestinationActivity.class);
    }

    /**
     * This method is called whenever the user wants to set a new destination
     * using the textbox in the initial screen.
     * @param view
     */
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

        if (mapUIElementsManager.destination != null) {
            previousDestination = mapUIElementsManager.destination.getId();
            getNameScreenIntent.putExtra("destination", mapUIElementsManager.getDestination().getId());
        }
        if (mapUIElementsManager.areSourceAndDestinationNotNull() && GeoJsonMap.isSourceAndDestinationInTheSameBuilding(mapUIElementsManager.getSource(), mapUIElementsManager.getDestination())) {
            getNameScreenIntent.putExtra("source", mapUIElementsManager.source.getId());
        }
        startActivityForResult(getNameScreenIntent, result);
    }

    private void handleRouteRequest(final String sourceName, final String targetName) {
        if (mapUIElementsManager.isRoutableSourceDestination()) {
            //Log.i(TAG, "Source: " + source);
            mapUIElementsManager.setSourceAndDestination(sourceName, targetName);
            //Log.i(TAG, "Destination level = " + mapUIElementsManager.destinationLevel);
            if (mapUIElementsManager.areSourceAndDestinationNotNull()) {

                Route route = dijkstraForEachBuilding.get(targetBuildingIndex).getPath(mapUIElementsManager.getSource());

                instructions = dijkstraForEachBuilding.get(targetBuildingIndex).routeInstructions;
                waypoints = dijkstraForEachBuilding.get(targetBuildingIndex).routeInstructionPoints;
                routeInstructionQueue = dijkstraForEachBuilding.get(targetBuildingIndex).instructionQueue;
//                String currInstruction = "";
//                LatLng prevPoint = null;
//                int index = 0;
//                for (LatLng point: waypoints) {
//                    if (prevPoint != null) {
//                        if (prevPoint.equals(point)) {
//                            currInstruction += ", " + instructions.get(index);
//                        } else {
//                            currInstruction = instructions.get(index);
//                            MarkerOptions options = new MarkerOptions()
//                                    .position(prevPoint)
//                                    .title(currInstruction);
//
//                            mapManager.mMap.addMarker(options);
//                        }
//                    }
//                    prevPoint = point;
//                }



                LatLng minPoint = new LatLng(dijkstraForEachBuilding.get(targetBuildingIndex).minRouteLat, dijkstraForEachBuilding.get(targetBuildingIndex).minRouteLng);
                LatLng maxPoint = new LatLng(dijkstraForEachBuilding.get(targetBuildingIndex).maxRouteLat, dijkstraForEachBuilding.get(targetBuildingIndex).maxRouteLng);
                mapUIElementsManager.setRoutePathOnMap(route, routeHandler, minPoint, maxPoint);
                routeHandler.post(new Runnable() {
                    public void run() {
                        isStreamingToTanglePossible = true;
                        findViewById(R.id.streamToggleButton).setVisibility(Button.VISIBLE);
                    }
                });


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

    private void startAsyncTaskToHandleRouteRequest(final String targetBuildingId) {
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String destinationName = mapUIElementsManager.getDestinationName();
                if (!previousDestination.equals(destinationName)) {
                    previousDestination = destinationName;
                    targetBuildingIndex = GeoJsonMap.buildingIndexes.get(targetBuildingId);
                    dijkstraForEachBuilding.get(targetBuildingIndex).startDijkstra(GeoJsonMap.findDestinationFromId(destinationName));
                }
                handleRouteRequest(mapUIElementsManager.getSourceName(), destinationName);
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
        if (!previousDestination.equals(mapUIElementsManager.getDestination().getId())) { // If destination changed start new dijkstraForEachBuilding
            previousDestination = mapUIElementsManager.getDestination().getId();
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    targetBuildingIndex = GeoJsonMap.buildingIndexes.get(mapUIElementsManager.getDestination().getBuildingId());
                    dijkstraForEachBuilding.get(targetBuildingIndex).startDijkstra(mapUIElementsManager.getDestination());
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
        String sourceName = data.getStringExtra("Starting point");
        String destinationName = data.getStringExtra("Destination");
        mapUIElementsManager.setSourceName(sourceName);
        mapUIElementsManager.setDestinationName(destinationName);
        //Log.i(TAG, "Source: " + mapUIElementsManager.sourceName + ", Destination: " + mapUIElementsManager.destinationName);
        buttonClickListener.showDirectionsButtons();
        if (mapUIElementsManager.isRoutableSourceDestination()) {
            final String targetBuildingId = GeoJsonMap.getBuildingIdFromRoomName(destinationName);
            final String sourceBuildingId = GeoJsonMap.getBuildingIdFromRoomName(sourceName);

            if (targetBuildingId.equals(sourceBuildingId) || sourceName.equals("Entrance of building")) {
                startAsyncTaskToHandleRouteRequest(targetBuildingId);
            } else {
                Toast.makeText(this, "Inter-building routing is not supported yet", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * UNUSED
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

    protected void onResume() {
        super.onResume();
        if (mapUIElementsManager != null) {
            mapUIElementsManager.getSensorChangeListener().registerListeners();
        }
    }

    protected void onPause() {
        super.onPause();
        if (mapUIElementsManager != null) {
            mapUIElementsManager.getSensorChangeListener().unregisterListeners();
        }
    }

    void initWifiScanner() {
        requestPermissionToAccessLocation();
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            wifi.isDeviceToApRttSupported();
        }
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "wifi is disabled..enabling it", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        wifiScanner = new WifiScanner(wifi, getApplicationContext(), this);
    }

}
