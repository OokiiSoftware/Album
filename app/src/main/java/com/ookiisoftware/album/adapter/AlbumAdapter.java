package com.ookiisoftware.album.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.auxiliar.Config;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.OnSwipeListener;
import com.ookiisoftware.album.modelo.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import static android.widget.ImageView.ScaleType.FIT_CENTER;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private ArrayList<HashMap<String, String>> data;
    private ConstraintSet set = new ConstraintSet();
    private OnSwipeListener swipeListener;
    private Activity activity;

    protected AlbumAdapter(Activity activity, ArrayList<HashMap<String, String>> data, OnSwipeListener swipeListener) {
        this.data = data;
        this.activity = activity;
        this.swipeListener = swipeListener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_album, parent, false);
        view.setOnTouchListener(swipeListener);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, final int position) {
        HashMap<String, String> song = data.get(+position);
        String width = song.get(Item.KEY_ITEM_WIDTH);
        String height = song.get(Item.KEY_ITEM_HEIGHT);
        String path = song.get(Item.KEY_PATH);
        String type = song.get(Item.KEY_ITEM_TYPE);
        String ratio = "";

        holder.imageView.setId(position);
        if (type != null && type.equals(Constantes.ITEM_TYPE_IMAGE)) {
            holder.videoIcon.setVisibility(View.GONE);
        } else {
            holder.videoIcon.setVisibility(View.VISIBLE);
        }
        if(song.get(Item.KEY_ITEM_SELECTED) == null)
            holder.destaque.setVisibility(View.GONE);
        else
            holder.destaque.setVisibility(View.VISIBLE);

        switch (Config.photo.TYPE) {
            case CENTER_CROP: {
                holder.imageView.setScaleType(CENTER_CROP);
                holder.videoIcon.setScaleType(CENTER_CROP);
                holder.destaque.setScaleType(CENTER_CROP);
                ratio = String.format("%s:%s", Constantes.photo.DEFAULT_HEIGHT, Config.photo.HEIGHT);
                break;
            }
            case FIT_CENTER: {
                holder.imageView.setScaleType(FIT_CENTER);
                holder.videoIcon.setScaleType(FIT_CENTER);
                holder.destaque.setScaleType(FIT_CENTER);
                ratio = String.format("%s:%s", width, height);
                break;
            }
        }
        set.clone(holder.constraintLayout);
        set.setDimensionRatio(holder.imageView.getId(), ratio);
        set.setDimensionRatio(holder.videoIcon.getId(), ratio);
        set.setDimensionRatio(holder.destaque.getId(), ratio);
        set.applyTo(holder.constraintLayout);
        try {
            if (path != null) {
                Glide.with(activity).load(new File(path)).into(holder.imageView);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    public ArrayList<HashMap<String, String>> getSelectedAll() {
        ArrayList<HashMap<String, String>> items = new ArrayList<>();
        for (HashMap<String, String> item : data)
            if(item.get(Item.KEY_ITEM_SELECTED) != null)
                items.add(item);
        return items;
    }

    public ArrayList<HashMap<String, String>> getItems() {
        return data;
    }

    public void unselectAll(){
        for (HashMap<String, String> item : data)
            item.put(Item.KEY_ITEM_SELECTED, null);
        notifyDataSetChanged();
    }

    public int selectAll(){
        for (HashMap<String, String> item : data)
            item.put(Item.KEY_ITEM_SELECTED, "");
        notifyDataSetChanged();
        return getItemCount();
    }

    public int selectIntervalo() {
        int inicio = -1;
        int fim = -1;
        for (int i = 0; i < getItemCount() -1; i++) {
            HashMap<String, String> item = data.get(i);
            if (item.get(Item.KEY_ITEM_SELECTED) != null) {
                inicio = i;
                break;
            }
        }
        for (int i = getItemCount() -1; i > 0; i--) {
            HashMap<String, String> item = data.get(i);
            if (item.get(Item.KEY_ITEM_SELECTED) != null) {
                fim = i;
                break;
            }
        }
        if (inicio >= 0 && fim > 0 && inicio < fim){
            for (int i = inicio; i < fim; i++) {
                HashMap<String, String> item = data.get(i);
                select(item);
            }
            return fim - inicio + 1;
        }
        return -1;
    }

    public void select(HashMap<String, String> item){
        item.put(Item.KEY_ITEM_SELECTED, "");
        notifyItemChanged(data.indexOf(item));
    }

    public void unselect(HashMap<String, String> item){
        item.put(Item.KEY_ITEM_SELECTED, null);
        notifyItemChanged(data.indexOf(item));
    }

//    public void remove(HashMap<String, String> item){
//        data.remove(item);
//        notifyItemRemoved(data.indexOf(item));
//    }

    public HashMap<String, String> getSelected(){
        ArrayList<HashMap<String, String>> items = getSelectedAll();
        if(items.size() == 1)
            return items.get(0);
        return null;
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView, videoIcon, destaque;
        ConstraintLayout constraintLayout;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.img_constraint);
            destaque = itemView.findViewById(R.id.item_destaque);
            imageView = itemView.findViewById(R.id.photo);
            videoIcon = itemView.findViewById(R.id.video_icone);
        }
    }
}