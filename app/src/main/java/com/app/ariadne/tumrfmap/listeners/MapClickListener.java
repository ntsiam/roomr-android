package com.app.ariadne.tumrfmap.listeners;

import android.content.Context;
import android.widget.LinearLayout;

import com.app.ariadne.tumrfmap.MapsActivity;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumrfmap.map.MapUIElementsManager;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

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
        double minDistance = 100.0;
        boolean isClickHandled = false;
        //TODO: handle negative levels
        if (mapUIElementsManager.level != null && !mapUIElementsManager.level.equals("")) {
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
                    buttonClickListener.setFloorAsChecked(Math.abs(levelToShow - mapUIElementsManager.sourceLevel));
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
