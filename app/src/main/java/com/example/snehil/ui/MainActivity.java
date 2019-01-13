

package com.example.snehil.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewStructure;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FilesAdapter.fileClickListener {

    public ArrayList<FileInitializer> arrayList;
    public SearchView search;
    private RecyclerView recyclerView;
    private FilesAdapter mAdapter;
    FileInitializer fileInitializer;
    HistoryDbHandler dbHandler;
    BookmarkDbHandler bookmarkDbHandler;
    ProgressDialog progressDialog;
    String query;
    RelativeLayout relativeLayout;
    ImageView Found;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //display search image in center
        Found = (ImageView) findViewById(R.id.Found);
        Found.setVisibility(View.VISIBLE);
        //to launch history activity on clicking fab
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });


        //Configuring searchView
        search = (SearchView) findViewById(R.id.searchView);
        search.setQueryHint("Enter your Keyword here");

        //Declaring a handler for databases
        dbHandler = new HistoryDbHandler(this, null, null, 1);
        bookmarkDbHandler = new BookmarkDbHandler(this, null, null, 1);

        //referencing the recycler view of main activity
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        search.setActivated(true);
        //creating a search manager that implements voice search

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        Intent intent = getIntent();
        handleIntent(intent);


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Found.setVisibility(View.GONE);
                search.clearFocus();
                MainActivity.this.query = query;
                //Launching Async Task that runs on a separate thread than UI thread
                MyAsyncTasks myAsyncTasks = new MyAsyncTasks();
                myAsyncTasks.execute();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        //Drawer and navigation bar customization
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }


    //MyAsyncTasks to handle UI on query text submit
    public class MyAsyncTasks extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //show process dialog while result is loading
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Searching");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //dismiss the process dialog when result is loaded
            progressDialog.dismiss();
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);

        }


        @Override
        protected Void doInBackground(Void... voids) {
            String path = Environment.getExternalStorageDirectory().getPath();

            arrayList = new ArrayList<>();
            //function to search for given query launched
            searchQuery(path, query);
            mAdapter = new FilesAdapter(MainActivity.this, arrayList);
            //to call setClickListener of filesAdapter
            mAdapter.setClickListener(MainActivity.this);
            if (arrayList.size() == 0) {
                //fileInitializer = new FileInitializer(query, "FILE NOT FOUND");
                //arrayList.add(fileInitializer);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Found.setImageResource(R.drawable.ic_notfound);
                        Found.setVisibility(View.VISIBLE);
                    }
                });

            }
            return null;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        //super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
            search.setQuery(query, false);
        }
    }
    //to add bookmark and implicit intent
    @Override
    public void fileClicked(View view, int position) {
        if((view.getId() == R.id.fileName || view.getId()==R.id.path )&& !arrayList.get(0).getPath().equals("FILE NOT FOUND")){
            //to initiate implicit intent
            String path = arrayList.get(position).getPath() + "/" + arrayList.get(position).getFileName();
            File file = new File(path);
            if(file.exists() && file.isDirectory()){
                listFiles(path);
                dbHandler.addHistory(arrayList.get(position));
            }
            if(file.isFile()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri apkURI = FileProvider.getUriForFile(
                        MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", file);
                intent.setDataAndType(apkURI, "application/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
                //to add to history when user clicks the file to open
                dbHandler.addHistory(arrayList.get(position));
            }
        }
        else if(view.getId() == R.id.star){
            //to add bookmark when star button is clicked
            bookmarkDbHandler.addBookmark(arrayList.get(position));
        }



    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //sorting the result in asccending order
        if (id == R.id.sortAsc) {
            Collections.sort(arrayList, new sortByNameAsc());
            //creating an adapter for the sorted list
            mAdapter = new FilesAdapter(MainActivity.this, arrayList);

        }
        //sorting the result in descending order
        else if (id == R.id.sortDesc) {
            Collections.sort(arrayList, new sortByNameDesc());
            //creating adapter for sorted list
            mAdapter = new FilesAdapter(MainActivity.this, arrayList);
        }
        mAdapter.setClickListener(MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        return true;

    }
    //declaring  comparator to handle comparisons
    class sortByNameAsc implements Comparator<FileInitializer>{
        public int compare(FileInitializer a, FileInitializer b){
            return a.getFileName().compareTo(b.getFileName());
        }
    }

    class sortByNameDesc implements Comparator<FileInitializer>{
        public int compare(FileInitializer a, FileInitializer b){
            return b.getFileName().compareTo(a.getFileName());
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_bookmark) {
            Intent intent = new Intent(MainActivity.this, BookmarkActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_exit) {
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //function to search the file based on query
    public void searchQuery(String path, String query) {


        File file = new File(path);
        if (!file.exists()) {
            Toast.makeText(MainActivity.this, "File doesn't exists!", Toast.LENGTH_SHORT).show();
        } else {
            for (File list : file.listFiles()) {
                if (list.exists() && list.isDirectory()) {
                    if(list.getName().contains(query)){
                        fileInitializer = new FileInitializer(list.getName(), path);
                        arrayList.add(fileInitializer);
                    }

                        searchQuery((path + "/" + list.getName()), query);


                } else {
                    String fileNameWithoutExtension = FilenameUtils.removeExtension(list.getName());

                    if (list.getName().contains(query) || fileNameWithoutExtension.contains(query)) {
                        fileInitializer = new FileInitializer(list.getName(), path);
                        arrayList.add(fileInitializer);

                    }

                }
            }

        }

    }
    public void listFiles(String path){
        File file = new File(path);
        arrayList.clear();
        mAdapter = new FilesAdapter(MainActivity.this, arrayList);
        mAdapter.notifyDataSetChanged();

        for(File list : file.listFiles()){
            fileInitializer = new FileInitializer(list.getName(), path);
            arrayList.add(fileInitializer);

        }
        mAdapter = new FilesAdapter(MainActivity.this, arrayList);
        mAdapter.setClickListener(MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

}