package com.ookiisoftware.album.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.adapter.AlbunsAdapter;
import com.ookiisoftware.album.auxiliar.Config;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Function;
import com.ookiisoftware.album.auxiliar.Import;
import com.ookiisoftware.album.auxiliar.MapComparator;
import com.ookiisoftware.album.auxiliar.OnSwipeListener;
import com.ookiisoftware.album.modelo.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static android.widget.ImageView.ScaleType.CENTER_CROP;

public class AlbunsActivity extends AppCompatActivity {

    //region Variáveis

//    private static final String TAG = "AlbunsActivity";
    private static final int REQUEST_PERMISSION_STORANGE = 1;
    private static final int REQUEST_PERMISSION_CAMERA = 2;
    private static boolean PERMISSION_STORANGE;

    private MenuItem menuItem_onder;
    private final ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
    private final OnSwipeListener onSwipeListener = new OnSwipeListener(){
        @Override
        public void onSwipeRight() {
            super.onSwipeRight();
            Constantes.firstInit.firstUse(AlbunsActivity.this, Constantes.firstInit.DESLIZE_ID, true);
            onSwipeED(-1);
        }
        @Override
        public void onSwipeLeft() {
            super.onSwipeLeft();
            Constantes.firstInit.firstUse(AlbunsActivity.this, Constantes.firstInit.DESLIZE_ID, true);
            onSwipeED(1);
        }
        /*@Override
        public void onSingleTouch() {
            super.onSingleTouch();
            Bundle bundle = new Bundle();
            bundle.putString("folder_name", albumList.get(+Function.item_clicado.getId()).get(Function.KEY_ALBUM));
            Intent intent = new Intent(AlbunsActivity.this, SingleAlbumActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);

            ScaleAnimation scaleAnimation = new ScaleAnimation(1f, 2f, 1f, 2f, Animation.RELATIVE_TO_SELF, (float)0.1, Animation.RELATIVE_TO_SELF, (float)0);

            scaleAnimation.setDuration(1000);
            scaleAnimation.setFillAfter(true);
            recyclerView.setAnimation(scaleAnimation);
            //overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
        }*/
    };
    private AlbunsAdapter adapter;
    private StaggeredGridLayoutManager recyclerLayoutManager;
    private SharedPreferences pref;
    private Activity activity;
    private int device_orientation;
    private int spanCount;
    private String ordenarPor;
    public static boolean lerMediaNovamente;

    // Elementos do layout
    private RecyclerView recyclerView;

    //endregion

    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Import.theme.get(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albuns);
        activity = this;
        Init();
    }

    @Override
    public void onResume() {
        super.onResume();
//        adapterUpdate();
        if (lerMediaNovamente) {
            Verificar_permicao();
            lerMediaNovamente = false;
        }
        if (pref == null)
            pref = getSharedPreferences("info", MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_albuns, menu);

        //region 0 - menuItem_onder
        menuItem_onder = menu.findItem(R.id.order);

        if (Config.album.ORDER_DIRECTION.equals(Item.KEY_ORDER_DCS)) {
            menuItem_onder.setIcon(R.drawable.ic_order_dcs_enabled);
            Config.ORDER_CRESCENTE = false;
        } else {
            menuItem_onder.setIcon(R.drawable.ic_order_asc_enabled);
            Config.ORDER_CRESCENTE = true;
        }
        //endregion

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(!PERMISSION_STORANGE)
            return true;
        final SharedPreferences.Editor editor = pref.edit();
        switch (item.getItemId()) {
            case R.id.order:{
                Config.ORDER_CRESCENTE = !Config.ORDER_CRESCENTE;
                if(Config.ORDER_CRESCENTE) {
                    Config.album.ORDER_DIRECTION = Item.KEY_ORDER_ASC;
                    menuItem_onder.setIcon(R.drawable.ic_order_asc_enabled);
                } else {
                    Config.album.ORDER_DIRECTION = Item.KEY_ORDER_DCS;
                    menuItem_onder.setIcon(R.drawable.ic_order_dcs_enabled);
                }

                editor.putString(Constantes.album.ORDER, Config.album.ORDER_DIRECTION);
                editor.apply();
                Collections.sort(albumList, new MapComparator(ordenarPor, Config.album.ORDER_DIRECTION));
                adapterUpdate();
                break;
            }
            case R.id.altura: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getResources().getString(R.string.menu_tamanho));

                LayoutInflater inflater = this.getLayoutInflater();
                @SuppressLint("InflateParams")
                View dialogView = inflater.inflate(R.layout.popup_number_pick, null);
                dialog.setView(dialogView);

                final NumberPicker numberPicker = dialogView.findViewById(R.id.popup_number_picker);
                numberPicker.setMaxValue(Constantes.album.MAX_HEIGHT);
                numberPicker.setMinValue(Constantes.album.MIN_HEIGHT);
                numberPicker.setValue(Config.album.HEIGHT - 2);
                numberPicker.setWrapSelectorWheel(false);

                dialog.setPositiveButton(getResources().getString(R.string.ok), (dialogInterface, i) -> {
                    int valor = numberPicker.getValue();
                    Config.album.HEIGHT = valor + 2;
                    editor.putInt(Constantes.album.HEIGHT, Config.album.HEIGHT);
                    editor.apply();
                    adapterUpdate();
                });
                dialog.setNeutralButton(getResources().getString(R.string.cancelar), null);

                AlertDialog alerta = dialog.create();
                alerta.show();
                break;
            }
            case R.id.tema:{
                Import.theme.alternar(this);
                break;
            }
            case R.id.settings:{
                popupSettings();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_STORANGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PERMISSION_STORANGE = true;
                    CarregarAlbuns();
                } else {
                    Toast.makeText(this, "Você recusou a permissão de leitura", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PERMISSION_CAMERA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AbrirCamera();
                } else {
                    Toast.makeText(this, "Você recusou a permissão de uso da camera", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    //endregion

    //region Métodos

    @SuppressLint("ClickableViewAccessibility")
    private void Init() {
        recyclerView = findViewById(R.id.recycler);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //region Ler Configs
        pref = getSharedPreferences("info", MODE_PRIVATE);
        Constantes.firstInit.firstUse(this, Constantes.firstInit.DESLIZE_ID, false);
//        Constantes.firstInit.firstUse(this, Constantes.firstInit.TAMANHO_ID, pref, false);
//        Constantes.firstInit.firstUse(this, Constantes.firstInit.ORDEM_ID, pref, false);
//
//        Constantes.firstInit.firstUse(this, Constantes.firstInit.TAMANHO_ID, pref, true);
//        Constantes.firstInit.firstUse(this, Constantes.firstInit.ORDEM_ID, pref, true);

        Config.album.HEIGHT = pref.getInt(Constantes.album.HEIGHT, Constantes.album.DEFAULT_HEIGHT);
        Config.album.GRID = pref.getBoolean(Constantes.album.GRID, Config.album.GRID);
        Config.album.ROW = pref.getInt(Constantes.album.ROWS, Config.album.ROW);
        Config.album.HIDE = pref.getBoolean(Constantes.album.HIDE, Config.album.HIDE);
        Config.album.ORDER_DIRECTION = pref.getString(Constantes.album.ORDER, Item.KEY_ORDER_DCS);

        Config.album.MESCLAR_ARMAZENAMENTO = pref.getBoolean(Constantes.album.MESCLAR_ARMAZENAMENTO, Config.album.MESCLAR_ARMAZENAMENTO);
        Config.album.MESCLAR_FOTOS_VIDEOS = pref.getBoolean(Constantes.album.MESCLAR_FOTOS_VIDEOS, Config.album.MESCLAR_FOTOS_VIDEOS);

        Config.album.ORDENACAO = pref.getString(Constantes.album.ORDENACAO, Constantes.Ordenacao.Data.name());
        if (Config.album.ORDENACAO.equals(Constantes.Ordenacao.Nome.name())) {
            ordenarPor = Item.KEY_ALBUM;
        } else {
            ordenarPor = Item.KEY_TIMESTAMP;
        }
        //if(Config.ALBUM_GRID)
        Config.album.TYPE = CENTER_CROP;
        //else
        //  Config.ALBUM_TYPE = FIT_CENTER;
        //endregion

        //region orientação da tela
        device_orientation = AlbunsActivity.this.getResources().getConfiguration().orientation;
        if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = Config.album.ROW;
        } else {
            spanCount = Config.album.ROW * 2;
        }
        //endregion

        Verificar_permicao();

        //region Botão flutuante
        FloatingActionButton fab_camera = findViewById(R.id.fab_camera);
        fab_camera.setOnClickListener(view -> {
            String[] PERMISSIONS1 = {Manifest.permission.CAMERA};
            if (!Function.hasPermissions(getBaseContext(), PERMISSIONS1)) {
                ActivityCompat.requestPermissions(AlbunsActivity.this, PERMISSIONS1, REQUEST_PERMISSION_CAMERA);
            } else {
                AbrirCamera();
                lerMediaNovamente = true;
            }
        });
        //endregion

        recyclerLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setOnTouchListener(onSwipeListener);
    }

    private void onSwipeED(int direcao) {
        Config.album.ROW += direcao;

        if (Config.album.ROW < Constantes.album.MIN_ROWS) {
            Config.album.ROW = Constantes.album.MIN_ROWS;
            return;
        }
        else if(Config.album.ROW > Constantes.album.MAX_ROWS) {
            Config.album.ROW = Constantes.album.MAX_ROWS;
            return;
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constantes.album.ROWS, Config.album.ROW);
        editor.apply();

        if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = Config.album.ROW;
        } else {
            spanCount = Config.album.ROW * 2;
        }
        recyclerLayoutManager.setSpanCount(spanCount);
//        Import.setRecyclerMananger(recyclerView, spanCount);
    }

    private void Verificar_permicao() {
        //region Verificar permissões de leitura de midia
        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!Function.hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_STORANGE);
        } else {
            PERMISSION_STORANGE = true;
            CarregarAlbuns();
        }
    }

    private void CarregarAlbuns(){
        LoadAlbum loadAlbumTask = new LoadAlbum();
        loadAlbumTask.execute();
    }

    private void AbrirCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        startActivity(intent);
    }

    private void adapterUpdate() {
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    private void popupSettings() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.popup_settings);

        //region findViewById
        final CheckBox mesclarArmazenamento = dialog.findViewById(R.id.mesclar_armazenamento);
        final CheckBox mesclarFotosVideos = dialog.findViewById(R.id.mesclar_fotos_videos);
        final RadioButton rd_album_nome = dialog.findViewById(R.id.rd_album_nome);
        final RadioButton rd_album_data = dialog.findViewById(R.id.rd_album_data);
        final RadioButton rd_photo_nome = dialog.findViewById(R.id.rd_photo_nome);
        final RadioButton rd_photo_data = dialog.findViewById(R.id.rd_photo_data);
        final Button reload = dialog.findViewById(R.id.reload_button);
        final Button cancelar = dialog.findViewById(R.id.cancel_button);
        final Button ok = dialog.findViewById(R.id.ok_button);
        //endregion

        //region setValues
        mesclarArmazenamento.setChecked(Config.album.MESCLAR_ARMAZENAMENTO);
        mesclarFotosVideos.setChecked(Config.album.MESCLAR_FOTOS_VIDEOS);
        if (Config.album.ORDENACAO.equals(Constantes.Ordenacao.Nome.name()))
            rd_album_nome.setChecked(true);
        else
            rd_album_data.setChecked(true);
        if (Config.photo.ORDENACAO.equals(Constantes.Ordenacao.Nome.name()))
            rd_photo_nome.setChecked(true);
        else
            rd_photo_data.setChecked(true);
        //endregion

        //region setListener
        ok.setOnClickListener(v -> {
            SharedPreferences.Editor editor = pref.edit();
            Config.album.MESCLAR_ARMAZENAMENTO = mesclarArmazenamento.isChecked();
            Config.album.MESCLAR_FOTOS_VIDEOS = mesclarFotosVideos.isChecked();

            if (rd_album_nome.isChecked()) {
                Config.album.ORDENACAO = Constantes.Ordenacao.Nome.name();
                ordenarPor = Item.KEY_ALBUM;
            } else {
                Config.album.ORDENACAO = Constantes.Ordenacao.Data.name();
                ordenarPor = Item.KEY_TIMESTAMP;
            }
            if (rd_photo_nome.isChecked()) {
                Config.photo.ORDENACAO = Constantes.Ordenacao.Nome.name();
            } else {
                Config.photo.ORDENACAO = Constantes.Ordenacao.Data.name();
            }

            editor.putBoolean(Constantes.album.MESCLAR_ARMAZENAMENTO, Config.album.MESCLAR_ARMAZENAMENTO);
            editor.putBoolean(Constantes.album.MESCLAR_FOTOS_VIDEOS, Config.album.MESCLAR_FOTOS_VIDEOS);
            editor.putString(Constantes.album.ORDENACAO, Config.album.ORDENACAO);
            editor.putString(Constantes.photo.ORDENACAO, Config.photo.ORDENACAO);
            editor.apply();
            Verificar_permicao();
            dialog.dismiss();
        });
        cancelar.setOnClickListener(v -> dialog.dismiss());
        reload.setOnClickListener(v -> {
            Verificar_permicao();
            dialog.dismiss();
        });
        //endregion

        dialog.show();
    }

    //endregion

    @SuppressLint("StaticFieldLeak")
    private class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        @SuppressLint("InlinedApi")
        protected String doInBackground(String... args) {
            String xml = "";
            String selection = "_data IS NOT NULL) GROUP BY (bucket_display_name";

            Cursor cursorExternalImage = Import.getContentResolver(activity, Constantes.photo.uriExternal, Constantes.album.projectionImage, selection);
            Cursor cursorInternalImage = Import.getContentResolver(activity, Constantes.photo.uriInternal, Constantes.album.projectionImage, selection);

            Cursor cursorExternalVideo = Import.getContentResolver(activity, Constantes.video.uriExternal, Constantes.album.projectionVideo, selection);
            Cursor cursorInternalVideo = Import.getContentResolver(activity, Constantes.video.uriInternal, Constantes.album.projectionVideo, selection);

            Constantes.Type listar = Constantes.Type.Tudo;

            if (Config.album.MESCLAR_ARMAZENAMENTO) {
                Cursor cursorImage = new MergeCursor(new Cursor[]{cursorExternalImage,cursorInternalImage});
                Cursor cursorVideo = new MergeCursor(new Cursor[]{cursorExternalVideo, cursorInternalVideo});

                MoveCursorImage(cursorImage, listar, Constantes.Armazenamento.Tudo);
                MoveCursorVideo(cursorVideo, listar, Constantes.Armazenamento.Tudo);

                cursorImage.close();
                cursorVideo.close();
            }
            else {
                Cursor cursorII = new MergeCursor(new Cursor[]{cursorInternalImage});
                Cursor cursorIE = new MergeCursor(new Cursor[]{cursorExternalImage});
                Cursor cursorVI = new MergeCursor(new Cursor[]{cursorInternalVideo});
                Cursor cursorVE = new MergeCursor(new Cursor[]{cursorExternalVideo});

                MoveCursorImage(cursorII, listar, Constantes.Armazenamento.Interno);
                MoveCursorImage(cursorIE, listar, Constantes.Armazenamento.Externo);

                MoveCursorVideo(cursorVI, listar, Constantes.Armazenamento.Interno);
                MoveCursorVideo(cursorVE, listar, Constantes.Armazenamento.Externo);

                cursorII.close();
                cursorIE.close();
                cursorVI.close();
                cursorVE.close();
            }

            /*while (cursorImage.moveToNext()) {
                String path;
                String album;
                String timestamp;
                String countPhoto;

                path = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Item.getCountFotos(AlbunsActivity.this, album);

                if (!Config.album.MESCLAR_FOTOS_VIDEOS)
                    listar = Constantes.ModoListar.Fotos;

                albumList.add(Item.mappingInbox(Constantes.ITEM_TYPE_ALBUM, album, path, timestamp, Function.converToTime(timestamp),
                        countPhoto, true, listar, armazenamento));
            }*/

            /*while (cursorVideo.moveToNext()) {
                String path;
                String album;
                String timestamp;
                String countPhoto;

                path = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                album = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
                countPhoto = Item.getCountVideos(AlbunsActivity.this, album);

                if (!Config.album.MESCLAR_FOTOS_VIDEOS)
                    listar = Constantes.ModoListar.Videos;

                HashMap<String, String> item = Item.mappingInbox(Constantes.ITEM_TYPE_ALBUM, album, path, timestamp,
                        Function.converToTime(timestamp), countPhoto, false, listar, armazenamento);


                boolean contain = false;
                if (Config.album.MESCLAR_FOTOS_VIDEOS)
                    for (HashMap<String, String> _item : albumList)
                        if(Objects.equals(_item.get(Item.KEY_ALBUM), item.get(Item.KEY_ALBUM))) {
                            _item.put(Item.KEY_VIDEO_COUNT, item.get(Item.KEY_VIDEO_COUNT));
                            contain = true;
                            break;
                        }
                if (!contain)
                    albumList.add(item);
            }*/

            Collections.sort(albumList, new MapComparator(ordenarPor, Config.album.ORDER_DIRECTION)); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            if (adapter == null) {
                adapter = new AlbunsAdapter(activity, albumList, onSwipeListener) {
                    @Override
                    public void onClick(View v) {
                        super.onClick(v);
                        int itemPosition = recyclerView.getChildAdapterPosition(v);
                        HashMap<String, String> item = albumList.get(itemPosition);
                        Intent intent = new Intent(AlbunsActivity.this, AlbumActivity.class);
                        intent.putExtra(Constantes.album.NAME, item.get(Item.KEY_ALBUM));
                        intent.putExtra(Constantes.album.O_QUE_LISTAR, item.get(Item.KEY_TYPE));
                        intent.putExtra(Constantes.album.ARMAZENAMENTO, item.get(Item.KEY_ARMAZENAMENTO));
                        startActivity(intent);

//                    float from = 5, to = 1;
//                    float pivoX = 0, pivoY = 1;
//                    ScaleAnimation scaleAnimation = new ScaleAnimation(from, to, from, to, Animation.RELATIVE_TO_SELF, pivoX, Animation.RELATIVE_TO_SELF, pivoY);
//                    scaleAnimation.setDuration(500);
//                    recyclerView.setAnimation(scaleAnimation);
//                    overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
                    }
                };
                recyclerView.setAdapter(adapter);
            } else {
                adapterUpdate();
            }
            Import.pastas = albumList;
        }

        @SuppressLint("InlinedApi")
        private void MoveCursorImage(Cursor cursor, Constantes.Type listar, Constantes.Armazenamento armazenamento) {
            while (cursor.moveToNext()) {
                String path;
                String album;
                String timestamp;
                String countPhoto;

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = Item.getCountFotos(activity, album);

                if (!Config.album.MESCLAR_FOTOS_VIDEOS)
                    listar = Constantes.Type.Fotos;

                albumList.add(Item.mappingInbox(Constantes.ITEM_TYPE_ALBUM, album, path, timestamp, Function.converToTime(timestamp),
                        countPhoto, true, listar, armazenamento));
            }
        }
        @SuppressLint("InlinedApi")
        private void MoveCursorVideo(Cursor cursor, Constantes.Type listar, Constantes.Armazenamento armazenamento) {
            while (cursor.moveToNext()) {
                String path;
                String album;
                String timestamp;
                String countPhoto;

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
                countPhoto = Item.getCountVideos(activity, album);

                if (!Config.album.MESCLAR_FOTOS_VIDEOS)
                    listar = Constantes.Type.Videos;

                HashMap<String, String> item = Item.mappingInbox(Constantes.ITEM_TYPE_ALBUM, album, path, timestamp,
                        Function.converToTime(timestamp), countPhoto, false, listar, armazenamento);


                boolean contain = false;
                if (Config.album.MESCLAR_FOTOS_VIDEOS)
                    for (HashMap<String, String> _item : albumList)
                        if(Objects.equals(_item.get(Item.KEY_ALBUM), item.get(Item.KEY_ALBUM))) {
                            _item.put(Item.KEY_VIDEO_COUNT, item.get(Item.KEY_VIDEO_COUNT));
                            contain = true;
                            break;
                        }
                if (!contain)
                    albumList.add(item);
            }
        }
    }

}
