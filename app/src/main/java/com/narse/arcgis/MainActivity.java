package com.narse.arcgis;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
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

public class MainActivity extends AppCompatActivity {
    MapView mv;
    private ListView cityList;
    private ArrayAdapter myAdapter;
    private Callout myCallout;
    String[] cities = {
            "New York",
            "Chicago",
            "Denver",
            "Detroit",
            "Las Vegas",
            "Paris"
    };
    private ArcGISMap myMap;
    private Viewpoint homeViewpoint;
    boolean myFlag;
    private BookmarkList myBookmarks;
    private Bookmark myBookmark;
    private ArrayList myBookmarksList;
    private String myQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mv = (MapView) findViewById(R.id.map1);
        myMap = new ArcGISMap(Basemap.createStreets());
        homeViewpoint = new Viewpoint(37.754178, -122.448095, 271261);
        myMap.setInitialViewpoint(homeViewpoint);
        mv.setMap(myMap);
        myMap.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                mv.setMagnifierEnabled(true);
                mv.setCanMagnifierPanMap(true);
            }
        });

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.myButton1);
        myFab.setOnClickListener(myFabOnClickListener);

        mv.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mv) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                android.graphics.Point screenPt = new android.graphics.Point(Math.round(e.getX()),
                        Math.round(e.getY()));
                Point mapPt = mMapView.screenToLocation(screenPt);
                Point wgs84Pt = (Point) GeometryEngine.project(mapPt, SpatialReferences.getWgs84());

                TextView myCalloutWords = new TextView(getApplicationContext());
                myCalloutWords.setTextColor(Color.BLUE);
                myCalloutWords.setLines(3);
                myCalloutWords.setText("Lat: " + String.format(Locale.US,
                        "%.6f", wgs84Pt.getY()) +
                        ",\nLon: " + String.format(Locale.US,
                        "%.6f", wgs84Pt.getX()) + ",\nScale: " + mv.getMapScale());
                myCallout = mv.getCallout();

                myCallout.setLocation(mapPt);
                myCallout.setContent(myCalloutWords);
                if (myCallout.isShowing()) {
                    myCallout.dismiss();
                } else {
                    myCallout.show();
                }
                return true;
            }
        });

        myBookmarks = myMap.getBookmarks();
        myBookmarksList = new ArrayList();

        createBookmarks();

        cityList = (ListView) findViewById(R.id.listview);
        myAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                myBookmarksList);
        cityList.setAdapter(myAdapter);
        cityList.setVisibility(View.GONE);
        cityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mv.setViewpointAsync(myBookmarks.get(i).getViewpoint());
                Toast.makeText(getApplicationContext()
                        , myBookmarks.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Configure SearchView
        final SearchManager searchMgr = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search1).getActionView();
        searchView.setQueryHint("input address here");
        searchView.setSearchableInfo(searchMgr.getSearchableInfo(getComponentName()));
//display the submit button
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                myQuery = query;
                Toast.makeText(MainActivity.this, "You are searching: " + myQuery, Toast.LENGTH_LONG).show();
                searchNewPlace();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_item1:
                myMap.setBasemap(Basemap.createStreets());
                return true;
            case R.id.action_item2:
                myMap.setBasemap(Basemap.createImagery());
                return true;
            case R.id.action_item3:
                myMap.setBasemap(Basemap.createTopographic());
                return true;
            case R.id.action_item4:
                myMap.setBasemap(Basemap.createOpenStreetMap());
                return true;
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
                return super.onOptionsItemSelected(item);
        }
    }

    private void createBookmarks() {
        Viewpoint viewpoint;
        Bookmark myBookmark;
        //create first bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.754178, -122.448095, 271261);
        myBookmark = new Bookmark(getResources().getString(R.string.san_francisco), viewpoint);
        myBookmarks.add(myBookmark);

        //create second bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.823785, -122.370654, 67820);
        myBookmark = new Bookmark(getResources().getString(R.string.treasure_island), viewpoint);
        myBookmarks.add(myBookmark);

        //create third bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.328353, -121.889616, 135630);
        myBookmark = new Bookmark(getResources().getString(R.string.san_jose), viewpoint);
        myBookmarks.add(myBookmark);

        //create fourth bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(33.761795, -118.238254, 542523);
        myBookmark = new Bookmark(getResources().getString(R.string.long_beach), viewpoint);
        myBookmarks.add(myBookmark);

        //create 5th bookmark, add to bookmarks, and assign to bookmarkslist
        viewpoint = new Viewpoint(37.4458, -122.1564, 67815);
        myBookmark = new Bookmark(getResources().getString(R.string.palo_alto), viewpoint);
        myBookmarks.add(myBookmark);


        for (int i = 0; i < myBookmarks.size(); i++) {
            myBookmarksList.add(i, myBookmarks.get(i).getName());
        }
    }

    private View.OnClickListener myFabOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mv.setViewpointAsync(homeViewpoint, 6);
            Toast.makeText(MainActivity.this, "Back to Home Viewpoint!", Toast.LENGTH_LONG).show();
        }
    };

    private void searchNewPlace(){
        //create a geocoding task using online service
        final LocatorTask geoCodeServer = new LocatorTask(getString(R.string.geoCode_server));
        final ListenableFuture<List<GeocodeResult>> geoCodeFuture =
                geoCodeServer.geocodeAsync(myQuery);

        geoCodeFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    //retrieve the geocoding results
                    List<GeocodeResult> geocodeResults = geoCodeFuture.get();
                    if (geocodeResults != null) {
                        //Use the first result candidate
                        GeocodeResult firstCandidate = geocodeResults.get(0);
                        Double x = firstCandidate.getDisplayLocation().getX();
                        Double y = firstCandidate.getDisplayLocation().getY();
                        Viewpoint myViewpoint = new Viewpoint(y,x,50000);
                        mv.setViewpointAsync(myViewpoint);
                        Point pt = new Point(x,y, SpatialReference.create(4326));
                        SimpleMarkerSymbol mySymbol = new
                                SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, Color.RED,18);
                        Graphic myGraphic = new Graphic(pt,mySymbol);
                        GraphicsOverlay myGraphicsOverlay = new GraphicsOverlay();
                        myGraphicsOverlay.getGraphics().add(myGraphic);
                        mv.getGraphicsOverlays().add(myGraphicsOverlay);
                        Toast.makeText(getApplicationContext(),firstCandidate.getLabel(),Toast.LENGTH_LONG).show();
                    }
                }
                catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.geocoding_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

