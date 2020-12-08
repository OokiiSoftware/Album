package com.ookiisoftware.album.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.auxiliar.Import;

public class AdsActivity extends AppCompatActivity {

    //region Variaveis
    private InterstitialAd interstitialAd;
    private Activity activity;
    //endregion

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Import.theme.get(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads);
        activity = this;
        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region Metodos

    private void init() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        AdView adView1 = findViewById(R.id.adView1);
//        AdView adView2 = findViewById(R.id.adView2);
//        AdView adView3 = findViewById(R.id.adView3);
//        AdView adView4 = findViewById(R.id.adView4);
//        AdView adView5 = findViewById(R.id.adView5);
//        AdView adView6 = findViewById(R.id.adView6);
//        AdView adView7 = findViewById(R.id.adView7);
//        AdView adView8 = findViewById(R.id.adView8);
//        AdView adView9 = findViewById(R.id.adView9);
//        AdView adView10 = findViewById(R.id.adView10);
        Button button = findViewById(R.id.button);

        setSupportActionBar(toolbar);

        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId(getResources().getString(R.string.ads_interstitial_id));

        AdRequest adRequest = new AdRequest.Builder().build();

        interstitialAd.loadAd(adRequest);
        adView1.loadAd(adRequest);
//        adView2.loadAd(adRequest);
//        adView3.loadAd(adRequest);
//        adView4.loadAd(adRequest);
//        adView5.loadAd(adRequest);
//        adView6.loadAd(adRequest);
//        adView7.loadAd(adRequest);
//        adView8.loadAd(adRequest);
//        adView9.loadAd(adRequest);
//        adView10.loadAd(adRequest);

        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        button.setOnClickListener(v -> {
            if (interstitialAd.isLoaded())
                interstitialAd.show();
            else
                Import.Alert.snakeBar(activity, getResources().getString(R.string.carregando_ad));
        });
    }

    //endregion
}