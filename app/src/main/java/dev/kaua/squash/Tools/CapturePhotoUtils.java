package dev.kaua.squash.Tools;

import java.io.OutputStream;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;

public class CapturePhotoUtils {

    /**
     * A copy of the Android internals  insertImage method, this method populates the
     * meta data with DATE_ADDED and DATE_TAKEN. This fixes a common problem where media
     * that is inserted manually gets saved at the end of the gallery (because date is not populated).
     * @see android.provider.MediaStore.Images.Media#insertImage(ContentResolver, Bitmap, String, String)
     */
    public static String insertImage(ContentResolver cr,
                                     Bitmap source,
                                     String title,
                                     String description) {

        final long currentTime = System.currentTimeMillis();

        ContentValues values = new ContentValues();
        values.put(Images.Media.TITLE, title);
        values.put(Images.Media.DISPLAY_NAME, title + currentTime);
        values.put(Images.Media.DESCRIPTION, description);
        values.put(Images.Media.MIME_TYPE, "image/jpeg");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Squash");

        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(Images.Media.DATE_ADDED, currentTime / 1000);
        values.put(Images.Media.DATE_TAKEN, currentTime);

        Uri url = null;
        String stringUrl = null;    /* value to be returned */

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                try (OutputStream imageOut = cr.openOutputStream(url)) {
                    source.compress(Bitmap.CompressFormat.JPEG, 50, imageOut);
                }

                long id = ContentUris.parseId(url);
                // Wait until MINI_KIND thumbnail is generated.
                Bitmap miniThumb = Images.Thumbnails.getThumbnail(cr, id, Images.Thumbnails.MINI_KIND, null);
                // This is for backward compatibility.
                storeThumbnail(cr, miniThumb, id);
            } else {
                cr.delete(url, null, null);
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = url.toString();
        }

        return stringUrl;
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static void storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = (float) 50.0 / source.getWidth();
        float scaleY = (float) 50.0 / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        final Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        final ContentValues values = new ContentValues(4);
        values.put(Images.Thumbnails.KIND, Images.Thumbnails.MICRO_KIND);
        values.put(Images.Thumbnails.IMAGE_ID,(int)id);
        values.put(Images.Thumbnails.HEIGHT,thumb.getHeight());
        values.put(Images.Thumbnails.WIDTH,thumb.getWidth());

        final Uri url = cr.insert(Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = cr.openOutputStream(url);
            thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            thumbOut.close();
        } catch (Exception ignored) {}
    }
}