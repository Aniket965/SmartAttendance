package com.scibots.smartattendance.ui.login;

import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scibots.smartattendance.AuthHelper;
import com.scibots.smartattendance.MainScreen;
import com.scibots.smartattendance.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(AuthHelper.getInstance(LoginActivity.this).getIdToken() !=null ) {
            Intent intent = new Intent(LoginActivity.this, MainScreen.class);
            startActivity(intent);
            finish();
        }


        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);
        final RequestQueue queue = Volley.newRequestQueue(this);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                String url ="http://115.111.246.28:5000/token";
                final JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("role", "Admin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final String requestBody = jsonBody.toString();
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response.toString());
                                 Toast.makeText(LoginActivity.this,"Welcome, " + jsonObject.getString("name") + " !",Toast.LENGTH_SHORT).show();
                                    AuthHelper.getInstance(LoginActivity.this).setIdToken(jsonObject.getString("token"));
                                    AuthHelper.getInstance(LoginActivity.this).setUserName(jsonObject.getString("name"));
                                    Intent intent = new Intent(LoginActivity.this, MainScreen.class);
                                    startActivity(intent);
                                    finish();

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                Log.d("LOGIN_ACTIVITY",response.toString());
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("LOGIN_ACTIVITY",error.toString());
                        Toast.makeText(LoginActivity.this,"Login failed !",Toast.LENGTH_SHORT).show();
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json; charset=UTF-8");
                        String creds = String.format("%s:%s",usernameEditText.getText().toString(),passwordEditText.getText().toString());
                        String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                        params.put("Authorization", auth);
                        return params;
                    }

                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        try {
                            return requestBody == null ? null : requestBody.getBytes("utf-8");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };

                queue.add(stringRequest);



            }
        });
    }



    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
