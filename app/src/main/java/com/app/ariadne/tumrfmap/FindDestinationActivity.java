package com.app.ariadne.tumrfmap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

import com.app.ariadne.tumrfmap.geojson.GeoJsonMap;
import com.app.ariadne.tumrfmap.util.EditTextWatcher;

import java.util.ArrayList;

public class FindDestinationActivity extends Activity implements AdapterView.OnItemClickListener{
    String givenDestinationName;
    ArrayAdapter adapterForDestination;
    ArrayList<String> idsForDestination;
    EditText destination;
    int returnCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the layout for the layout we created
        setContentView(R.layout.find_destination_layout);

        // Get the Intent that called for this Activity to open

        Intent activityThatCalled = getIntent();

        // Get the data that was sent

        returnCode = RESULT_OK;
        String previousActivity = activityThatCalled.getExtras().getString("callingActivity");
//        givenDestinationName = activityThatCalled.getExtras().getString("destination");
//        AutoCompleteTextView autoCompleteDestination = findViewById(R.id.destination);
//        autoCompleteDestination.setText(givenDestinationName);
//        AutoCompleteTextView autoCompleteSource = findViewById(R.id.starting_point);
//        autoCompleteSource.setText("My location");

        ArrayList<String> ids = new ArrayList<>(GeoJsonMap.sourcePointsIds);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, ids);


        destination = findViewById(R.id.destination_autocomplete);
        ListView destinationList = findViewById(R.id.destination_list);
        idsForDestination = new ArrayList<>(GeoJsonMap.targetPointsIds);
        for (int i = 0; i < idsForDestination.size(); i++) {
            if (idsForDestination.get(i).equals("entrance") || idsForDestination.get(i).equals("Entrance of building")) {
                //Log.i("FindDestinationActivity", idsForDestination.get(i));
                idsForDestination.remove(i);
            }
        }
        this.adapterForDestination =  new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, idsForDestination);
        destinationList.setAdapter(this.adapterForDestination);
        EditTextWatcher destinationTextWatcher = new EditTextWatcher(adapterForDestination);
        destination.addTextChangedListener(destinationTextWatcher);

        destinationList.setOnItemClickListener(this);


//        TextView callingActivityMessage = (TextView)
//                findViewById(R.id.calling_activity_info_text_view);
//
//        callingActivityMessage.append(" " + previousActivity);
    }

    public void onSendDestinationId(View view) {

        // Get the users name from the EditText
//        AutoCompleteTextView destination = findViewById(R.id.destination);
//        AutoCompleteTextView source = findViewById(R.id.starting_point);

        // Get the name typed into the EditText
//        String destinationName = String.valueOf(destination.getText());
//        String sourceName = String.valueOf(source.getText());

        String destinationName = String.valueOf(destination.getText());

        if (destinationName == null) {
            destinationName = "";
        }
        // Define our intention to go back to ActivityMain
        Intent goingBack = new Intent();

        // Define the String name and the value to assign to it
//        goingBack.putExtra("Starting point", sourceName);
        goingBack.putExtra("Activity Name", "FindDestinationActivity");
        goingBack.putExtra("Destination", destinationName);

        // Sends data back to the parent and can use RESULT_CANCELED, RESULT_OK, or any
        // custom values starting at RESULT_FIRST_USER. RESULT_CANCELED is sent if
        // this Activity crashes
        setResult(returnCode, goingBack);

        // Close this Activity
        finish();

    }

    public void cancelResult(View view) {
        returnCode = RESULT_CANCELED;
        onSendDestinationId(view);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //Log.i("SECOND", "Item clicked: " + idsForDestination.get(i));
        destination.setText(idsForDestination.get(i));
        adapterForDestination.clear();
        onSendDestinationId(view);
    }
}