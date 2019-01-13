package com.example.snehil.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.File;
import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements CommonAdapter.ClickListener{
    HistoryDbHandler dbHandler;
    CommonAdapter mAdapter, clearAdapter;
    RecyclerView recyclerView;
    ImageView found;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        found = (ImageView) findViewById(R.id.Found);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        dbHandler = new HistoryDbHandler(this, null, null, 1);
        printDatabase();
    }
    //printing each record of database
    public void printDatabase(){
        ArrayList<FileInitializer> fileList = dbHandler.read();
        //display alternate image if there is no search result
        if(fileList.size()!=0)
            found.setVisibility(View.GONE);
        else
            found.setVisibility(View.VISIBLE);

        mAdapter = new CommonAdapter(this, fileList);
        //to activate click events on recycler view
        mAdapter.setClickListener(this);
        //to display recycler view
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void itemClicked(View view, int position) {

        ArrayList<FileInitializer> arrayList = dbHandler.read();
        if((view.getId() == R.id.fileName || view.getId()==R.id.path )&& !arrayList.get(0).getPath().equals("FILE NOT FOUND")){
            //to initiate implicit intent
            File file = new File(arrayList.get(position).getPath() + "/" + arrayList.get(position).getFileName());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri apkURI = FileProvider.getUriForFile(
                    HistoryActivity.this,
                    BuildConfig.APPLICATION_ID + ".provider", file);
            intent.setDataAndType(apkURI, "application/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            //to add to history when user clicks the file to open
            dbHandler.addHistory(arrayList.get(position));
        }
        //remove the history if user clicks on cross button
        else if(view.getId() == R.id.imageView) {
            ArrayList<FileInitializer> fileList = dbHandler.read();
            dbHandler.deleteHistory(fileList.get(position).getFileName());
            mAdapter.notifyDataSetChanged();
            printDatabase();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home: finish();
                break;
            case R.id.clear:
                ArrayList<FileInitializer> fileList = dbHandler.read();
                if(fileList.size()!=0) {
                    for (FileInitializer file : fileList) {
                        dbHandler.deleteHistory(file.getFileName());
                        printDatabase();
                    }
                }

                break;
        }

        return super.onOptionsItemSelected(item);

    }
}
