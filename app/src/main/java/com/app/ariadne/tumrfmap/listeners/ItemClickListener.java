package com.app.ariadne.tumrfmap.listeners;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.app.ariadne.tumrfmap.MapsActivity;
import com.app.ariadne.tumrfmap.geojson.LatLngWithTags;
import com.app.ariadne.tumrfmap.map.MapUIElementsManager;

import static com.app.ariadne.tumrfmap.MapsActivity.findLevelFromId;

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
        mapUIElementsManager.addDestinationDescription();
        int level = findLevelFromId(mapUIElementsManager.target.getId());
        Log.i(TAG, "Set floor as checked: " + level);
        buttonClickListener.setFloorAsChecked(level);

    }
}
