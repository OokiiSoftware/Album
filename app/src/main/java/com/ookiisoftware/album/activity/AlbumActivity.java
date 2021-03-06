package com.ookiisoftware.album.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.adapter.AlbumAdapter;
import com.ookiisoftware.album.adapter.AlbunsAdapter;
import com.ookiisoftware.album.async.InBackground;
import com.ookiisoftware.album.auxiliar.Config;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Function;
import com.ookiisoftware.album.auxiliar.Import;
import com.ookiisoftware.album.auxiliar.MapComparator;
import com.ookiisoftware.album.auxiliar.OnSwipeListener;
import com.ookiisoftware.album.modelo.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

public class AlbumActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    //region Variáveis
//    private static final String TAG = "AlbumActivity";
    // Layout elements
    private Dialog dialog = null;
    private TextView fab_text_count;
    private RecyclerView recyclerView;
    private BottomNavigationView navBar;
    private BottomNavigationView navBar_2;
    private FloatingActionButton fab_item_count;
    private MenuItem menuItem_altura, menuItem_onder;

    private ArrayList<HashMap<String, String>> imageList = new ArrayList<>();
    private OnSwipeListener onSwipeListener = new OnSwipeListener() {
        @Override
        public void onSwipeRight() {
            super.onSwipeRight();
            Constantes.firstInit.firstUse(AlbumActivity.this, Constantes.firstInit.DESLIZE_ID, true);
            onSwipeED(-1);
        }

        @Override
        public void onSwipeLeft() {
            super.onSwipeLeft();
            Constantes.firstInit.firstUse(AlbumActivity.this, Constantes.firstInit.DESLIZE_ID, true);
            onSwipeED(1);
        }

        @Override
        public void onTouchUp() {
            if (dialog != null)
                if (dialog.isShowing())
                    dialog.dismiss();
        }
    };
    private int device_orientation;
    private SharedPreferences pref;
    private StaggeredGridLayoutManager recyclerLayoutManager;
    private AlbumAdapter adapter;
    private Activity activity;
    private String album_name;
    private boolean SELECIONAR_ITEM;
    private String ordenarPor;
    private Constantes.Type listar;
    private Constantes.Armazenamento armazenamento;
//    private int[] doisUltimos = new int[2];
    private int spanCount;

    private AlbunsAdapter adapterCopyMove;
    private InBackground background;
    private boolean disableAdapterCopyMoveClick;

    //endregion

    //region Overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Import.theme.get(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        activity = this;
        Init();
    }

    @Override
    public void onBackPressed() {
        if(SELECIONAR_ITEM){
            adapter.unselectAll();
            RemoverSelecaoDeTodosOsItens();
        } else
            super.onBackPressed();
//        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Import.itemsDeletados.size() > 0) {
            imageList.removeAll(Import.itemsDeletados);
            if(adapter != null)
                adapter.notifyDataSetChanged();
            Import.itemsDeletados.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_album, menu);

        //region menuItem_gride
        MenuItem menuItem_gride = menu.findItem(R.id.grid);
        menuItem_gride.setChecked(Config.photo.GRID);
        if (Config.photo.GRID)
            menuItem_gride.setIcon(R.drawable.ic_grid_no_fix_enabled);
        else
            menuItem_gride.setIcon(R.drawable.ic_grid_fix_enabled);
        //endregion

        //region menuItem_onder
        menuItem_onder = menu.findItem(R.id.order);

        if (Config.photo.ORDER_DIRECTION.equals(Item.KEY_ORDER_DCS)) {
            menuItem_onder.setIcon(R.drawable.ic_order_dcs_enabled);
            Config.ORDER_CRESCENTE = false;
        } else {
            menuItem_onder.setIcon(R.drawable.ic_order_asc_enabled);
            Config.ORDER_CRESCENTE = true;
        }
        //endregion

        //region menuItem_altura
        menuItem_altura = menu.findItem(R.id.altura);
        menuItem_altura.setVisible(Config.photo.GRID);
        //endregion

        /*{
            i++;
            menuItem_usar_como = menu.getItem(i);
            menuItem_usar_como.setVisible(false);
        }*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences.Editor editor = pref.edit();
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.grid: {
                Config.photo.GRID = !Config.photo.GRID;

                Config.photo.TYPE = Config.photo.GRID ? CENTER_CROP : FIT_CENTER;
                item.setIcon(Config.photo.GRID ? R.drawable.ic_grid_no_fix_enabled : R.drawable.ic_grid_fix_enabled);

                menuItem_altura.setVisible(Config.photo.GRID);
                editor.putBoolean(Constantes.photo.GRID, Config.photo.GRID);
                item.setChecked(Config.photo.GRID);
                Common(editor);
                break;
            }
            case R.id.order:{
                Config.ORDER_CRESCENTE = !Config.ORDER_CRESCENTE;
                Ordem(Config.ORDER_CRESCENTE, editor);
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
                numberPicker.setMaxValue(Constantes.photo.MAX_HEIGHT);
                numberPicker.setMinValue(Constantes.photo.MIN_HEIGHT);
                numberPicker.setValue(Config.photo.HEIGHT - 2);
                numberPicker.setWrapSelectorWheel(false);

                dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                    int valor  = numberPicker.getValue();
                    Config.photo.HEIGHT = valor + 2;
                    editor.putInt(Constantes.photo.HEIGHT, Config.photo.HEIGHT);
                    Common(editor);
                });
                dialog.setNeutralButton("Cancelar", null);

                AlertDialog alerta = dialog.create();
                alerta.show();
                break;
            }
            /*case R.id.image_usar_como: {
                WindowManager windowManager = getWindowManager();
                Function.SetWallpaper(this, imageList.get(itens_selecionados.get(0)).get(Function.KEY_PATH), windowManager, this.getLayoutInflater());
                break;
            }*/
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ArrayList<HashMap<String, String>> itemsTemp = adapter.getSelectedAll();
        switch (item.getItemId()){
            case R.id.nav_share: {
                Function.CompartilharDados(AlbumActivity.this, itemsTemp);
//                RemoverSelecaoDeTodosOsItens();
                break;
            }
            case R.id.nav_delete: {
                Function.DeletarDados(this, itemsTemp, adapter, null);
                break;
            }
            case R.id.nav_selecionar_intervalo: {
                int quant = adapter.selectIntervalo();
                if (quant >= 0)
                    fab_text_count.setText(IntToString(quant));
                break;
            }
            case R.id.nav_mais: {
                if (Objects.equals(item.getTitle(), getString(R.string.mais))) {
//                    navBar_2.setVisibility(View.VISIBLE);
                    bottonMenu_2(true);
                    item.setIcon(R.drawable.ic_order_asc);
                    item.setTitle(R.string.menos);
                } else {
//                    navBar_2.setVisibility(View.GONE);
                    bottonMenu_2(false);
                    item.setIcon(R.drawable.ic_order_dcs);
                    item.setTitle(R.string.mais);
                }
                break;
            }

            case R.id.nav_copiar: {
                if (itemsTemp.size() == 0)
                    break;
                popupCopyMoveFile(itemsTemp, true);
                break;
            }
            case R.id.nav_mover: {
                if (itemsTemp.size() == 0)
                    break;
                popupCopyMoveFile(itemsTemp,false);
                break;
            }
            case R.id.nav_set_wallpaper: {
                if (itemsTemp.size() != 1)
                    break;
                String path = itemsTemp.get(0).get(Item.KEY_PATH);
                Import.wallpaper.set(activity, path);
                break;
            }
            case R.id.nav_selecionar_tudo: {
                if (Objects.equals(item.getTitle(), getString(R.string.selecionar_tudo))) {
                    fab_text_count.setText(IntToString(adapter.selectAll()));

                    navBar.getMenu().findItem(R.id.nav_share).setEnabled(true);
                    navBar.getMenu().findItem(R.id.nav_delete).setEnabled(true);
                    navBar_2.getMenu().findItem(R.id.nav_mover).setEnabled(true);
                    navBar_2.getMenu().findItem(R.id.nav_copiar).setEnabled(true);
                    item.setIcon(R.drawable.ic_unselect);
                    item.setTitle(R.string.deselecionar_tudo);
                } else {
                    if (itemsTemp.size() != 0) {
                        adapter.unselectAll();
                        fab_text_count.setText("0");
                        navBar.getMenu().findItem(R.id.nav_share).setEnabled(false);
                        navBar.getMenu().findItem(R.id.nav_delete).setEnabled(false);
                        navBar_2.getMenu().findItem(R.id.nav_mover).setEnabled(false);
                        navBar_2.getMenu().findItem(R.id.nav_copiar).setEnabled(false);
                        item.setIcon(R.drawable.ic_select);
                        item.setTitle(R.string.selecionar_tudo);
                    }
                }

//                item.setEnabled(false);
                break;
            }
        }
        item.setCheckable(false);
        return true;
    }

    //endregion

    //region Métodos

    @SuppressLint("ClickableViewAccessibility")
    private void Init() {
        //region findViewById
        Toolbar toolbar = findViewById(R.id.toolbar);
        navBar = findViewById(R.id.single_album_navbar);
        navBar_2 = findViewById(R.id.single_album_navbar_2);
        recyclerView = findViewById(R.id.recycler);
        fab_item_count = findViewById(R.id.fab_item_count);
        fab_text_count = findViewById(R.id.fab_text_count);
        //endregion

        //region Navbar

        bottonMenu(false);
        bottonMenu_2(false);

        navBar.setOnNavigationItemSelectedListener(this);
        navBar.setVisibility(View.GONE);
        navBar.getMenu().findItem(R.id.nav_share).setCheckable(false);

        navBar_2.setOnNavigationItemSelectedListener(this);
        navBar_2.setVisibility(View.GONE);
        navBar_2.getMenu().findItem(R.id.nav_copiar).setCheckable(false);
        navBar_2.getMenu().findItem(R.id.nav_mover).setCheckable(false);

        //endregion

        //region Bundle
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            album_name = bundle.getString(Constantes.album.NAME);
            String type = bundle.getString(Constantes.album.O_QUE_LISTAR);
            String arm = bundle.getString(Constantes.album.ARMAZENAMENTO);
            if (type == null)
                type = Constantes.Type.tudo.name();
            if (arm == null)
                arm = Constantes.Armazenamento.tudo.name();

            switch (type) {
                case Constantes.ITEM_TYPE_IMAGE: {
                    listar = Constantes.Type.imagem;
                    break;
                }
                case Constantes.ITEM_TYPE_VIDEO: {
                    listar = Constantes.Type.video;
                    break;
                }
                case Constantes.ITEM_TUDO: {
                    listar = Constantes.Type.tudo;
                    break;
                }
            }
            switch (arm) {
                case Constantes.ITEM_INTERNO: {
                    armazenamento = Constantes.Armazenamento.interno;
                    break;
                }
                case Constantes.ITEM_EXTERNO: {
                    armazenamento = Constantes.Armazenamento.externo;
                    break;
                }
                case Constantes.ITEM_TUDO: {
                    armazenamento = Constantes.Armazenamento.tudo;
                    break;
                }
            }
        }

        //endregion

        //region Mostrar tutorial e ler configurações
        pref = getSharedPreferences("info", MODE_PRIVATE);
        Constantes.firstInit.firstUse(this, Constantes.firstInit.DOUBLE_CLICK_ID, false);
        Constantes.firstInit.firstUse(this, Constantes.firstInit.LONG_CLICK_ID, false);

        Config.photo.HEIGHT = pref.getInt(Constantes.photo.HEIGHT, Config.photo.HEIGHT);
        Config.photo.GRID = pref.getBoolean(Constantes.photo.GRID, Config.photo.GRID);
        Config.photo.ROW = pref.getInt(Constantes.photo.ROWS, Config.photo.ROW);
        Config.photo.ORDER_DIRECTION = pref.getString(Constantes.photo.ORDER, Item.KEY_ORDER_DCS);

        if(Config.photo.GRID)
            Config.photo.TYPE = CENTER_CROP;
        else
            Config.photo.TYPE = FIT_CENTER;

        if (Config.photo.ORDENACAO.equals(Constantes.Ordenacao.Nome.name())) {
            ordenarPor = Item.KEY_ITEM_NAME;
        }
        else {
            ordenarPor = Item.KEY_TIMESTAMP;
        }
        //endregion

        //region SupportActionBar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(album_name);
        }
        //endregion

        //region Configuração do botão flutuante
        fab_item_count.hide();
        fab_item_count.setEnabled(false);
        fab_item_count.setOnClickListener(view -> {
            Intent intent = new Intent(AlbumActivity.this, EditorActivity.class);
            intent.putExtra(Constantes.intent.EDITOR_PATH, adapter.getSelected().get(Item.KEY_PATH));
            intent.putExtra(Constantes.intent.EDITOR_ID, Long.parseLong(Objects.requireNonNull(adapter.getSelected().get(Item.KEY_ITEM_ID))));
            startActivity(intent);
            //Toast.makeText(SingleAlbumActivity.this, "Função indisponível", Toast.LENGTH_SHORT).show();
            RemoverSelecaoDeTodosOsItens();
        });
        //endregion

        //region Pegando a orientação da tela
        device_orientation = this.getResources().getConfiguration().orientation;
        if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = Config.photo.ROW;
        } else {
            spanCount = Config.photo.ROW * 2;
        }
        //endregion

        recyclerLayoutManager = new StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.setOnTouchListener(onSwipeListener);

        LoadAlbumImages loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();
    }

    private void RemoverSelecaoDeTodosOsItens() {
        SELECIONAR_ITEM = false;
        fab_text_count.setText(null);
        fab_item_count.hide();

        //region Animation
        bottonMenu(false);
        //endregion
    }

    private void bottonMenu(boolean mostrar) {
        if (mostrar) {
            navBar.animate().translationY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    navBar.setVisibility(View.VISIBLE);
                    if (Objects.equals(navBar.getMenu().findItem(R.id.nav_mais).getTitle(), getResources().getString(R.string.menos)))
                        bottonMenu_2(true);
                }

                @Override
                public void onAnimationEnd(Animator animation) {}

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        } else {
            navBar.animate().translationY(-navBar.getHeight()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    bottonMenu_2(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    navBar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }
    }
    private void bottonMenu_2(boolean mostrar) {
        if (mostrar) {
            navBar_2.animate().translationY(0).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    navBar_2.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {}

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        } else {
            navBar_2.animate().translationY(-navBar_2.getHeight()).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    navBar_2.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }
    }

    private void Ordem(boolean ordem_asc, SharedPreferences.Editor editor) {
        if(ordem_asc) {
            Config.photo.ORDER_DIRECTION = Item.KEY_ORDER_ASC;
            menuItem_onder.setIcon(R.drawable.ic_order_asc_enabled);
        } else {
            Config.photo.ORDER_DIRECTION = Item.KEY_ORDER_DCS;
            menuItem_onder.setIcon(R.drawable.ic_order_dcs_enabled);
        }

        editor.putString(Constantes.photo.ORDER, Config.photo.ORDER_DIRECTION);
        Common(editor);
    }

    private void Common(SharedPreferences.Editor editor) {
        editor.apply();
        Collections.sort(imageList, new MapComparator(ordenarPor, Config.photo.ORDER_DIRECTION));
        if(adapter != null)
            adapter.notifyDataSetChanged();
    }

    private String IntToString(int i){
        return "" + i;
    }

    private void openPreviewActivity(View view, int position) {
        String transitionName = "transition";
        Intent intent = new Intent(this, PreviewPagerActivity.class);
        intent.putExtra(Constantes.intent.TRANSITION_NAME, transitionName);
        intent.putExtra(Constantes.intent.ITEM_POSITION, position);
        Function.imageList = imageList;

        ViewCompat.setTransitionName(view, transitionName);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, view, transitionName);
        startActivity(intent, options.toBundle());

        //=========== Animação
        /*int width = Integer.parseInt(imageList.get(position).get(Function.KEY_IMAGE_WIDTH));
        int height = Integer.parseInt(imageList.get(position).get(Function.KEY_IMAGE_HEIGHT));
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom_in);
        animation.initialize(width, height, recyclerView.getWidth(), recyclerView.getHeight());
        Function.item_clicado.startAnimation(animation);*/
    }

    private void popupPhoto(boolean isImagem, Uri uri) {
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_photo);
        dialog.setOnDismissListener(dialog -> recyclerView.suppressLayout(false));
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        if(isImagem){
            ImageView image = dialog.findViewById(R.id.photo);
            image.setVisibility(View.VISIBLE);
            Glide.with(this).load(new File(uri.toString())).into(image);
            image.requestLayout();
        } else {
            VideoView video = dialog.findViewById(R.id.video);
            video.setVisibility(View.VISIBLE);
            video.setVideoURI(uri);
            video.setOnCompletionListener(MediaPlayer::start);
            video.start();
            video.requestLayout();
            video.bringToFront();
        }
    }

    private void popupCopyMoveFile(ArrayList<HashMap<String, String>> items, boolean isCopy) {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.popup_copy_move);

        RecyclerView recyclerView = dialog.findViewById(R.id.recycler);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        Button cancel = dialog.findViewById(R.id.cancel_button);

        adapterCopyMove = new AlbunsAdapter(activity, Import.pastas, onSwipeListener) {
            @Override
            public void onClick(View v) {
                if (!disableAdapterCopyMoveClick) {
                    int itemPosition = recyclerView.getChildAdapterPosition(v);
                    HashMap<String, String> item = adapterCopyMove.getItem(itemPosition);
                    String destino = item.get(Item.KEY_PATH);
                    int acao;
                    if (isCopy)
                        acao = Constantes.FILE_COPY;
                    else
                        acao = Constantes.FILE_MOVE;

                    Import.Alert.snakeBar(activity, getResources().getString(R.string.aguarde));
                    progressBar.setVisibility(View.VISIBLE);
                    disableAdapterCopyMoveClick = true;
                    background = new InBackground(activity, items, adapter, dialog, acao, destino);
                    background.execute();
                } else
                    Import.Alert.snakeBar(activity, getResources().getString(R.string.aguarde));
            }
        };
        recyclerView.setAdapter(adapterCopyMove);

        cancel.setOnClickListener(v2 -> {
            if (background != null)
                background.cancel(true);
            Import.Alert.snakeBar(activity, getResources().getString(R.string.acao_cancelada));
            dialog.dismiss();
        });

        dialog.show();
    }

    private void onSwipeED(int direcao) {
        Config.photo.ROW += direcao;

        if (Config.photo.ROW < Constantes.photo.MIN_ROWS) {
            Config.photo.ROW = Constantes.photo.MIN_ROWS;
            return;
        }
        else if(Config.photo.ROW > Constantes.photo.MAX_ROWS) {
            Config.photo.ROW = Constantes.photo.MAX_ROWS;
            return;
        }

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(Constantes.photo.ROWS, Config.photo.ROW);
        editor.apply();

        if (device_orientation == Configuration.ORIENTATION_PORTRAIT) {
            spanCount = Config.photo.ROW;
        } else {
            spanCount = Config.photo.ROW * 2;
        }

        recyclerLayoutManager.setSpanCount(spanCount);
    }

    //endregion

    @SuppressLint("StaticFieldLeak")
    private class LoadAlbumImages extends AsyncTask<String, Void, String> {
        private int clicks = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        @SuppressLint("InlinedApi")
        protected String doInBackground(String... args) {
            String xml = "";
            String selection = "bucket_display_name = \"" + album_name + "\"";

            //region Cursor
            Cursor cursorExternalImage = Import.getContentResolver(activity, Constantes.photo.uriExternal, Constantes.photo.projection, selection);
            Cursor cursorInternalImage = Import.getContentResolver(activity, Constantes.photo.uriInternal, Constantes.photo.projection, selection);

            Cursor cursorExternalVideo = Import.getContentResolver(activity, Constantes.video.uriExternal, Constantes.video.projection, selection);
            Cursor cursorInternalVideo = Import.getContentResolver(activity, Constantes.video.uriInternal, Constantes.video.projection, selection);

            Cursor cursorImage;
            Cursor cursorVideo;
            if (armazenamento == Constantes.Armazenamento.interno) {
                cursorImage = new MergeCursor(new Cursor[]{cursorInternalImage});
                cursorVideo = new MergeCursor(new Cursor[]{cursorInternalVideo});
            } else if (armazenamento == Constantes.Armazenamento.externo) {
                cursorImage = new MergeCursor(new Cursor[]{cursorExternalImage});
                cursorVideo = new MergeCursor(new Cursor[]{cursorExternalVideo});
            } else {
                cursorImage = new MergeCursor(new Cursor[]{cursorExternalImage, cursorInternalImage});
                cursorVideo = new MergeCursor(new Cursor[]{cursorExternalVideo, cursorInternalVideo});
            }

            //endregion

            if (listar == Constantes.Type.imagem || listar == Constantes.Type.tudo)
                while (cursorImage.moveToNext()) {
                    String id;
                    String path;
                    String name;
                    String album;
                    String timestamp;
                    String height;
                    String width;
                    String orientation;
                    int size;

                    id = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID));
                    path = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                    name = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    album = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED));
                    height = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT));
                    width = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH));
                    size = Integer.parseInt(cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)));
                    orientation = cursorImage.getString(cursorImage.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION));

                    if (orientation == null)
                        orientation = "0";
                        imageList.add(Item.mappingInbox(Constantes.ITEM_TYPE_IMAGE, id, album, name, path, timestamp,
                        Function.converToTime(timestamp), null, Function.converToMb(size), height, width, orientation));
            }

            if (listar == Constantes.Type.video || listar == Constantes.Type.tudo)
                while (cursorVideo.moveToNext()) {
                    String id;
                    String path;
                    String name;
                    String album;
                    String timestamp;
                    String height;
                    String width;
                    int size;

                    id = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.VideoColumns._ID));
                    path = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));
                    name = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                    album = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME));
                    timestamp = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
                    height = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
                    width = cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
                    size = Integer.parseInt(cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)));

                    imageList.add(Item.mappingInbox(Constantes.ITEM_TYPE_VIDEO, id, album, name, path, timestamp,
                        Function.converToTime(timestamp), null, Function.converToMb(size), height, width, null));
            }

            cursorImage.close();
            cursorVideo.close();
            Collections.sort(imageList, new MapComparator(ordenarPor, Config.photo.ORDER_DIRECTION)); // Arranging photo album by timestamp decending

            return xml;
        }

        @Override
        @SuppressLint("ClickableViewAccessibility")
        protected void onPostExecute(String xml) {
            adapter = new AlbumAdapter(activity, imageList, onSwipeListener){
                @Override
                public void onClick(View v) {
                    clicks++;
                    int itemPosition = recyclerView.getChildAdapterPosition(v);
                    HashMap<String, String> item = imageList.get(itemPosition);
                    super.onClick(v);

                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        if(clicks == 1) {
                            if(SELECIONAR_ITEM) {
                                if (item.get(Item.KEY_ITEM_SELECTED) == null) {// Adiciona o item
                                    adapter.select(item);
                                } else {// Remove o item
                                    adapter.unselect(item);
                                }
                                ArrayList<HashMap<String, String>> itemsTemp = adapter.getSelectedAll();

                                navBar.getMenu().findItem(R.id.nav_share).setEnabled(itemsTemp.size() != 0);
                                navBar.getMenu().findItem(R.id.nav_delete).setEnabled(itemsTemp.size() != 0);

                                if (itemsTemp.size() == 0) {
                                    //menuItem_usar_como.setVisible(false);
                                    fab_text_count.setText("0");
                                    fab_item_count.setEnabled(false);
                                    fab_item_count.setImageResource(R.drawable.ic_null);

                                    navBar.getMenu().findItem(R.id.nav_share).setEnabled(false);
                                    navBar.getMenu().findItem(R.id.nav_delete).setEnabled(false);
                                }
                                /*else if (itemsTemp.size() == 1) {
                                    //menuItem_usar_como.setVisible(true);
                                    if (Objects.equals(item.get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_VIDEO)) {
                                        fab_item_count.setImageResource(R.drawable.ic_null);
                                        fab_item_count.setEnabled(false);
                                        fab_text_count.setText("1");
                                    } else {
                                        fab_item_count.setImageResource(R.drawable.ic_editar_light_enabled);
                                        fab_item_count.setEnabled(true);
                                        fab_text_count.setText(null);
                                    }

                                    navBar.getMenu().getItem(NAV_ID_SHARE).setEnabled(true);
                                    navBar.getMenu().getItem(NAV_ID_DELETE).setEnabled(true);
                                    navBar.getMenu().getItem(NAV_ID_UNSELECT_ALL).setEnabled(true);
                                }*/
                                else {
                                    //menuItem_usar_como.setVisible(false);
                                    fab_item_count.setEnabled(false);
//                                    fab_item_count.setImageResource(R.drawable.ic_null);
                                    fab_text_count.setText(IntToString(itemsTemp.size()));

                                    navBar.getMenu().findItem(R.id.nav_share).setEnabled(true);
                                    navBar.getMenu().findItem(R.id.nav_delete).setEnabled(true);
                                }
                            }
                            else {
                                openPreviewActivity(v, itemPosition);
                            }
                        }
                        else if(clicks == 2) {
                            if(!SELECIONAR_ITEM) {
                                Constantes.firstInit.firstUse(AlbumActivity.this, Constantes.firstInit.DOUBLE_CLICK_ID, true);
                                //menuItem_usar_como.setVisible(true);
                                SELECIONAR_ITEM = true;
                                adapter.select( item);

                                //region Config.IMAGE_ORDER_DIRECTION
//                                if (Config.photo.ORDER_DIRECTION.equals(Item.KEY_ORDER_DCS))
//                                    menuItem_onder.setIcon(R.drawable.ic_order_dcs_disabled);
//                                else
//                                    menuItem_onder.setIcon(R.drawable.ic_order_asc_disabled);
                                //endregion

                                fab_item_count.show();
                                /*if (Objects.equals(item.get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_VIDEO)) {
                                    fab_text_count.setText("1");
                                    fab_item_count.setEnabled(false);
                                    fab_item_count.setImageResource(R.drawable.ic_null);
                                } else {
                                    fab_item_count.setEnabled(true);
                                    fab_item_count.setImageResource(R.drawable.ic_editar_light_enabled);
                                }*/
                                fab_text_count.setText("1");
                                fab_item_count.setEnabled(false);

                                navBar.getMenu().findItem(R.id.nav_share).setEnabled(true);
                                navBar.getMenu().findItem(R.id.nav_delete).setEnabled(true);

                                Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_down);
                                animation.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {
                                        navBar.setVisibility(View.VISIBLE);
                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {}

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {}
                                });
//                                navBar.startAnimation(animation);
                                bottonMenu(true);
                            }
                        }
                        clicks = 0;
                    }, Constantes.DOUBLETAP);
                }

                @Override
                public boolean onLongClick(View v) {
                    int itemPosition = recyclerView.getChildAdapterPosition(v);
                    HashMap<String, String> item = imageList.get(+itemPosition);
                    if (Config.photo.ROW != 1) {
                        Constantes.firstInit.firstUse(AlbumActivity.this, Constantes.firstInit.LONG_CLICK_ID, true);

                        Uri uri = Uri.parse(item.get(Item.KEY_PATH));
                        String type = item.get(Item.KEY_ITEM_TYPE);
                        if (type != null) {
                            if(type.equalsIgnoreCase(Constantes.ITEM_TYPE_IMAGE))
                                popupPhoto(true, uri);
                            else
                                popupPhoto(false, uri);
                        }
                        recyclerView.suppressLayout(true);
                    }
                    return super.onLongClick(v);
                }
            };
            recyclerView.setAdapter(adapter);
        }
    }

}
