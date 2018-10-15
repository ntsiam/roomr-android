package com.app.ariadne.tumaps.tileProvider;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.app.ariadne.tumaps.download.FileStorageManager;
import com.app.ariadne.tumrfmap.BuildConfig;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    private static final String TAG = "CustomTileProvider";
//    private AssetManager mAssets;
    private String level;
    Context context;
    String path;
    SharedPreferences sharedPref;
    String ipAddress;

    public CustomMapTileProvider(Context context) {
        this.context = context;
        String packageName = this.context.getPackageName();
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Android/data/" + packageName + "/files/";
//        path = Environment.getDataDirectory().getAbsolutePath()+ "/Android/data/" + packageName + "/files/";
        ipAddress = BuildConfig.TILE_SERVER_URL;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);

    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] image = readTileImage(x, y, zoom);
        if (image == null) {
            try {
                //Log.i(TAG, "Image was null");
                URL tileServerURL = new URL(getUrl(this.level, x, y, zoom));
                InputStream tileInputStream = tileServerURL.openStream();
                ByteArrayOutputStream tileOutputStream = new ByteArrayOutputStream();
                zza(tileInputStream, tileOutputStream);
                FileStorageManager fileStorageManager = new FileStorageManager(this.context);
                Bitmap bitmap = BitmapFactory.decodeByteArray(tileOutputStream.toByteArray(), 0,
                        tileOutputStream.toByteArray().length);

                // Could this be done in background?
                fileStorageManager.writeToExternalStoragePublic(getDestinationPath(level, x, zoom), getFilename(y), bitmap);
                return new Tile(TILE_WIDTH, TILE_HEIGHT, tileOutputStream.toByteArray());
            } catch (IOException var8) {
                //Log.i(TAG, "Exception while downloading image: " + var8);
                return null;
            }
        } else {
            //Log.i(TAG, "Image was there");
            return new Tile(TILE_WIDTH, TILE_HEIGHT, image);
        }
    }


    /**
     * This method was copied from UrlTileProvider class, no idea what happens here
     */
    private static long zza(InputStream var0, OutputStream var1) throws IOException {
        byte[] var2 = new byte[4096];

        long var3;
        int var5;
        for(var3 = 0L; (var5 = var0.read(var2)) != -1; var3 += (long)var5) {
            var1.write(var2, 0, var5);
        }

        return var3;
    }


    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;
        try {
            File file = new File(getDestinationPath(level, x, zoom), getFilename(y));
            in =  new FileInputStream(file); //mAssets.open(getTileFilename(level, x, y, zoom));
            return readTileFromFile(buffer, in);

        } catch (IOException e) {
            e.printStackTrace();
//            fileDownloader.downloadFile(getUrl(level, x, y, zoom), getDestinationPath(level, x, zoom), getFilename(y));
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private byte[] readTileFromFile(ByteArrayOutputStream buffer, InputStream in) throws IOException {
        buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[BUFFER_SIZE];

        while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();

        return buffer.toByteArray();
    }

    private String getTileFilename(int level, int x, int y, int zoom) {
        return path + "map" + "/" + zoom + '/' + level + "/" + x + '/' + y + ".png";
    }
    private String getDestinationPath(String level, int x, int zoom) {
        return path + "map" + "/" + zoom + '/' + level + "/" + x + '/';
    }
    private String getFilename(int y) {
        return y + ".png";
    }

    private String getUrl(String level, int x, int y, int zoom) {
        //PreferenceManager.setDefaultValues(this.context, R.xml.preferences, false);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
        //String ipAddress = sharedPref.getString("tile_server_ip", "Could not load tile server url");
        //String ipAddress = BuildConfig.TILE_SERVER_URL;
        //Log.i("CustomTileProvider", "Tile Server url: " + ipAddress);
        String lvlStr = String.valueOf(level);
        if (lvlStr.equals("0")) {
            //lvlStr = "";
        }
        String s = String.format("http://" + ipAddress + "/hot" + lvlStr + "/%d/%d/%d.png",
                zoom, x, y);

        Log.i(TAG, "Tile server url: " + s);
        return s;
    }

    public boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return false;
        }
        return true;
    }

}