package com.mycompany.john.pickaplace.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.utils.Statics;

public class ShowLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // finals
    private final int REQUEST_CHECK_SETTINGS = 120;

    // ui components
    private EditText mMessageEdt;
    private Marker mTrackedLocationMarker;
    private Marker mMyPositionMarker;

    // live location related
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private final LocationRequest mLocationRequest = new LocationRequest();

    // data
    private String mLatitude, mLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();
                updateMyLocation(location);

            }
        };

        initViews();

        if (getIntent().hasExtra(Statics.LOCATION_LONGITUDE)) {
            mLongitude = getIntent().getStringExtra(Statics.LOCATION_LONGITUDE);
        }
        if (getIntent().hasExtra(Statics.LOCATION_LATITUDE)) {
            mLatitude = getIntent().getStringExtra(Statics.LOCATION_LATITUDE);
        }
        if (getIntent().hasExtra(Statics.LOCATION_MESSAGE)) {
            final String message = getIntent().getStringExtra(Statics.LOCATION_MESSAGE);
            if (message != null && !message.equals("null") && !message.isEmpty()) {
                mMessageEdt.setText(message);
                mMessageEdt.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorBlack, null));
            }
        }
    }

    private void updateMyLocation(Location location) {
        LatLng customLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMyPositionMarker == null) {
            mMyPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(customLocation)
                    .title("You are here ))"));
            mMyPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.baseline_face_black_24dp));
        } else {
            mMyPositionMarker.setPosition(customLocation);
        }
    }

    private void initViews() {
        mMessageEdt = (EditText) findViewById(R.id.message_edt_id);
        mMessageEdt.setEnabled(false);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        createLocationRequest();

        if (mLongitude != null && mLatitude != null) {
            // Add a marker in Sydney and move the camera
            LatLng customLocation = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
            mTrackedLocationMarker = mMap.addMarker(new MarkerOptions().position(customLocation).title("Picked Location"));
            mTrackedLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.twotone_location_on_black_24));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customLocation, 15.0f));
        } else {
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }

    private void createLocationRequest() {
        mLocationRequest.setInterval(5000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                try {
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null);
                } catch (SecurityException sExp) {
                    Toast.makeText(getApplicationContext(), "Can't find your MyCustomLocation." +
                            " Try to drag and drop marker to check needed place from here", Toast.LENGTH_LONG).show();
                }
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {

                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(ShowLocationMapsActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                try {
                    mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest,
                            mLocationCallback,
                            null);
                } catch (SecurityException sExp) {
                    Toast.makeText(getApplicationContext(), "Can't find your MyCustomLocation." +
                            " Try to drag and drop marker to check needed place from here", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Can't locate your position! Try to " +
                        "restart the app and check settings, plz", Toast.LENGTH_LONG).show();
            }
        }
    }
}
