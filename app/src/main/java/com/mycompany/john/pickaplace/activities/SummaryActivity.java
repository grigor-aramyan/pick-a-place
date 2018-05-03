package com.mycompany.john.pickaplace.activities;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.john.pickaplace.R;
import com.robertsimoes.shareable.Shareable;

public class SummaryActivity extends AppCompatActivity {

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fb_icon:
                    Shareable shareAction = new Shareable.Builder(that)
                            .message("Pick a Place code: " + mCode)
                            .socialChannel(Shareable.Builder.FACEBOOK)
                            .build();
                    shareAction.share();
                    break;
                case R.id.twitter_icon:
                    Shareable shareAction1 = new Shareable.Builder(that)
                            .message("Pick a Place code: " + mCode)
                            .socialChannel(Shareable.Builder.TWITTER)
                            .build();
                    shareAction1.share();
                    break;
                case R.id.gp_icon:
                    Shareable shareAction2 = new Shareable.Builder(that)
                            .message("Pick a Place code: " + mCode)
                            .socialChannel(Shareable.Builder.GOOGLE_PLUS)
                            .build();
                    shareAction2.share();
                    break;
                default:
                    break;
            }
        }
    };

    // UI components
    private ImageView mFBIcon, mTwitterIcon, mGPIcon;
    private TextView mCodeTxt;

    // data
    private String mCode;
    private Activity that;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        that = this;

        initViews();

        if (getIntent().hasExtra("code")) {
            mCode = getIntent().getStringExtra("code");

            mCodeTxt.setText(
                "Code: " + mCode
            );
        } else {
            Toast.makeText(getApplicationContext(), "Sorry! Something very unexpected happened!! " +
                    "We are trying hard to fix all issues. Try to restart the app, plz))",
                    Toast.LENGTH_LONG).show();

            finish();
        }
    }

    private void initViews() {
        mCodeTxt = (TextView) findViewById(R.id.code_txt_id);

        mFBIcon = (ImageView) findViewById(R.id.fb_icon);
        mFBIcon.setOnClickListener(mClickListener);
        mTwitterIcon = (ImageView) findViewById(R.id.twitter_icon);
        mTwitterIcon.setOnClickListener(mClickListener);
        mGPIcon = (ImageView) findViewById(R.id.gp_icon);
        mGPIcon.setOnClickListener(mClickListener);
    }

    @Override
    protected void onDestroy() {
        that = null;

        super.onDestroy();
    }
}
