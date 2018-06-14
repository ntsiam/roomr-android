package com.app.ariadne.tumrfmap;

import com.google.android.gms.maps.model.UrlTileProvider;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class WMSTileProvider extends UrlTileProvider {

    public WMSTileProvider(int i, int i1) {
        super(i, i1);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {

        /* Define the URL pattern for the tile images */
        String s = String.format("http://my.image.server/images/%d/%d/%d.png",
                zoom, x, y);

        if (!checkTileExists(x, y, zoom)) {
            return null;
        }

        try {
            return new URL(s);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /*
     * Check that the tile server supports the requested x, y and zoom.
     * Complete this stub according to the tile range you support.
     * If you support a limited range of tiles at different zoom levels, then you
     * need to define the supported x, y range at each zoom level.
     */
    private boolean checkTileExists(int x, int y, int zoom) {
        int minZoom = 12;
        int maxZoom = 16;

        if ((zoom < minZoom || zoom > maxZoom)) {
            return false;
        }

        return true;
    }
}