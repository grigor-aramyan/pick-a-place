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
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import com.mycompany.john.pickaplace.eventBusModels.DeleteBroadcastedLiveLocationRecordEvent;
import com.mycompany.john.pickaplace.eventBusModels.UpdateBroadcastedLocationEvent;
import com.mycompany.john.pickaplace.models.Coordinates;
import com.mycompany.john.pickaplace.models.MyCustomLocation;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;
import com.mycompany.john.pickaplace.utils.PhoenixChannels;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IMessageCallback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BroadcastLiveLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // data
    private final int REQUEST_CHECK_SETTINGS = 110;
    private String mLiveLocationCode;

    // map related
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private final LocationRequest mLocationRequest = new LocationRequest();
    private Marker mMyPositionMarker;

    // ui components
    private EditText mInfoEdt, mCodeEdt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_live_location_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initViews();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                if (mLiveLocationCode == null) {
                    createInitialData(location);
                } else {
                    serverUpdateLiveLocationData(location);
                }
            }
        };

        setupMessageHandlers();
    }

    private void initViews() {
        mInfoEdt = (EditText) findViewById(R.id.info_edt_id);
        mInfoEdt.setEnabled(false);
        mCodeEdt = (EditText) findViewById(R.id.code_edt_id);
        mCodeEdt.setEnabled(false);
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

    private void serverUpdateLiveLocationData(Location location) {
        ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                .put("code", mLiveLocationCode)
                .put("longitude", location.getLongitude() + "")
                .put("latitude",  location.getLatitude() + "");
        try {
            PhoenixChannels.getChannel()
                    .push(PhoenixChannels.CHANNEL_MSG_UPDATE_LIVE_LOCATION, node);
        } catch (IOException ioExp) {
            Toast.makeText(getApplicationContext(), "Something is wrong!! Try to " +
                    "restart the app, plz, or try later", Toast.LENGTH_LONG).show();
        }
    }

    private void createInitialData(Location location) {
        Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                .createLiveAnonymousLocation(new MyCustomLocation(new Coordinates(location.getLatitude() + "",
                        location.getLongitude() + "", "")));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {

                    if (response.code() == 201) {
                        JSONObject data = new JSONObject(response.body().string());
                        final String code = data.getJSONObject("data").getString("code");
                        final String message = data.getJSONObject("data").getString("message");
                        final String longitude = data.getJSONObject("data").getString("longitude");
                        final String latitude = data.getJSONObject("data").getString("latitude");

                        mLiveLocationCode = code;
                        mCodeEdt.setText("Shared Code: " + mLiveLocationCode);
                        setInitialMyPositionMarker(longitude, latitude);
                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong with connection, " +
                                "can't push data to servers! Try to restart the app, plz or try later ))",
                                Toast.LENGTH_LONG).show();
                    }

                } catch (IOException ioExp) {
                    Toast.makeText(getApplicationContext(), "Something wrong happened! " +
                            "Try to restart the app, plz))", Toast.LENGTH_LONG).show();
                } catch (JSONException jexp) {
                    Toast.makeText(getApplicationContext(), "Something wrong with our servers! " +
                            "Try later, plz))", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " +
                        t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setInitialMyPositionMarker(String longitude, String latitude) {
        LatLng currentPosition = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        mMyPositionMarker = mMap.addMarker(new MarkerOptions()
                .position(currentPosition)
                .title("You are here"));
        mMyPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.twotone_location_on_black_24));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15.0f));
    }

    private void resetMyLocationMarker(String longitude, String latitude) {
        mMyPositionMarker.setPosition(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)));
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

        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
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
                        resolvable.startResolutionForResult(BroadcastLiveLocationMapsActivity.this,
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
                    Toast.makeText(getApplicationContext(), "Can't work out your location " +
                            "settings! Try to restart the app, plz",
                            Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Can't locate your current location! " +
                        "Check settings, plz, otherwise broadcasting your position will be impossible!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteBroadcastedLiveLocationRecordEvent(DeleteBroadcastedLiveLocationRecordEvent event) {
        JsonNode node = event.getNode();
        final String code = node.get("code").asText();

        if (code.equals(mLiveLocationCode)) {
            if (node.has("msg")) {
                Toast.makeText(getApplicationContext(), "Live location data deleted",
                        Toast.LENGTH_LONG).show();
            } else if (node.has("error")) {
                Toast.makeText(getApplicationContext(), "Error occured while deleting " +
                        "your location data. We are trying hard to fix things. Thanks " +
                        "for bearing with us ))", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateBroadcastedLocationEvent(UpdateBroadcastedLocationEvent event) {
        JsonNode node = event.getNode();

        final String longitude = node.get("longitude").asText();
        final String latitude = node.get("latitude").asText();
        final String code = node.get("code").asText();

        if (code.equals(mLiveLocationCode)) {
            resetMyLocationMarker(longitude, latitude);
            updateBroadcastInfo();
        }
    }

    private void updateBroadcastInfo() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SS");

        mInfoEdt.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorWhite, null));
        mInfoEdt.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.colorKhaki, null));
        mInfoEdt.setText("Last Broadcast: " + formatter.format(new Date()));

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

    @Override
    protected void onDestroy() {

        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);

        ObjectNode node = new ObjectNode(JsonNodeFactory.instance)
                .put("code", mLiveLocationCode);
        try {
            PhoenixChannels.getChannel()
                    .push(PhoenixChannels.CHANNEL_MSG_DELETE_LIVE_LOCATION, node);
        } catch (IOException ioExp) {
            Toast.makeText(getApplicationContext(), "Can't delete your location data from " +
                    "our servers! Restart the app, plz! We are trying hard to fix all issues ))",
                    Toast.LENGTH_LONG).show();
        }

        super.onDestroy();
    }
}
