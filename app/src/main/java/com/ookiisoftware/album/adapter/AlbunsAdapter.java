package com.ookiisoftware.album.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

public class AlbunsAdapter extends RecyclerView.Adapter<AlbunsAdapter.AlbunsViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private ArrayList<HashMap<String, String>> data;
    private ConstraintSet set = new ConstraintSet();
    private OnSwipeListener swipeListener;
    private Activity activity;

    protected AlbunsAdapter(Activity activity, ArrayList<HashMap<String, String>> data, OnSwipeListener swipeListener) {
        this.data = data;
        this.activity = activity;
        this.swipeListener = swipeListener;
    }

    @NonNull
    @Override
    public AlbunsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_albuns, parent, false);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
        view.setOnTouchListener(swipeListener);
        return new AlbunsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbunsViewHolder holder, int position) {
        HashMap<String, String> item = data.get(position);
        String titulo = item.get(Item.KEY_ALBUM);
        String image_count = item.get(Item.KEY_IMAGE_COUNT);
        String video_count = item.get(Item.KEY_VIDEO_COUNT);
        String path = item.get(Item.KEY_PATH);
        String type = item.get(Item.KEY_TYPE);
        String ratio = String.format("%s:%s", Constantes.album.DEFAULT_HEIGHT, Config.album.HEIGHT);

        holder.image.setScaleType(CENTER_CROP);
        holder.image.setId(position);

        set.clone(holder.constraintLayout);
        set.setDimensionRatio(holder.image.getId(), ratio);
        set.applyTo(holder.constraintLayout);

        holder.title.setText(titulo);
        if (image_count == null)
            holder.image_count.setVisibility(View.GONE);
        else {
            holder.image_count.setVisibility(View.VISIBLE);
            holder.image_count.setText(image_count);
        }

        if (video_count == null)
            holder.video_count.setVisibility(View.GONE);
        else {
            holder.video_count.setVisibility(View.VISIBLE);
            holder.video_count.setText(video_count);
        }

        if (type != null && type.equals(Constantes.Type.Videos.name()))
            holder.video_icon.setVisibility(View.VISIBLE);
        else
            holder.video_icon.setVisibility(View.GONE);

        try {
            if (path != null) {
                Glide.with(activity).load(new File(path)).into(holder.image);
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

    public ArrayList<HashMap<String, String>> getItems() {
        return data;
    }

    static class AlbunsViewHolder extends RecyclerView.ViewHolder {

        ImageView image, video_icon;
        TextView image_count, video_count, title;
        ConstraintLayout constraintLayout;

        AlbunsViewHolder(@NonNull View itemView) {
            super(itemView);
            constraintLayout = itemView.findViewById(R.id.constraint);
            image = itemView.findViewById(R.id.image);
            image_count = itemView.findViewById(R.id.image_count);
            video_count = itemView.findViewById(R.id.video_count);
            title = itemView.findViewById(R.id.title);
            video_icon = itemView.findViewById(R.id.video_icon);
        }
    }
}
