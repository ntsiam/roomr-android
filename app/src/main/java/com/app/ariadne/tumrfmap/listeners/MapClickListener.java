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
        LinearLayout descriptionLayout = ((MapsActivity) context).findViewById(R.id.targetDescriptionLayout);
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
                buttonClickListener.setFloorAsChecked(levelToShow);
                mapUIElementsManager.managePolylineOptions(levelToShow);
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

}
