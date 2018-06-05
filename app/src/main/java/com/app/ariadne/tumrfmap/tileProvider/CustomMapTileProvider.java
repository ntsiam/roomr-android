package com.app.ariadne.tumrfmap.tileProvider;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.app.ariadne.tumrfmap.download.FileDownloader;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomMapTileProvider implements TileProvider {
    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;

    private AssetManager mAssets;
    private int level;
    Context context;
    FileDownloader fileDownloader;
    String path;


    public CustomMapTileProvider(AssetManager assets, Context context) {
        mAssets = assets;
        this.context = context;
        this.fileDownloader = new FileDownloader(context);
        String packageName = this.context.getPackageName();
        path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Android/data/" + packageName + "/files/";

    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        byte[] image = readTileImage(x, y, zoom);
        return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        InputStream in = null;
        ByteArrayOutputStream buffer = null;
        try {
            File file = new File(getDestinationPath(level, x, zoom),getFilename(y));
            in =  new FileInputStream(file); //mAssets.open(getTileFilename(level, x, y, zoom));


            buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[BUFFER_SIZE];

            while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();

            return buffer.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            fileDownloader.downloadFile(getUrl(level, x, y, zoom), getDestinationPath(level, x, zoom), getFilename(y));
            return null;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) try { in.close(); } catch (Exception ignored) {}
            if (buffer != null) try { buffer.close(); } catch (Exception ignored) {}
        }
    }

    private String getTileFilename(int level, int x, int y, int zoom) {
        return path + "map" + "/" + zoom + '/' + level + "/" + x + '/' + y + ".png";
    }
    private String getDestinationPath(int level, int x, int zoom) {
        return path + "map" + "/" + zoom + '/' + level + "/" + x + '/';
    }
    private String getFilename(int y) {
        return y + ".png";
    }

    private String getUrl(int level, int x, int y, int zoom) {
        String ipAddress = "ec2-18-191-35-229.us-east-2.compute.amazonaws.com";
        String lvlStr = String.valueOf(level);
        if (lvlStr.equals("0")) {
//            lvlStr = "";
        }
        String s = String.format("http://" + ipAddress + "/hot" + lvlStr + "/%d/%d/%d.png",
                zoom, x, y);

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