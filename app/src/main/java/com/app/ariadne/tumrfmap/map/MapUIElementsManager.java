package com.app.ariadne.tumrfmap.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.app.ariadne.tumrfmap.MapsActivity;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumrfmap.geojson.LatLngWithTags;
import com.app.ariadne.tumrfmap.listeners.ButtonClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import static com.app.ariadne.tumrfmap.MapsActivity.findLevelFromId;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonHelper.ListToArrayList;
import static com.app.ariadne.tumrfmap.geojson.GeoJsonMap.findDestinationFromId;

public class MapUIElementsManager {
    public ArrayList<PolylineOptions> routePolylineOptionsInLevels;
    public PolylineOptions routePolylineOptions;
    public PolylineOptions routePolylineOptionsGray;
    Context context;
    private static final String TAG = "MAPUIElements";
    public String level;
    private final int MIN_FLOOR = -4;
    public static final int MAX_FLOOR = 3;
    public String sourceName;
    public String targetName;
    public Circle sourceMarker;
    public GoogleMap mMap;
    public Marker destinationMarker;
    public LatLngWithTags target;
    public LatLngWithTags source;
    public int sourceLevel;
    public int destinationLevel;
    public ArrayList<Polyline> routeLines;
    ButtonClickListener buttonClickListener;


    public MapUIElementsManager(Context context, ButtonClickListener buttonClickListener, GoogleMap mMap) {
        this.context = context;
        this.buttonClickListener = buttonClickListener;
        this.mMap = mMap;
    }

    public void managePolylineOptions(int requestedLevel) {
        int indexOfLevelInButtonList = MAX_FLOOR - requestedLevel;
        int currentIndexInButtonList = 0;
        for (ToggleButton levelButton: buttonClickListener.floorButtonList) {
            if (currentIndexInButtonList != indexOfLevelInButtonList) {
                levelButton.setChecked(false);
            } else {
                if (!levelButton.isChecked()) {
                    Log.i(TAG, "Entered here");
                    level = ""; //no tiles for "0" - ground floor is ""
                    if (routePolylineOptionsInLevels != null) {
                        addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
                    }
                } else {
                    if (routePolylineOptionsInLevels != null) {
                        int polyLineIndex = findPolyLineIndex(requestedLevel);
                        addRouteLineFromPolyLineOptions(polyLineIndex);
                    }
                }
            }
            currentIndexInButtonList++;
        }

    }

    public void setDestinationOnMap(String destinationId) {
        target = findDestinationFromId(destinationId);
        if (target != null) {
//            destinationEditText = findViewById(R.id.findDestination);
            addMarkerAndZoomCameraOnTarget(target);
            ((MapsActivity)(context)).addDestinationDescription();
            buttonClickListener.showDestinationFoundButtons(destinationId);

            int level = findLevelFromId(target.getId());
            Log.i(TAG, "Set floor as checked: " + level);
            buttonClickListener.setFloorAsChecked(level);
        }
    }

    public void addMarkerAndZoomCameraOnTarget(LatLngWithTags target) {
        removeDestinationMarker();
        Projection projection = mMap.getProjection();
        Point mapPoint = projection.toScreenLocation(target.getLatlng());
        destinationMarker = mMap.addMarker(new MarkerOptions().position(target.getLatlng())
                .title(target.getId()));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(target.getLatlng(),
                18, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing)));
    }



    public void addRouteLineFromPolyLineOptions(int index) {
        removeRouteLine();
        int currentIndex = 0;
        for (PolylineOptions polylineOptions: routePolylineOptionsInLevels) {
            addPathToPolylineOptionsGray(index, currentIndex, polylineOptions);
            currentIndex++;
        }
        if (index != Integer.MIN_VALUE) {
            addRouteLine(routePolylineOptionsInLevels.get(index));
            routePolylineOptionsGray.clickable(true);
        }
        removeSourceCircle();
        addSourceCircle();
    }

    private void addPathToPolylineOptionsGray(int index, int currentIndex, PolylineOptions polylineOptions) {
        routePolylineOptionsGray = new PolylineOptions().width(20).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 20);
        if (currentIndex != index) {
            addPointsToPolyLineOptionsGray(polylineOptions);
        }
        addRouteLine(routePolylineOptionsGray);
        routePolylineOptionsGray.clickable(true);
    }

    private void addPointsToPolyLineOptionsGray(PolylineOptions polylineOptions) {
        for (LatLng point: polylineOptions.getPoints()) {
            routePolylineOptionsGray.add(point);
        }
    }


    public void removeSourceCircle() {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }
    }

    public void addSourceCircle() {
        CircleOptions circleOptions = new CircleOptions()
                .center(source.getLatlng())
                .radius(1.0) // radius in meters
                .fillColor(0xBB00CCFF) //this is a half transparent blue, change "88" for the transparency
                .strokeColor(Color.BLUE) //The stroke (border) is blue
                .strokeWidth(2) // The width is in pixel, so try it!
                .clickable(true)
                .zIndex(Integer.MAX_VALUE);
        sourceMarker = mMap.addCircle(circleOptions);
        sourceMarker.setClickable(true);
        sourceMarker.setTag(sourceName);
    }

    public int findPolyLineIndex(int level) {
        int index = Integer.MIN_VALUE;
        if (sourceLevel == level) {
            index = 0;
        } else if (destinationLevel == level) {
            index = routePolylineOptionsInLevels.size() - 1;
        } else if (sourceLevel < level && destinationLevel > level) {
            index = Math.abs(level - sourceLevel);
        }
        Log.i(TAG, "Polyline index returned: " + index + ", for level: " + level + ". Levels: " + routePolylineOptionsInLevels.size());
        return index;
    }

    public void removeRouteLine() {
        if (routeLines != null) {
            removeAllRouteLines(routeLines);
        }
        routeLines = null;
    }

    private void removeAllRouteLines(ArrayList<Polyline> routeLines) {
        for (int i = 0; i < routeLines.size(); i++) {
            removeSingleRouteLine(routeLines.get(i));
        }
    }

    private void removeSingleRouteLine(Polyline routeLine) {
        if (routeLine != null) {
            routeLine.remove();
        }
    }

    public void addRouteLine(PolylineOptions polylineOptions) {
        if (routeLines == null) {
            routeLines = new ArrayList<>();
        }
        routeLines.add(mMap.addPolyline(polylineOptions));

    }

    public void cancelTarget() {
        target = null;
        removeDestinationMarker();
        removeDestinationDescription();
        removeSourceCircle();
        removeRouteLine();
        routePolylineOptionsInLevels = null;
        routePolylineOptions = null;
    }

    public void removeDestinationMarker() {
        if (destinationMarker != null) {
            destinationMarker.remove();
            destinationMarker = null;
        }
    }

    public void removeSourceMarker() {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }
    }

    public void removeDestinationDescription() {
        Activity activity = (MapsActivity) context;
        LinearLayout descriptionLayout = activity.findViewById(R.id.targetDescriptionLayout);
        descriptionLayout.setVisibility(LinearLayout.GONE);
        TextView descriptionText = activity.findViewById(R.id.targetDescriptionHeader);
        descriptionText.setText("");
        TextView descriptionTextBody = activity.findViewById(R.id.targetDescriptionBody);
        descriptionTextBody.setText("");
    }

    public void handleRoutePolyline(Handler routeHandler) {
        if (routePolylineOptionsInLevels != null && routePolylineOptionsInLevels.size() > 0) {
            routePolylineOptions = routePolylineOptionsInLevels.get(0);
            routeHandler.post(new Runnable() {
                public void run() {
                    ArrayList<ArrayList<LatLng>> route = new ArrayList<>();
                    if (routePolylineOptions != null && routePolylineOptions.getPoints().size() > 0) {
                        route.add(ListToArrayList(routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
                        moveCameraToStartingPosition();
                        removeRouteLine();
                        routeLines = new ArrayList<>();
                        routeLines.add(mMap.addPolyline(routePolylineOptions));
                    }
//                    System.out.println("Number of points: " + route.get(0).size());
                    removeDestinationMarker();
                    removeSourceMarker();
                    addSourceCircle();
                    buttonClickListener.setFloorAsChecked(sourceLevel);

                    ProgressBar progressBar = ((MapsActivity)(context)).findViewById(R.id.progressBar2);
                    progressBar.setVisibility(ProgressBar.GONE);

                    //back on UI thread...
                }
            });
        }

    }

    public void moveCameraToStartingPosition() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source.getLatlng(), 18));
    }

}
