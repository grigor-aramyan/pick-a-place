package com.mycompany.john.pickaplace.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
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
import com.mycompany.john.pickaplace.eventBusModels.DeleteBroadcastedLiveLocationRecordEvent;
import com.mycompany.john.pickaplace.eventBusModels.UpdateBroadcastedLocationEvent;
import com.mycompany.john.pickaplace.utils.PhoenixChannels;
import com.mycompany.john.pickaplace.utils.Statics;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IMessageCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowLiveLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // finals
    private final String TAG = "tag-for-marker";
    private final int REQUEST_CHECK_SETTINGS = 108;

    // data
    private String mLongitude, mLatitude, mCode;

    // ui components
    private Marker mLivePositionMarker;
    private Marker mMyPositionMarker;
    private EditText mInfoEdt, mCodeEdt;

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
                updateMyLocation(location);

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
        joinChannel();
    }

    private void initViews() {
        mInfoEdt = (EditText) findViewById(R.id.info_edt_id);
        mInfoEdt.setEnabled(false);
        mCodeEdt = (EditText) findViewById(R.id.code_edt_id);
        mCodeEdt.setEnabled(false);
    }

    private void updateMyLocation(Location location) {
        if (mMyPositionMarker == null) {
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMyPositionMarker = mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .title("My position"));
        } else {
            mMyPositionMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        }
    }

    private void joinChannel() {
        try {
            PhoenixChannels.getChannel()
                    .join()
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            setupMessageHandlers();
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

    private void setupMessageHandlers() {
        PhoenixChannels.getChannel()
                .on(PhoenixChannels.CHANNEL_MSG_GET_LIVE_LOCATION, new IMessageCallback() {
                    @Override
                    public void onMessage(Envelope envelope) {
                        EventBus.getDefault().post(new UpdateBroadcastedLocationEvent(envelope.getPayload()));
                    }
                });

        PhoenixChannels.getChannel()
                .on(PhoenixChannels.CHANNEL_MSG_DELETE_LIVE_LOCATION, new IMessageCallback() {
                    @Override
                    public void onMessage(Envelope envelope) {
                        EventBus.getDefault().post(new DeleteBroadcastedLiveLocationRecordEvent(envelope.getPayload()));
                    }
                });
    }

    private void updateTrackedPositionMarker(Double latitude, Double longitude) {
        mLivePositionMarker.setPosition(new LatLng(latitude, longitude));
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteBroadcastedLiveLocationRecordEvent(DeleteBroadcastedLiveLocationRecordEvent event) {
        JsonNode node = event.getNode();
        final String code = node.get("code").asText();

        if (code.equals(mCode)) {

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SS");
            mInfoEdt.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));

            if (node.has("msg")) {
                mInfoEdt.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorOrange, null));
                mInfoEdt.setText("Last Fetch: " + formatter.format(new Date()) +
                    " || Broadcast stopped!");

                Toast.makeText(getApplicationContext(), "Location broadcast stopped!",
                        Toast.LENGTH_LONG).show();
            } else if (node.has("error")) {
                mInfoEdt.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorRed, null));
                mInfoEdt.setText("Last Fetch: " + formatter.format(new Date()) +
                        " || Broadcast stopped with error!");

                Toast.makeText(getApplicationContext(), "Error while trying to stop " +
                        "broadcast )) It's error on our servers, but maybe you won't be able to " +
                        "get updates on this location now!! Contact your broadcaster, plz )) We " +
                        "are trying hard to fix all issues in our system!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateBroadcastedLocationEvent(UpdateBroadcastedLocationEvent event) {
        JsonNode node = event.getNode();

        final String longitude = node.get("longitude").asText();
        final String latitude = node.get("latitude").asText();
        final String code = node.get("code").asText();

        if (code.equals(mCode)) {
            updateTrackedPositionMarker(Double.parseDouble(latitude), Double.parseDouble(longitude));
            updateBroadcastInfo();
        }
    }

    private void updateBroadcastInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SS");

        mInfoEdt.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        mInfoEdt.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorKhaki, null));
        mInfoEdt.setText("Last Fetch: " + formatter.format(new Date()));

        new CountDownTimer(2000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mInfoEdt.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.full_border, null));
                mInfoEdt.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorBlack, null));
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }
}
