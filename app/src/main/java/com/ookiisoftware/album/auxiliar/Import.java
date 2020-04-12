package com.ookiisoftware.album.auxiliar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.snackbar.Snackbar;
import com.ookiisoftware.album.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Import {

    public static ArrayList<HashMap<String, String>> itemsDeletados = new ArrayList<>();
    public static ArrayList<HashMap<String, String>> pastas = new ArrayList<>();

    public static Cursor getContentResolver(Activity activity, Uri uri, String[] projection, String selection) {
        return activity.getContentResolver().query(uri, projection, selection, null, null);
    }

    public static class theme{
        public static int get(Activity activity){
            SharedPreferences pref = activity.getSharedPreferences("info", Context.MODE_PRIVATE);
            AppCompatDelegate.setDefaultNightMode(pref.getInt(Constantes.NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_NO));
            return pref.getInt(Constantes.THEME, R.style.AppThemeLight);
        }

        private static void save(Activity activity, int tema, int nitghtMode){
            SharedPreferences pref = activity.getSharedPreferences("info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt(Constantes.THEME, tema);
            editor.putInt(Constantes.NIGHT_MODE, nitghtMode);
            editor.apply();
        }

        private static void restartActivity(Activity activity){
            Intent intent = new Intent(activity, activity.getClass());
            activity.startActivity(intent);
            activity.finish();
        }

        public static void alternar(Activity activity){
            if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                save(activity, R.style.AppThemeLight, AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                save(activity, R.style.AppThemeDark, AppCompatDelegate.MODE_NIGHT_YES);
            }
            restartActivity(activity);
        }
    }

    public static class Alert{
        public static void toast(Activity activity, String texto) {
            Toast.makeText(activity, texto, Toast.LENGTH_LONG).show();
        }

        public static void snakeBar(View view, String texto){
            Snackbar.make(view, texto, Snackbar.LENGTH_LONG).setAction("Fechar", null).show();
        }

        public static void msg(String tag, String titulo, String texto){
            Log.e(tag, "msg: " + titulo + ": " + texto);
        }

        public static void msg(String tag, String titulo, String texto, String msg){
            Log.e(tag, "msg: " + titulo + ": " + texto + ": " + msg);
        }

        public static void erro(String tag, Exception ex){
            String msg = "erro:";
            msg += "\nMensagem: "+ ex.getMessage();
            msg += "\nLocalizedMessage: "+ ex.getLocalizedMessage();
            msg += "\n-------";
            Log.e(tag, msg);
        }
        public static void erro(String tag, String titulo, Exception ex){
            String msg = "erro:";
            msg += "\nMétodo: "+ titulo;
            msg += "\nMensagem: "+ ex.getMessage();
            msg += "\nLocalizedMessage: "+ ex.getLocalizedMessage();
            msg += "\n-------";
            Log.e(tag, msg);
        }
        public static void erro(String tag, String titulo, String texto){
            Log.e(tag, "erro: " + titulo + ": " + texto);
        }
    }

    public static class wallpaper {
        private static final String TAG = "wallpaper";
        static Bitmap bitmap1, bitmap2;
        public static final int RQS_OPEN_IMAGE = 542;
//        static int width, height;

        @SuppressLint("InflateParams")
        public static void set (Activity activity, String path) {
            Bitmap newOriginalBM = Import.wallpaper.loadBitmap(path);
            reloadWallpaper(activity, newOriginalBM);
        }

        static Bitmap loadBitmap(String src) {
            Bitmap bm = null;
            try {
                bm = BitmapFactory.decodeFile(src);
            } catch (Exception ex) {
                Alert.erro(TAG, "loadBitmap", ex);
            }
            return bm;
        }

        static void reloadWallpaper(Activity activity, Bitmap bm) {
            if(bm != null) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(wallpaperManager.isWallpaperSupported()) {
                        try {
                            wallpaperManager.setBitmap(bm);
                            Alert.snakeBar(activity.getCurrentFocus(), activity.getResources().getString(R.string.papel_de_parede_aplicado));
                        } catch (IOException ex) {
                            Alert.toast(activity, activity.getResources().getString(R.string.erro_papel_de_parede));
                            Alert.erro(TAG, "reloadWallpaper 0", ex);
                        }
                    }else{
                        Alert.toast(activity, activity.getResources().getString(R.string.erro_papel_de_parede_not_supported));
                    }
                }else{
                    try {
                        wallpaperManager.setBitmap(bm);
                        Alert.snakeBar(activity.getCurrentFocus(), activity.getResources().getString(R.string.papel_de_parede_aplicado));
                    } catch (IOException ex) {
                        Alert.erro(TAG, "reloadWallpaper 1", ex);
                    }
                }
            }else{
                Alert.toast(activity, activity.getResources().getString(R.string.erro_papel_de_parede));
            }
        }
    }
}
