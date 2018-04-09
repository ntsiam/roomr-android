package com.app.ariadne.tumrfmap.geojson;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LatLngWithTags {
    private LatLng latlng;
    private String id;
    private String level;

    public LatLngWithTags(LatLng latLng, String level, String id) {
        this.latlng = latLng;
        this.level = level;
        this.id = id;
    }

    public LatLngWithTags(double latitude, double longitude) {
        this.latlng = new LatLng(latitude, longitude);
        this.level = "";
        this.id = this.latlng.toString();
    }

    public LatLngWithTags(double latitude, double longitude, String level) {
        this(latitude, longitude);
        this.level = level;
    }

    public LatLngWithTags(LatLng latLng) {
        this(latLng.latitude, latLng.longitude);
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public String getId() {
        return id;
    }

    public String getLevel() {
        return level;
    }

    public void setLatlng(LatLng location) {
        this.latlng = location;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String toString() {
        return "id: " + this.getId() + ", level: " + this.getLevel() + ", location: " + this.getLatlng().toString();
    }

    public static LatLngWithTags fromLatLngToLatLngWithTags(LatLng latLng) {
        return new LatLngWithTags(latLng, "", latLng.toString());
    }

    public static ArrayList<LatLngWithTags> fromLatLngToLatLngWithTagsArrayList(ArrayList<LatLng> latLngs, String level) {
        ArrayList<LatLngWithTags> latLngWithTags = new ArrayList<>();
        for (LatLng latLng : latLngs) {
            latLngWithTags.add(new LatLngWithTags(latLng, level, latLng.toString()));
        }
        return latLngWithTags;
    }

}
