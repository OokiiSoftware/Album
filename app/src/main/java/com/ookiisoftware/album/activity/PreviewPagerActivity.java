package com.ookiisoftware.album.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.adapter.CustomViewPager;
import com.ookiisoftware.album.auxiliar.Config;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Function;
import com.ookiisoftware.album.modelo.Item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

public class PreviewPagerActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, BottomNavigationView.OnNavigationItemSelectedListener {

    //region Variáveis
//    private static final String TAG = "PreviewPagerActivity";
    private ArrayList<HashMap<String, String>> imageList;
    private Handler ocultarUITouch = new Handler();

    // Elementos do layout de vídeos
    private BottomNavigationView navBar;
    private LinearLayout bottonMenu;
    private PreviewAdapter adapter;
    private TextView tempo_inicio;
    private CustomViewPager viewPager;
//    private ViewPager viewPager;
    private ActionBar actionBar;
    private VideoView videoView;
    private TextView tempo_fim;
    private SeekBar seekBar;

    private boolean bottonMenuCanShow;
    private boolean showUI = false;

    private int currentItemPosition = -1;
    private int currentVideoduration;
    private Thread thread;

    private final int NAV_PLAY = 2;
    private SharedPreferences pref;
    //endregion

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pager);
        Init();
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int video_time = pref.getInt(Constantes.VIDEO_TIME, 0);
        currentItemPosition = pref.getInt(Constantes.intent.ITEM_POSITION, -1);
        boolean autoPlay = pref.getBoolean(Constantes.VIDEO_IS_PLEYING, true);
        if (currentItemPosition >= 0) {
            if (currentItemPosition <= 5)
                currentItemPosition -= 4;
            prepareVideo(currentItemPosition, video_time, autoPlay && Config.video.AUTO_PLAY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(videoView != null) {
            SharedPreferences.Editor editor = pref.edit();
            if (videoView.isShown()) {
                threadStop();
                editor.putInt(Constantes.VIDEO_TIME, videoView.getCurrentPosition());
                editor.putInt(Constantes.intent.ITEM_POSITION, currentItemPosition);
                editor.putBoolean(Constantes.VIDEO_IS_PLEYING, videoView.isPlaying());
            } else {
                editor.putInt(Constantes.VIDEO_TIME, 0);
                editor.putBoolean(Constantes.VIDEO_IS_PLEYING, false);
                editor.putInt(Constantes.intent.ITEM_POSITION, -1);
            }
            editor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview, menu);
        MenuItem item = menu.findItem(R.id.preview_auto_play);
        if (Config.video.AUTO_PLAY) {
            item.setIcon(R.drawable.ic_video_pause);
        } else{
            item.setIcon(R.drawable.ic_video_play_dark);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ArrayList<HashMap<String, String>> files = new ArrayList<>();
        files.add(imageList.get(viewPager.getCurrentItem()));
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                break;
            }
            case R.id.preview_compartilhar:{
                Function.CompartilharDados(this, files);
                break;
            }
            case R.id.preview_deletar:{
                Function.DeletarDados(this, files, null, adapter);
                break;
            }
            case R.id.preview_auto_play: {
                Config.video.AUTO_PLAY = !Config.video.AUTO_PLAY;
                if (Config.video.AUTO_PLAY) {
                    item.setIcon(R.drawable.ic_video_pause);
                } else{
                    item.setIcon(R.drawable.ic_video_play_dark);
                }
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(Constantes.video.AUTO_PLAY, Config.video.AUTO_PLAY);
                editor.apply();
                break;
            }
            case R.id.preview_editar: {
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra(Constantes.intent.EDITOR_PATH, imageList.get(viewPager.getCurrentItem()).get(Item.KEY_PATH));
                intent.putExtra(Constantes.intent.EDITOR_ID, imageList.get(viewPager.getCurrentItem()).get(Item.KEY_ITEM_ID));
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
                //Toast.makeText(this, "Função indisponível", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        ocultarUITouch.removeCallbacks(delayOcultarUI);
        ocultarUITouch.postDelayed(delayOcultarUI, Constantes.MENU_TIME_TO_HIDE);

        switch (item.getItemId()){
            case R.id.preview_video_play: {
                if(videoView.isPlaying()) {
                    videoView.pause();
                    item.setIcon(R.drawable.ic_video_play_dark);
                } else {
                    videoView.start();
                    item.setIcon(R.drawable.ic_video_pause);
                }
                break;
            }
            case R.id.preview_video_mais_5: {
                videoView.seekTo(videoView.getCurrentPosition() + 5000);
                break;
            }
            case R.id.preview_video_menos_5: {
                videoView.seekTo(videoView.getCurrentPosition() - 5000);
                break;
            }
            case R.id.preview_video_mais_10: {
                videoView.seekTo(videoView.getCurrentPosition() + 10000);
                break;
            }
            case R.id.preview_video_menos_10: {
                videoView.seekTo(videoView.getCurrentPosition() - 10000);
                break;
            }
        }
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        actionBar.setTitle(getItemName(position));
        threadStop();
        if (itemIsVideo(position))
            prepareVideo(position, 0, Config.video.AUTO_PLAY);
        else{
            navBar.getMenu().getItem(NAV_PLAY).setIcon(getDrawable(R.drawable.ic_video_play_dark));
            videoView.pause();
            videoView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    //endregion

    //region Métodos

    private void Init() {
        //region findViewById
        navBar = findViewById(R.id.preview_video_menu);
        viewPager = findViewById(R.id.album_preview_pager);
        bottonMenu = findViewById(R.id.botton_menu);
        seekBar = findViewById(R.id.seekBar);
        videoView = findViewById(R.id.video);
        tempo_inicio = findViewById(R.id.tempo_inicio);
        tempo_fim = findViewById(R.id.tempo_fim);
        //endregion

        //region Bundle
        Bundle bundle = getIntent().getExtras();
        int posic = 0;
        if (bundle != null) {
            posic = bundle.getInt(Constantes.intent.ITEM_POSITION);
        }
        //endregion

        pref = getSharedPreferences("info", MODE_PRIVATE);
        Config.video.AUTO_PLAY = pref.getBoolean(Constantes.video.AUTO_PLAY, true);

        if (Function.imageList == null) return;
        else imageList = Function.imageList;

        thread = new Thread(videoPlayUpdate);

        navBar.setOnNavigationItemSelectedListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ocultarUITouch.removeCallbacks(delayOcultarUI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
                ocultarUITouch.postDelayed(delayOcultarUI, Constantes.MENU_TIME_TO_HIDE);
            }
        });

        videoView.setOnCompletionListener(mediaPlayer -> {
            videoView.setVisibility(View.GONE);
            seekBar.setProgress(0);
            thread = new Thread(videoPlayUpdate);
            bottonMenuCanShow = false;
            AnimationUI(true);
        });
        videoView.setOnPreparedListener(mp -> {
            currentVideoduration = videoView.getDuration();
            tempo_inicio.setText(calculateTime(currentVideoduration /1000));
            seekBar.setMax(videoView.getDuration());
            thread.start();
        });

        //region ActionBar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getItemName(posic));
        }
        //endregion

        //region ViewPager
        adapter = new PreviewAdapter(this, imageList){
            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View view, MotionEvent event) {
                ImageViewTouch item = (ImageViewTouch) view;
                viewPager.setPagingEnabled(item.getScale() <= 1);
                return super.onTouch(view, event);
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);

        //region Animação da UI
        AnimationUI(true);
        bottonMenu(false);
        //endregion

        viewPager.setCurrentItem(posic);
        //endregion
    }

    private Runnable delayOcultarUI = () -> {
        showUI = false;
        AnimationUI(false);
    };
    private Runnable videoPlayUpdate = () -> {
        do{
            tempo_fim.post(() -> {
                int time = (currentVideoduration - videoView.getCurrentPosition());
                tempo_fim.setText(calculateTime(time/1000));
                if(!seekBar.isPressed())
                    seekBar.setProgress(videoView.getCurrentPosition());
            });
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while(videoView.getCurrentPosition() < currentVideoduration && videoView.isPlaying());
    };

    private void threadStop() {
        thread.interrupt();
        Thread.currentThread().interrupt();
        thread = new Thread(videoPlayUpdate);
    }

    private void AnimationUI(boolean showUI) {
        if(showUI) {
            actionBar.show();
            if(bottonMenuCanShow)
                if(itemIsVideo(viewPager.getCurrentItem()))
                    bottonMenu(true);
            ocultarUITouch.postDelayed(delayOcultarUI, Constantes.MENU_TIME_TO_HIDE);
        } else {
            actionBar.hide();
            bottonMenu(false);
            ocultarUITouch.removeCallbacks(delayOcultarUI);
        }
    }
    private void bottonMenu(boolean mostrar){
        if (mostrar) bottonMenu.animate().translationY(0);
        else bottonMenu.animate().translationY(2000);
    }

    private String getItemName(int position){
        return imageList.get(position).get(Item.KEY_ITEM_NAME);
    }
    private String getPath(int position) {
        if (itemIsVideo(position))
            return imageList.get(position).get(Item.KEY_PATH);
        return null;
    }
    private boolean itemIsVideo(int position) {
        return Objects.equals(imageList.get(position).get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_VIDEO);
    }

    private void prepareVideo(int position, int video_time, boolean autoPlay) {
        String path = getPath(position);

        currentItemPosition = position;

        if (path != null) {
            videoView.setVideoPath(path);
            if (video_time > 0) {
                videoView.seekTo(video_time);
            }

            if (autoPlay) {
                bottonMenuCanShow = true;
                videoView.setVisibility(View.VISIBLE);
                videoView.start();
            }
            else if (video_time == 0) {
                bottonMenuCanShow = false;
                bottonMenu(false);
                videoView.setVisibility(View.GONE);
            }
            else {
                videoView.setVisibility(View.VISIBLE);
                bottonMenuCanShow = true;
            }

            navBar.getMenu().getItem(NAV_PLAY).setIcon(R.drawable.ic_video_pause);
        } else {
            bottonMenuCanShow = false;
            bottonMenu(false);
            navBar.getMenu().getItem(NAV_PLAY).setIcon(R.drawable.ic_video_play_dark);
            videoView.setVisibility(View.GONE);
        }
    }

    private String calculateTime(long seconds) {
        int day = (int)TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day *24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds)* 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) *60);

        return ( hours + ":" + minute + ":" + second);
    }

    //endregion

    public class PreviewAdapter extends PagerAdapter implements ImageViewTouch.OnImageViewTouchSingleTapListener, View.OnTouchListener {
        private Activity activity;
        private ArrayList<HashMap<String, String>> data;

        PreviewAdapter(Activity activity, ArrayList<HashMap<String, String>> data) {
            this.activity = activity;
            this.data = data;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        @SuppressLint("ClickableViewAccessibility")
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            View iView = inflater.inflate(R.layout.item_preview_pager, container, false);
            iView.setId(position);
            container.addView(iView);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;

            String path = data.get(position).get(Item.KEY_PATH);

            ImageView videoIcon = iView.findViewById(R.id.video_icon);
            ImageViewTouch imageView = iView.findViewById(R.id.photo);
            imageView.setMaxHeight(height);
            imageView.setMaxWidth(width);

            try {
                assert path != null;
                Glide.with(activity).load(path).into(imageView);
            } catch (Exception ignored) {}

            imageView.setSingleTapListener(this);

            if(Objects.equals(imageList.get(position).get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_VIDEO)) {
                imageView.setDoubleTapEnabled(false);
                videoIcon.setVisibility(View.VISIBLE);
                videoIcon.setOnClickListener(view -> prepareVideo(position, 0, true));
            } else {
                imageView.setDoubleTapEnabled(true);
                imageView.setOnTouchListener(this);
                videoIcon.setVisibility(View.GONE);
            }
            return iView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public void onSingleTapConfirmed() {
            showUI = !showUI;
            AnimationUI(showUI);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View view, MotionEvent event) {
            return false;
        }

        public ArrayList<HashMap<String, String>> getItems(){
            return data;
        }

    }

}
