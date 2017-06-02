package com.jstesta.osmapp.data.elevation;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by joseph.testa on 5/18/2017.
 * <p>
 * Height files have the extension .HGT and are signed two byte integers. The
 * bytes are in Motorola "big-endian" order with the most significant byte first,
 * directly readable by systems such as Sun SPARC, Silicon Graphics and Macintosh
 * computers using Power PC processors. DEC Alpha, most PCs and Macintosh
 * computers built after 2006 use Intel ("little-endian") order so some byte-swapping
 * may be necessary. Heights are in meters referenced to the WGS84/EGM96 geoid.
 * Data voids are assigned the value -32768.
 */
class HGTLoader {
    private static final String TAG = HGTLoader.class.getName();

    private static final int BYTES_PER_VALUE = 2;
    protected static final int DIM = 1201;
    private static final int FILE_LEN = DIM * DIM * BYTES_PER_VALUE;

    static short[] load(Context c, int id) {
        Log.d(TAG, "loading id: " + id);

        byte[] bytes;
        try {
            bytes = read(c, id);
        } catch (IOException e) {
            Log.d(TAG, "failed to load id: " + id, e);
            throw new RuntimeException("failed to load HGT file");
        }

        short[] shorts = new short[FILE_LEN / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    private static byte[] read(Context c, int id) throws IOException {
        try (InputStream is = c.getResources().openRawResource(id)) {
            byte[] bytes = new byte[FILE_LEN];

            int read = 0;
            do {
                read = is.read(bytes);
            } while (read != -1);

            return bytes;
        }
    }
}
