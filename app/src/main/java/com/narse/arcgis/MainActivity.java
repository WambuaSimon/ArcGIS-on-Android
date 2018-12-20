package com.narse.arcgis;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;

public class MainActivity extends AppCompatActivity {
    MapView mv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mv = (MapView) findViewById(R.id.map1);
        ArcGISMap myMap = new ArcGISMap(Basemap.createNationalGeographic());
//        Viewpoint myViewpoint = new Viewpoint(-1.283120,36.806990,200000);
//        myMap.setInitialViewpoint(myViewpoint);
//        ArcGISMap map = new ArcGISMap(Basemap.Type.IMAGERY, -1.283120,36.806990,11);
        Envelope myEnvelope = new Envelope(-122.396659, 37.835778, -122.341212, 37.809201,
                SpatialReference.create(4326));
        Viewpoint myViewpoint2 = new Viewpoint(myEnvelope);
        myMap.setInitialViewpoint(myViewpoint2);
        mv.setMap(myMap);
        mv.setOnTouchListener(new DefaultMapViewOnTouchListener(this,mv) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                int x = (int)e.getX();
                int y = (int)e.getY();
                android.graphics.Point clickedPoint = new android.graphics.Point(x,y);
                Point myWebMercatorPoint = mv.screenToLocation(clickedPoint);
                Point myWGS84Point = (Point) GeometryEngine.project(myWebMercatorPoint,
                        SpatialReferences.getWgs84());

                Toast.makeText(getApplicationContext(), "map is touched ! " + myWGS84Point.getX() +
                                " \n" + myWGS84Point.getY(),
                        Toast.LENGTH_LONG).show();

                return true;
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mv.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mv.resume();
    }
}
