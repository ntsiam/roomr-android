package com.app.ariadne.tumrfmap.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.app.ariadne.tumrfmap.MapsActivity;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumrfmap.geojson.Entrance;
import com.app.ariadne.tumrfmap.geojson.GeoJsonMap;
import com.app.ariadne.tumrfmap.geojson.LatLngWithTags;
import com.app.ariadne.tumrfmap.listeners.ButtonClickListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
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
    ArrayList<Marker> routeMarkers;
    GeoJsonMap geoJsonMap;
    boolean areMapElementsVisible;
    LinearLayout descriptionLayout;
    EditText destinationEditText;
    ImageButton cancelButton;



    public MapUIElementsManager(Context context, ButtonClickListener buttonClickListener, GoogleMap mMap, GeoJsonMap geoJsonMap) {
        this.context = context;
        this.buttonClickListener = buttonClickListener;
        this.mMap = mMap;
        this.geoJsonMap = geoJsonMap;
        areMapElementsVisible = true;
        descriptionLayout = ((MapsActivity) context).findViewById(R.id.targetDescriptionLayout);
        destinationEditText = ((MapsActivity) context).findViewById(R.id.findDestination);
        cancelButton = ((MapsActivity) context).findViewById(R.id.cancel_button);
    }

//    public void managePolylineOptions(int requestedLevel) {
//        int indexOfLevelInButtonList = MAX_FLOOR - requestedLevel;
//        int currentIndexInButtonList = 0;
//        for (ToggleButton levelButton: buttonClickListener.floorButtonList) {
//            if (currentIndexInButtonList != indexOfLevelInButtonList) {
//                levelButton.setChecked(false);
//            } else {
//                if (!levelButton.isChecked()) {
//                    Log.i(TAG, "Entered here");
//                    level = ""; //no tiles for "0" - ground floor is ""
//                    if (routePolylineOptionsInLevels != null) {
//                        addRouteLineFromPolyLineOptions(Integer.MIN_VALUE);
//                    }
//                } else {
//                    if (routePolylineOptionsInLevels != null) {
//                        int polyLineIndex = findPolyLineIndex(requestedLevel);
//                        addRouteLineFromPolyLineOptions(polyLineIndex);
//                    }
//                }
//            }
//            currentIndexInButtonList++;
//        }
//
//    }

    public void toggleMapUIElementVisibility() {
        if (areMapElementsVisible) {
            areMapElementsVisible = false;

            if (descriptionLayout.getVisibility() == LinearLayout.VISIBLE) {
//                ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
//                params.height = 50;
//                descriptionLayout.setLayoutParams(params);
//                Log.i(TAG, "clicked on map: " + latLng.toString());
//                descriptionLayout.setOnClickListener(this);
                descriptionLayout.setVisibility(LinearLayout.GONE);
                destinationEditText.setVisibility(EditText.GONE);
                cancelButton.setVisibility(ImageButton.GONE);
            }

        } else {
            descriptionLayout.setVisibility(LinearLayout.VISIBLE);
            destinationEditText.setVisibility(EditText.VISIBLE);
            cancelButton.setVisibility(ImageButton.VISIBLE);
            areMapElementsVisible = true;
        }
    }

    public void setDestinationOnMap(String destinationId) {
        target = findDestinationFromId(destinationId);
        if (target != null) {
//            destinationEditText = findViewById(R.id.findDestination);
            addMarkerAndZoomCameraOnTarget(target);
            addDestinationDescription(target);
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
        addDestinationMarker(target);
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(target.getLatlng(),
                18, mMap.getCameraPosition().tilt, mMap.getCameraPosition().bearing)));
    }

    private void addDestinationMarker(LatLngWithTags target) {
        destinationMarker = mMap.addMarker(new MarkerOptions().position(target.getLatlng())
                .title(target.getId()));
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
        } else if (sourceLevel > level && destinationLevel < level) {
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
        resetRouteMarkers();
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

    public void addDestinationDescription(LatLngWithTags latLngWithTags) {
        Log.i(TAG, "Find entrance for destination: " + latLngWithTags + ", buildingId: " + latLngWithTags.getBuildingId());
        Entrance entrance = geoJsonMap.findEntranceForDestination(latLngWithTags);
        Activity activity = (MapsActivity) context;
        LinearLayout descriptionLayout = activity.findViewById(R.id.targetDescriptionLayout);
        TextView destinationTextview = activity.findViewById(R.id.findDestination);
        if (entrance != null) {
            Log.i(TAG, "Entrance found: " + entrance.getAddress());
            destinationTextview.setText(target.getId());
            descriptionLayout.setVisibility(LinearLayout.VISIBLE);
            TextView descriptionText = activity.findViewById(R.id.targetDescriptionHeader);
            descriptionText.setText(String.format("%s, %s", target.getId(), entrance.getBuilding()));
            TextView descriptionTextBody = activity.findViewById(R.id.targetDescriptionBody);
            descriptionTextBody.setText(String.format("%s, %s, %s", entrance.getAddress(), entrance.getPlz(), entrance.getCity()));
        } else {

            descriptionLayout.setVisibility(LinearLayout.GONE);
            destinationTextview.setText("");
        }
    }

    public void handleRoutePolyline(Handler routeHandler, final LatLngWithTags target, final LatLng minPoint, final LatLng maxPoint) {
        if (routePolylineOptionsInLevels != null && routePolylineOptionsInLevels.size() > 0) {
            routePolylineOptions = routePolylineOptionsInLevels.get(0);
            routeHandler.post(new Runnable() {
                public void run() {
                    //back on UI thread...
                    ArrayList<ArrayList<LatLng>> route = new ArrayList<>();
                    if (routePolylineOptions != null && routePolylineOptions.getPoints().size() > 0) {
                        route.add(ListToArrayList(routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
                        moveCameraToStartingPosition(minPoint, maxPoint);
                        removeRouteLine();
                        routeLines = new ArrayList<>();
                        routeLines.add(mMap.addPolyline(routePolylineOptions));
                    }
//                    System.out.println("Number of points: " + route.get(0).size());
                    removeDestinationMarker();
                    addDestinationMarker(target);
                    addDestinationDescription(target);
                    removeSourceMarker();
                    addSourceCircle();
                    buttonClickListener.setFloorAsChecked(sourceLevel);

                    ProgressBar progressBar = ((MapsActivity)(context)).findViewById(R.id.progressBar2);
                    progressBar.setVisibility(ProgressBar.GONE);
                    addRouteMarkers();
                }
            });
        }

    }

    private void addRouteMarkers() {
        resetRouteMarkers();
        for (int i = 1; i < routePolylineOptionsInLevels.size(); i++) {
            Marker stair;
            if (sourceLevel < destinationLevel) {
                stair = mMap.addMarker(new MarkerOptions()
                        .position(routePolylineOptionsInLevels.get(i - 1).getPoints().get(routePolylineOptionsInLevels.get(i - 1).getPoints().size() - 1))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_up)).title("Take the stairs up one level"));
            } else {
                stair = mMap.addMarker(new MarkerOptions()
                        .position(routePolylineOptionsInLevels.get(i - 1).getPoints().get(routePolylineOptionsInLevels.get(i - 1).getPoints().size() - 1))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_down)).title("Take the stairs down one level"));
            }
            routeMarkers.add(stair);
        }
    }

    private void resetRouteMarkers() {
        if (routeMarkers!= null && routeMarkers.size() > 0) {
            removeRouteMarkers();
        }
        routeMarkers = new ArrayList<>();
    }

    private void removeRouteMarkers() {
        for (Marker marker : routeMarkers) {
            if (marker != null) {
                marker.remove();
                marker = null;
            }
        }
    }

    public LatLngWithTags getDestination() {
//        String destinationName = autoCompleteDestination.getText().toString();
//        destinationName = destinationName.substring(0, destinationName.length() - 1);
//        Toast.makeText(this, "Destination: " + destinationName, Toast.LENGTH_LONG).show();
//        target = findDestinationFromId(destinationName);
        if (target != null) {
            return target;
        } else {
            return null;
        }
    }

    public void moveCameraToStartingPosition(LatLng minPoint, LatLng maxPoint) {
        LatLngBounds routePosition = new LatLngBounds(
                minPoint, maxPoint);
        Log.i(TAG, "Southwest: " + minPoint);
        Log.i(TAG, "Northeast: " + maxPoint);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePosition.getCenter(), 18));

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source.getLatlng(), 18));
    }

}
