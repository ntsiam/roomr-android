package com.app.ariadne.tumrfmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.app.ariadne.tumrfmap.geojson.GeoJsonMap;

public class FindDestinationActivity extends Activity implements AdapterView.OnItemClickListener{
    String givenDestinationName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the layout we created
        setContentView(R.layout.find_destination_layout);

        // Get the Intent that called for this Activity to open

        Intent activityThatCalled = getIntent();

        // Get the data that was sent

        String previousActivity = activityThatCalled.getExtras().getString("callingActivity");
        givenDestinationName = activityThatCalled.getExtras().getString("destination");
        AutoCompleteTextView autoCompleteDestination = findViewById(R.id.destination);
        autoCompleteDestination.setText(givenDestinationName);
        AutoCompleteTextView autoCompleteSource = findViewById(R.id.starting_point);
        autoCompleteSource.setText("My location");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, GeoJsonMap.sourcePointsIds);
        autoCompleteDestination.setAdapter(adapter);
        autoCompleteSource.setAdapter(adapter);
        autoCompleteDestination.setOnItemClickListener(this);


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

        if (destinationName == null) {
            destinationName = givenDestinationName;
        }
        // Define our intention to go back to ActivityMain
        Intent goingBack = new Intent();

        // Define the String name and the value to assign to it
        goingBack.putExtra("Starting point", sourceName);
        goingBack.putExtra("Destination", destinationName);

        // Sends data back to the parent and can use RESULT_CANCELED, RESULT_OK, or any
        // custom values starting at RESULT_FIRST_USER. RESULT_CANCELED is sent if
        // this Activity crashes
        setResult(RESULT_OK, goingBack);

        // Close this Activity
        finish();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.i("SECOND", "View clicked: " + view.getId());
    }
}