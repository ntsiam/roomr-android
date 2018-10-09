package com.app.ariadne.tumaps;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.geojson.LatLngWithTags;
import com.app.ariadne.tumaps.models.Entrance;
import com.app.ariadne.tumaps.models.MapSources;
import com.app.ariadne.tumrfmap.R;
import com.google.android.gms.maps.GoogleMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
//import static org.mockito.Mockito.mock;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GeoJsonMapInstrumentedTest {
    GoogleMap mMockGoogleMap;
    GeoJsonMap geoJsonMap;

    @Before
    public void setUp() {
        geoJsonMap = new GeoJsonMap(mMockGoogleMap);
        int [] myMapSources = { R.raw.test_map_source };
        MapSources mapSources = new MapSources(myMapSources);
        geoJsonMap.setMapSources(mapSources);
        Context appContext = InstrumentationRegistry.getTargetContext();

        // Parse geojson and load the topology
        geoJsonMap.loadIndoorTopology(appContext);
    }

    @Test
    public void givenSourceMap_whenGeoJsonIsParsed_thenTargetPointsAreFound() {
        // Context of the app under test.
        assertEquals(2, GeoJsonMap.targetPointsTagged.size());
        assertEquals(2, GeoJsonMap.targetPointsIds.size());
        assertEquals(2, GeoJsonMap.sourcePointsIds.size());
//        assertEquals(GeoJsonMap.targetPointsTagged.get(0).getId(), "03.06.060, Mathematics Informatics");
    }

    @Test
    public void givenSourceMap_whenGeoJsonIsParsed_thenTargetIsFound() {
        LatLngWithTags target = GeoJsonMap.findDestinationFromId("00.01.10");
        assertEquals("00.01.10", target.getId());

    }


    @Test
    public void givenSourceMap_whenGeoJsonIsParsed_thenEntranceForTargetIsFound() {
        LatLngWithTags target = GeoJsonMap.findDestinationFromId(GeoJsonMap.targetPointsTagged.get(0).getId());
        Entrance returnedEntrance = geoJsonMap.findEntranceForDestination(target);
        assertEquals("entrance", returnedEntrance.getEntranceLatLngWithTags().getId());
    }

    @Test
    public void givenSourceMap_whenGeoJsonIsParsed_thenRoutablePathIsCreated() {
        assertEquals(1, GeoJsonMap.routablePathForEachBuilding.size());
        assertEquals(1, GeoJsonMap.routablePathForEachBuilding.get(0).size());
        assertEquals(4, GeoJsonMap.routablePathForEachBuilding.get(0).get(0).size());
    }

}
