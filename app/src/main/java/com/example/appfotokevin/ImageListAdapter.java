package com.example.appfotokevin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.Instant;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {

    private final Context context;
    private final List<ImageData> imageDataList;

    public ImageListAdapter(Context context, List<ImageData> imageDataList) {
        this.context = context;
        this.imageDataList = imageDataList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageData imageData = imageDataList.get(position);

        holder.textName.setText(imageData.getName());
        holder.textDescription.setText(imageData.getDescription());

        Instant Glide = null;

    }

    @Override
    public int getItemCount() {
        return imageDataList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageThumbnail;
        TextView textName;
        TextView textDescription;

        public ImageViewHolder(View itemView) {
            super(itemView);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
            textName = itemView.findViewById(R.id.textName);
            textDescription = itemView.findViewById(R.id.textDescription);
        }
    }
}
