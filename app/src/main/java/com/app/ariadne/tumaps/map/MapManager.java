package com.app.ariadne.tumaps.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.MapsConfiguration;
import com.app.ariadne.tumaps.RequestLocalizationTask;
import com.app.ariadne.tumaps.db.models.WifiAPDetails;
import com.app.ariadne.tumaps.listeners.LocationButtonClickListener;
import com.app.ariadne.tumaps.listeners.MapClickListener;
import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.listeners.ButtonClickListener;
import com.app.ariadne.tumaps.listeners.CircleClickListener;
import com.app.ariadne.tumaps.tileProvider.CustomMapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import java.util.ArrayList;

import static com.app.ariadne.tumaps.MapsActivity.wifiAPDetailsList;


public class MapManager {
    public GoogleMap mMap;
    private Context context;
    private MapUIElementsManager mapUIElementsManager;
    TileOverlay tileOverlay;
    private static final String TAG = "MapManager";
    MapsConfiguration mapsConfigurationInstance;

    public MapManager(Context context) {
        this.context = context;
        mapsConfigurationInstance = MapsConfiguration.getInstance();

    }

    public void setOnMapClickListener(MapClickListener mapClickListener) {
        mMap.setOnMapClickListener(mapClickListener);
    }

    public GoogleMap getMap() {
        return mMap;
    }

    public void setOnCameraIdleListener(ButtonClickListener buttonClickListener) {
        mMap.setOnCameraIdleListener(buttonClickListener);
    }

    public void setUpMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        mMap.setLatLngBoundsForCameraTarget(mapsConfigurationInstance.getMapBounds());
        mMap.setOnCircleClickListener(new CircleClickListener());
        mMap.setIndoorEnabled(false);

        LatLng mapCenter = mapsConfigurationInstance.getMapCenter();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(mapCenter, mapsConfigurationInstance.getInitialZoom(), mMap.getCameraPosition().tilt,
                mMap.getCameraPosition().bearing)));

        ArrayList<String> targetPointsIds = GeoJsonMap.targetPointsIds;
        targetPointsIds.remove("Entrance of building");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, targetPointsIds);

        mMap.setPadding(0, 150, 0, 0);

        mMap.setOnMyLocationButtonClickListener(new LocationButtonClickListener(context, mMap));
    }

    public void setMyLocationEnabled(boolean choice) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(choice);
    }

    public void requestLocalizationWifi() {

        String url = "ec2-18-188-248-182.us-east-2.compute.amazonaws.com";
        RequestLocalizationTask requestLocalizationTask = new RequestLocalizationTask(getMap(), (MapsActivity) context);
        String request = "{\"prediction_type\": 0, \"position\":{\"wifiAPDetailsArrayList\":[";
        if (wifiAPDetailsList.size() > 0) {
            for (int i = 0; i < wifiAPDetailsList.size() - 1; i++) {
                WifiAPDetails wifiAPDetails = wifiAPDetailsList.get(i);
                request += "{\"macAddress\":\"" + wifiAPDetails.getMacAddress() + "\",\"signalStrength\":\"" + wifiAPDetails.getSignalStrength() + "\"},";
            }
            WifiAPDetails wifiAPDetails = wifiAPDetailsList.get(wifiAPDetailsList.size() - 1);
            request += "{\"macAddress\":\"" + wifiAPDetails.getMacAddress() + "\",\"signalStrength\":\"" +
                    wifiAPDetails.getSignalStrength() + "\"}],\"lat\":\"" + "0.0" + "\",\"lng\":\"" +
                    "0.0" + "\",\"timestamp\":\"" + System.currentTimeMillis() + "\"}}";
            Log.i(TAG, "Request: " + request);
            requestLocalizationTask.execute(url, request);
            wifiAPDetailsList = new ArrayList<>();
        }
    }


    public void addTileProvider(final String level) {
        TileProvider tileProvider;
        //Log.i(TAG, "Add tiles, level: " + level);
        if (!level.equals("")) {
            CustomMapTileProvider customMapTileProvider = new CustomMapTileProvider(context);
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


}
