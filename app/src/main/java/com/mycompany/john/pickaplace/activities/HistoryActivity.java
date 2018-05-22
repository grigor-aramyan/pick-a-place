package com.mycompany.john.pickaplace.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.utils.Statics;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPreferences = getSharedPreferences(Statics.SHARED_PREF_FOR_APP, MODE_PRIVATE);
        final String id = sharedPreferences.getString(Statics.CURRENT_USER_ID, "");
        final String email = sharedPreferences.getString(Statics.CURRENT_USER_EMAIL, "");
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
