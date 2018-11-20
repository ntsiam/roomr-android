package com.app.ariadne.tumaps.listeners;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.map.PositionManager;

import static android.content.Context.SENSOR_SERVICE;

public class SensorChangeListener implements SensorEventListener {
    private Context applicationContext;
    private MapsActivity mapsActivity;
    private boolean hasNavigationStarted;
    private boolean isFirstTime;
    private PositionManager positionManager;
    private float stepsTillNow;
    private final static String TAG = "SensorChangeListener";
    private SensorManager mSensorManager;
    private Sensor mStepSensor;
    private Sensor mAccelerometerSensor;
    private boolean firstInstructionHasNotBeenPlayed;




    public SensorChangeListener(Context context) {
        this.applicationContext = context;
        this.mapsActivity = (MapsActivity) context;
        this.positionManager = positionManager;
        stepsTillNow = 0;
        isFirstTime = true;
        registerListeners();
        firstInstructionHasNotBeenPlayed = true;

    }

    public void registerListeners() {
        initSensors();
        mSensorManager.registerListener(this, mStepSensor, SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void unregisterListeners() {
        mSensorManager.unregisterListener(this);
    }

    private void initSensors() {
        mSensorManager = (SensorManager)mapsActivity.getSystemService(SENSOR_SERVICE);
        mStepSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
//        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void setPositionManager(PositionManager positionManager) {
        this.positionManager = positionManager;
    }

    public void startNavigation() {
        hasNavigationStarted = true;
        if (positionManager != null) {
            positionManager.startNavigation();
        }
    }

    public void cancelNavigation() {
        hasNavigationStarted = false;
        isFirstTime = true;
        if (positionManager != null) {
            positionManager.cancelNavigation();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "Sensor update: " + sensorEvent.sensor.getName());
        if (isFirstTime && positionManager != null && firstInstructionHasNotBeenPlayed) {
            positionManager.playFirstInstruction();
            firstInstructionHasNotBeenPlayed = false;
            Log.i(TAG, "I should have said the first instruction");
        } else if (positionManager == null){
            Log.i(TAG, "position manager null");
        } else if (firstInstructionHasNotBeenPlayed) {
            Log.i(TAG, "first instruction has been played");
        }
        if (hasNavigationStarted) {
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_STEP_COUNTER:
                    handleStepCounterSensor(sensorEvent.values[0]);
                    break;
                default:
                    String sensorName = sensorEvent.sensor.getName();
//                System.out.println(sensorName + ": X: " + sensorEvent.values[0]);
                    break;
            }
        }
    }

    private void handleStepCounterSensor(float steps) {
        if (isFirstTime) {
            stepsTillNow = steps - 6;
            isFirstTime = false;

        }
        float newSteps = steps - stepsTillNow;
        if (newSteps > 0.0 && positionManager != null) {
            Log.i(TAG, "Update position for steps: " + newSteps);
            positionManager.updatePosition(newSteps);
        }
        stepsTillNow = steps;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



}
