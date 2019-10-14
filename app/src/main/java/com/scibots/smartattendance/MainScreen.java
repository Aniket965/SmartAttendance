package com.scibots.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.scibots.smartattendance.ui.login.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class MainScreen extends AppCompatActivity {
    public Button mark;
    private String TAG = "MAIN_SCREEN";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        final RequestQueue queue = Volley.newRequestQueue(this);

        Log.d(TAG,AuthHelper.getInstance(this).getIdToken());
        Toast.makeText(MainScreen.this,"Welcome, " + AuthHelper.getInstance(this).getUserName() + " !",Toast.LENGTH_SHORT).show();
        mark = (Button) findViewById(R.id.attedance);


        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markTodaysAttendance()) {
                    String url ="http://115.111.246.28:5000/attendancesystem/mark";
                    final JSONObject jsonBody = new JSONObject();
                    try {
                        // TODO: take roomkey from BLE beacons @aniket965
                        jsonBody.put("room_key", "123456");

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
                                        Log.d(TAG,jsonObject.toString());

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {


                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("Content-Type", "application/json; charset=UTF-8");
                            String creds = AuthHelper.getInstance(MainScreen.this).getIdToken() + ":unused";
                            String auth = "Basic " + Base64.encodeToString( creds.getBytes(), Base64.NO_WRAP);
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
                Toast.makeText(MainScreen.this,"Marking Your Attendance",Toast.LENGTH_SHORT).show();

            }
        });

    }

    public  Boolean markTodaysAttendance() {
        // TODO:    NIKHIL YHA DAAL
        // return true if authenticated
        // return false if not authenticated or not sensor not available
        return true;
    }
}
