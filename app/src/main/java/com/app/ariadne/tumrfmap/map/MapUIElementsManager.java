package com.app.ariadne.tumrfmap.map;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.ToggleButton;

import com.app.ariadne.tumrfmap.geojson.LatLngWithTags;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapUIElementsManager {
    public ArrayList<PolylineOptions> routePolylineOptionsInLevels;
    public PolylineOptions routePolylineOptions;
    public PolylineOptions routePolylineOptionsGray;
    Context context;
    private static final String TAG = "MAPUIElements";
    public String level;
    private final int MIN_FLOOR = -4;
    public static final int MAX_FLOOR = 3;
    public ArrayList<ToggleButton> floorButtonList;
    public String sourceName;
    public String targetName;
    public Circle sourceMarker;
    public GoogleMap mMap;
    public Marker destinationMarker;
    public LatLngWithTags target;
    public LatLngWithTags source;
    public int sourceLevel;
    public int destinationLevel;
    public ArrayList<Polyline> routeLines;

    public MapUIElementsManager(Context context, ArrayList<ToggleButton> floorButtonList, GoogleMap mMap) {
        this.context = context;
        this.floorButtonList = floorButtonList;
        this.mMap = mMap;
    }

    public void managePolylineOptions(int requestedLevel) {
        int indexOfLevelInButtonList = MAX_FLOOR - requestedLevel;
        int currentIndexInButtonList = 0;
        for (ToggleButton levelButton: floorButtonList) {
            if (currentIndexInButtonList != indexOfLevelInButtonList) {
                levelButton.setChecked(false);
            } else {
                if (!levelButton.isChecked()) {
                    Log.i(TAG, "Entered here");
                    level = ""; //no tiles for "0" - ground floor is ""
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

    public void addRouteLineFromPolyLineOptions(int index) {
        removeRouteLine();
        int currentIndex = 0;
        for (PolylineOptions polylineOptions: routePolylineOptionsInLevels) {
            routePolylineOptionsGray = new PolylineOptions().width(20).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 20);
            if (currentIndex != index) {
                for (LatLng point: polylineOptions.getPoints()) {
                    routePolylineOptionsGray.add(point);
                }
            }
            addRouteLine(routePolylineOptionsGray);
            routePolylineOptionsGray.clickable(true);
            currentIndex++;
        }
        if (index != Integer.MIN_VALUE) {
            addRouteLine(routePolylineOptionsInLevels.get(index));
            routePolylineOptionsGray.clickable(true);
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
                .radius(1.0) // radius in meters
                .fillColor(0xBB00CCFF) //this is a half transparent blue, change "88" for the transparency
                .strokeColor(Color.BLUE) //The stroke (border) is blue
                .strokeWidth(2) // The width is in pixel, so try it!
                .clickable(true)
                .zIndex(Integer.MAX_VALUE);
        sourceMarker = mMap.addCircle(circleOptions);
        sourceMarker.setClickable(true);
        sourceMarker.setTag(sourceName);
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

}
