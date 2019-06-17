package com.app.ariadne.tumaps.listeners;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import static com.app.ariadne.tumaps.MapsActivity.isStreamingToTangleEnabled;
import java.util.ArrayList;

public class ButtonClickListener implements View.OnClickListener, GoogleMap.OnCameraIdleListener {
    private MapUIElementsManager mapUIElementsManager;
    private Context context;
    private ToggleButton level4;
    private ToggleButton level3;
    private ToggleButton level2;
    private ToggleButton level1;
    private ToggleButton level0;
    private ToggleButton leveln1;
    private ToggleButton leveln2;
    private ToggleButton leveln3;
    private ToggleButton leveln4;
    private ToggleButton iota;
    private ArrayList<ToggleButton> floorButtonList;
    private Button directionsButton;
    private Button localizationButton;
    private ImageButton revertButton;
    private EditText destinationEditText;
    private MapManager mapManager;

    private static IndoorBuildingBoundsAndFloors[] boundsForIndoorButtons;

    private static final String TAG = "ButtonClickListener";

    public ButtonClickListener(Context context, MapManager mapManager) {
        this.mapManager = mapManager;
        this.context = context;
        initFloorButtonList();
        MapsActivity mapsActivity = (MapsActivity)context;
        directionsButton = mapsActivity.findViewById(R.id.directions);
        localizationButton = mapsActivity.findViewById(R.id.localization);
        revertButton = mapsActivity.findViewById(R.id.revert);
        directionsButton.setOnClickListener(this);
        localizationButton.setOnClickListener(this);
        revertButton.setOnClickListener(this);
        destinationEditText = mapsActivity.findViewById(R.id.findDestination);
        boundsForIndoorButtons = MapsConfiguration.getInstance().getBoundsForIndoorButtons();
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

    private void clickFloor(int requestedLevel) {
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
                    if (mapUIElementsManager.getRoute() != null) {
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
                    }
                } else {
                    if (mapUIElementsManager.getRoute() != null) {
//                        int polyLineIndex = mapUIElementsManager.findPolyLineIndex(requestedLevel);
                        mapUIElementsManager.addRouteLineFromPolyLineOptions(requestedLevel);
                    }
                }
            }
            currentIndexInButtonList++;
        }
        mapUIElementsManager.setLevel(level);
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
        iota = mapsActivity.findViewById(R.id.streamToggleButton);
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
        iota.setOnClickListener(this);
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

    private void setButtonVisibilityForSingleLocation(IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors) {
        LatLng cameraPosition = mapUIElementsManager.getCameraPosition();
        BoundingBox boundingBox = indoorBuildingBoundsAndFloors.getBoundingBox();
        if (isCameraPositionInBoundingBox(cameraPosition, boundingBox)) {
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


    private boolean isCameraPositionInBoundingBox(LatLng cameraPosition, BoundingBox boundingBox) {
        int minZoom = MapsConfiguration.getInstance().getMinZoomForIndoorMaps();
        double maxLat = boundingBox.getMaxLat();
        double maxLng = boundingBox.getMaxLng();
        double minLat = boundingBox.getMinLat();
        double minLng = boundingBox.getMinLng();
        return cameraPosition.latitude < maxLat && cameraPosition.latitude > minLat && cameraPosition.longitude < maxLng &&
                cameraPosition.longitude > minLng && mapUIElementsManager.getZoomLevel() > minZoom;
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
        } else if (view.getId() == R.id.localization) {
            mapManager.requestLocalizationWifi();
        } else if (view.getId() == R.id.revert) {
            ((MapsActivity)(context)).onFindOriginDestinationClick();
            mapUIElementsManager.removeAllDestinationElementsFromMap();
        } else if (view.getId() == R.id.streamToggleButton) {
            if (iota.isChecked()) {
                isStreamingToTangleEnabled = true;
            } else {
                isStreamingToTangleEnabled = false;
                ((MapsActivity)(context)).endStreamingToTangle();
            }
        } else if (view.getId() == R.id.targetDescriptionLayout) {
//            LinearLayout descriptionLayout = findViewById(R.id.targetDescriptionLayout);
//            descriptionLayout.setOnClickListener(null);
//            ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
//            params.height = 200;
//            descriptionLayout.setLayoutParams(params);

        }
        if (view.getId() != R.id.localization && view.getId() != R.id.revert) {
            mapManager.addTileProvider(mapUIElementsManager.level);
        }

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
            for (IndoorBuildingBoundsAndFloors indoorBuildingBoundsAndFloors : boundsForIndoorButtons) {
                setButtonVisibilityForSingleLocation(indoorBuildingBoundsAndFloors);
            }
        }
    }


    @Override
    public void onCameraIdle() {
        setButtonVisibilityBasedOnCameraPosition();
    }
}
