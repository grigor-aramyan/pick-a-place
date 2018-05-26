package com.mycompany.john.pickaplace.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.models.LocationCode;
import com.mycompany.john.pickaplace.models.User;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;
import com.mycompany.john.pickaplace.utils.PhoenixChannels;
import com.mycompany.john.pickaplace.utils.Statics;

import org.json.JSONException;
import org.json.JSONObject;
import org.phoenixframework.channels.Envelope;
import org.phoenixframework.channels.IErrorCallback;
import org.phoenixframework.channels.IMessageCallback;
import org.phoenixframework.channels.ISocketOpenCallback;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.history_img_id:
                    gotoHistoryPage();
                    break;
                case R.id.live_broadcasting_btn_id:
                    prepareLiveBroadcasting();
                    break;
                case R.id.live_tracking_btn_id:
                    prepareLiveTracking();
                    break;
                case R.id.logout_link:
                    logoutUser();
                    break;
                case R.id.login_link:
                    prepareLoginDialog();
                    break;
                case R.id.register_link:
                    startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    break;
                case R.id.pick_btn_id:
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    break;
                case R.id.code_btn_id:
                    prepareEnterCodeDialog();
                    break;
                default:
                    break;
            }
        }
    };

    // UI components
    private TextView mLoginTxt, mRegisterTxt, mLogoutTxt;
    private Button mPickAPlaceBtn, mEnterCodeBtn,
        mLiveBroadcastingBtn, mLiveTrackingBtn;
    private ConstraintLayout mMainConstraintLayout;
    private LinearLayout mTopLayout;
    private ImageView mLogoIcon, mAppNameStyledImg, mHistoryImg;
    private ProgressBar mProgressBar;
    private AdView mBottomAdView;

    // data
    private static boolean sChannelJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectSocket();

        MobileAds.initialize(this, Statics.ADMOB_APP_ID);

        initViews();

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT &&
                !sChannelJoined) {
            getSupportActionBar().hide();

            initAnimation();
        } else {
            if (!sChannelJoined) {
                joinChannel();
            }

            mAppNameStyledImg.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);

            mTopLayout.setVisibility(View.VISIBLE);
            mPickAPlaceBtn.setVisibility(View.VISIBLE);
            mLiveTrackingBtn.setVisibility(View.VISIBLE);
            mEnterCodeBtn.setVisibility(View.VISIBLE);
            mLiveBroadcastingBtn.setVisibility(View.VISIBLE);
        }

        fetchAds();
    }

    private void fetchAds() {
        if (null != mBottomAdView) {
            AdRequest request = new AdRequest.Builder().build();
            mBottomAdView.loadAd(request);

            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    if (null != mBottomAdView) {
                        mBottomAdView.setVisibility(View.VISIBLE);
                    }
                }
            }.start();
        }
    }

    private void gotoHistoryPage() {
        startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
    }

    private void initAnimation() {
        Animation scaleAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.scale_animation);
        scaleAnimation.setStartOffset(5000);
        mLogoIcon.startAnimation(scaleAnimation);

        new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                if (millisUntilFinished <= 2000) {
                    if (!sChannelJoined) {
                        joinChannel();
                    }
                }
            }

            @Override
            public void onFinish() {
                getSupportActionBar().show();
                mAppNameStyledImg.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
                implodeComponents();
            }
        }.start();
    }

    private void implodeComponents() {
        TranslateAnimation translateAnimation = new TranslateAnimation(-300, 0,
                0, 0);
        translateAnimation.setDuration(2000);
        translateAnimation.setInterpolator(new AccelerateInterpolator());
        translateAnimation.setStartOffset(500);
        mTopLayout.startAnimation(translateAnimation);

        TranslateAnimation translateAnimation1 = new TranslateAnimation(0, 0,
                300, 0);
        translateAnimation1.setDuration(2000);
        translateAnimation1.setInterpolator(new AccelerateInterpolator());
        translateAnimation1.setStartOffset(500);
        mPickAPlaceBtn.startAnimation(translateAnimation1);

        TranslateAnimation translateAnimation2 = new TranslateAnimation(0, 0,
                -300, 0);
        translateAnimation2.setDuration(2000);
        translateAnimation2.setInterpolator(new AccelerateInterpolator());
        translateAnimation2.setStartOffset(500);
        mLiveTrackingBtn.startAnimation(translateAnimation2);

        TranslateAnimation translateAnimation3 = new TranslateAnimation(300, 0,
                0, 0);
        translateAnimation3.setDuration(2000);
        translateAnimation3.setInterpolator(new AccelerateInterpolator());
        translateAnimation3.setStartOffset(500);
        mEnterCodeBtn.startAnimation(translateAnimation3);

        TranslateAnimation translateAnimation4 = new TranslateAnimation(-300, 0,
                0, 0);
        translateAnimation4.setDuration(2000);
        translateAnimation4.setInterpolator(new AccelerateInterpolator());
        translateAnimation4.setStartOffset(500);
        mLiveBroadcastingBtn.startAnimation(translateAnimation4);

        mTopLayout.setVisibility(View.VISIBLE);
        mPickAPlaceBtn.setVisibility(View.VISIBLE);
        mLiveTrackingBtn.setVisibility(View.VISIBLE);
        mEnterCodeBtn.setVisibility(View.VISIBLE);
        mLiveBroadcastingBtn.setVisibility(View.VISIBLE);
    }

    private void connectSocket() {
        PhoenixChannels.getSocket(getApplicationContext())
                .onOpen(new ISocketOpenCallback() {
                    @Override
                    public void onOpen() {
                        Log.e("mmm", "socket opened");
                    }
                })
                .onError(new IErrorCallback() {
                    @Override
                    public void onError(String reason) {
                        Toast.makeText(getApplicationContext(), "Problems occured! Live position broadcasting " +
                                        "and Live tracking won't be available. We are trying hard to fix issues",
                                Toast.LENGTH_LONG).show();
                        Log.e("mmm", "socket connection error: " + reason);
                    }
                });
        try {
            PhoenixChannels.getSocket(getApplicationContext()).connect();
        } catch (IOException ioExp) {
            Toast.makeText(getApplicationContext(), "Problems occured! Live position broadcasting " +
                    "and Live tracking won't be available. We are trying hard to fix issues",
                    Toast.LENGTH_LONG).show();
            Log.e("mmm", "socket connection exception: " + ioExp.getLocalizedMessage());
        }
    }

    private void joinChannel() {
        try {
            PhoenixChannels.getChannel()
                    .join()
                    .receive("ok", new IMessageCallback() {
                        @Override
                        public void onMessage(Envelope envelope) {
                            sChannelJoined = true;
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

    private void prepareLiveTracking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_enter_shared_code_dialog, null);
        final EditText codeEdt = (EditText) view.findViewById(R.id.code_edt_id);

        builder.setTitle("Enter the Code")
                .setView(view)
                .setMessage("Shared code for Live position, plz))")
                .setPositiveButton("Check", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredCode = codeEdt.getText().toString();
                        if (enteredCode.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Enter some code " +
                                    "for me to check :)", Toast.LENGTH_LONG).show();
                        } else {
                            Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                                    .getLiveLocationByCode(new LocationCode(enteredCode));
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        try {
                                            JSONObject initialData = new JSONObject(response.body().string());
                                            if (initialData.has("errors")) {
                                                Toast.makeText(getApplicationContext(),
                                                        initialData.getJSONObject("errors").getString("detail"),
                                                        Toast.LENGTH_LONG).show();
                                            } else if (initialData.has("data")) {
                                                JSONObject data = initialData.getJSONObject("data");
                                                final int locationId = data.getInt("id");
                                                final String code = data.getString("code");
                                                final String longitude = data.getString("longitude");
                                                final String latitude = data.getString("latitude");
                                                final String message = data.getString("message");

                                                if (PhoenixChannels.getSocket(getApplicationContext()) != null &&
                                                        PhoenixChannels.getSocket(getApplicationContext()).isConnected()) {

                                                    startActivity(new Intent(getApplicationContext(), ShowLiveLocationMapsActivity.class)
                                                            .putExtra(Statics.LOCATION_LATITUDE, latitude)
                                                            .putExtra(Statics.LOCATION_LONGITUDE, longitude)
                                                            .putExtra(Statics.LOCATION_MESSAGE, message)
                                                            .putExtra(Statics.LOCATION_CODE, code));

                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Can't fetch live stream " +
                                                            "now. Try later, plz ))", Toast.LENGTH_LONG).show();
                                                }

                                            } else {
                                                Toast.makeText(getApplicationContext(), "Something unexpected " +
                                                        "happened. Try later, plz))", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (IOException ioExp) {
                                            Toast.makeText(getApplicationContext(), "Something wrong with app! " +
                                                    "Try to restart it, plz", Toast.LENGTH_LONG).show();
                                        } catch (JSONException jExp) {
                                            Toast.makeText(getApplicationContext(), "Something wrong with our " +
                                                    "servers. Try later, plz!! We are trying hard to fix any issue " +
                                                    "that occurs..", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error getting location! " +
                                                "Check your code/try later, plz. We are trying hard to " +
                                                "fix any issue that may occur!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Error: " +
                                            t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(true);
        builder.create().show();
    }

    private void prepareLiveBroadcasting() {
        if (PhoenixChannels.getSocket(getApplicationContext()) != null &&
                PhoenixChannels.getSocket(getApplicationContext()).isConnected()) {
            startActivity(new Intent(getApplicationContext(), BroadcastLiveLocationMapsActivity.class));
        } else {
            Toast.makeText(getApplicationContext(), "No persistent connection with servers! " +
                    "Try to restart the app or try later, plz ))", Toast.LENGTH_LONG).show();
        }
    }

    private void logoutUser() {
        Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                .signOutUser();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject initialData = new JSONObject(response.body().string());
                        JSONObject data = initialData.getJSONObject("data");

                        if (data.getString("msg").equals("Signed out")) {
                            SharedPreferences sharedPreferences = getSharedPreferences(Statics.SHARED_PREF_FOR_APP, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.remove(Statics.CURRENT_USER_ID);
                            editor.remove(Statics.CURRENT_USER_EMAIL);
                            editor.commit();

                            mHistoryImg.setVisibility(View.GONE);
                            mLogoutTxt.setVisibility(View.GONE);
                            mLoginTxt.setVisibility(View.VISIBLE);
                            mRegisterTxt.setVisibility(View.VISIBLE);

                            Toast.makeText(getApplicationContext(), "Signed out!\n" +
                                data.getString("email"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Couldn't sign you out! " +
                                    "We're trying hard to fix issues)) Try to restart the app " +
                                    "or try a bit later, plz", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException ioExp) {
                        Toast.makeText(getApplicationContext(), "Something wrong happened! " +
                                "Try to restart the app, plz))", Toast.LENGTH_LONG).show();
                    } catch (JSONException jExp) {
                        Toast.makeText(getApplicationContext(), "Something wrong with our servers! " +
                                "Try later, plz))", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Error signing out! " +
                            "Try to restart the app, plz))", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error: " +
                        t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void prepareLoginDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_login_dialog, null);
        final EditText emailEdt = (EditText) view.findViewById(R.id.email_edt_id);
        final EditText passwordEdt = (EditText) view.findViewById(R.id.password_edt_id);

        builder.setTitle("Login")
                .setView(view)
                .setMessage("Email/Password, please ))")
                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredEmail = emailEdt.getText().toString();
                        String enteredPassword = passwordEdt.getText().toString();
                        if (enteredEmail.isEmpty() || enteredPassword.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Both fields are required!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                                    .signInUser(new User(enteredEmail, enteredPassword));
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    try {

                                        if (response.code() == 200) {
                                            JSONObject data = new JSONObject(response.body().string())
                                                    .getJSONObject("data")
                                                    .getJSONObject("user");
                                            final String id = data.getString("id");
                                            final String email = data.getString("email");

                                            SharedPreferences sharedPreferences = getSharedPreferences(Statics.SHARED_PREF_FOR_APP,
                                                    MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString(Statics.CURRENT_USER_ID, id);
                                            editor.putString(Statics.CURRENT_USER_EMAIL, email);
                                            editor.commit();

                                            mLoginTxt.setVisibility(View.GONE);
                                            mRegisterTxt.setVisibility(View.GONE);
                                            mLogoutTxt.setVisibility(View.VISIBLE);
                                            mHistoryImg.setVisibility(View.VISIBLE);

                                            Toast.makeText(getApplicationContext(), "Signed in!",
                                                    Toast.LENGTH_LONG).show();

                                        } else {
                                            Toast.makeText(getApplicationContext(), "Error " +
                                                    "signing in! Check your email/password, plz))",
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
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(true);
        builder.create().show();
    }

    private void prepareEnterCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_enter_shared_code_dialog, null);
        final EditText codeEdt = (EditText) view.findViewById(R.id.code_edt_id);

        builder.setTitle("Enter the Code")
                .setView(view)
                .setMessage("Shared code for your event, plz))")
                .setPositiveButton("Check", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredCode = codeEdt.getText().toString();
                        if (enteredCode.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Enter some code " +
                            "for me to check :)", Toast.LENGTH_LONG).show();
                        } else {
                            Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                                    .getLocationByCode(new LocationCode(enteredCode));
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.code() == 200) {
                                        try {
                                            JSONObject initialData = new JSONObject(response.body().string());
                                            if (initialData.has("errors")) {
                                                Toast.makeText(getApplicationContext(),
                                                        initialData.getJSONObject("errors").getString("detail"),
                                                        Toast.LENGTH_LONG).show();
                                            } else if (initialData.has("data")) {
                                                JSONObject data = initialData.getJSONObject("data");
                                                final int locationId = data.getInt("id");
                                                final String code = data.getString("code");
                                                final String longitude = data.getString("longitude");
                                                final String latitude = data.getString("latitude");
                                                final String message = data.getString("message");

                                                startActivity(new Intent(getApplicationContext(), ShowLocationMapsActivity.class)
                                                    .putExtra(Statics.LOCATION_LATITUDE, latitude)
                                                    .putExtra(Statics.LOCATION_LONGITUDE, longitude)
                                                    .putExtra(Statics.LOCATION_MESSAGE, message));

                                            } else {
                                                Toast.makeText(getApplicationContext(), "Something unexpected " +
                                                        "happened. Try later, plz))", Toast.LENGTH_LONG).show();
                                            }
                                        } catch (IOException ioExp) {
                                            Toast.makeText(getApplicationContext(), "Something wrong with app! " +
                                                    "Try to restart it, plz", Toast.LENGTH_LONG).show();
                                        } catch (JSONException jExp) {
                                            Toast.makeText(getApplicationContext(), "Something wrong with our " +
                                                    "servers. Try later, plz!! We are trying hard to fix any issue " +
                                                    "that occurs..", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Error getting location! " +
                                                "Check your code/try later, plz. We are trying hard to " +
                                                "fix any issue that may occur!", Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(getApplicationContext(), "Error: " +
                                            t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setCancelable(true);
        builder.create().show();
    }

    private void initViews() {
        if (null != findViewById(R.id.bottom_adview_id)) {
            mBottomAdView = (AdView) findViewById(R.id.bottom_adview_id);
            mBottomAdView.setVisibility(View.GONE);
        }
        mHistoryImg = (ImageView) findViewById(R.id.history_img_id);
        mHistoryImg.setOnClickListener(mClickListener);
        mAppNameStyledImg = (ImageView) findViewById(R.id.app_name_img_id);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar_id);
        mLogoIcon = (ImageView) findViewById(R.id.geotag_icon_id);
        mTopLayout = (LinearLayout) findViewById(R.id.crapy_id);
        mTopLayout.setVisibility(View.GONE);
        mMainConstraintLayout = (ConstraintLayout) findViewById(R.id.main_container_id);
        mLogoutTxt = (TextView) findViewById(R.id.logout_link);
        mLogoutTxt.setOnClickListener(mClickListener);
        mLoginTxt = (TextView) findViewById(R.id.login_link);
        mLoginTxt.setOnClickListener(mClickListener);
        mRegisterTxt = (TextView) findViewById(R.id.register_link);
        mRegisterTxt.setOnClickListener(mClickListener);
        mPickAPlaceBtn = (Button) findViewById(R.id.pick_btn_id);
        mPickAPlaceBtn.setOnClickListener(mClickListener);
        mPickAPlaceBtn.setVisibility(View.INVISIBLE);
        mEnterCodeBtn = (Button) findViewById(R.id.code_btn_id);
        mEnterCodeBtn.setOnClickListener(mClickListener);
        mEnterCodeBtn.setVisibility(View.INVISIBLE);
        mLiveBroadcastingBtn = (Button) findViewById(R.id.live_broadcasting_btn_id);
        mLiveBroadcastingBtn.setOnClickListener(mClickListener);
        mLiveBroadcastingBtn.setVisibility(View.GONE);
        mLiveTrackingBtn = (Button) findViewById(R.id.live_tracking_btn_id);
        mLiveTrackingBtn.setOnClickListener(mClickListener);
        mLiveTrackingBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getSharedPreferences(Statics.SHARED_PREF_FOR_APP, MODE_PRIVATE);
        if (!sharedPreferences.getString(Statics.CURRENT_USER_ID, "").isEmpty()) {
            mLoginTxt.setVisibility(View.GONE);
            mRegisterTxt.setVisibility(View.GONE);
            mLogoutTxt.setVisibility(View.VISIBLE);
            mHistoryImg.setVisibility(View.VISIBLE);
        }
    }
}
