package com.app.ariadne.tumaps.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import com.app.ariadne.tumaps.geojson.GeoJsonMap;

public class EditTextWatcher implements TextWatcher {
    ArrayAdapter adapterForDestination;

    public EditTextWatcher(ArrayAdapter adapterForDestination) {
        this.adapterForDestination = adapterForDestination;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
//        Log.i("SECOND", "Text after change: " + editable.toString());
        adapterForDestination.clear();
        adapterForDestination.notifyDataSetChanged();
//        Log.i("SECOND", "Source point size original: " + GeoJsonMap.sourcePointsIds.size());
//            Log.i("SECOND", "Source point size: " + ids.size());
        for (String id: GeoJsonMap.sourcePointsIds) {
            if (id.contains(editable.toString())) {
//                Log.i("SECOND", "id: " + id);
                adapterForDestination.add(id);
//            } else {
//                Log.i("SECOND","id: " + id + ", does not contain string: " + editable.toString());
            }
        }
//        Log.i("SECOND", "Adapter size: " + adapterForDestination.getCount());
        adapterForDestination.notifyDataSetChanged();
    }
}
