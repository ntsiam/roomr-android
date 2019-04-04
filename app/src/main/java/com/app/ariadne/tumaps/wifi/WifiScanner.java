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

    public WifiScanner(WifiManager wifiManager, Context context) {
        wifi = wifiManager;
        initTimerToUpdateWifiList();
        this.context = context;
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
//            wifiAPDetailsArrayList = new ArrayList<>();
            context.unregisterReceiver(this);
            if (size <= 0) {
                Log.i("WIFISCANNER","No WiFi results!");
            } else {
                try {
                    while (size > 0) {
                        size--;
                        wifiList.add(results.get(size).level + ", " + results.get(size).BSSID + " " + results.get(size).SSID);
                        MapsActivity.wifiAPDetailsList.add(new WifiAPDetails(results.get(size).level, results.get(size).BSSID, results.get(size).frequency));
//                        wifiAPDetailsArrayList.add(new WifiAPDetails(results.get(size).level, results.get(size).BSSID, results.get(size).frequency));
//                        adapter.notifyDataSetChanged();
                        //System.out.println("more WiFi networks!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        Log.i("WIFISCANNER", "Wifi: " + results.get(size).BSSID + ", strength" + results.get(size).level);

                    }
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
