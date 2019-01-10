package com.narse.arcgis;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Bookmark;
import com.esri.arcgisruntime.mapping.BookmarkList;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.tasks.geocode.GeocodeResult;
import com.esri.arcgisruntime.tasks.geocode.LocatorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    String[] basemapNames;
    boolean myFlag;
    private ListView cityList;
    private ArrayAdapter myAdapter;
    ArcGISScene myScene;
    Viewpoint homeViewpoint;
    private SceneView sv;
    private ActionBarDrawerToggle myToggle;
    private DrawerLayout myDrawerLayout;
    private ListView myLeftDrawerList;
    String[] cities = {
            "New York",
            "Chicago",
            "Denver",
            "Detroit",
            "Las Vegas",
            "Paris"
    };
    private BookmarkList myBookmarks;
    private Bookmark myBookmark;
    private ArrayList myBookmarksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv = (SceneView) findViewById(R.id.map1);
        myScene = new ArcGISScene(Basemap.createImagery());
        sv.setScene(myScene);

        ArcGISTiledElevationSource myElevation = new ArcGISTiledElevationSource(getString(R.string.elevation3dUrl));
        myScene.getBaseSurface().getElevationSources().add(myElevation);

        Point pt = new Point(35.59520, 138.78045, SpatialReference.create(4326));
        Camera myCamera = new Camera(35.59520, 138.78045, 2719.97086, 195.47934, 79.357552, 0.0);
        homeViewpoint = new Viewpoint(pt, 50000, myCamera);
        myScene.setInitialViewpoint(homeViewpoint);
        sv.setViewpointCamera(myCamera);
        myBookmarks = myScene.getBookmarks();
        myBookmarksList = new ArrayList();
//create a default bookmarks and add all bookmark items to the ArrayList
        createBookmarks();
        cityList = findViewById(R.id.listview);
        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                myBookmarksList);
        cityList.setAdapter(myAdapter);
        cityList.setVisibility(View.GONE);

        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                cityList.setVisibility(View.GONE);
                myFlag = false;
                sv.setViewpointAsync(myBookmarks.get(i).getViewpoint());
                Toast.makeText(getApplicationContext()
                        , myBookmarks.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sv.setViewpointCameraAsync(homeViewpoint.getCamera(), 6);
                Toast.makeText(MainActivity.this, "Fab clicked", Toast.LENGTH_LONG).show();
            }
        });

        myDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        myLeftDrawerList = (ListView) findViewById(R.id.left_drawer);
        basemapNames = getResources().getStringArray(R.array.basemaps);
        myLeftDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                basemapNames));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        myLeftDrawerList.setOnItemClickListener(this);
        configureToggle();
        sv.setOnTouchListener(new DefaultSceneViewOnTouchListener(sv) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                android.graphics.Point screenPt = new android.graphics.Point(Math.round(e.getX()),
                        Math.round(e.getY()));
                final ListenableFuture<Point> mapPt = sv.screenToLocationAsync(screenPt);
                showAlertDialog();
                return true;
            }
        });
    }

    void showAlertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle("Map Viewpoint Information:");
        String lat1 = String.valueOf("Lat: " + sv.getCurrentViewpointCamera().getLocation().getX());
        String lon1 = String.valueOf("Lon: " + sv.getCurrentViewpointCamera().getLocation().getY());
        String ele1 = String.valueOf("Elevation: " +
                sv.getCurrentViewpointCamera().getLocation().getZ());
        String heading = String.valueOf("Heading: " +
                sv.getCurrentViewpointCamera().getHeading());
        String pitching = String.valueOf("Pitching: " + sv.getCurrentViewpointCamera().getPitch());
        String roll = String.valueOf("Roll: " + sv.getCurrentViewpointCamera().getRoll());
        dialog.setMessage(lat1 + "\n" + lon1 + "\n" + ele1 + "\n" + heading + "\n" + pitching + "\n" + roll);
        dialog.setPositiveButton("I got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(MainActivity.this, "I got it!", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sv.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sv.resume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bookmarks1:
                if (myFlag) {
                    cityList.setVisibility(View.GONE);
                    myFlag = false;
                } else {
                    cityList.setVisibility(View.VISIBLE);
                    myFlag = true;
                }
                return true;
            default:
                return (myToggle.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item);
        }
    }

    private void configureToggle() {
        myToggle = new ActionBarDrawerToggle(this, myDrawerLayout, R.string.open, R.string.close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

        };
        myToggle.setDrawerIndicatorEnabled(true);
        myDrawerLayout.addDrawerListener(myToggle);

        myToggle.syncState();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                myScene.setBasemap(Basemap.createStreets());
                break;
            case 1:
                myScene.setBasemap(Basemap.createImagery());
                break;
            case 2:
                myScene.setBasemap(Basemap.createTopographic());
                break;
            case 3:
                myScene.setBasemap(Basemap.createOpenStreetMap());
                break;
        }
        myDrawerLayout.closeDrawers();
        Toast.makeText(getApplicationContext(), "This basemap is selected as: " + basemapNames[i],
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void createBookmarks() {
        Viewpoint viewpoint;
        Bookmark myBookmark;
        Point pt;
        Camera myCamera;
        //create first bookmark, add to bookmarks, and assign to bookmarkslist
        pt = new Point(35.197085, 138.9079, SpatialReference.create(4326));
        myCamera = new Camera(35.197085, 138.9079, 4554.184, 66.7696, 70.5252, 0.0);
        viewpoint = new Viewpoint(pt, 50000, myCamera);
        myBookmark = new Bookmark(getResources().getString(R.string.hakone), viewpoint);
        myBookmarks.add(myBookmark);
        //create second bookmark, add to bookmarks, and assign to bookmarkslist
        pt = new Point(35.6416, 139.71247, SpatialReference.create(4326));
        myCamera = new Camera(35.6416, 139.71247, 1865.368, 347.6451, 71.62, 0.0);
        viewpoint = new Viewpoint(pt, 50000, myCamera);
        myBookmark = new Bookmark(getResources().getString(R.string.shinjuku), viewpoint);
        myBookmarks.add(myBookmark);
        //create third bookmark, add to bookmarks, and assign to bookmarkslist
        pt = new Point(35.47389, 138.71558, SpatialReference.create(4326));
        myCamera = new Camera(35.47389, 138.71558, 1787, 39.79, 79.31, 0.0);
        viewpoint = new Viewpoint(pt, 50000, myCamera);
        myBookmark = new Bookmark(getResources().getString(R.string.kawaguchi), viewpoint);
        myBookmarks.add(myBookmark);

        pt = new Point(35.2277,138.9889, SpatialReference.create(4326));
        myCamera = new Camera(35.2277,138.9889,837,66.76,87.147,0.0);
        viewpoint = new Viewpoint(pt,50000,myCamera);
        myBookmark = new Bookmark(getResources().getString(R.string.newBookmark), viewpoint);
        myBookmarks.add(myBookmark);

        for (int i = 0; i < myBookmarks.size(); i++) {
            myBookmarksList.add(i, myBookmarks.get(i).getName());
        }
    }

}

