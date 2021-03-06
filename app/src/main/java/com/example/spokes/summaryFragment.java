package com.example.spokes;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
/** Summary fragment showing the current trip summary */
public class summaryFragment extends Fragment {

    private static final String TAG = "SummaryFragment" ;
    //Object Views
    TextView mTime;
    TextView mDistance;
    TextView mSpeed;

    SupportMapFragment mapFragment;

    //Route list used in creation of route on mapFragment
    private List<LatLng> mRoute = new ArrayList<>();

    private static final int POLYLINE_STROKE_WIDTH_PX = 12;

    private static boolean added;

    //Database
    FirebaseFirestore mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_summary, container, false);

        //Setup
        mTime = (TextView) v.findViewById(R.id.timeSummary);
        mDistance = (TextView) v.findViewById(R.id.distanceSummary);
        mSpeed = (TextView) v.findViewById(R.id.avgSpeedSummary);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapSummary);

        //Grab the current trip in the data base and display it
        mDatabase = FirebaseFirestore.getInstance();
        DocumentReference docRef = mDatabase.collection("allTrips").document("current");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> currentTrip = document.getData();

                        mTime.setText("Time Traveled: " + (String) currentTrip.get("TimeTraveled"));
                        mDistance.setText("Distance Traveled: " + getDistance((double) currentTrip.get("DistanceTraveled")));
                        mSpeed.setText("Avg Speed: " + getSpeed((double) currentTrip.get("AvgSpeed")));
                        //Show the current route on mapFragment included
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull GoogleMap googleMap) {
                                List<Object> locations = (List<Object>) currentTrip.get("Route");
                                for (Object locationObj : locations) {
                                    Map<String, Object> location = (Map<String, Object>) locationObj;
                                    LatLng latLng = new LatLng((Double) location.get("latitude"), (Double) location.get("longitude"));
                                    mRoute.add(latLng);
                                }
                                LatLng startLoc = mRoute.get(0);
                                googleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                        .position(startLoc)
                                        .title("Start Location"));
                                LatLng endLoc = mRoute.get(mRoute.size()-1);
                                googleMap.addMarker(new MarkerOptions()
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                        .position(endLoc)
                                        .title("End Location"));
                                LatLng midLoc = new LatLng((startLoc.latitude+endLoc.latitude)/2, (startLoc.longitude+endLoc.longitude)/2);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(midLoc));
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLoc, 15f));

                                //Prevents the creation of two routes
                                SharedPreferences sp = getContext().getSharedPreferences("sp1", Context.MODE_PRIVATE);
                                added = sp.getBoolean("added", false);
                                if(!added) {
                                    Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                                            .clickable(false)
                                            .addAll(mRoute));
                                    stylePolyline(polyline1);
                                    added = true;
                                    SharedPreferences.Editor editor = sp.edit();
                                    editor.putBoolean("added", added);
                                    editor.apply();
                                }
                            }
                        });
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        return v;
    }

    //Helper functions for converting double parameters to strings
    public String getDistance(double distance){
        return String.format(Locale.getDefault(), "%.2f mi", distance);
    }
    public String getSpeed(double speed){
        return String.format(Locale.getDefault(), "%.2f mph", speed);
    }
    //Draw parameters for route
    private void stylePolyline(Polyline polyline) {
        polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
        polyline.setColor(Color.RED);
        polyline.setJointType(JointType.ROUND);
    }
}