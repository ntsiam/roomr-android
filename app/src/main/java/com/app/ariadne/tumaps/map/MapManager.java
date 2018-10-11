package com.app.ariadne.tumaps.map;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.app.ariadne.tumaps.MapsConfiguration;
import com.app.ariadne.tumaps.listeners.ItemClickListener;
import com.app.ariadne.tumaps.listeners.LocationButtonClickListener;
import com.app.ariadne.tumaps.listeners.MapClickListener;
import com.app.ariadne.tumaps.geojson.GeoJSONDijkstra;
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


public class MapManager {
    private GoogleMap mMap;
    private Context context;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private GoogleMap.OnCircleClickListener onCircleClickListener;
    MapClickListener mapClickListener;
    ItemClickListener itemClickListener;
    private ButtonClickListener buttonClickListener;
    private MapUIElementsManager mapUIElementsManager;
    public GeoJsonMap geoJsonMap;
    public ArrayList<GeoJSONDijkstra> dijkstra;
    TileOverlay tileOverlay;
    private static final String TAG = "MapManager";
    MapsConfiguration mapsConfigurationInstance;

    public MapManager(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
        this.onMapClickListener = new MapClickListener(context, buttonClickListener, mapUIElementsManager);
        this.onCircleClickListener = new CircleClickListener();
        mapsConfigurationInstance = MapsConfiguration.getInstance();

    }

    public void setUpMap(GoogleMap googleMap) {
        buttonClickListener = new ButtonClickListener(context);
        mMap = googleMap;
        mMap.setMinZoomPreference(10.0f);
        mMap.setLatLngBoundsForCameraTarget(mapsConfigurationInstance.getMapBounds());
        mMap.setOnCircleClickListener(new CircleClickListener());
        mMap.setIndoorEnabled(false);

        LatLng mapCenter = mapsConfigurationInstance.getMapCenter();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(mapCenter, mapsConfigurationInstance.getInitialZoom(), mMap.getCameraPosition().tilt,
                mMap.getCameraPosition().bearing)));

        mMap.setOnCameraIdleListener(buttonClickListener);
        dijkstra = new ArrayList<>();
        for (int i = 0; i < GeoJsonMap.routablePathForEachBuilding.size(); i++) {
            dijkstra.add(new GeoJSONDijkstra(GeoJsonMap.routablePathForEachBuilding.get(i)));
        }
        ArrayList<String> targetPointsIds = GeoJsonMap.targetPointsIds;
        targetPointsIds.remove("Entrance of building");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_dropdown_item_1line, targetPointsIds);

        mMap.setPadding(0,150,0,0);

        mMap.setOnMyLocationButtonClickListener(new LocationButtonClickListener(context, mMap));
        mapUIElementsManager = new MapUIElementsManager(context, buttonClickListener, mMap, geoJsonMap);
        buttonClickListener.setMapUIElementsManager(mapUIElementsManager);
        mapClickListener = new MapClickListener(context, buttonClickListener, mapUIElementsManager);
        mMap.setOnMapClickListener(mapClickListener);
        itemClickListener = new ItemClickListener(context, mapUIElementsManager, buttonClickListener);
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
