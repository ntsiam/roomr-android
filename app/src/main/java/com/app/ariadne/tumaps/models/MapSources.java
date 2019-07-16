package com.app.ariadne.tumaps.models;

import com.app.ariadne.tumrfmap.R;

public class MapSources {
    private static final int[] defaultMapSources = {R.raw.mi, R.raw.path_all_mw, R.raw.mc, R.raw.mueldorf};
//    private static final int[] defaultMapSources = {R.raw.hypermotion};
    private int[] mapSources;

    public MapSources(int[] mapSources) {
        this.mapSources = mapSources;
    }

    public MapSources() {
        this.mapSources = defaultMapSources;
    }

    public int[] getMapSources() {
        return mapSources;
    }
}
