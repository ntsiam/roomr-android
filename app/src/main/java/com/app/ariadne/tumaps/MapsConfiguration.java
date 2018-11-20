package com.app.ariadne.tumaps;

import com.app.ariadne.tumaps.geojson.BoundingBox;
import com.app.ariadne.tumaps.geojson.IndoorBuildingBoundsAndFloors;
import com.app.ariadne.tumaps.util.analyzers.MIDestinationIdAnalyzer;
import com.app.ariadne.tumaps.util.analyzers.MWDestinationIdAnalyzer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

public class MapsConfiguration {
    private static final MapsConfiguration ourInstance = new MapsConfiguration();

    private static final BoundingBox MASCHINENWESEN_BB = new BoundingBox(new LatLng(48.266825, 11.671324), new LatLng(48.264547, 11.667344));
    private static final BoundingBox MAIN_CAMPUS_BB = new BoundingBox(new LatLng(48.151462, 11.570093), new LatLng(48.147561, 11.565581));
    private static final BoundingBox MUENCHNER_FREIHEIT_BB = new BoundingBox(new LatLng(48.162697, 11.587385), new LatLng(48.160468, 11.584937));
    private static final BoundingBox HAUPTBAHNHOF_BB = new BoundingBox(new LatLng(48.143000, 11.565000), new LatLng(48.137300, 11.554600));
    private static final BoundingBox GARCHING_MI_BB = new BoundingBox(new LatLng(48.2636, 11.6705), new LatLng(48.2613, 11.6653));
    private static final BoundingBox HYPERMOTION_BB = new BoundingBox(new LatLng(50.113844, 8.650535), new LatLng(50.111545, 8.643418));
    private static final IndoorBuildingBoundsAndFloors MAIN_CAMPUS = new IndoorBuildingBoundsAndFloors(MAIN_CAMPUS_BB, 0, 5);
    private static final IndoorBuildingBoundsAndFloors MASCHINENWESEN = new IndoorBuildingBoundsAndFloors(MASCHINENWESEN_BB, 0, 3);
    private static final IndoorBuildingBoundsAndFloors MUENCHNER_FREIHEIT = new IndoorBuildingBoundsAndFloors(MUENCHNER_FREIHEIT_BB, -2, 0);
    private static final IndoorBuildingBoundsAndFloors HAUPTBAHNHOF = new IndoorBuildingBoundsAndFloors(HAUPTBAHNHOF_BB, -4, 0);
    private static final IndoorBuildingBoundsAndFloors GARCHING_MI = new IndoorBuildingBoundsAndFloors(GARCHING_MI_BB, 0, 3);
    private static final IndoorBuildingBoundsAndFloors HYPERMOTION = new IndoorBuildingBoundsAndFloors(HYPERMOTION_BB, 0, 0);


    private static final IndoorBuildingBoundsAndFloors[] BOUNDS_FOR_INDOOR_BUTTONS = new IndoorBuildingBoundsAndFloors[] {
            MUENCHNER_FREIHEIT, HAUPTBAHNHOF, GARCHING_MI, MAIN_CAMPUS, MASCHINENWESEN, HYPERMOTION
    };

    private MIDestinationIdAnalyzer miDestinationIdAnalyzer;
    private MWDestinationIdAnalyzer mwDestinationIdAnalyzer;
//    private final LatLng MAP_CENTER = new LatLng(48.262534, 11.667992);
    private final LatLng MAP_CENTER = new LatLng(50.11241390288, 8.64781737294);
    private final LatLngBounds MAP_BOUNDS = new LatLngBounds(
            new LatLng(48.5361, 8.443418), new LatLng(52.113844, 12.062));
    //            new LatLng(47.8173, 11.063), new LatLng(48.5361, 12.062));
    private final int INITIAL_ZOOM = 14;
    private final int MIN_ZOOM_FOR_INDOOR_MAPS = 15;

    public static MapsConfiguration getInstance() {
        return ourInstance;
    }

    private MapsConfiguration() {
        miDestinationIdAnalyzer = new MIDestinationIdAnalyzer();
        mwDestinationIdAnalyzer = new MWDestinationIdAnalyzer();
    }

    public IndoorBuildingBoundsAndFloors[] getBoundsForIndoorButtons() {
        return BOUNDS_FOR_INDOOR_BUTTONS;
    }

    public LatLng getMapCenter() {
        return MAP_CENTER;
    }

    public int getInitialZoom() {
        return INITIAL_ZOOM;
    }

    public LatLngBounds getMapBounds() {
        return MAP_BOUNDS;
    }

    public int getMinZoomForIndoorMaps() { return MIN_ZOOM_FOR_INDOOR_MAPS; }

    /**
     * This method needs to be updated for every new building that follows a new naming pattern.
     * It is assumed that the level of the destination is encoded in its name.
     * @param id
     * @return
     */
    public int getLevelFromId(String id) {
        int level = 0;
        if (miDestinationIdAnalyzer.isValidDestinationId(id)) {
            level = miDestinationIdAnalyzer.getLevelFromId(id);
        } else if (mwDestinationIdAnalyzer.isValidDestinationId(id)) {
            level = mwDestinationIdAnalyzer.getLevelFromId(id);
        } else {
            // Fill in code for additional buildings
        }
        return level;
    }
}
