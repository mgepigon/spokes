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
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Camera;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Maps Objects
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Location Objects
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private boolean mInitial;
    private Marker mCurrentMarker;
    private Location mStart;
    private Location mCurrent;
    private Location mFinish;

    //Toolbar & Tracking Button
    private Toolbar mToolbar;
    private FloatingActionButton mTrack;
    private boolean mTracking;

    private TextView mSpeedView;
    private TextView mDistView;

    //Tracking Variables
    private double mSpeed;
    private double mDistance;
    private double mTime;

    //Timer
    private Handler mTimeHandler;
    private TextView mTimeView;
    private String mTimeString;

    //To Storage --> Firebase
    private List<Object> mMetrics;

    /** Lifecycle Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Toolbar setup
        mToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);

        //Initialization
        mStart = new Location("");
        mFinish = new Location("");

        //Timer
        mTime = 0;
        mTimeView = findViewById(R.id.timer);
        mTimeView.setText(getTime(mTime));
        timerSetup();

        //Tracking button set up
        mTrack = findViewById(R.id.track);
        mTrack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (mTracking){
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
                    Log.d ("Tracked", "Distance: " + distance(mFinish, mStart));
                    //Save all info switch activities to summary screen --> Firebase implementation / SharedPreferences
                    mFinish = mCurrent;
                    mTime = 0; mDistance = 0; mTracking = false;

                    //Revert Map Settings
                    mMap.getUiSettings().setAllGesturesEnabled(true);
                    mMap.getUiSettings().setCompassEnabled(true);
                    mMap.getUiSettings().setMyLocationButtonEnabled(true);
                    //Clear all text views
                    clearViews();


                }
                else{
                    //Start timer, distance, and speed tracking
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    //Change Camera Position
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

                    mStart = mCurrent;
                    //Update TextViews
                    mSpeedView.setText(getSpeed(mSpeed));
                    mDistView.setText(getDistance(mDistance));
                    mTracking = true;

                }
            }
        });

        //Performance Metrics Set up
        mSpeedView = findViewById(R.id.speed);
        mDistView = findViewById(R.id.distance);

        //Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mInitial = true;

        //Setup Location Request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(500);
        mLocationRequest.setFastestInterval(250);
        mLocationRequest.setSmallestDisplacement(2);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        startLocationUpdates();

        //Show little blue marker & other settings on map given that current location is known
        mMap.setMyLocationEnabled(true);
    }

    //Current Location Change -- used by FusedLocationClient call in onMapReady
    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //Last Location in the list is the most current location
                Location location = locationList.get(locationList.size() - 1);
                //Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mCurrent = location;

                //Create a LatLng Object based on current Location
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                if (mInitial){
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
                    mInitial = false;
                }

                if (mTracking){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //Find Speed in m/s
                    mDistance = distance(mCurrent, mStart);
                    mSpeed = location.getSpeed();
                    //Update TextViews
                    mSpeedView.setText(getSpeed(mSpeed));
                    mDistView.setText(getDistance(mDistance));
                    Log.d ("Tracked", "Distance: " + mDistance);
                }
            }
        }
    };

    //Calculate Distance
    public double distance(Location to, Location from){
        double lng = Math.pow((from.getLongitude() - to.getLongitude()), 2);
        double lat = Math.pow((from.getLatitude() - to.getLatitude()), 2);
        return Math.pow(lng+lat, (0.5));
    }
    //Draw Route
    public void drawRoute(List<Location> route){
    }

    /** Time Tracker */
    public void timerSetup(){
        mTimeHandler = new Handler();
        mTimeHandler.post(new Runnable (){
            @Override
            public void run() {
                // Increment if tracking
                if (mTracking) {
                    // Set the text
                    mTimeView.setText(getTime(mTime));
                    mTime++;
                }
                mTimeHandler.postDelayed(this, 1000);
            }
        });
    }

    /** String Conversions */
    public String getDistance(double distance){
        return String.format(Locale.getDefault(), "%.2f m", distance);
    }
    public String getSpeed(double speed){
        //Use activity recognition to get more accurate results (if time permits)
        return String.format(Locale.getDefault(), "%.2f m/s", speed);
    }
    public String getTime(double time){
        mTimeString = String.format(Locale.getDefault(), "%02d:%02d:%02d",
                (int) time / 3600, (int) (time % 3600) / 60, (int) time % 60);
        return mTimeString;
    }

    /** Firebase TODO: Need to figure out how to work this so everything is stored within app */

    /** Misc. Helper Functions */
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

    //Converters
    private double convertMiles(double meters){
        return meters*.000621371;
    }
    private double convertMeters(double miles){
        return miles/1609.34;
    }

    private void clearViews(){
        mTimeView.setText("");
        mSpeedView.setText("");
        mDistView.setText("");
    }
}