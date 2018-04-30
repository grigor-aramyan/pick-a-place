package com.mycompany.john.pickaplace.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.models.User;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;

import org.json.JSONException;
import org.json.JSONObject;

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
                case R.id.login_link:
                    prepareLoginDialog();
                    Toast.makeText(getApplicationContext(), "login...", Toast.LENGTH_LONG).show();
                    break;
                case R.id.register_link:
                    startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                    break;
                case R.id.pick_btn_id:
                    Toast.makeText(getApplicationContext(), "pick...", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    break;
                case R.id.code_btn_id:
                    prepareEnterCodeDialog();
                    Toast.makeText(getApplicationContext(), "code...", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    };

    // UI components
    private TextView mLoginTxt, mRegisterTxt;
    private Button mPickAPlaceBtn, mEnterCodeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
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
                            Call<ResponseBody> call = RetrofitInstance.getBackendService()
                                    .singInUser(new User(enteredEmail, enteredPassword));
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

                                            Log.e("mmm", "signed in. id: " + id + "\nmail: " + email);

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
                .setMessage("Shared code for picked MyCustomLocation")
                .setPositiveButton("Check", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredCode = codeEdt.getText().toString();
                        if (enteredCode.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Enter some code " +
                            "for me to check :)", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "checking...", Toast.LENGTH_LONG).show();
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
        mLoginTxt = (TextView) findViewById(R.id.login_link);
        mLoginTxt.setOnClickListener(mClickListener);
        mRegisterTxt = (TextView) findViewById(R.id.register_link);
        mRegisterTxt.setOnClickListener(mClickListener);
        mPickAPlaceBtn = (Button) findViewById(R.id.pick_btn_id);
        mPickAPlaceBtn.setOnClickListener(mClickListener);
        mEnterCodeBtn = (Button) findViewById(R.id.code_btn_id);
        mEnterCodeBtn.setOnClickListener(mClickListener);
    }
}
