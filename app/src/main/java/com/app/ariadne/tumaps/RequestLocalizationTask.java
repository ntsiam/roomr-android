package com.app.ariadne.tumaps;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.app.ariadne.tumaps.MapsActivity.predictionRectanglePolyline;

//import static com.roomr.ariadne.tumnavigator.MapsActivity.initialUncertaintyRadius;

public class RequestLocalizationTask extends AsyncTask {
    private GoogleMap mMap;
    TextView prediction;
    public static Marker predictionMarker;
    public static Marker deadReckoningMarker;
    private MapsActivity mapsActivity;


    public RequestLocalizationTask(GoogleMap map, TextView prediction, MapsActivity mapsActivity) {
        this.mMap = map;
        this.prediction = prediction;
        this.mapsActivity = mapsActivity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String urlString = (String) objects[0]; // URL to call

        String data = (String) objects[1]; //data to post

        OutputStream out = null;
        String response = "";
        System.out.println("Do in background localization task");
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            out = new BufferedOutputStream(urlConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(data);
            writer.flush();
            writer.close();
            out.close();
//            urlConnection.connect();

//            int responseCode = urlConnection.getResponseCode();

//            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    response+= line;
                }
//            }
        } catch (Exception e) {
            Log.i("ERROR", "Exception");
            System.out.println(e.getMessage());
        }
        return response;
    }

    @Override
    protected void onPostExecute(Object response) {
        try {
            JSONObject example = new JSONObject("{\"this\":\"is\"}");
            Log.i("EXAMPLE", example.toString());
            String res = (String) response;
            Log.i("LOCALIZE", "Server Response: " + res);
            JSONObject reader = new JSONObject(res);
            String clusterId = reader.getString("cluster-id");
            Double drLat = Double.valueOf(reader.getString("dead-reck-lat"));
            Double drLng = Double.valueOf(reader.getString("dead-reck-lng"));
            Double prLat = Double.valueOf(reader.getString("predicted-lat"));
            Double prLng = Double.valueOf(reader.getString("predicted-lng"));
            Double rectMinLat = Double.valueOf(reader.getString("min-lat"));
            Double rectMinLng = Double.valueOf(reader.getString("min-lng"));
            Double rectMaxLat = Double.valueOf(reader.getString("max-lat"));
            Double rectMaxLng = Double.valueOf(reader.getString("max-lng"));
            Log.i("LOCALIZE", "Cluster id: " +  clusterId);
            addPredictionMarkers(new LatLng(prLat, prLng), new LatLng(drLat, drLng), clusterId, rectMinLat, rectMinLng, rectMaxLat, rectMaxLng);
//            startingPoint = new LatLngWithTags(prLat, prLng);
            prediction.setText(clusterId);

//            INITIAL_UNCERTAINTY_RADIUS = 4.0;
//            mapsActivity.initLocationServices();
        } catch (JSONException e) {
            e.printStackTrace();
            String message = "Invalid Server Response";
            Toast accuracyToast = Toast.makeText(mapsActivity, message, Toast.LENGTH_SHORT);
            accuracyToast.show();

        }
    }

    void addPredictionMarkers(LatLng predictedLatLng, LatLng deadReckoningLatLng, String clusterId, double rectMinLat, double rectMinLng,
                              double rectMaxLat, double rectMaxLng) {

        System.out.println("LOCALIZE Adding markers");
        if (predictionMarker != null) {
            Log.i("LOCALIZE", "Removing prediction marker");
            predictionMarker.remove();
            predictionMarker = null;
        }
        if (deadReckoningMarker != null) {
            deadReckoningMarker.remove();
            deadReckoningMarker = null;
        }
        if (predictionRectanglePolyline != null) {
            predictionRectanglePolyline.remove();
            predictionRectanglePolyline = null;
        }
        predictionMarker = mMap.addMarker(new MarkerOptions()
                .position(predictedLatLng)
                .title("Prediction"));

        deadReckoningMarker = mMap.addMarker(new MarkerOptions()
                .position(deadReckoningLatLng)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("Dead Reckoning"));


        PolylineOptions rectOptions = new PolylineOptions()
                .add(new LatLng(rectMinLat, rectMinLng))
                .add(new LatLng(rectMaxLat, rectMinLng))  // North of the previous point, but at the same longitude
                .add(new LatLng(rectMaxLat, rectMaxLng))  // Same latitude, and 30km to the west
                .add(new LatLng(rectMinLat, rectMaxLng))  // Same longitude, and 16km to the south
                .add(new LatLng(rectMinLat, rectMinLng)); // Closes the polyline.

        predictionRectanglePolyline = mMap.addPolyline(rectOptions);
    }

}
