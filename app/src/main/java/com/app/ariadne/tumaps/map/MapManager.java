package com.app.ariadne.tumaps.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.app.ariadne.tumaps.listeners.MapClickListener;
import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.geojson.GeoJSONDijkstra;
import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.listeners.ButtonClickListener;
import com.app.ariadne.tumaps.listeners.CircleClickListener;
import com.app.ariadne.tumaps.tileProvider.CustomMapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import static com.app.ariadne.tumaps.geojson.GeoJsonMap.routablePath;

public class MapManager implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Context context;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnCircleClickListener onCircleClickListener;
    private GoogleMap.OnMyLocationButtonClickListener onMyLocationButtonClickListener;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;
    private ButtonClickListener buttonClickListener;
    private MapUIElementsManager mapUIElementsManager;
    public GeoJsonMap geoJsonMap;
    public GeoJSONDijkstra dijkstra;
    TileOverlay tileOverlay;
    private static final String TAG = "MapManager";
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;

    public MapManager(Context context, GoogleMap mMap, MapUIElementsManager mapUIElementsManager) {
        this.context = context;
        this.mMap = mMap;
        this.mapUIElementsManager = mapUIElementsManager;

//        this.buttonClickListener = new ButtonClickListener(this, mapUIElementsManager, this.context);
        this.onMapClickListener = new MapClickListener(context, buttonClickListener, mapUIElementsManager);
        this.onCircleClickListener = new CircleClickListener();
//        this.onMyLocationButtonClickListener = new LocationButtonClickListener(context);
//        this.onCameraIdleListener = new CameraIdleListener();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        LatLngBounds MUNICH = new LatLngBounds(
                new LatLng(47.8173, 11.063), new LatLng(48.5361, 12.062));
        mMap.setOnMapClickListener(this.onMapClickListener);
        mMap.setLatLngBoundsForCameraTarget(MUNICH);
        mMap.setOnCircleClickListener(this.onCircleClickListener);

        // Add a marker in Sydney and move the camera
        LatLng munich = new LatLng(48.137, 11.574);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(munich));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(munich, 14, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing)));

        mMap.setOnCameraIdleListener(this.onCameraIdleListener);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(MUNICH, 0));

        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionToAccessLocation();
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this.context, "No permissions!", Toast.LENGTH_SHORT).show();
            return;
        }

        geoJsonMap = new GeoJsonMap(mMap);
        geoJsonMap.loadIndoorTopology(this.context);
        dijkstra = new GeoJSONDijkstra(routablePath);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.context,
                android.R.layout.simple_dropdown_item_1line, geoJsonMap.targetPointsIds);
//        autoCompleteDestination.setAdapter(adapter);
//        autoCompleteDestination.setOnItemClickListener(this);

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this.onMyLocationButtonClickListener);
        mMap.setPadding(0,150,0,0);
        Toast.makeText(this.context, "Camera position: " + mMap.getCameraPosition(), Toast.LENGTH_SHORT).show();

//        mMap.setOnMyLocationClickListener(this);

    }

    public void requestPermissionToAccessLocation() {
        if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No permission to access location!");
            ActivityCompat.requestPermissions((MapsActivity) this.context,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
    }

    public void addTileProvider(final String level) {
//        http://ec2-18-191-35-229.us-east-2.compute.amazonaws.com/hot

        TileProvider tileProvider;
        Log.i(TAG, "Add tiles, level: " + level);
        if (!level.equals("")) {
            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(this.context);
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


}
