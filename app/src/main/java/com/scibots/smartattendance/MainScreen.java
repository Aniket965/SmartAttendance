package com.scibots.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.scibots.smartattendance.ui.login.LoginActivity;

public class MainScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.d("MAINSCREEN",AuthHelper.getInstance(this).getIdToken());
        Toast.makeText(MainScreen.this,"Welcome, " + AuthHelper.getInstance(this).getUserName() + " !",Toast.LENGTH_SHORT).show();
    }
}
