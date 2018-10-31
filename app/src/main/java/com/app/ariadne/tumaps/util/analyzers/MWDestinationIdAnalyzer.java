package com.app.ariadne.tumaps.util.analyzers;

import com.app.ariadne.tumaps.geojson.GeoJsonMap;

import java.util.StringTokenizer;

public class MWDestinationIdAnalyzer implements BuildingDestinationIdAnalyzer {
    @Override
    public boolean isValidDestinationId(String id) {
        String name = GeoJsonMap.getRoomIdFromBuildingName(id);
        StringTokenizer multiTokenizer = new StringTokenizer(name, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            //Log.i(TAG, "MORE tokens");
        }
        return index == 1 && isInteger(name);
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }


    @Override
    public int getLevelFromId(String id) {
        return Integer.valueOf(id.substring(0,1));
    }
}
