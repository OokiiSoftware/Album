package com.ookiisoftware.album.auxiliar;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.ookiisoftware.album.R;

import java.util.Objects;

public class Constantes {
    //=================================================== PRIMEIRA INICIALIZACAO===============================
    public static final long MENU_TIME_TO_HIDE = 5000;
    public static final String THEME = "theme";
    public static final String NIGHT_MODE = "night_mode";
    public static final String VIDEO_IS_PLEYING = "isPlaying";
    public static final String VIDEO_TIME = "video_time";
    public static final int FILE_DELETE = 867;
    public static final int FILE_MOVE = 243;
    public static final int FILE_COPY = 126;

    public static class firstInit {

        public static final int DESLIZE_ID = R.drawable.ic_seta_dupla_horizontal;
        public static final int DOUBLE_CLICK_ID = R.drawable.ic_double_touch;
        public static final int LONG_CLICK_ID = R.drawable.ic_touch;

        public static void firstUse(Context context, int id, boolean salvar) {
            SharedPreferences pref = context.getSharedPreferences("info", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            boolean aux = pref.getBoolean(INICIALIZACAO + id, true);
            if(!aux)
                return;

            if(salvar)
                editor.putBoolean(INICIALIZACAO + id, false);
            else
                switch (id) {
                    case DESLIZE_ID: {
                        TutorialPopup(context, R.string.tutorial_deslize, R.drawable.ic_seta_dupla_horizontal);
                        break;
                    }
                   /* case TAMANHO_ID: {
                        TutorialPopup(context, R.drawable.tutorial_tamanho);
                        break;
                    }
                    case ORDEM_ID: {
                        TutorialPopup(context, R.drawable.tutorial_ordem);
                        break;
                    }
                    case GRIDE_ID: {
                        TutorialPopup(context, R.drawable.tutorial_gride);
                        break;
                    }
                    case HIDE_ID: {
                        TutorialPopup(context, R.drawable.tutorial_hide);
                        break;
                    }*/
                    case DOUBLE_CLICK_ID: {
                        TutorialPopup(context, R.string.tutorial_double_click, R.drawable.ic_double_touch);
                        break;
                    }
                    case LONG_CLICK_ID: {
                        TutorialPopup(context, R.string.tutorial_long_click, R.drawable.ic_touch);
                        break;
                    }
                }
            editor.apply();
        }
    }

    private static void TutorialPopup(Context context, int titulo, int uri) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.popup_tutorial);

        ImageView image = dialog.findViewById(R.id.photo);
        TextView _titulo = dialog.findViewById(R.id.titulo);
        CheckBox box = dialog.findViewById(R.id.checkbox);
        box.setText(R.string.nao_mostrar_novamente);

        _titulo.setText(titulo);
        image.setImageResource(uri);
        image.requestLayout();
        dialog.setOnDismissListener(dialog1 -> {
            if (box.isChecked()){
                firstInit.firstUse(context, uri, true);
            }
        });
        dialog.show();

    }
    //==========================================================================================================
    public enum Type {
        Fotos, Videos, Tudo
    }
    public enum Armazenamento {
        Interno, Externo, Tudo
    }
    public enum Ordenacao {
        Nome, Data
    }

    public static class album{
        public static final String HEIGHT = "album_height";
        public static final String HIDE = "album_hide";
        public static final String GRID = "album_grid";
        public static final String ROWS = "album_row";
        public static final String ORDER = "album_order";
        public static final String NAME = "album_name";
        public static final String ORDENACAO = "album_ordenar_por";

        public static final String MESCLAR_ARMAZENAMENTO = "mesclar_armazenamento";
        public static final String MESCLAR_FOTOS_VIDEOS = "mesclar_fotos_e_videos";

        public static final String O_QUE_LISTAR = "o_que_listar";
        public static final String ARMAZENAMENTO = "armazenamento";

        public static final int MIN_HEIGHT = 1;
        public static final int MAX_HEIGHT = 4;
        public static final int DEFAULT_HEIGHT = 3;

        public static final int MIN_ROWS = 2;
        public static final int MAX_ROWS = 4;

        public static final String[] projectionImage = {
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED};

        public static final String[] projectionVideo = {
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DATE_MODIFIED};
    }
    public static class photo{
        public static final String HEIGHT = "image_height";
        public static final String HIDE = "image_hide";
        public static final String GRID = "image_grid";
        public static final String ROWS = "image_row";
        public static final String ORDER = "image_order";
        public static final String ORDENACAO = "image_ordenar_por";

        public static final int MIN_HEIGHT = 1;
        public static final int MAX_HEIGHT = 6;
        public static final int DEFAULT_HEIGHT = 5;

        public static final int MIN_ROWS = 1;
        public static final int MAX_ROWS = 4;

        public static final Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        public static final Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        public static final String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_MODIFIED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.ORIENTATION,
                MediaStore.Images.Media.SIZE
        };
    }
    public static class video {
        public static String AUTO_PLAY = "video_auto_play";

        public static final Uri uriExternal = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        public static final Uri uriInternal = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;
        public static final String[] projection = {
                MediaStore.Video.VideoColumns._ID,
                MediaStore.Video.VideoColumns.DATA,
                MediaStore.Video.Media.DATE_MODIFIED,
                MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.WIDTH,
                MediaStore.Video.Media.SIZE
        };
    }
    public static class intent {
        public static final String ITEM_POSITION = "item_posic";
        public static final String TRANSITION_NAME = "transition_name";
    }

    public static final String INICIALIZACAO = "inicializacao";

    public static final String ITEM_TYPE_ALBUM = "album";
    public static final String ITEM_TYPE_IMAGE = "imagem";
    public static final String ITEM_TYPE_VIDEO = "video";

    public static final int LONGCLICK = 300;
    public static final int DOUBLETAP = 200;
    public static final int SWIPE_RADIO_LIMITE = 10;
    public static final int SWIPE_RANGE_LIMITE = 10;

    public static final String IMAGE_EDITOR_PATH = "image_editor_path";
    public static final String IMAGE_EDITOR_ID = "image_editor_id";

}
