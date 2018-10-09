package com.app.ariadne.tumaps.models;

import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class Route {
    private HashMap<Integer, ArrayList<PolylineOptions>> routeHashMapForLevels;
    private int maxRouteLevel;
    private int minRouteLevel;
    private int sourceLevel;
    private HashMap<Integer, ArrayList<MarkerOptions>> stairMarkers;

    public Route(HashMap<Integer, ArrayList<PolylineOptions>> routeHashMapForLevels, int maxRouteLevel, int minRouteLevel, int sourceLevel,
                 HashMap<Integer, ArrayList<MarkerOptions>> stairMarkers) {
        this.routeHashMapForLevels = routeHashMapForLevels;
        this.maxRouteLevel = maxRouteLevel;
        this.minRouteLevel = minRouteLevel;
        this.sourceLevel = sourceLevel;
        this.stairMarkers = stairMarkers;
    }

    public void setMaxRouteLevel(int maxRouteLevel) {
        this.maxRouteLevel = maxRouteLevel;
    }

    public void setMinRouteLevel(int minRouteLevel) {
        this.minRouteLevel = minRouteLevel;
    }

    public void setRouteHashMapForLevels(HashMap<Integer, ArrayList<PolylineOptions>> routeHashMapForLevels) {
        this.routeHashMapForLevels = routeHashMapForLevels;
    }

    public void setSourceLevel(int sourceLevel) {
        this.sourceLevel = sourceLevel;
    }

    public HashMap<Integer, ArrayList<PolylineOptions>> getRouteHashMapForLevels() {
        return routeHashMapForLevels;
    }

    public int getMaxRouteLevel() {
        return maxRouteLevel;
    }

    public int getMinRouteLevel() {
        return minRouteLevel;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }

    public HashMap<Integer, ArrayList<MarkerOptions>> getStairMarkers() {
        return stairMarkers;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}

