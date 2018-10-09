package com.app.ariadne.tumaps.geojson;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LatLngWithTags {
    private LatLng latlng;
    private String id;
    private String level;
    private String buildingId;

    public LatLngWithTags(LatLng latLng, String level, String id) {
        this.latlng = latLng;
        this.level = level;
        this.id = id;
    }

    public LatLngWithTags(LatLng latLng, String level, String id, String buildingId) {
        this(latLng, level, id);
        this.buildingId = buildingId;
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

    public String getBuildingId() {
        return buildingId;
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

    public void setBuildingId(String buildingId) {
        this.buildingId = buildingId;
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

    @Override
    public final boolean equals(Object var1) {
        if (this == var1) {
            return true;
        } else if (!(var1 instanceof LatLngWithTags)) {
            return false;
        } else {
            LatLngWithTags var2 = (LatLngWithTags) var1;
            if (var2.getBuildingId() != null && this.getBuildingId() != null) {
                if (var2.getLevel() != null && this.getLevel() != null) {
                    return Double.doubleToLongBits(this.getLatlng().latitude) == Double.doubleToLongBits(var2.getLatlng().latitude)
                            && Double.doubleToLongBits(this.getLatlng().longitude) == Double.doubleToLongBits(var2.getLatlng().longitude)
                            && this.getId().equals(var2.getId()) && this.getBuildingId().equals(var2.getBuildingId())
                            && this.getLevel().equals(var2.getLevel());
                }
            }
            return false;
        }
    }


}
