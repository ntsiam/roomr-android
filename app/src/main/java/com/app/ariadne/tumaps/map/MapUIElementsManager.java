package com.app.ariadne.tumaps.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.ariadne.tumaps.MapsConfiguration;
import com.app.ariadne.tumaps.geojson.GeoJsonHelper;
import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.listeners.SensorChangeListener;
import com.app.ariadne.tumaps.models.RouteInstruction;
import com.app.ariadne.tumrfmap.R;
import com.app.ariadne.tumaps.models.Entrance;
import com.app.ariadne.tumaps.geojson.GeoJsonMap;
import com.app.ariadne.tumaps.geojson.LatLngWithTags;
import com.app.ariadne.tumaps.listeners.ButtonClickListener;
import com.app.ariadne.tumaps.models.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

import static com.app.ariadne.tumaps.geojson.GeoJsonMap.findDestinationFromId;

public class MapUIElementsManager implements TextToSpeech.OnInitListener {
    public HashMap<Integer, ArrayList<PolylineOptions>> routePolylineOptionsInLevels;
    public Route route;
    public PolylineOptions routePolylineOptions;
    public PolylineOptions routePolylineOptionsGray;
    Context context;
    private static final String TAG = "MAPUIElements";
    public String level;
    private final int MIN_FLOOR = -4;
    public static final int MAX_FLOOR = 4;
    public String sourceName;
    public String destinationName;
    public Circle sourceMarker;
    public GoogleMap mMap;
    public Marker destinationMarker;
    public LatLngWithTags destination;
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
    TextToSpeech tts;
    public Queue<RouteInstruction> routeInstructionsFinal;
    public ArrayList<RouteInstruction> routeInstructionsArrayList;
    ListView instructionList;
    ArrayList<String> instructions = new ArrayList<>();
    ArrayList<LatLng> waypoints = new ArrayList<>();
    ArrayAdapter adapterForInstructions;
    Marker instructionMarker = null;
    ArrayList<String> finalInstructions;
    private SensorChangeListener sensorChangeListener;
    PositionManager positionManager;






    public MapUIElementsManager(final Context context, ButtonClickListener buttonClickListener, GoogleMap mMap, GeoJsonMap geoJsonMap) {
        this.context = context;
        this.buttonClickListener = buttonClickListener;
        this.mMap = mMap;
        this.geoJsonMap = geoJsonMap;
        areMapElementsVisible = true;
        descriptionLayout = ((MapsActivity) context).findViewById(R.id.targetDescriptionLayout);
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final float scale = context.getResources().getDisplayMetrics().density;
                Log.i(TAG, "Clicked on Description Layout, number of elements: " + instructionList.getAdapter().getCount());
                ViewGroup.LayoutParams params = v.getLayoutParams();
                if (instructionList.getAdapter().getCount() > 0) {
                    int dps = 150;
                    // Changes the height and width to the specified *pixels*
                    int pixels = (int) (dps * scale + 0.5f);
                    Log.i(TAG, "Height: " + params.height);
//                    if (params.height > pixels || params.height < 0) {
                    pixels = (int) (150 * scale + 0.5f);
                    params.height = pixels;
                    ArrayAdapter tempAdapterForInstructions =  new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList());
                    instructionList.setAdapter(tempAdapterForInstructions);
//                    } else {
//                        pixels = (int) (400 * scale + 0.5f);
////                        params.height = pixels;
//                        params.height = -2; // wrap content?
//                    }n
                } else {
                    instructionList.setAdapter(adapterForInstructions);
                    if (instructionList.getAdapter().getCount() > 0) {
                        params.height = -2; // wrap content?
                    } else {
                        int pixels = (int) (150 * scale + 0.5f);
                        params.height = pixels;
                    }
                }
                v.setLayoutParams(params);
            }
        };

        descriptionLayout.setOnClickListener(onClickListener);
//        descriptionLayout.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                Log.i(TAG,"Touched description layout");
//                int eventaction = event.getActionMasked();
//                ViewGroup.LayoutParams params = v.getLayoutParams();
//                final float scale = context.getResources().getDisplayMetrics().density;
//
//
//                switch (eventaction) {
//                    case MotionEvent.ACTION_DOWN:
//                        Log.i(TAG, "Motionevent down");
//                        int pixels = (int) (150 * scale + 0.5f);
//                        params.height = pixels;
//                        v.setLayoutParams(params);
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        Log.i(TAG, "Motionevent up");
//                        params.height = -2;
//                        v.setLayoutParams(params);
//                        break;
//                }
//                v.setLayoutParams(params);
//                return false;
//            }
//        });
        destinationEditText = ((MapsActivity) context).findViewById(R.id.findDestination);
        cancelButton = ((MapsActivity) context).findViewById(R.id.cancel_button);

//        tts = new TextToSpeech(context, this);
        instructionList = ((MapsActivity) context).findViewById(R.id.instruction_list);
        adapterForInstructions =  new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList());
        instructionList.setAdapter(adapterForInstructions);
        sensorChangeListener = new SensorChangeListener(context);

    }

    public void removeInstructionMarker() {
        if (instructionMarker != null) {
            instructionMarker.remove();
        }
        instructionMarker = null;
    }

    public void addNewInstructionMarker(int index) {
        RouteInstruction routeInstruction = routeInstructionsArrayList.get(index);
        MarkerOptions options = new MarkerOptions()
                .position(routeInstruction.getPoint())
                .title(routeInstruction.getInstruction());
        instructionMarker = mMap.addMarker(options);
        instructionMarker.showInfoWindow();
    }

    public void addNewInstructionMarkerGivenInstruction(RouteInstruction routeInstruction) {
        MarkerOptions options = new MarkerOptions()
                .position(routeInstruction.getPoint())
                .title(routeInstruction.getInstruction());
        instructionMarker = mMap.addMarker(options);
        instructionMarker.showInfoWindow();
    }


    public LatLngWithTags getSource() {
        return source;
    }

    public LatLngWithTags getDestination() {
        return destination;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Route getRoute() {
        return route;
    }

    public int getSourceLevel() {
        return sourceLevel;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public LatLng getCameraPosition() {
        return mMap.getCameraPosition().target;
    }

    public double getZoomLevel() {
        return mMap.getCameraPosition().zoom;
    }

    public void handleClickOnMap(LatLng latLngClicked) {
        if (level != null) {
            boolean isClickHandled = false;
            int levelToShow = Integer.valueOf(level);
            if (level != null && !level.equals("") && route != null) {
//            Log.i("OnClick", "mapUIElementsManager.level is defined");
//                Log.i("OnClick", "mapUIElementsManager.route is defined");
                for (int i = route.getMinRouteLevel(); i <= route.getMaxRouteLevel(); i++) {
                    levelToShow = findLevelToShowBasedOnUserClickedLatLng(i, latLngClicked, levelToShow);
                }
                if (levelToShow != Integer.valueOf(level)) {
                    buttonClickListener.setFloorAsChecked(levelToShow);
                    addRouteLineFromPolyLineOptions(levelToShow);
                    isClickHandled = true;
                }
            }
            if (!isClickHandled && destination != null) {
                toggleMapUIElementVisibility();
            }
        }
    }

    private int findLevelToShowBasedOnUserClickedLatLng(int currentLevel, LatLng latLngClicked, int levelToShow) {
        double minDistance = 100.0;
        ArrayList<PolylineOptions> routeLevelContainer = route.getRouteHashMapForLevels().get(currentLevel);
        for (PolylineOptions routeLevel : routeLevelContainer) {
            for (LatLng point : routeLevel.getPoints()) {
                double tmpDistance = SphericalUtil.computeDistanceBetween(point, latLngClicked);
                if (tmpDistance < minDistance) {
                    minDistance = tmpDistance;
                    if (minDistance < 5.0) {
                        levelToShow = currentLevel;
                    }
//                                Log.i("OnClick", "mindistance: " + minDistance);
                }
            }
        }
        return levelToShow;
    }


    public boolean isRoutableSourceDestination() {
        return !(sourceName.equals("My Location") || sourceName.equals(destinationName) || sourceName.contains("ntrance")
                && destinationName.contains("ntrance") || destinationName.contains("ntrance") && sourceName.contains("ntrance"));
    }

    public boolean areSourceAndDestinationNotNull() {
        return source != null && destination != null;
    }

    public void setRoutePathOnMap(Route routePath, Handler routeHandler, LatLng minPoint, LatLng maxPoint) {
        routePolylineOptionsInLevels = null;
        route = routePath;
        handleRoutePolyline(routeHandler, minPoint, maxPoint);
    }

    public void setSourceAndDestination(final String sourceName, final String targetName) {
        if (sourceName.equals("Entrance of building")){
//                mapUIElementsManager.destination = findDestinationFromId(destinationName);
            source = geoJsonMap.findEntranceForDestination(destination).getEntranceLatLngWithTags();
        } else {
            source = GeoJsonMap.findDestinationFromId(sourceName);
        }
        if (targetName.equals("Entrance of building")) {
            destination = geoJsonMap.findEntranceForDestination(source).getEntranceLatLngWithTags();
        } else {
            destination = GeoJsonMap.findDestinationFromId(targetName);
        }
        sourceLevel = MapsConfiguration.getInstance().getLevelFromId(sourceName);
        //Log.i(TAG, "Source level = " + mapUIElementsManager.sourceLevel);
        destinationLevel = MapsConfiguration.getInstance().getLevelFromId(targetName);
    }

    public void toggleMapUIElementVisibility() {
        if (areMapElementsVisible) {
            areMapElementsVisible = false;

            if (descriptionLayout.getVisibility() == LinearLayout.VISIBLE) {
                descriptionLayout.setVisibility(LinearLayout.GONE);
                destinationEditText.setVisibility(EditText.GONE);
                cancelButton.setVisibility(ImageButton.GONE);
                removeInstructionMarker();
            }

        } else {
            descriptionLayout.setVisibility(LinearLayout.VISIBLE);
            destinationEditText.setVisibility(EditText.VISIBLE);
            cancelButton.setVisibility(ImageButton.VISIBLE);
            areMapElementsVisible = true;
        }
    }

    public void setDestinationOnMap(String destinationId) {
        destination = findDestinationFromId(destinationId);
        if (destination != null) {
//            destinationEditText = findViewById(R.id.findDestination);
            addMarkerAndZoomCameraOnTarget(destination);
            addDestinationDescription(destination);
            buttonClickListener.showDestinationFoundButtons(destinationId);

            int level = MapsConfiguration.getInstance().getLevelFromId(destination.getId());
            //Log.i(TAG, "Set floor as checked: " + level);
            buttonClickListener.setFloorAsChecked(level);
        }
    }

    public void addMarkerAndZoomCameraOnTarget(LatLngWithTags target) {
        removeInstructionMarker();
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



    public void addRouteLineFromPolyLineOptions(int level) {
        removeRouteLine();
        int currentIndex = 0;
        for (int i = route.getMinRouteLevel(); i <= route.getMaxRouteLevel(); i++) {
            for (PolylineOptions polylineOptions : route.getRouteHashMapForLevels().get(i)) {
                addPathToPolylineOptionsGray(level, i, polylineOptions);
            }

        }
//        for (PolylineOptions polylineOptions: routePolylineOptionsInLevels) {
//            addPathToPolylineOptionsGray(index, currentIndex, polylineOptions);
//            currentIndex++;
//        }
        if (level != Integer.MIN_VALUE) {
            if (route.getRouteHashMapForLevels().containsKey(level)) {
                for (PolylineOptions polylineOptions : route.getRouteHashMapForLevels().get(level)) {
                    addRouteLine(polylineOptions);
                }
            }
            routePolylineOptionsGray.clickable(true);
        }
//        removeSourceCircle();
//        addSourceCircle(source.getLatlng());
    }

    private void addPathToPolylineOptionsGray(int index, int currentIndex, PolylineOptions polylineOptions) {
        routePolylineOptionsGray = new PolylineOptions().width(10).color(Color.GRAY).zIndex(Integer.MAX_VALUE - 2000);
        if (currentIndex != index) {
            addPointsToPolyLineOptionsGray(polylineOptions);
        }
        addRouteLine(routePolylineOptionsGray);
        routePolylineOptionsGray.clickable(true);
    }

    private void addPointsToPolyLineOptionsGray(PolylineOptions polylineOptions) {
        for (LatLng point: polylineOptions.getPoints()) {
//            //Log.i(TAG, "addPointsToPolyLineOptionsGray, point: " + point.toString());
            routePolylineOptionsGray.add(point);
        }
    }


    public void removeSourceCircle() {
        if (sourceMarker != null) {
            sourceMarker.remove();
            sourceMarker = null;
        }
    }

    public void addSourceCircle(LatLng sourceLatLng) {
        if (sourceMarker != null) {
            removeSourceCircle();
        }
        CircleOptions circleOptions = new CircleOptions()
                .center(sourceLatLng)
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
        //Log.i(TAG, "Polyline index returned: " + index + ", for level: " + level + ". Levels: " + routePolylineOptionsInLevels.size());
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

    public void removeAllDestinationElementsFromMap() {
        destination = null;
        removeInstructionMarker();
        removeDestinationMarker();
        removeDestinationDescription();
        removeSourceCircle();
        removeRouteLine();
        routePolylineOptionsInLevels = null;
        routePolylineOptions = null;
        resetRouteMarkers();
        route = null;
        instructions = new ArrayList<>();
        waypoints = new ArrayList<>();
        adapterForInstructions =  new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, new ArrayList());
        instructionList.setAdapter(adapterForInstructions);
        instructionList.setVisibility(ListView.GONE);
        ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
        toggleInstructions(true);
        sensorChangeListener.cancelNavigation();

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
        removeInstructionMarker();
    }

    public void addDestinationDescription(LatLngWithTags latLngWithTags) {
        //Log.i(TAG, "Find entrance for destination: " + latLngWithTags + ", buildingId: " + latLngWithTags.getBuildingId());
        Entrance entrance = geoJsonMap.findEntranceForDestination(latLngWithTags);
        Activity activity = (MapsActivity) context;
        LinearLayout descriptionLayout = activity.findViewById(R.id.targetDescriptionLayout);
        TextView destinationTextview = activity.findViewById(R.id.findDestination);
        if (entrance != null) {
            //Log.i(TAG, "Entrance found: " + entrance.getAddress());
            destinationTextview.setText(destination.getId());
            descriptionLayout.setVisibility(LinearLayout.VISIBLE);
            TextView descriptionText = activity.findViewById(R.id.targetDescriptionHeader);
            descriptionText.setText(String.format("%s, %s", GeoJsonMap.getRoomIdFromBuildingName(destination.getId()), entrance.getBuilding()));
            TextView descriptionTextBody = activity.findViewById(R.id.targetDescriptionBody);
            descriptionTextBody.setText(String.format("%s, %s, %s", entrance.getAddress(), entrance.getPlz(), entrance.getCity()));
        } else {

            descriptionLayout.setVisibility(LinearLayout.GONE);
            destinationTextview.setText("");
        }
    }

    public void handleRoutePolyline(Handler routeHandler, final LatLng minPoint, final LatLng maxPoint) {
//        if (routePolylineOptionsInLevels != null && routePolylineOptionsInLevels.size() > 0) {
        if (route != null && route.getRouteHashMapForLevels()!= null && route.getRouteHashMapForLevels().get(route.getSourceLevel())
                != null && route.getRouteHashMapForLevels().get(route.getSourceLevel()).size() > 0) {
            routePolylineOptions = route.getRouteHashMapForLevels().get(route.getSourceLevel()).get(0);
            positionManager = new PositionManager(context, this);

            routeHandler.post(new Runnable() {
                public void run() {
                    //back on UI thread...
                    ArrayList<ArrayList<LatLng>> routeArrayList = new ArrayList<>();
                    if (routePolylineOptions != null && routePolylineOptions.getPoints().size() > 0) {
                        routeArrayList.add(GeoJsonHelper.ListToArrayList(routePolylineOptions.getPoints()));
//                                currentLevel = Integer.valueOf(dijkstra.level);
                        moveCameraToStartingPosition(minPoint, maxPoint);
                        removeRouteLine();
                        routeLines = new ArrayList<>();
                        routeLines.add(mMap.addPolyline(routePolylineOptions));
                    }
                    removeDestinationMarker();
                    addDestinationMarker(destination);
                    addDestinationDescription(destination);
                    removeSourceMarker();
                    addSourceCircle(source.getLatlng());
                    sourceLevel = route.getSourceLevel();
                    buttonClickListener.setFloorAsChecked(sourceLevel);

                    ProgressBar progressBar = ((MapsActivity)(context)).findViewById(R.id.progressBar2);
                    progressBar.setVisibility(ProgressBar.GONE);
                    addRouteMarkers(sourceLevel);



                    instructions = MapsActivity.instructions;
                    waypoints = MapsActivity.waypoints;
                    Queue<RouteInstruction> instructionQueue = MapsActivity.routeInstructionQueue;
                    routeInstructionsFinal = new LinkedList<>();
                    routeInstructionsArrayList = new ArrayList<>();
                    finalInstructions = new ArrayList<>();
                    String currInstruction = "";
                    LatLng prevPoint = null;
                    int index = 0;
                    Log.i(TAG, "Number of waypoints: " + waypoints.size());
                    RouteInstruction routeInstruction = null;
//                    for (LatLng point: waypoints) {
                    while (!instructionQueue.isEmpty()) {
                        routeInstruction = instructionQueue.poll();
                        Log.i(TAG, "Instruction: " + routeInstruction.getInstruction());
                        LatLng point = routeInstruction.getPoint();
                        if (prevPoint != null) {
                            if (prevPoint.equals(point)) {
                                currInstruction += routeInstruction.getInstruction();
//                                currInstruction += instructions.get(index);
                            } else {
//                                MarkerOptions options = new MarkerOptions()
//                                        .position(prevPoint)
//                                        .title(currInstruction);
//
//                                mMap.addMarker(options);
//                                tts.speak(currInstruction, TextToSpeech.QUEUE_ADD, null);
                                routeInstructionsFinal.add(new RouteInstruction(currInstruction, prevPoint, routeInstruction.getLevel()));
                                routeInstructionsArrayList.add(new RouteInstruction(currInstruction, prevPoint, routeInstruction.getLevel()));
                                finalInstructions.add(currInstruction);
                                currInstruction = routeInstruction.getInstruction();
//                                currInstruction = instructions.get(index) + ", ";
                            }
                        } else {
//                            currInstruction = instructions.get(index);
                            currInstruction = routeInstruction.getInstruction();
//                            MarkerOptions options = new MarkerOptions()
//                                    .position(point)
//                                    .title(currInstruction);
//
//                            mMap.addMarker(options);
//                            tts.speak(currInstruction, TextToSpeech.QUEUE_ADD, null);
//                            routeInstructionsFinal.add(new RouteInstruction(currInstruction, point, routeInstruction.getLevel()));
//                            routeInstructionsArrayList.add(new RouteInstruction(currInstruction, point, routeInstruction.getLevel()));
//                            finalInstructions.add(currInstruction);

                        }
                        prevPoint = point;
                        index++;
                    }
                    if (prevPoint != null) {
                        currInstruction = routeInstruction.getInstruction();
//                        currInstruction = instructions.get(index - 1);
//                        MarkerOptions options = new MarkerOptions()
//                                .position(prevPoint)
//                                .title(currInstruction);
//
//                        mMap.addMarker(options);
//                        tts.speak(currInstruction, TextToSpeech.QUEUE_ADD, null);
                        routeInstructionsFinal.add(new RouteInstruction(currInstruction, prevPoint, routeInstruction.getLevel()));
                        routeInstructionsArrayList.add(new RouteInstruction(currInstruction, prevPoint, routeInstruction.getLevel()));
                        finalInstructions.add(currInstruction);

                    }


                    // Show instructions on ListView
                    adapterForInstructions =  new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, finalInstructions);
                    instructionList.setAdapter(adapterForInstructions);
                    if (instructionList.getAdapter().getCount() > 0) {
                        instructionList.setVisibility(ListView.VISIBLE);
                        AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                removeInstructionMarker();
                                addNewInstructionMarker(position);
//                                moveCameraToStartingPosition(routeInstructionsArrayList.get(position).getPoint(), routeInstructionsArrayList.get(position).getPoint());

                            }
                        };

                        instructionList.setOnItemClickListener(onItemClickListener);
                        toggleInstructions(false);
                    } else {
                        instructionList.setVisibility(ListView.GONE);
                        toggleInstructions(true);
                    }

//                    positionManager = new PositionManager(context, routeInstructionsFinal, getSource().getLatlng());
                    positionManager.setRouteInstructionQueue(routeInstructionsFinal);
                    positionManager.setInitialPosition(getSource().getLatlng());
                    sensorChangeListener.setPositionManager(positionManager);
                    sensorChangeListener.startNavigation();
                    addNewInstructionMarker(0);

                }
            });
        }

    }

    public SensorChangeListener getSensorChangeListener() {
        return sensorChangeListener;
    }

    private void toggleInstructions(boolean actionHide) {
        ViewGroup.LayoutParams params = descriptionLayout.getLayoutParams();
        if (actionHide) {
            final float scale = context.getResources().getDisplayMetrics().density;
            params.height = (int) (150 * scale + 0.5f);
        } else {
            params.height = -2;
        }
        descriptionLayout.setLayoutParams(params);
    }

    public void addRouteMarkers(int level) {
        resetRouteMarkers();
        if (route!= null && route.getStairMarkers().containsKey(level)) {
            ArrayList<MarkerOptions> markerOptions = route.getStairMarkers().get(level);
            for (MarkerOptions markerOptions1: markerOptions) {
                Marker stair;
                stair = mMap.addMarker(markerOptions1);
                routeMarkers.add(stair);
            }
            if (routeMarkers.size() > 0) {
                routeMarkers.get(0).showInfoWindow();
            }
        }
//        for (int i = 1; i < routePolylineOptionsInLevels.size(); i++) {
//            Marker stair;
//            if (sourceLevel < destinationLevel) {
//                stair = mMap.addMarker(new MarkerOptions()
//                        .position(routePolylineOptionsInLevels.get(i - 1).getPoints().get(routePolylineOptionsInLevels.get(i - 1).getPoints().size() - 1))
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_up)).snippet("one level").title("Take the stairs up"));
//            } else {
//                stair = mMap.addMarker(new MarkerOptions()
//                        .position(routePolylineOptionsInLevels.get(i - 1).getPoints().get(routePolylineOptionsInLevels.get(i - 1).getPoints().size() - 1))
//                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.stairs_down)).snippet("one level").title("Take the stairs down"));
//            }
//            routeMarkers.add(stair);
//        }
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

    public void moveCameraToStartingPosition(LatLng minPoint, LatLng maxPoint) {
        LatLngBounds routePosition = new LatLngBounds(
                minPoint, maxPoint);
        //Log.i(TAG, "Southwest: " + minPoint);
        //Log.i(TAG, "Northeast: " + maxPoint);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePosition.getCenter(), 18));

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source.getLatlng(), 18));
    }

    public void moveCameraToPosition(LatLng minPoint, LatLng maxPoint, int zoom) {
        LatLngBounds routePosition = new LatLngBounds(
                minPoint, maxPoint);
        //Log.i(TAG, "Southwest: " + minPoint);
        //Log.i(TAG, "Northeast: " + maxPoint);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routePosition.getCenter(), zoom));

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(source.getLatlng(), 18));
    }


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
//            tts.setLanguage(Locale.US);
        }
    }
}
