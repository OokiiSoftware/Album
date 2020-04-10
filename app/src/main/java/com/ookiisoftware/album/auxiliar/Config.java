package com.ookiisoftware.album.auxiliar;

import android.widget.ImageView;

import com.ookiisoftware.album.modelo.Item;

public class Config {

    public static class video {
        public static boolean AUTO_PLAY = true;
    }

    public static class photo {
        public static boolean GRID = false;
        public static int HEIGHT = Constantes.photo.DEFAULT_HEIGHT;
        public static ImageView.ScaleType TYPE = ImageView.ScaleType.FIT_CENTER;
        public static int ROW = 2;
        public static String ORDER_DIRECTION = Item.KEY_ORDER_DCS;
        public static String ORDENACAO = Item.KEY_TIMESTAMP;
    }

    public static class album {
        public static boolean GRID = false;
        public static boolean HIDE = false;
        public static boolean MESCLAR_ARMAZENAMENTO = true;
        public static boolean MESCLAR_FOTOS_VIDEOS = true;
        public static int HEIGHT = Constantes.album.DEFAULT_HEIGHT;
        public static int ROW = 2;
        public static String ORDER_DIRECTION = Item.KEY_ORDER_DCS;
        public static ImageView.ScaleType TYPE = ImageView.ScaleType.FIT_CENTER;
        public static String ORDENACAO = Item.KEY_TIMESTAMP;
    }

    public static boolean ORDER_CRESCENTE;
}
