package com.app.ariadne.tumrfmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.app.ariadne.tumrfmap.geojson.GeoJsonMap;

import java.util.ArrayList;

public class FindOriginDestinationActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener, View.OnTouchListener {
    String givenDestinationName;
    String givenSourceName;
    private static final String TAG = "SecondActivity";
    AutoCompleteTextView autoCompleteSource;
    AutoCompleteTextView autoCompleteDestination;
    Button getDirectionsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the layout we created
        setContentView(R.layout.find_origin_destination_layout);
        getDirectionsButton = findViewById(R.id.directionsButton);

        // Get the Intent that called for this Activity to open

        Intent activityThatCalled = getIntent();

        // Get the data that was sent

        String previousActivity = activityThatCalled.getExtras().getString("callingActivity");
        givenDestinationName = activityThatCalled.getExtras().getString("destination");
        givenSourceName = activityThatCalled.getExtras().getString("source");
        autoCompleteDestination = findViewById(R.id.destination);
        autoCompleteDestination.setText(givenDestinationName);
        autoCompleteSource = findViewById(R.id.starting_point);
        if (givenSourceName == null) {
            autoCompleteSource.setText("Entrance of building");
        } else {
            autoCompleteSource.setText(givenSourceName);
        }

        ArrayList<String> sourcePointsIds = GeoJsonMap.sourcePointsIds;

        ArrayList<String> targetPointsIds = GeoJsonMap.targetPointsIds;
        for (int i = 0; i < targetPointsIds.size(); i++) {
            if (targetPointsIds.get(i).equals("entrance") || targetPointsIds.get(i).equals("Entrance of building")) {
                targetPointsIds.remove(i);
            }
        }

        ArrayAdapter<String> sourceAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, sourcePointsIds);
        ArrayAdapter<String> destinationAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, targetPointsIds);
        autoCompleteDestination.setAdapter(destinationAdapter);
        autoCompleteSource.setAdapter(sourceAdapter);
        autoCompleteSource.setOnItemClickListener(this);
        autoCompleteSource.setOnClickListener(this);
        autoCompleteSource.setOnTouchListener(this);
        autoCompleteDestination.setOnItemClickListener(this);
        autoCompleteDestination.setOnClickListener(this);
        autoCompleteDestination.setOnTouchListener(this);


//        TextView callingActivityMessage = (TextView)
//                findViewById(R.id.calling_activity_info_text_view);
//
//        callingActivityMessage.append(" " + previousActivity);
    }

    public void onSendUsersName(View view) {
        // Get the users name from the EditText
        AutoCompleteTextView destination = findViewById(R.id.destination);
        AutoCompleteTextView source = findViewById(R.id.starting_point);

        // Get the name typed into the EditText
        String destinationName = String.valueOf(destination.getText());
        String sourceName = String.valueOf(source.getText());

//        if (destinationName == null) {
//            destinationName = givenDestinationName;
//        }
//        if (sourceName == null) {
//            sourceName = givenSourceName;
//        }
        if (sourceName != null && destinationName != null && !sourceName.equals("") && !destinationName.equals("")) {
            // Define our intention to go back to ActivityMain
            Intent goingBack = new Intent();

            // Define the String name and the value to assign to it
            goingBack.putExtra("Activity Name", "FindOriginDestination");
            goingBack.putExtra("Starting point", sourceName);
            goingBack.putExtra("Destination", destinationName);

            // Sends data back to the parent and can use RESULT_CANCELED, RESULT_OK, or any
            // custom values starting at RESULT_FIRST_USER. RESULT_CANCELED is sent if
            // this Activity crashes
            setResult(RESULT_OK, goingBack);

            // Close this Activity
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Log.i("SECOND", "View clicked: " + view.getId());
        /*
        if (view.getId() == R.id.starting_point) {
            autoCompleteSource.setText("");
        }
        */
        getDirectionsButton.setEnabled(false);
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (in != null) {
            in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
        }
        AutoCompleteTextView destination = findViewById(R.id.destination);
        AutoCompleteTextView source = findViewById(R.id.starting_point);

        String destinationName = String.valueOf(destination.getText());
        String sourceName = String.valueOf(source.getText());
        if (sourceName != null && destinationName != null && !sourceName.equals("") && !destinationName.equals("")) {
            getDirectionsButton.setEnabled(true);
        }

    }

    private void resetView() {
        AutoCompleteTextView autoCompleteSource = findViewById(R.id.starting_point);
        autoCompleteSource.setText("Entrance of building");
    }

    public void changeOriginDestination(View view) {
        String destination = autoCompleteDestination.getText().toString();
        String origin = autoCompleteSource.getText().toString();
        autoCompleteDestination.setText(origin);
        autoCompleteSource.setText(destination);
    }

    @Override
    public void onBackPressed() {
        // your code.
        //Log.i(TAG, "Returning to previous activity");
        Intent goingBack = new Intent();
        setResult(RESULT_CANCELED, goingBack);
        // Close this Activity
        finish();
    }

    @Override
    public void onClick(View view) {
        //Log.i(TAG, "Clicked on view: " + view.getId());
        if (view.getId() == R.id.starting_point) {
            autoCompleteSource.setText("");
        } else if (view.getId() == R.id.destination) {
            //Log.i(TAG, "Destination view: " + view.getId());
            autoCompleteDestination.setText("");
        }

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        getDirectionsButton.setEnabled(false);
        if (view.getId() == R.id.starting_point) {
            autoCompleteSource.setText("");
        } else if (view.getId() == R.id.destination) {
            //Log.i(TAG, "Destination view: " + view.getId());
            autoCompleteDestination.setText("");
        }

        return false;
    }
}