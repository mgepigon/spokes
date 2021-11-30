package com.example.spokes;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class summaryFragment extends Fragment {

    private static final String TAG = "SummaryFragment" ;
    //Object Views
    TextView mTime;
    TextView mDistance;
    TextView mSpeed;

    //Database
    FirebaseFirestore mDatabase;

    //TODO: setup map to show all locations in route array and draw route using lines?
    //TODO: change font of textviews for aesthetics
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_summary, container, false);

        //Setup
        mTime = (TextView) v.findViewById(R.id.timeSummary);
        mDistance = (TextView) v.findViewById(R.id.distanceSummary);
        mSpeed = (TextView) v.findViewById(R.id.avgSpeedSummary);

        mDatabase = FirebaseFirestore.getInstance();
        DocumentReference docRef = mDatabase.collection("allTrips").document("current");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> currentTrip = document.getData();
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        mTime.setText((String) currentTrip.get("TimeTraveled"));
                        mDistance.setText(getDistance((double) currentTrip.get("DistanceTraveled")));
                        mSpeed.setText(getSpeed((double) currentTrip.get("AvgSpeed")));
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

    public String getDistance(double distance){
        return String.format(Locale.getDefault(), "%.2f m", distance);
    }
    public String getSpeed(double speed){
        //Use activity recognition to get more accurate results (if time permits)
        return String.format(Locale.getDefault(), "%.2f m/s", speed);
    }

    //Draw Route
    public void drawRoute(List<Location> route){
    }
}