package com.app.ariadne.tumrfmap.listeners;

import android.content.Context;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.app.ariadne.tumrfmap.MapsActivity;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import static com.app.ariadne.tumrfmap.MapsActivity.isFirstTime;

//import static com.roomr.ariadne.tumnavigator.GeoJsonMap.startingPoint;
//import static com.roomr.ariadne.tumnavigator.MapsActivity.IS_INITIAL_POSITION_DEFINED;
//import static com.roomr.ariadne.tumnavigator.MapsActivity.initialUncertaintyRadius;
//import static com.roomr.ariadne.tumnavigator.MapsActivity.isLocalizationActivated;

public class MapLocationListener implements LocationListener {
    private GoogleMap mMap;
    private Context applicationContext;
    private Marker positionMarker;
//    private boolean isFirstTime = true;
    public static LatLng gpsLocation = null;
    public static double accuracy = Double.MAX_VALUE;
    public static double altitude = Double.MAX_VALUE;
    private MapsActivity mainActivity;

    public MapLocationListener(Context applicationContext, MapsActivity mainActivity) {
        System.out.println("MapLocationListerner constructor!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        this.applicationContext = applicationContext;
        this.mainActivity = mainActivity;
    }

    public void setMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        System.out.println("MapLocationListerner onLocationChanged, latitude: " + latitude + ", longitude: " + longitude);
        LatLng latLng = new LatLng(latitude, longitude);
        Geocoder geocoder = new Geocoder(applicationContext);
        accuracy = location.getAccuracy();
        if (accuracy < 100.0) {
            altitude = location.getAltitude();
        }
//        try {
//            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
//            String address = addressList.get(0).getLocality();
//            address += ", " + addressList.get(0).getCountryName();
//            if (positionMarker != null) {
//                positionMarker.remove();
//            }
//        if (isFirstTime && !IS_INITIAL_POSITION_DEFINED && !isLocalizationActivated) {
//            positionMarker = mMap.addMarker(new MarkerOptions().position(latLng));
//            isFirstTime = false;
//            initialUncertaintyRadius = location.getAccuracy();
//            Activity activity = (MapsActivity) applicationContext;
//            double prLat = 48.766709;
//            double prLng = 11.430921;
//            startingPoint = new LatLngWithTags(latLng);
//            startingPoint = new LatLngWithTags(prLat, prLng);
//            currentLatLngWithTags = startingPoint;
//            mainActivity.initLocationServices();

//        }

        gpsLocation = latLng;
        if (isFirstTime) {
            Log.i("LocationListener", "Got new location: " + gpsLocation.toString());
            Toast.makeText(mainActivity, "GPS location: " + gpsLocation.toString() + ", accuracy: " + accuracy
                    + ", altitude: " + altitude, Toast.LENGTH_SHORT).show();
            isFirstTime = false;
        }

//        gpsButton.setVisibility(Button.VISIBLE);

//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}