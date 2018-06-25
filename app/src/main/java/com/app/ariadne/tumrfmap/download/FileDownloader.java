package com.app.ariadne.tumrfmap.download;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.app.ariadne.tumrfmap.tileProvider.CustomMapTileProvider;

public class FileDownloader {
    Context context;
    FileStorageManager fileStorageManager;
    RequestQueue queue;

    public FileDownloader(Context context) {

        this.context = context;
        this.fileStorageManager = new FileStorageManager(this.context);
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this.context);
        String url ="http://www.google.com";

    }


    public void downloadFile(String url, final String destinationPath, final String filename) {
        Log.i("Downloader", "URL: " + url);
        ImageRequest imgRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                //do stuff
                fileStorageManager.writeToExternalStoragePublic(destinationPath, filename, response);
//                tileProvider.getTile(x, y, zoom);
            }
        }, 0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //do stuff
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(imgRequest);
    }
}

