package com.example.spokes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_history, container, false);

        mDatabase = FirebaseFirestore.getInstance();

        //Setup of List
        mTripArray = new ArrayList<Trip>();
        mHistoryList = v.findViewById(R.id.history);
        adapter = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_list_item_1, mTripArray);
        mHistoryList.setAdapter(adapter);

        //Create history list TODO: Change name of each entry to something that makes sense
        loadHistory();

        mHistoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Creates current view of trip selected
                Trip trip = mTripArray.get(position);
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
        DocumentReference docRef = mDatabase.collection("allTrips").document("history");
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
    }
}