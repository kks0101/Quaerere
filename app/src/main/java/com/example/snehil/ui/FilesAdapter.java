package com.example.snehil.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.MyViewHolder> {

    private ArrayList<FileInitializer> filesList;
    public Context context;
    public fileClickListener clickListener;
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView fileName, path;
        public ImageView star;
        public MyViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.fileName);
            path = (TextView) view.findViewById(R.id.path);
            star = (ImageView) view.findViewById(R.id.star);
            fileName.setOnClickListener(this);
            path.setOnClickListener(this);
            star.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(clickListener!=null){
                clickListener.fileClicked(v, getAdapterPosition());
            }
        }
    }

    public interface fileClickListener{
        void fileClicked(View view, int position);
    }

    public FilesAdapter(Context context, ArrayList<FileInitializer> filesList) {
        this.filesList = filesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void setClickListener(fileClickListener clickListener){
        this.clickListener = clickListener;
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileInitializer filesInitializer = filesList.get(position);
        holder.fileName.setText(filesInitializer.getFileName());
        holder.path.setText(filesInitializer.getPath());
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }


}
