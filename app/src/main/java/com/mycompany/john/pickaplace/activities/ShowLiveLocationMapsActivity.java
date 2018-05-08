package com.mycompany.john.pickaplace.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.models.Coordinates;
import com.mycompany.john.pickaplace.utils.PhoenixChannels;
import com.mycompany.john.pickaplace.utils.Statics;

import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IMessageCallback;

import java.io.IOException;

public class ShowLiveLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // finals
    private final String TAG = "tag-for-marker";
    private final int REQUEST_CHECK_SETTINGS = 108;

    // data
    private String mLongitude, mLatitude, mCode;

    // ui components
    private Marker mLivePositionMarker;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    final LocationRequest mLocationRequest = new LocationRequest();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_live_location_maps);
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

            }
        };

        if (getIntent().hasExtra(Statics.LOCATION_LONGITUDE)) {
            mLongitude = getIntent().getStringExtra(Statics.LOCATION_LONGITUDE);
        }
        if (getIntent().hasExtra(Statics.LOCATION_LATITUDE)) {
            mLatitude = getIntent().getStringExtra(Statics.LOCATION_LATITUDE);
        }
        if (getIntent().hasExtra(Statics.LOCATION_MESSAGE)) {
            final String message = getIntent().getStringExtra(Statics.LOCATION_MESSAGE);

        }
        if (getIntent().hasExtra(Statics.LOCATION_CODE)) {
            mCode = getIntent().getStringExtra(Statics.LOCATION_CODE);
        }

        setInitialMarker();
        subscribeToChannelMessages();
    }

    private void subscribeToChannelMessages() {
        try {
            PhoenixChannels.getChannel()
                    .join()
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            setupMessageHandler();
                        }
                    })
                    .receive("ignore", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            Toast.makeText(getApplicationContext(), "Something wrong happened! Try to " +
                                    "restart the app, plz", Toast.LENGTH_LONG).show();
                        }
                    });
        } catch (IOException ioExp) {
            Toast.makeText(getApplicationContext(), "Something wrong happened! Try to " +
                    "restart the app, plz", Toast.LENGTH_LONG).show();
        }
    }

    private void setupMessageHandler() {
        PhoenixChannels.getChannel()
                .on(PhoenixChannels.CHANNEL_MSG_GET_LIVE_LOCATION, new IMessageCallback() {
                    @Override
                    public void onMessage(Envelope envelope) {
                        Log.e("mmm", "envelope: " + envelope.toString());
                    }
                });
    }

    private void setInitialMarker() {

        LatLng currentPosition = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
        mLivePositionMarker = mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .title("Live position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15.0f));

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

        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
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
                        resolvable.startResolutionForResult(ShowLiveLocationMapsActivity.this,
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
