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
import com.esri.arcgisruntime.mapping.ArcGISScene;
import com.esri.arcgisruntime.mapping.ArcGISTiledElevationSource;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Camera;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.DefaultSceneViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.mapping.view.SceneView;

public class MainActivity extends AppCompatActivity {
    MapView mv;
    SceneView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sv = (SceneView) findViewById(R.id.map1);
        ArcGISScene myScene = new ArcGISScene(Basemap.createTopographic());
        sv.setScene(myScene);

        Camera myCamera = new Camera(35.59520, 138.78045, 2719.97086, 195.47934, 79.357552, 0.0);
        sv.setViewpointCamera(myCamera);
        ArcGISTiledElevationSource myElevation = new ArcGISTiledElevationSource(getString(R.string.elevation3dUrl));
        myScene.getBaseSurface().getElevationSources().add(myElevation);

        sv.setOnTouchListener(new DefaultSceneViewOnTouchListener(sv) {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                String lat1 = String.valueOf("Lat: " + sv.getCurrentViewpointCamera().getLocation().getX());
                String lon1 = String.valueOf("Lon: " + sv.getCurrentViewpointCamera().getLocation().getY());
                String ele1 = String.valueOf("Elevation: " + sv.getCurrentViewpointCamera().getLocation().getZ());
                String heading = String.valueOf("Heading: " + sv.getCurrentViewpointCamera().getHeading());
                String pitching = String.valueOf("Pitching: " + sv.getCurrentViewpointCamera().getPitch());
                String roll = String.valueOf("Roll: " + sv.getCurrentViewpointCamera().getRoll());
                Toast.makeText(getApplicationContext(),
                        lat1 + "\n" + lon1 + "\n" + ele1 + "\n" + heading + "\n" + pitching + "\n" + roll, Toast.LENGTH_SHORT).show();

                return true;

            }
        });

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
}
