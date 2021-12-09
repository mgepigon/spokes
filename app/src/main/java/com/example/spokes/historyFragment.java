package com.example.spokes;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.Fragment;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class historyFragment extends Fragment {
    //TODO setup a scrollable listview and grab history from Firestore

    private static final String TAG = "HistoryFragment" ;

    //History List
    ArrayList<Trip> mTripArray;
    ArrayAdapter adapter;
    ListView mHistoryList;

    //Trip Database
    FirebaseFirestore mDatabase;

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    SupportMapFragment mapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_history, container, false);
        //View sum = inflater.inflate(R.layout.fragment_summary, container, false);

        mDatabase = FirebaseFirestore.getInstance();

        //Setup of List
        mTripArray = new ArrayList<Trip>();
        mHistoryList = v.findViewById(R.id.history);
        adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, mTripArray);
        mHistoryList.setAdapter(adapter);

        //Create history list TODO: Change name of each entry to something that makes sense
        loadHistory();

        mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Creates current view of trip selected
                Trip trip = mTripArray.get(position);
                Double dist = BigDecimal.valueOf(trip.getDistance())
                        .setScale(3, RoundingMode.HALF_UP)
                        .doubleValue();
                Double speed = BigDecimal.valueOf(trip.getAvgSpeed())
                        .setScale(3, RoundingMode.HALF_UP)
                        .doubleValue();
                builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Time Traveled: " + trip.getTime() + "\n" + "Distance Traveled: " + dist + " m\n" + "Avg Speed: " + speed + " m/s");
                dialog = builder.create();
                dialog.show();

                /*mTime.setText(trip.getTime());
                mDistance.setText(""+trip.getDistance());
                mSpeed.setText(""+trip.getAvgSpeed());*/

                //TripDialogFragment dialog = new TripDialogFragment ();
                //FragmentManager fragmentManager = getFragmentManager();
                //dialog.show(fragmentManager, "dialog");
                //dialog.show
                //new TripDialogFragment().show(getActivity().getSupportFragmentManager(), null);

                /*Fragment newSumFragment = new summaryFragment();
                // consider using Java coding conventions (upper first char class names!!!)
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack
                transaction.replace(R.layout.fragment_summary, newSumFragment);
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();*/
                //Add the current trip to allTrips collection
                //TODO: Come up with a way of showing the selected location (either put it in summary or open a new activity? or dialog box?
                mDatabase.collection("allTrips").document("selected")
                        .set(trip)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully written!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error writing document", e);
                            }
                        });
            }
        });
        return v;
    }

    public void loadHistory(){
        CollectionReference collRef = mDatabase.collection("allTrips").document("history").collection("historyTrips");
        collRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        if (document.exists()) {
                            //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            Map<String, Object> currentTrip = document.getData();
                            //Add trip to Trip array so it shows up as one of the entries in the list adapter
                            Trip trip = new Trip((double)currentTrip.get("DistanceTraveled"), (double) currentTrip.get("AvgSpeed"),
                                    (String) currentTrip.get("TimeTraveled"), (List<Location>) currentTrip.get("Route"));
                            mTripArray.add(trip);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    }
                } else {
                    Log.w(TAG, "Error getting documents.", task.getException());
                }
            }
        });
        /*DocumentReference docRef = mDatabase.collection("allTrips").document("history");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        //Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Map<String, Object> currentTrip = document.getData();
                        //Add trip to Trip array so it shows up as one of the entries in the list adapter
                        Trip trip = new Trip((double)currentTrip.get("DistanceTraveled"), (double) currentTrip.get("AvgSpeed"),
                                (String) currentTrip.get("TimeTraveled"), (List<Location>) currentTrip.get("Route"));
                        mTripArray.add(trip);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    */}
}