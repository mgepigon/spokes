package com.example.spokes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TripActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    //Buttons
    private boolean menuOpen;
    private FloatingActionButton mSave;
    private FloatingActionButton mBack;
    private FloatingActionButton mShow;

    //Firebase
    private FirebaseFirestore mDatabase;

    private Trip mTrip;

    private static final String TAG = "Firestore Add";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        //Connect to Firestore Database of app
        mDatabase = FirebaseFirestore.getInstance();

        if(getIntent().getExtras() != null) {
            mTrip = (Trip) getIntent().getParcelableExtra("savedTrip");
            // Put Current Trip into Firebase for use in Fragments
            Map<String, Object> trip = new HashMap<>();
            trip.put("Created", FieldValue.serverTimestamp());
            trip.put("TimeTraveled", mTrip.getTime());
            trip.put("DistanceTraveled", mTrip.getDistance());
            trip.put("AvgSpeed", mTrip.getAvgSpeed());
            trip.put("Route", mTrip.getRoute());

            // Store into Database as "Current" trip
            mDatabase.collection("allTrips").document("current")
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

        //Setup tabs & page adapter
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout.setupWithViewPager(mViewPager);

        pageAdapter adapter = new pageAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new summaryFragment(), "SUMMARY");
        adapter.addFragment(new historyFragment(), "HISTORY");
        mViewPager.setAdapter(adapter);

        //Buttons setup
        menuOpen = false;
        //Show button
        mShow = findViewById(R.id.show);
        mShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menuOpen){
                    closeMenu();
                }else{
                    showMenu();
                }
            }
        });

        //Save button --> add to database
        mSave = findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved to Database", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                // Create a trip into Firebase
                Map<String, Object> trip = new HashMap<>();
                trip.put("Created", FieldValue.serverTimestamp());
                trip.put("TimeTraveled", mTrip.getTime());
                trip.put("DistanceTraveled", mTrip.getDistance());
                trip.put("AvgSpeed", mTrip.getAvgSpeed());
                trip.put("Route", mTrip.getRoute());

                // Add to history
                mDatabase.collection("allTrips").document("history")
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

        //Back button
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete current trip
                mDatabase.collection("allTrips").document("current")
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "DocumentSnapshot successfully deleted!");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error deleting document", e);
                            }
                        });
                Intent maps = new Intent(TripActivity.this, MapsActivity.class);
                startActivity(maps);
            }
        });
    }

    //Menu Animations
    private void showMenu(){
        menuOpen=true;
        mBack.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mSave.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeMenu(){
        menuOpen=false;
        mBack.animate().translationY(0);
        mSave.animate().translationY(0);
    }
}