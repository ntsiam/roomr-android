package com.app.ariadne.tumaps.listeners;

import android.content.Context;

import com.app.ariadne.tumaps.map.MapUIElementsManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MapClickListener implements GoogleMap.OnMapClickListener {
    private MapUIElementsManager mapUIElementsManager;

    public MapClickListener(MapUIElementsManager mapUIElementsManager) {
        this.mapUIElementsManager = mapUIElementsManager;
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        Log.i("OnClick", "Clicked on map");
        mapUIElementsManager.handleClickOnMap(latLng);
    }

}
