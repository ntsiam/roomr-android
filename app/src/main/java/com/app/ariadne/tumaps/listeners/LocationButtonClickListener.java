package com.app.ariadne.tumaps.listeners;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;
import com.app.ariadne.tumaps.MapsActivity;
import com.google.android.gms.maps.GoogleMap;

import static android.content.Context.LOCATION_SERVICE;
import static com.app.ariadne.tumaps.MapsActivity.isFirstTime;

public class LocationButtonClickListener implements GoogleMap.OnMyLocationButtonClickListener {
    Context context;
    LocationManager locationManager;
//    MapManager mapManager;
    MapLocationListener mapLocationListener;
//    MapUIElementsManager mapUIElementsManager;
    GoogleMap mMap;
    private static final String TAG = "LocationButtonListener";

//    public LocationButtonClickListener(MapManager mapManager, Context context, MapUIElementsManager mapUIElementsManager) {
//        this.context = context;
//        this.mapUIElementsManager = mapUIElementsManager;
//    }

    public LocationButtonClickListener(Context context, GoogleMap mMap) {
        this.context = context;
        this.mMap = mMap;
    }


    @Override
    public boolean onMyLocationButtonClick() {
        initLocationUpdates();
        return false;
    }

    private void initLocationUpdates() {
        locationManager = (LocationManager) ((MapsActivity)(this.context)).getSystemService(LOCATION_SERVICE);
        if (isFirstTime) {
            setLocationUpdates(locationManager); // Used for the GPS/Network localization
        }
    }

    private void setLocationUpdates(LocationManager locationManager) {
        mapLocationListener = new MapLocationListener(((MapsActivity)(this.context)).getApplicationContext(), ((MapsActivity)(this.context)));
        mapLocationListener.setMap(mMap);
        String provider;
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //Log.i(TAG, "Provider: Network Provider");
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Log.i(TAG, "Provider: GPS Provider");
            provider = LocationManager.GPS_PROVIDER;
        } else {
            provider = LocationManager.PASSIVE_PROVIDER;
            //Log.i(TAG, "Provider: Passive Provider");
        }
        //Log.i(TAG, "onCreate");
        if (ActivityCompat.checkSelfPermission(this.context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(provider, 0, 0, mapLocationListener);
    }


}
