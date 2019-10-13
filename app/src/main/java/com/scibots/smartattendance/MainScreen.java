package com.scibots.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.scibots.smartattendance.ui.login.LoginActivity;

public class MainScreen extends AppCompatActivity {
    public Button mark;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Log.d("MAINSCREEN",AuthHelper.getInstance(this).getIdToken());
        Toast.makeText(MainScreen.this,"Welcome, " + AuthHelper.getInstance(this).getUserName() + " !",Toast.LENGTH_SHORT).show();
        mark = (Button) findViewById(R.id.attedance);
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markTodaysAttendance();
                Toast.makeText(MainScreen.this,"Marking Your Attendance",Toast.LENGTH_SHORT).show();

            }
        });

    }

    public  Boolean markTodaysAttendance() {
        // TODO: NIKHIL YHA DAAL
        // return true if authenticated
        // return false if not authenticated or not sensor not available
        return false;
    }
}
