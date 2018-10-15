package com.app.ariadne.tumaps.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStorageManager {
    Context context;
    private final String TAG = "WRITE TO STORAGE";

    public FileStorageManager(Context context) {
        this.context = context;
    }

    public void writeToExternalStoragePublic(String path, String filename, Bitmap data) {
        try {
            boolean exists = (new File(path)).exists();
            if (!exists) {
                new File(path).mkdirs();
//            } else {
//                Log.i(TAG, "Path: " + path + " exists");
            }
            if (data != null) {

                // Open output stream
                Log.i(TAG, "Storing file: " + path + filename);
                FileOutputStream fOut = new FileOutputStream(path + filename, false);
                data.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
//            Log.i(TAG, "Failed to create path: " + path);
            e.printStackTrace();
        }
    }

}
