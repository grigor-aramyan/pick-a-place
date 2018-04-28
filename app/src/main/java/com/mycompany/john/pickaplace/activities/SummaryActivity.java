package com.mycompany.john.pickaplace.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.mycompany.john.pickaplace.R;

public class SummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        if (getIntent().hasExtra("code")) {
            ((TextView) findViewById(R.id.code_txt_id)).setText(
                    getIntent().getStringExtra("code")
            );
        }
    }
}
