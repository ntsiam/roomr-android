package com.app.ariadne.tumaps.listeners;

import android.content.Context;

import com.app.ariadne.tumaps.map.MapUIElementsManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

public class MapClickListener implements GoogleMap.OnMapClickListener {
    private Context context;
    private ButtonClickListener buttonClickListener;
    private MapUIElementsManager mapUIElementsManager;

    public MapClickListener(Context context, ButtonClickListener buttonClickListener, MapUIElementsManager mapUIElementsManager) {
        this.context = context;
        this.buttonClickListener = buttonClickListener;
        this.mapUIElementsManager = mapUIElementsManager;
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        Log.i("OnClick", "Clicked on map");
        double minDistance = 100.0;
        boolean isClickHandled = false;
        //TODO: handle negative levels
        if (mapUIElementsManager.level != null && !mapUIElementsManager.level.equals("")) {
//            Log.i("OnClick", "mapUIElementsManager.level is defined");
            int levelToShow = Integer.valueOf(mapUIElementsManager.level);
            if (mapUIElementsManager.route != null) {
//                Log.i("OnClick", "mapUIElementsManager.route is defined");
                int currLevel = mapUIElementsManager.route.getMinRouteLevel();
                for (int i = mapUIElementsManager.route.getMinRouteLevel(); i <= mapUIElementsManager.route.getMaxRouteLevel(); i++) {
                    ArrayList<PolylineOptions> routeLevelContainer = mapUIElementsManager.route.getRouteHashMapForLevels().get(i);
                    for (PolylineOptions routeLevel : routeLevelContainer) {
                        for (LatLng point : routeLevel.getPoints()) {
                            double tmpDistance = SphericalUtil.computeDistanceBetween(point, latLng);
                            if (tmpDistance < minDistance) {
                                minDistance = tmpDistance;
                                levelToShow = currLevel;
//                                Log.i("OnClick", "mindistance: " + minDistance);
                            }
                        }
                    }
                    currLevel++;
                }
                if (minDistance < 5.0) {
                    buttonClickListener.setFloorAsChecked(levelToShow);
                    mapUIElementsManager.addRouteLineFromPolyLineOptions(levelToShow);
                    isClickHandled = true;
                }
            }
        }
        if (!isClickHandled && mapUIElementsManager.target != null) {
            mapUIElementsManager.toggleMapUIElementVisibility();
        }

    }

}
