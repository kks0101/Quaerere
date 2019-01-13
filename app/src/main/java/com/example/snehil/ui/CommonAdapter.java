package com.example.snehil.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class CommonAdapter extends RecyclerView.Adapter<CommonAdapter.MyViewHolder>{

    private ArrayList<FileInitializer> filesList;
    public Context context;
    public ClickListener clickListener;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView fileName, path;
        public ImageView folder, clear;

        public MyViewHolder(View view) {
            super(view);
            fileName = (TextView) view.findViewById(R.id.fileName);
            path = (TextView) view.findViewById(R.id.path);
            folder = (ImageView) view.findViewById(R.id.folder);
            clear = (ImageView) view.findViewById(R.id.imageView);
            clear.setOnClickListener(this);
            fileName.setOnClickListener(this);
            path.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(clickListener!=null){
                clickListener.itemClicked(v, getAdapterPosition());
            }
        }
    }


    public interface ClickListener{
        void itemClicked(View view, int position);
    }


    public CommonAdapter(Context context, ArrayList<FileInitializer> filesList) {
        this.filesList = filesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_row_history, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FileInitializer filesInitializer = filesList.get(position);
        holder.fileName.setText(filesInitializer.getFileName());
        holder.path.setText(filesInitializer.getPath());

    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

}
