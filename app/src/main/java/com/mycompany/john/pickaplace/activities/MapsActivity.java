package com.mycompany.john.pickaplace.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.models.Coordinates;
import com.mycompany.john.pickaplace.models.MyCustomLocation;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap.OnMarkerDragListener mMarkerDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            if (marker.getTag().equals(TAG)) {
                Toast.makeText(getApplicationContext(), "lang: " + marker.getPosition().latitude,
                        Toast.LENGTH_LONG).show();

                mPickedCoordinates = new Coordinates(marker.getPosition().latitude + "",
                        marker.getPosition().longitude + "", "");
            }
        }
    };

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.save_btn:
                    final String message = mMessageEdt.getText().toString();
                    if (message.isEmpty()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(that);

                        builder.setTitle("Empty message")
                                .setMessage("Location will be submitted without any message!")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                                                .createAnonymousLocation(new MyCustomLocation(mPickedCoordinates));
                                        call.enqueue(new Callback<ResponseBody>() {
                                            @Override
                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                try {

                                                    if (response.code() == 201) {
                                                        JSONObject data = new JSONObject(response.body().string());
                                                        final String code = data.getJSONObject("data").getString("code");
                                                        final String message = data.getJSONObject("data").getString("message");

                                                        startActivity(new Intent(getApplicationContext(), SummaryActivity.class)
                                                                .putExtra("code", code));
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
                                });
                        builder.setCancelable(true);
                        builder.create().show();
                    } else {
                        mPickedCoordinates.setMessage(message);
                        Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                                .createAnonymousLocation(new MyCustomLocation(mPickedCoordinates));
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                try {

                                    if (response.code() == 201) {
                                        JSONObject data = new JSONObject(response.body().string());
                                        final String code = data.getJSONObject("data").getString("code");
                                        final String message = data.getJSONObject("data").getString("message");

                                        startActivity(new Intent(getApplicationContext(), SummaryActivity.class)
                                                .putExtra("code", code));
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
                    break;
                default:
                    break;
            }
        }
    };

    // finals
    private final String TAG = "tag-for-marker";
    private final int REQUEST_CHECK_SETTINGS = 107;

    private GoogleMap mMap;
    private Button mSaveBtn;
    private EditText mMessageEdt;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback mLocationCallback;
    private final LocationRequest mLocationRequest = new LocationRequest();

    private Coordinates mPickedCoordinates;

    private Activity that;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        that = this;

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                Location location = locationResult.getLastLocation();

                setInitialMarker(location);

                mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            }
        };

        initViews();
    }

    private void setInitialMarker(Location location) {

        if (location == null) {
            mPickedCoordinates = new Coordinates("-34", "151", "");

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions()
                    .position(sydney)
                    .title("Marker in Sydney")
                    .draggable(true)).setTag(TAG);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        } else {
            mPickedCoordinates = new Coordinates(location.getLatitude() + "",
                    location.getLongitude() + "", "");

            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .title("You are here")
                    .draggable(true)).setTag(TAG);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15.0f));
        }
    }

    private void initViews() {
        mSaveBtn = (Button) findViewById(R.id.save_btn);
        mSaveBtn.setOnClickListener(mClickListener);

        mMessageEdt = (EditText) findViewById(R.id.message_edt_id);
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

        mMap.setOnMarkerDragListener(mMarkerDragListener);

        createLocationRequest();
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
                        resolvable.startResolutionForResult(MapsActivity.this,
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
                mPickedCoordinates = new Coordinates("-34", "151", "");

                // Add a marker in Sydney and move the camera
                LatLng sydney = new LatLng(-34, 151);
                mMap.addMarker(new MarkerOptions()
                        .position(sydney)
                        .title("Marker in Sydney")
                        .draggable(true)).setTag(TAG);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            }
        }
    }

    @Override
    protected void onDestroy() {
        that = null;

        super.onDestroy();
    }
}
