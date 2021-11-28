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
import android.os.Looper;
import android.util.Log;
import android.view.View;

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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Maps Objects
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    //Location Objects
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Marker mCurrentMarker;
    private Location mLastLocation;

    //Toolbar & Tracking Button
    private Toolbar mToolbar;
    private FloatingActionButton mTrack;
    private boolean mTracking;

    //Tracking Variables
    private double mSpeed;
    private double mDistance;
    private double mTime;

    /** Lifecycle Functions */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Toolbar setup
        mToolbar = findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);

        //Tracking button set up
        mTrack = findViewById(R.id.track);
        mTrack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (mTracking){
                    //Save all info switch activities to summary screen
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.teal_200)));
                    //mTrack.setImageResource(R.drawable.play);
                    mTracking = false;
                }
                else{
                    //Start timer, distance, and speed tracking
                    mTrack.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    //mTrack.setImageResource(R.drawable.stop);
                    mTracking = true;
                }
            }
        });

        //Location Services
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        //Setup Location Request
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setSmallestDisplacement(1);
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
                mLastLocation = location;
                if (mCurrentMarker != null) {
                    mCurrentMarker.remove();
                }

                //Create a LatLng Object based on current Location
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                //Make a Marker if Needed
//                MarkerOptions markerOptions = new MarkerOptions();
//                markerOptions.position(latLng);
//                markerOptions.title("Current Position");
//                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
//                mCurrentMarker = mMap.addMarker(markerOptions);

                //Change Camera Position
                CameraPosition mCamera = new CameraPosition.Builder()
                        .target(latLng)
                        .zoom(18)
                        .tilt(40)
                        .build();
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCamera));

                Log.i("MapsActivity", "Speed: " + convertMiles(location.getSpeed()));
            }
        }
    };

    /** Time Tracker */

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
}