package com.app.ariadne.tumaps.util.analyzers;

import java.util.StringTokenizer;

public class MIDestinationIdAnalyzer implements BuildingDestinationIdAnalyzer {

    @Override
    public boolean isValidDestinationId(String id) {
        StringTokenizer multiTokenizer = new StringTokenizer(id, ".");
        int index = 0;
        while (multiTokenizer.hasMoreTokens()) {
            multiTokenizer.nextToken();

            index++;
            //Log.i(TAG, "MORE tokens");
        }
        return index == 3;
    }

    @Override
    public int getLevelFromId(String id) {
        return Integer.valueOf(id.substring(1,2));
    }
}
