package com.scibots.smartattendance;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.scibots.smartattendance.helper.AudioTrainHelper;

public class AudioMatch extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_match);

        findViewById(R.id.train_audio).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioTrainHelper audioTrainHelper = new AudioTrainHelper(AudioMatch.this);
                audioTrainHelper.train();
            }
        });
    }
}
