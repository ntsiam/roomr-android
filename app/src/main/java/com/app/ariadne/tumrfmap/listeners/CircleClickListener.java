package com.app.ariadne.tumrfmap.listeners;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Circle;

public class CircleClickListener implements GoogleMap.OnCircleClickListener {
    private static final String TAG = "CircleClickListener";

    @Override
    public void onCircleClick(Circle circle) {
        Log.i(TAG, "Clicked on circle: " + circle.getTag());

    }
}
