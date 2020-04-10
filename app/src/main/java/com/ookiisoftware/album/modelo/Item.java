package com.ookiisoftware.album.modelo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.ookiisoftware.album.R;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Import;

import java.io.IOException;
import java.util.HashMap;

public class Item {

    //region Variáveis

    private static final String TAG = "Function";
//    public static boolean atualizar_adapter = false;

    public static final String KEY_ALBUM = "album_name";
    public static final String KEY_PATH = "path";
    public static final String KEY_TYPE = "type";// fotos, videos ou tudo
    public static final String KEY_ARMAZENAMENTO = "armazenamento";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_ORDER_ASC = "asc";
    public static final String KEY_ORDER_DCS = "dcs";
    private static final String KEY_TIME = "date";
    public static final String KEY_IMAGE_COUNT = "image_count";
    public static final String KEY_VIDEO_COUNT = "video_count";

    public static final String KEY_ITEM_ID = "item_id";
    public static final String KEY_ITEM_TYPE = "item_type";
    public static final String KEY_ITEM_NAME = "bucket_display_name";
    public static final String KEY_ITEM_WIDTH = "image_width";
    public static final String KEY_ITEM_HEIGHT = "image_height";
    public static final String KEY_ITEM_SIZE = "image_size";
    public static final String KEY_ITEM_SELECTED = "image_selected";
    private static final String KEY_ITEM_ORIENTATION = "image_orientation";

    //endregion

    public static HashMap<String, String> mappingInbox(String tipo, String id, String album, String name, String path,
                                                       String timestamp, String time, String count, String size,
                                                       String height, String width, String orientation) {
        HashMap<String, String> map = new HashMap<>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TIME, time);
        map.put(KEY_IMAGE_COUNT, count);

        if(height == null)
            height = "100";
        if(width == null)
            width = "100";

        map.put(KEY_ITEM_ID, id);
        map.put(KEY_ITEM_TYPE, tipo);
        map.put(KEY_ITEM_NAME, name);
        map.put(KEY_ITEM_SIZE, size);
        map.put(KEY_ITEM_SELECTED, null);

        if (tipo.equals(Constantes.ITEM_TYPE_IMAGE)) {
            map.put(KEY_ITEM_ORIENTATION, orientation);

            if (orientation.equals("90") || orientation.equals("270")) {
                map.put(KEY_ITEM_WIDTH, height);
                map.put(KEY_ITEM_HEIGHT, width);
            } else {
                map.put(KEY_ITEM_WIDTH, width);
                map.put(KEY_ITEM_HEIGHT, height);
            }

        }
        if (tipo.equals(Constantes.ITEM_TYPE_VIDEO)) {
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(path);
            } catch (IOException ex) {
                Import.Alert.erro(TAG, ex);
            }

            int orientacao = -1;
            if(exif != null){
                orientacao = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            }
            if(orientacao != -1)
                if (orientacao == 90 || orientacao == 270){
                    map.put(KEY_ITEM_WIDTH, height);
                    map.put(KEY_ITEM_HEIGHT, width);
                } else {
                    map.put(KEY_ITEM_HEIGHT, height);
                    map.put(KEY_ITEM_WIDTH, width);
                }

        }

        return map;
    }

    public static HashMap<String, String> mappingInbox(String tipo, String album, String path, String timestamp, String time, String count, boolean isImage,
                                                       Constantes.Type type, Constantes.Armazenamento armazenamento) {
        HashMap<String, String> map = new HashMap<>();
        map.put(KEY_ALBUM, album);
        map.put(KEY_PATH, path);
        map.put(KEY_TIMESTAMP, timestamp);
        map.put(KEY_TYPE, type.name());
        map.put(KEY_ARMAZENAMENTO, armazenamento.name());
        map.put(KEY_TIME, time);
        if (isImage)
            map.put(KEY_IMAGE_COUNT, count);
        else
            map.put(KEY_VIDEO_COUNT, count);

        map.put(KEY_ITEM_TYPE, tipo);
        map.put(KEY_ITEM_SELECTED, null);

        return map;
    }

    public static String getCountFotos(Context c, String album_name) {
        Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        String selection = "bucket_display_name = \""+album_name+"\"";

        @SuppressLint("InlinedApi")
        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
        Cursor cursorExternal = c.getContentResolver().query(uriExternal, projection, selection, null, null);
        Cursor cursorInternal = c.getContentResolver().query(uriInternal, projection, selection, null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

        return cursor.getCount() == 1 ? cursor.getCount() + c.getString(R.string.foto) : cursor.getCount() + c.getString(R.string.fotos);
    }
    public static String getCountVideos(Context c, String album_name) {
        Uri uriExternal = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Uri uriInternal = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;
        String selection = "bucket_display_name = \""+album_name+"\"";

        @SuppressLint("InlinedApi")
        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
        Cursor cursorExternal = c.getContentResolver().query(uriExternal, projection, selection, null, null);
        Cursor cursorInternal = c.getContentResolver().query(uriInternal, projection, selection, null, null);
        Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});

        return cursor.getCount() == 1 ? cursor.getCount() + c.getString(R.string.video) : cursor.getCount() + c.getString(R.string.videos);
    }
}
