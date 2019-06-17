package com.app.ariadne.tumaps.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.app.ariadne.tumaps.MapsActivity;
import com.app.ariadne.tumaps.db.models.WifiAPDetails;
import com.google.firebase.database.DatabaseReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import android.support.v4.app.ActivityCompat;

public class WifiScanner {
    WifiManager wifi;
    List<ScanResult> results;
//    ArrayAdapter adapter;
    ArrayList<String> wifiList = new ArrayList<>();
    private DatabaseReference mDatabase;
    Timer myTimer;
    Handler myHandler;
    Context context;
    public static ArrayList<WifiAPDetails> wifiAPDetailsArrayList = new ArrayList<>();
    private MapsActivity mapsActivity;

    public WifiScanner(WifiManager wifiManager, Context context, MapsActivity mapsActivity) {
        wifi = wifiManager;
        initTimerToUpdateWifiList();
        this.context = context;
        this.mapsActivity = mapsActivity;
    }

    private void initTimerToUpdateWifiList() {
        if (myTimer != null) {
            myTimer.cancel();
        }
//        if (myHandler == null) {
//            myHandler = new Handler();
//        }
//        myHandler.post(scannerTask);
        myTimer = new Timer();
        myTimer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        //Called at every 1000 milliseconds (1 second)
                        scanWifiNetworks();
                    }
                },
                //set the amount of time in milliseconds before first execution
                1000,
                //Set the amount of time between each execution (in milliseconds)
                5000);
    }

    final Runnable scannerTask = new Runnable() {
        public void run() {
            scanWifiNetworks();
        }
    };

    private void scanWifiNetworks(){
        wifiList.clear();
        Log.i("WIFISCANNER", "Start scanning for wifi..");
        context.registerReceiver(wifi_receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
    }

    // This is called when wifi.startScan returns
    BroadcastReceiver wifi_receiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            Log.i("WIFISCANNER", "Wifi scan results ready!");
            results = wifi.getScanResults();
            int size = results.size();
            MapsActivity.wifiAPDetailsList = new ArrayList<>();
//            JSONObject wifiAPDetailsJSON = new JSONObject();
            JSONArray wifiAPDetailsListJSON = new JSONArray();
//            wifiAPDetailsArrayList = new ArrayList<>();
            context.unregisterReceiver(this);
            if (size <= 0) {
                Log.i("WIFISCANNER","No WiFi results!");
            } else {
                try {
                    while (size > 0) {
                        JSONObject wifiAPDetailsJSON = new JSONObject();
                        size--;
                        wifiList.add(results.get(size).level + ", " + results.get(size).BSSID + " " + results.get(size).SSID);
                        MapsActivity.wifiAPDetailsList.add(new WifiAPDetails(results.get(size).level, results.get(size).BSSID, results.get(size).frequency));
//                        wifiAPDetailsArrayList.add(new WifiAPDetails(results.get(size).level, results.get(size).BSSID, results.get(size).frequency));
//                        adapter.notifyDataSetChanged();
                        //System.out.println("more WiFi networks!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                        Log.i("WIFISCANNER", "Wifi: " + results.get(size).BSSID + ", strength" + results.get(size).level);
                        wifiAPDetailsJSON.put("signal_strength", results.get(size).level);


                        // Create MessageDigest instance for MD5
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        //Add password bytes to digest
                        md.update(results.get(size).BSSID.getBytes());

                        //Get the hash's bytes
                        byte[] bytes = md.digest();
                        //This bytes[] has bytes in decimal format;
                        //Convert it to hexadecimal format
                        StringBuilder sb = new StringBuilder();
                        for(int i=0; i< bytes.length ;i++) {
                            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                        }
                        //Get complete hashed password in hex format
                        String hashedAddress = sb.toString();
//                        Log.i("Scanner", "hashedAddress: " + hashedAddress);

                        wifiAPDetailsJSON.put("mac_address", hashedAddress);
                        wifiAPDetailsJSON.put("frequency", results.get(size).frequency);
                        wifiAPDetailsListJSON.put(wifiAPDetailsJSON);

                    }
//                    MapsActivity.streamWifiToTangle();
                    Log.i("WIFISCANNER", "Streaming data...");
                    mapsActivity.streamWifiToTangle(wifiAPDetailsListJSON);
//                    int[] signalStrengths = extractSignalStrengthForAPs(MapsActivity.wifiAPDetailsList);
//                predictLocation(signalStrengths);
//                    writeToExternalStoragePublic(fileNameFromUserToStoreWifiData.getText().toString(), wifiAPDetailsList);
//                    writeSignalStrengthsToFirebase(wifiAPDetailsList);
                } catch (Exception e) {
                    System.out.println("WifScanner Exception: " + e);

                }
            }
//            myHandler.post(scannerTask);

        }
    };

}
