package com.narse.arcgis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    MapView mv;
    private ListView cityList;
    private ArrayAdapter myAdapter;
    String[] cities = {
            "New York",
            "Chicago",
            "Denver",
            "Detroit",
            "Las Vegas",
            "Paris"
    };
    private ArcGISMap myMap;

    boolean myFlag;
    private BookmarkList myBookmarks;
    private Bookmark myBookmark;
    private ArrayList myBookmarksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mv = (MapView) findViewById(R.id.map1);
        myMap = new ArcGISMap(Basemap.Type.STREETS_VECTOR, 37.7531, -122.4479, 11);
        mv.setMap(myMap);

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
                        , myBookmarks.get(i).getName(),Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
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

        for (int i = 0; i < myBookmarks.size(); i++) {
            myBookmarksList.add(i, myBookmarks.get(i).getName());
        }
    }
}

