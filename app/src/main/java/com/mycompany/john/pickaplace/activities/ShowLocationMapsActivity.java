package com.mycompany.john.pickaplace.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.utils.Statics;

public class ShowLocationMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    // ui components
    private EditText mMessageEdt;

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
            }
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

        if (mLongitude != null && mLatitude != null) {
            // Add a marker in Sydney and move the camera
            LatLng customLocation = new LatLng(Double.parseDouble(mLatitude), Double.parseDouble(mLongitude));
            mMap.addMarker(new MarkerOptions().position(customLocation).title("Picked Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customLocation, 15.0f));
        } else {
            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
    }
}
