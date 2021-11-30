package com.example.spokes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.spokes.databinding.ActivityMapsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Maps Objects
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Location Objects
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mInitial;
    private Location mCurrent;
    private Location mLast;

    //Toolbar & Tracking Button
    private Toolbar mToolbar;
    private FloatingActionButton mTrack;
    private boolean mTracking;

    private TextView mSpeedView;
    private TextView mDistView;

    //Tracking Variables
    private double mCurrSpeed;
    private double mDistance;
    private double mTime;

    //Timer
    private Handler mTimeHandler;
    private TextView mTimeView;
    private String mTimeString;

    //To Storage --> Access to firebase in Fragments
    private List<Location> mRoute;
    private double mAvgSpeed;

    /** Lifecycle Functions */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Toolbar setup
        mToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);

        //Route List setup
        mRoute = new Vector<Location>();

        //Timer
        mTime = 0;
        //Start a timer thread ticking every second
        timerSetup();

        //Views
        //Performance Metrics Set up
        mSpeedView = findViewById(R.id.speed);
        mDistView = findViewById(R.id.distance);
        //Time setup
        mTimeView = findViewById(R.id.timer);
        mTimeView.setText(getTime(mTime));

        //Tracking button setup
        mTrack = findViewById(R.id.track);
        mTrack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (mTracking){
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
                    //Add last location to route and find average speed
                    mRoute.add(mCurrent);
                    mAvgSpeed = avg(mRoute);
                    //Save all into a Trip object (defined in Trip)
                    Trip finishedTrip = new Trip(mDistance, mAvgSpeed, mTimeString, mRoute);

                    //Reset everything
                    mTime = 0; mDistance = 0; mCurrSpeed = 0;
                    mTracking = false;
                    mRoute.clear();

                    //Sanity Check (make sure there's actually stuff in here)
                    Log.d ("Tracked", "Distance: " + finishedTrip.getDistance());
                    Log.d ("Tracked", "AvgSpeed: " + finishedTrip.getAvgSpeed());
                    Log.d ("Tracked", "Route Size: " + finishedTrip.getRouteSize());

                    //Revert Map UI Settings
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);

                    //Clear all text views
                    clearViews();

                    //Go to trip summary
                    Intent summary = new Intent(MapsActivity.this, TripActivity.class);
                    summary.putExtra("savedTrip", finishedTrip);
                    startActivity(summary);
                }
                else{
                    //Start timer, distance, and speed tracking

                    //Change button color  TODO: make into a stop button
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    //Change camera position TODO: camera tracking to where phone is facing
                    LatLng latLng = new LatLng(mCurrent.getLatitude(), mCurrent.getLongitude());
                    CameraPosition mCamera = new CameraPosition.Builder()
                            .target(latLng)
                            .zoom(18)
                            .tilt(40)
                            .build();
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCamera));
                    mMap.getUiSettings().setAllGesturesEnabled(false);
                    mMap.getUiSettings().setCompassEnabled(false);
                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                    //Starting Location
                    mRoute.add(mCurrent);
                    //Update TextViews (set to 0s)
                    mSpeedView.setText(getSpeed(mCurrSpeed));
                    mDistView.setText(getDistance(mDistance));

                    mTracking = true;
                }
            }
        });

        //Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //At the start, nothing
        clearViews();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        //Stop location updates when inactive
        if (mFusedLocationClient != null){
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStart() throws SecurityException {
        super.onStart();
        //Grab all permissions
        int PERMISSION_ALL = 69;
        // Permissions Needed (add more if necessary)
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET
        };

        // Asks for all Permissions
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    /** Map & Location Tracking */

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        //First time map is setup (used in Location callback)
        mInitial = true;

        //Setup Location Request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //Start Location callback
        startLocationUpdates();

        //Show little blue marker & other settings on map given that current location is known
        mMap.setMyLocationEnabled(true);
    }

    //Current Location Change (update interval set in onMapReady)
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //Last Location in the list is the most current location
                Location location = locationList.get(locationList.size() - 1);
                mCurrent = location;

                //Create a LatLng Object based on current Location
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                //On first instance just zoom into current location
                if (mInitial){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    mInitial = false;
                }

                //TODO: use of activity detection package to detect that we're riding
                // a bicycle (ON_BICYCLE) for better accuracy
                //When tracking make camera follow movement -- store locations into Route, update views
                if (mTracking){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //Find speed in m/s & distance from Start
                    //TODO: distance calculation should be accumulation of each displacement per location,
                    // right now it's just displacement from current and the start

                    if(mLast == null) {
                        mDistance = distance(mCurrent, mRoute.get(0));
                        Log.d("mTracking", "null");
                    }
                    else
                        mDistance = mDistance + distance(mCurrent, mLast);

                    //mDistance = distance(mCurrent, mRoute.get(0));
                    mRoute.add(mCurrent);
                    mCurrSpeed = location.getSpeed();
                    //Update TextViews
                    mSpeedView.setText(getSpeed(mCurrSpeed));
                    mDistView.setText(getDistance(mDistance));

                    mLast = mCurrent;
                }
            }
        }
    };

    /** Time Tracker */
    public void timerSetup(){
        mTimeHandler = new Handler();
        mTimeHandler.post(new Runnable (){
            @Override
            public void run() {
                // Increment time
                if (mTracking) {
                    // Set the text (starts at 0)
                    mTimeView.setText(getTime(mTime));
                    mTime++;
                }
                //Happen every 1000ms -> 1sec
                mTimeHandler.postDelayed(this, 1000);
            }
        });
    }

    /** String Conversions */
    public String getDistance(double distance){
        return String.format(Locale.getDefault(), "%.2f m", distance);
    }
    public String getSpeed(double speed){
        return String.format(Locale.getDefault(), "%.2f m/s", speed);
    }
    public String getTime(double time){
        mTimeString = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                (int) time / 3600, (int) (time % 3600) / 60, (int) time % 60);
        return mTimeString;
    }

    /** Misc. Helper Functions*/
    //Find average (in this case, speed)
    public double avg(List<Location> route){
        double result = 0;
        //Use mRoute to extract speeds from each location stored
        if (!route.isEmpty()){
            for (Location location: route){
                result+=location.getSpeed();
            }
            //Find average speed
            return result/route.size();
        }
        return result;
    }

    //Converters --> can be used in a settings feature that can change units
    private double convertMiles(double meters){
        return meters*.000621371;
    }
    private double convertMeters(double miles){
        return miles/1609.34;
    }

    //Calculate Distance (in meters)
    public double distance(Location to, Location from){
        return to.distanceTo(from);
    }

    // Checks for multiple permissions at once
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //Starts Location Updates
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper());
    }

    //Stops Location Updates
    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    //Empty TextViews
    private void clearViews(){
        mTimeView.setText("");
        mSpeedView.setText("");
        mDistView.setText("");
    }
}