package com.app.ariadne.tumaps.listeners;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.geojson.LatLngWithTags;
import com.app.ariadne.tumaps.map.MapUIElementsManager;

public class ItemClickListener implements AdapterView.OnItemClickListener {
    private static final String TAG = "ItemClickListener";
    private Context context;
    private MapUIElementsManager mapUIElementsManager;
    private ButtonClickListener buttonClickListener;

    public ItemClickListener(Context context, MapUIElementsManager mapUIElementsManager, ButtonClickListener buttonClickListener) {
        this.context = context;
        this.mapUIElementsManager = mapUIElementsManager;
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//        Toast.makeText(this, "i = " + i + ", l = " + l + ", view: " + view.toString(), Toast.LENGTH_LONG).show();
        InputMethodManager in = (InputMethodManager) ((MapsActivity)(context)).getSystemService(Context.INPUT_METHOD_SERVICE); //Hide keyboard
        in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        LatLngWithTags destination = mapUIElementsManager.getDestination();
        mapUIElementsManager.addMarkerAndZoomCameraOnTarget(destination);
        buttonClickListener.showDestinationFoundButtons(destination.getId());
        mapUIElementsManager.addDestinationDescription(destination);
        int level = MapsActivity.findLevelFromId(mapUIElementsManager.target.getId());
        //Log.i(TAG, "Set floor as checked: " + level);
        buttonClickListener.setFloorAsChecked(level);

    }
}
