package com.mycompany.john.pickaplace.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mycompany.john.pickaplace.R;
import com.mycompany.john.pickaplace.models.User;
import com.mycompany.john.pickaplace.models.UserWrapper;
import com.mycompany.john.pickaplace.retrofit.RetrofitInstance;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.register_btn_id:
                    final String email = mEmailEdt.getText().toString();
                    final String password = mPasswordEdt.getText().toString();
                    final String confirmPassword = mConfirmPasswordEdt.getText().toString();

                    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "All fields are " +
                                "required!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (!(Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9]{3,}\\.[a-zA-Z]{2,}").matcher(email).matches())) {
                        Toast.makeText(getApplicationContext(), "Email format seems wrong! Check, " +
                                "please ))", Toast.LENGTH_LONG).show();
                        mEmailEdt.setText("");
                        return;
                    }

                    if (password.length() < 8) {
                        Toast.makeText(getApplicationContext(), "Passwords at least 8 chare long",
                                Toast.LENGTH_LONG).show();
                        mPasswordEdt.setText("");
                        return;
                    }

                    if (!(Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$")
                            .matcher(password).matches())) {

                        Toast.makeText(getApplicationContext(), "Passwords should contain " +
                                "lowercase, uppercase letters, numbers, at least one of this chars (" +
                                "@#$%^&+=)", Toast.LENGTH_LONG).show();
                        mPasswordEdt.setText("");
                        return;
                    }

                    if (!password.equals(confirmPassword)) {
                        Toast.makeText(getApplicationContext(), "Password doesn't match " +
                                "confirm password field", Toast.LENGTH_LONG).show();
                        mConfirmPasswordEdt.setText("");
                        return;
                    }

                    Call<ResponseBody> call = RetrofitInstance.getBackendService()
                            .registerUser(new UserWrapper(new User(email, password)));
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {

                                if (response.code() == 201) {
                                    JSONObject data = new JSONObject(response.body().string());
                                    final String id = data.getJSONObject("data").getString("id");
                                    final String email = data.getJSONObject("data").getString("email");

                                    Log.e("mmm", "reged: id: " + id + "\nmail: " + email);
                                    finish();
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
                    break;
                default:
                    break;
            }
        }
    };

    // UI components
    private EditText mEmailEdt, mPasswordEdt, mConfirmPasswordEdt;
    private Button mRegisterBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initViews();
    }

    private void initViews() {
        mEmailEdt = (EditText) findViewById(R.id.email_edt_id);
        mPasswordEdt = (EditText) findViewById(R.id.password_edt_id);
        mConfirmPasswordEdt = (EditText) findViewById(R.id.confirm_password_edt_id);
        mRegisterBtn = (Button) findViewById(R.id.register_btn_id);
        mRegisterBtn.setOnClickListener(mClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
