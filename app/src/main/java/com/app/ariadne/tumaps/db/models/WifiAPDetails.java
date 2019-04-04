package com.app.ariadne.tumaps.db.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class WifiAPDetails {
    private int signalStrength;
    private String macAddress;
    private int frequency;

    public WifiAPDetails(int signalStrength, String macAddress, int frequency) {
        this.signalStrength = signalStrength;
        this.macAddress = macAddress;
        this.frequency = frequency;
    }

    public int getSignalStrength() {
        return -signalStrength;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getFrequency() { return frequency; }

    public void setSignalStrength(int signalStrength) {
        this.signalStrength = signalStrength;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setFrequency(int frequency) { this.frequency = frequency; }

    public String toString() {
        return this.macAddress + " " + String.valueOf(this.signalStrength) + "\n";
    }
}
