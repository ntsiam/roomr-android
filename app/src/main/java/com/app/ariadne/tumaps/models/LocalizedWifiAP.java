package com.app.ariadne.tumaps.models;

import com.app.ariadne.tumaps.db.models.WifiAPDetails;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.IgnoreExtraProperties;

import java.security.MessageDigest;
import java.util.ArrayList;

@IgnoreExtraProperties
public class LocalizedWifiAP {
    private ArrayList<WifiAPDetails> wifiAPDetailsArrayList;
//    private LatLng location;
    private double lat;
    private double lng;
    private double climbingStairsProbability;
    private long timestamp;

    public LocalizedWifiAP(MessageDigest md, ArrayList<WifiAPDetails> wifiAPDetails, LatLng location, long timestamp) {
        this.wifiAPDetailsArrayList = wifiAPDetails;
//        this.location = location;
        this.lat = location.latitude;
        this.lng = location.longitude;
        this.timestamp = timestamp;
    }

    LocalizedWifiAP(MessageDigest md, ArrayList<WifiAPDetails> wifiAPDetails, LatLng location, long timestamp, double climbingStairsProbability) {
        this(md, wifiAPDetails, location, timestamp);
        this.climbingStairsProbability = climbingStairsProbability;
    }

//    LatLng getLocation() {
//        return location;
//    }

    public ArrayList<WifiAPDetails> getWifiAPDetailsArrayList() {
        return wifiAPDetailsArrayList;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return  lng;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getClimbingStairsProbability() {
        return climbingStairsProbability;
    }

    public void setLat(double lat) { this.lat = lat; }

    public void setLng(double lng) { this.lng = lng; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public void setClimbingStairsProbability(double climbingStairsProbability) { this.climbingStairsProbability = climbingStairsProbability; }

    @Override
    public String toString() {
//        return (location.latitude + "," + location.longitude).replace(".",":");
        return (getLat() + "," + getLng()).replace(".",":");
    }
}
