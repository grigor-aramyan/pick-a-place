package com.mycompany.john.pickaplace.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.adapters.LocationHistoryAdapter;
import com.mycompany.john.pickaplace.models.UserId;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;
import com.mycompany.john.pickaplace.utils.Statics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {
    // ui components
    private RecyclerView mLocationsList;
    private TextView mEmptyListTxt, mEmailTxt;
    private LinearLayout mTopInfoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();

        SharedPreferences sharedPreferences = getSharedPreferences(Statics.SHARED_PREF_FOR_APP, MODE_PRIVATE);
        final String id = sharedPreferences.getString(Statics.CURRENT_USER_ID, "");
        final String email = sharedPreferences.getString(Statics.CURRENT_USER_EMAIL, "");

        mEmailTxt.setText(email);

        fetchLocations(id);
    }

    private void fetchLocations(String id) {
        Call<ResponseBody> call = RetrofitInstance.getBackendService(getApplicationContext())
                .getLocationsByUserId(new UserId(Integer.parseInt(id)));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response != null && response.code() == 200) {
                    try {
                        JSONObject initialData = new JSONObject(response.body().string());
                        JSONArray data = initialData.getJSONArray("data");

                        if (data.length() > 0) {
                            ArrayList<Map<String, String>> dataset = new ArrayList<>();

                            JSONObject location = null;
                            Map<String, String> tmp = null;
                            for (int i = 0; i < data.length(); i++) {
                                location = data.getJSONObject(i);
                                tmp = new HashMap<>();

                                tmp.put("code", location.getString("code"));
                                tmp.put("msg", location.getString("message"));
                                tmp.put("live", location.getBoolean("live") + "");

                                dataset.add(tmp);
                            }

                            mLocationsList.setAdapter(new LocationHistoryAdapter(dataset));
                            mLocationsList.setVisibility(View.VISIBLE);
                            mTopInfoLayout.setVisibility(View.VISIBLE);
                            mEmptyListTxt.setVisibility(View.GONE);
                        } else {
                            mLocationsList.setVisibility(View.GONE);
                            mTopInfoLayout.setVisibility(View.GONE);
                            mEmptyListTxt.setVisibility(View.VISIBLE);
                        }

                    } catch (IOException ioExp) {
                        Toast.makeText(getApplicationContext(), "Something wrong with app! " +
                                "Try to restart it, plz", Toast.LENGTH_LONG).show();
                    } catch (JSONException jExp) {
                        Toast.makeText(getApplicationContext(), "Something went wrong on " +
                                "our servers! We are trying hard to solve all problems. Tune in " +
                                "later, plz", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Sorry! Something bad happened (( and " +
                        "we couldn't fetch your data. Try later, plz!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initViews() {
        mTopInfoLayout = (LinearLayout) findViewById(R.id.top_info_layout_id);
        mEmailTxt = (TextView) findViewById(R.id.email_txt_id);
        mEmptyListTxt = (TextView) findViewById(R.id.empty_list_txt_id);
        mLocationsList = (RecyclerView) findViewById(R.id.locations_list_id);
        mLocationsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
