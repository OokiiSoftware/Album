package com.ookiisoftware.album.auxiliar;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class OnSwipeListener implements View.OnTouchListener{

    private String TAG = "OnSwipeListener";
    private final Handler handlersingleTouch = new Handler();
    private final Handler handlerLongTouch = new Handler();
//    private boolean toqueSimples;
    private boolean clickFocus;
    private boolean itemNoNull;
    private boolean swipped;
    private int touchCount;

    private float initX, initY;

    private Runnable longTouch = new Runnable() {
        public void run() {
//            toqueSimples = false;
            touchCount = 0;
            handlersingleTouch.removeCallbacks(singleTouch);
//            onLongTouch();
        }
    };
    private Runnable singleTouch = new Runnable() {
        @Override
        public void run() {
//            if (toqueSimples && touchCount == 1)
//                onSingleTouch();
            touchCount = 0;
        }
    };

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View view, MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                initX = event.getX();
                initY = event.getY();

                clickFocus = true;
                if (view.getTag() != null) {//tag == null é o recyclerView
                    itemNoNull = true;
                    handlerLongTouch.postDelayed(longTouch, Constantes.LONGCLICK);
//                    toqueSimples = true;
                    if (touchCount == 1)
                        touchCount++;
                    if (touchCount == 0) {
                        handlersingleTouch.postDelayed(singleTouch, Constantes.DOUBLETAP);
                        touchCount++;
                    }
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                swipped = false;
                onTouchUp();
                handlerLongTouch.removeCallbacks(longTouch);
                if (clickFocus) {
//                    if (!Config.SELECIONAR_ITEM)
                    {
                        if (touchCount == 2) {
//                            onDoubleTouch();
                            touchCount = 0;
                        }
                    }
//                    else
                        {
                        if(itemNoNull) {
//                            onSingleTouch();
                            handlersingleTouch.removeCallbacks(singleTouch);
                            touchCount = 0;
                            itemNoNull = false;
                        }
                    }
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {// O recyclerView só usa essa parte
                float diffX = x - initX;
                float diffY = y - initY;

                clickFocus = !(Math.abs(diffX) > Constantes.SWIPE_RADIO_LIMITE || Math.abs(diffY) > Constantes.SWIPE_RADIO_LIMITE);

                if (!swipped) {
                    swipped = true;
                    if (Math.abs(diffX) > Constantes.SWIPE_RANGE_LIMITE)
                        if (Math.abs(diffX) > Math.abs(diffY))
                            if (diffX > 0)
                                onSwipeRight();
                            else
                                onSwipeLeft();
                }
                if (!clickFocus) {
                    handlerLongTouch.removeCallbacks(longTouch);
                    handlersingleTouch.removeCallbacks(singleTouch);
                    touchCount = 0;
//                    toqueSimples = false;
                }
                break;
            }
        }
        return false;
    }

    public void onSwipeRight() {
        Log.e(TAG, "Direita");
    }

    public void onSwipeLeft() {
        Log.e(TAG, "Esquerda");
    }

    public void onSwipeTop() {
        Log.e(TAG, "Cima");
    }

    public void onSwipeBottom() {
        Log.e(TAG, "Baixo");
    }

    public void onTouchUp() {
        Log.e(TAG, "ToqueUp");
    }
    /*public void onSingleTouch() {
        Log.e(TAG, "ToqueSimples");
    }


    public void onLongTouch() {
        Log.e(TAG, "ToqueLongo");
    }

    public void onDoubleTouch() {
        Log.e(TAG, "ToqueDuplo");
    }*/
}
