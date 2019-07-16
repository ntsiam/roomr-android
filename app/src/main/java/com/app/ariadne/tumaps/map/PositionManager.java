package com.app.ariadne.tumaps.map;

import android.content.Context;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.models.RouteInstruction;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.util.Locale;
import java.util.Queue;


public class PositionManager implements TextToSpeech.OnInitListener {
    private Context applicationContext;
    private MapsActivity mapsActivity;
    private Queue<RouteInstruction> routeInstructionQueue;
    public LatLng currentPosition;
    private RouteInstruction previousInstruction;
    private RouteInstruction nextInstruction;
    public double currentHeading;
    private final double STEP_LENGTH = 0.66;
    private MapUIElementsManager mapUIElementsManager;
    private final static String TAG = "PositionManager";
    private int instructionIndex = 0;
    public TextToSpeech tts;
    private boolean isCloserToNextInstruction;


    public PositionManager(Context context, MapUIElementsManager mapUIElementsManager) {
        this.applicationContext = context;
        this.mapsActivity = (MapsActivity) context;
        this.mapUIElementsManager = mapUIElementsManager;
        tts = new TextToSpeech(context, this);
        isCloserToNextInstruction = false;


    }

    public PositionManager(Context context, Queue<RouteInstruction> routeInstructionQueue, LatLng initialPosition) {
        this.applicationContext = context;
        this.mapsActivity = (MapsActivity) context;
        this.routeInstructionQueue = routeInstructionQueue;
        this.currentPosition = initialPosition;
        tts = new TextToSpeech(context, this);
        isCloserToNextInstruction = false;

    }

    public void setRouteInstructionQueue(Queue<RouteInstruction> routeInstructionQueue) {
        this.routeInstructionQueue = routeInstructionQueue;
    }

    public void setInitialPosition(LatLng initialPosition) {
        this.currentPosition = initialPosition;
    }

    public void setMapUIElementsManager(MapUIElementsManager mapUIElementsManager) {
        this.mapUIElementsManager = mapUIElementsManager;
    }

    public void startNavigation() {
        previousInstruction = routeInstructionQueue.poll();
        nextInstruction = routeInstructionQueue.poll();
        this.currentHeading = SphericalUtil.computeHeading(previousInstruction.getPoint(), nextInstruction.getPoint());
//        LatLng firstPoint = mapUIElementsManager.routeMarkers.get(0).getPosition();
//        LatLng secondPoint = mapUIElementsManager.routeMarkers.get(1).getPosition();
//        this.currentHeading = SphericalUtil.computeHeading(firstPoint, secondPoint);
        instructionIndex = 0;
        mapUIElementsManager.addSourceCircle(currentPosition);
//        Log.i(TAG, "First instruction: " + previousInstruction.getInstruction());
//        tts.speak(previousInstruction.getInstr++uction().toString(), TextToSpeech.QUEUE_FLUSH, null);
//        tts.speak("This is the first instruction", TextToSpeech.QUEUE_ADD, null);



    }

    public void playFirstInstruction() {
//        tts.speak(previousInstruction.getInstruction(), TextToSpeech.QUEUE_ADD,null);
    }

    public void cancelNavigation() {
        mapUIElementsManager.removeSourceCircle();
        tts.stop();
    }


    public void updatePosition(float numOfSteps) {
        LatLng previousPosition = currentPosition;
        double distance = numOfSteps * STEP_LENGTH;
        Log.i(TAG, "Number of steps: " + numOfSteps);

        if (routeInstructionQueue!= null && !routeInstructionQueue.isEmpty() || SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint()) > distance) {
            LatLng tempPosition = SphericalUtil.computeOffset(currentPosition, distance, currentHeading);
            while (SphericalUtil.computeDistanceBetween(currentPosition, tempPosition) > SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint()) && !routeInstructionQueue.isEmpty()) {
                distance = distance - SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint());
                currentPosition = nextInstruction.getPoint();
                previousInstruction = nextInstruction;
                nextInstruction = routeInstructionQueue.poll();
                currentHeading = SphericalUtil.computeHeading(previousInstruction.getPoint(), nextInstruction.getPoint());
                tempPosition = SphericalUtil.computeOffset(currentPosition, distance, currentHeading);
            }
            currentPosition = tempPosition;
            mapUIElementsManager.removeSourceCircle();
            mapUIElementsManager.addSourceCircle(currentPosition);
            if (!isCloserToNextInstruction && (3 * STEP_LENGTH) > SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint()) || SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint()) > SphericalUtil.computeDistanceBetween(previousPosition, nextInstruction.getPoint())) {
                instructionIndex++;
                mapUIElementsManager.removeInstructionMarker();
                mapUIElementsManager.addNewInstructionMarkerGivenInstruction(nextInstruction);
                tts.speak(nextInstruction.getInstruction(), TextToSpeech.QUEUE_ADD, null);
                isCloserToNextInstruction = true;

            } else if (SphericalUtil.computeDistanceBetween(previousInstruction.getPoint(), nextInstruction.getPoint()) < (3 * STEP_LENGTH) || (3 * STEP_LENGTH) < SphericalUtil.computeDistanceBetween(currentPosition, nextInstruction.getPoint())) {
                isCloserToNextInstruction = false;
            }
        } else {
            currentPosition = nextInstruction.getPoint();
        }
//        checkIfStarsCollected(currentPosition);
//        mapUIElementsManager.moveCameraToPosition(currentPosition, currentPosition, 20);
        CameraPosition cameraPosition = new CameraPosition(currentPosition, 23, 0, (float)currentHeading);
        CameraUpdate newCameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mapUIElementsManager.mMap.animateCamera(newCameraUpdate);

    }

    void checkIfStarsCollected(LatLng currentPosition) {

        for (Marker star: mapUIElementsManager.routeMarkers) {
            if (SphericalUtil.computeDistanceBetween(star.getPosition(), currentPosition) < 1.0) {
                star.setVisible(false);
                Vibrator vibe = (Vibrator) mapsActivity.getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(100);

            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

}
