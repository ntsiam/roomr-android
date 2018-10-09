package com.app.ariadne.tumaps.geojson;

import com.google.android.gms.maps.model.LatLng;

public class BoundingBox {
    double maxLat;
    double maxLng;
    double minLat;
    double minLng;
    public BoundingBox(double maxLat, double maxLng, double minLat, double minLng) {
        this.maxLat = maxLat;
        this.maxLng = maxLng;
        this.minLat = minLat;
        this.minLng = minLng;
    }

    public BoundingBox(LatLng upperRight, LatLng lowerLeft) {
        this(upperRight.latitude, upperRight.longitude, lowerLeft.latitude, lowerLeft.longitude);
    }

    public double getMaxLat() { return maxLat; }
    public double getMaxLng() { return maxLng; }
    public double getMinLat() { return minLat; }
    public double getMinLng() { return minLng; }
}
