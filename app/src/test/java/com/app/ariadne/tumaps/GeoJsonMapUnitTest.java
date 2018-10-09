package com.app.ariadne.tumaps;

import android.content.Context;

import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.geojson.LatLngWithTags;
import com.app.ariadne.tumaps.models.Entrance;
import com.app.ariadne.tumaps.models.MapSources;
import com.app.ariadne.tumrfmap.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class GeoJsonMapUnitTest {
    private GeoJsonMap geoJsonMap;
    private MapSources mapSources;
    private int[] testMapSources = {R.raw.test_map_source};
    LatLngWithTags target;
    GoogleMap mMockGoogleMap;

    @Before
    public void setUp() {
//        mMockGoogleMap = mock(GoogleMap.class);
        geoJsonMap = new GeoJsonMap(mMockGoogleMap);
//        mapSources = new MapSources(testMapSources);
//        geoJsonMap.setMapSources(mapSources);
        GeoJsonMap.targetPointsTagged = new ArrayList<>();
        target = new LatLngWithTags(new LatLng(41.05, 11.04), "0", "00.11.013", "mi");
    }

    @Test
    public void givenNullAppContext_whenLoadIndoorTopologyIsCalled_thenExceptionIsCaught() {

//        assertEquals(4, 2 + 2);
    }


    @Test
    public void givenEmptyDestinationList_whenFindDestinationFromIdIsCalled_thenNullIsReturned() {
        LatLngWithTags destination = GeoJsonMap.findDestinationFromId("00");
        assertEquals(null, destination);
    }

    @Test
    public void givenDestinationList_whenFindDestinationFromIdIsCalled_thenDestinationIsReturned() {
        GeoJsonMap.targetPointsTagged.add(new LatLngWithTags(new LatLng(41.04, 11.03), "0", "00.11.012"));
        GeoJsonMap.targetPointsTagged.add(target);
        GeoJsonMap.targetPointsTagged.add(new LatLngWithTags(new LatLng(41.06, 11.05), "0", "00.11.014"));
        LatLngWithTags destination = GeoJsonMap.findDestinationFromId("00.11.013");
        System.out.println("Destination: " + destination.getId());
        assertEquals(target, destination);
    }

    @Test
    public void givenEntranceForBuilding_whenFindEntranceForDestinationIsCalled_thenEntranceIsReturned() {
        LatLngWithTags entranceLatLngWithTags = new LatLngWithTags(new LatLng(41.05, 11.04), "0", "entrance");
        Entrance entrance = new Entrance("Bolzmanstr 3", "Mathematics Informatics", "Garching", "0000", entranceLatLngWithTags);
        geoJsonMap.addEntranceToEntranceHashMap("mi", entrance);
        Entrance returnedEntrance = geoJsonMap.findEntranceForDestination(target);
        assertEquals(returnedEntrance, entrance);
    }

}