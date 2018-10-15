package com.app.ariadne.tumaps.listeners;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.ToggleButton;

import com.app.ariadne.tumaps.MapsConfiguration;
import com.app.ariadne.tumaps.geojson.IndoorBuildingBoundsAndFloors;
import com.app.ariadne.tumaps.map.MapManager;
import com.app.ariadne.tumaps.map.MapUIElementsManager;
import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumaps.geojson.BoundingBox;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class ButtonClickListener implements View.OnClickListener, GoogleMap.OnCameraIdleListener {
    MapUIElementsManager mapUIElementsManager;
    Context context;
    ToggleButton level4;
    ToggleButton level3;
    ToggleButton level2;
    ToggleButton level1;
    ToggleButton level0;
    ToggleButton leveln1;
    ToggleButton leveln2;
    ToggleButton leveln3;
    ToggleButton leveln4;
    public ArrayList<ToggleButton> floorButtonList;
    Button directionsButton;
    ImageButton revertButton;
    EditText destinationEditText;
    MapManager mapManager;
    //Maschinenwesen min: 48.264547, 11.667344, max: 48.266825, 11.671324
    //Main campus: 48.147561, 11.565581, 48.151462, 11.570093

    private static IndoorBuildingBoundsAndFloors[] BOUNDS_FOR_INDOOR_BUTTONS;

    private static final String TAG = "ButtonClickListener";

    public ButtonClickListener(Context context, MapManager mapManager) {
        this.mapManager = mapManager;
        this.context = context;
        initFloorButtonList();
        MapsActivity mapsActivity = (MapsActivity)context;
        directionsButton = mapsActivity.findViewById(R.id.directions);
        revertButton = mapsActivity.findViewById(R.id.revert);
        directionsButton.setOnClickListener(this);
        revertButton.setOnClickListener(this);
        destinationEditText = mapsActivity.findViewById(R.id.findDestination);
        BOUNDS_FOR_INDOOR_BUTTONS = MapsConfiguration.getInstance().getBoundsForIndoorButtons();
    }

    public void setMapUIElementsManager(MapUIElementsManager mapUIElementsManager) {
        this.mapUIElementsManager = mapUIElementsManager;
    }

    public void setFloorAsChecked(int level) {
        int indexOfLevelInButtonList = MapUIElementsManager.MAX_FLOOR - level;
        floorButtonList.get(indexOfLevelInButtonList).setChecked(true);
        mapManager.addTileProvider(String.valueOf(level));
        clickFloor(level);
    }

    public void clickFloor(int requestedLevel) {
        String level;
        level = String.valueOf(requestedLevel);
        //Log.i(TAG, "Requested level: " + level);
        level = level.replace("-", "n");
        Log.i(TAG, "Requested level after replace: " + level);
        int indexOfLevelInButtonList = MapUIElementsManager.MAX_FLOOR - requestedLevel;
        int currentIndexInButtonList = 0;
        for (ToggleButton levelButton: floorButtonList) {
            if (currentIndexInButtonList != indexOfLevelInButtonList) {
                levelButton.setChecked(false);
            } else {
                if (!levelButton.isChecked()) {
                    //Log.i(TAG, "Entered here");
                    level = ""; //no tiles for ""
                    if (mapUIElementsManager.route != null) {
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
                    }
                } else {
                    if (mapUIElementsManager.route != null) {
//                        int polyLineIndex = mapUIElementsManager.findPolyLineIndex(requestedLevel);
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(requestedLevel);
                    }
                }
            }
            currentIndexInButtonList++;
        }
        mapUIElementsManager.level = level;
        if (!level.equals("")) {
            mapUIElementsManager.addRouteMarkers(requestedLevel);
        }
        mapManager.addTileProvider(level);
    }

    private void initFloorButtonList() {
        MapsActivity mapsActivity = (MapsActivity)context;
        level4 = mapsActivity.findViewById(R.id.button4);
        level3 = mapsActivity.findViewById(R.id.button3);
        level2 = mapsActivity.findViewById(R.id.button2);
        level1 = mapsActivity.findViewById(R.id.button1);
        level0 = mapsActivity.findViewById(R.id.button0);
        leveln1 = mapsActivity.findViewById(R.id.buttonn1);
        leveln2 = mapsActivity.findViewById(R.id.buttonn2);
        leveln3 = mapsActivity.findViewById(R.id.buttonn3);
        leveln4 = mapsActivity.findViewById(R.id.buttonn4);
        floorButtonList = new ArrayList<>();
        floorButtonList.add(level4);
        floorButtonList.add(level3);
        floorButtonList.add(level2);
        floorButtonList.add(level1);
        floorButtonList.add(level0);
        floorButtonList.add(leveln1);
        floorButtonList.add(leveln2);
        floorButtonList.add(leveln3);
        floorButtonList.add(leveln4);
        for (ToggleButton button: floorButtonList) {
            button.setOnClickListener(this);
        }
    }

    public void removeButtons() {
        directionsButton.setVisibility(Button.GONE);
        revertButton.setVisibility(ImageButton.GONE);
        destinationEditText.setText("");
        ImageButton cancelButton = ((MapsActivity)(context)).findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.INVISIBLE);

    }

    public void showDirectionsButtons() {
        directionsButton.setVisibility(Button.GONE);
        revertButton.setVisibility(ImageButton.VISIBLE);
    }

    public void showDestinationFoundButtons(String destinationId) {
        if (destinationId != null && !destinationId.equals("")) {
            destinationEditText.setText(destinationId);
        }
        ImageButton cancelButton = ((MapsActivity)(context)).findViewById(R.id.cancel_button);
        cancelButton.setVisibility(Button.VISIBLE);
        revertButton.setVisibility(ImageButton.GONE);
        directionsButton.setVisibility(Button.VISIBLE);
    }

    public void setButtonVisibilityForSingleLocation(IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors) {
        int minZoom = 15;
        LatLng cameraPosition = mapUIElementsManager.mMap.getCameraPosition().target;
        BoundingBox boundingBox = indoorBuildingBoundsAndFloors.getBoundingBox();
        double maxLat = boundingBox.getMaxLat();
        double maxLng = boundingBox.getMaxLng();
        double minLat = boundingBox.getMinLat();
        double minLng = boundingBox.getMinLng();
        ScrollView sv = (ScrollView)((MapsActivity)(context)).findViewById(R.id.scrollViewButtons);
        if (cameraPosition.latitude < maxLat && cameraPosition.latitude > minLat && cameraPosition.longitude < maxLng &&
                cameraPosition.longitude > minLng && mapUIElementsManager.mMap.getCameraPosition().zoom > minZoom) {
            int floor = MapUIElementsManager.MAX_FLOOR;
            for (ToggleButton button: floorButtonList) {
                // Here set the corresponding floor button to visible, if the current map includes a layer for the button's floor
                // The first item in the button list refers to the minimum floor
                if (indoorBuildingBoundsAndFloors.getMinFloor() <= floor && indoorBuildingBoundsAndFloors.getMaxFloor() >= floor) {
                    button.setVisibility(ToggleButton.VISIBLE);
                }
                floor--;
            }
        }
    }



    @Override
    public void onClick(View view) {
        //Log.i(TAG, "onClick");
        if (view.getId() == R.id.button4) {
            clickFloor(4);
        } else if (view.getId() == R.id.button3) {
                clickFloor(3);
        } else if (view.getId() == R.id.button2) {
            clickFloor(2);
        } else if (view.getId() == R.id.button1) {
            clickFloor(1);
        } else if (view.getId() == R.id.button0) {
            clickFloor(0);
        } else if (view.getId() == R.id.buttonn1) {
            clickFloor(-1);
        } else if (view.getId() == R.id.buttonn2) {
            clickFloor(-2);
        } else if (view.getId() == R.id.buttonn3) {
            clickFloor(-3);
        } else if (view.getId() == R.id.buttonn4) {
            clickFloor(-4);
        } else if (view.getId() == R.id.directions) {
            ((MapsActivity)(context)).onFindOriginDestinationClick();
        } else if (view.getId() == R.id.revert) {
            ((MapsActivity)(context)).onFindOriginDestinationClick();
        } else if (view.getId() == R.id.targetDescriptionLayout) {
//            LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//            descriptionLayout.setOnClickListener(null);
//            ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
//            params.height = 200;
//            descriptionLayout.setLayoutParams(params);

        }
        mapManager.addTileProvider(mapUIElementsManager.level);

    }

    private void setButtonVisibilityBasedOnCameraPosition() {
        level4.setVisibility(Button.GONE);
        level3.setVisibility(Button.GONE);
        level2.setVisibility(Button.GONE);
        level1.setVisibility(Button.GONE);
        level0.setVisibility(Button.GONE);
        leveln1.setVisibility(Button.GONE);
        leveln2.setVisibility(Button.GONE);
        leveln3.setVisibility(Button.GONE);
        leveln4.setVisibility(Button.GONE);
        if (mapUIElementsManager != null) {
            for (IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors : BOUNDS_FOR_INDOOR_BUTTONS) {
                setButtonVisibilityForSingleLocation(indoorBuildingBoundsAndFloors);
            }
        }
    }


    @Override
    public void onCameraIdle() {
        setButtonVisibilityBasedOnCameraPosition();
    }
}
