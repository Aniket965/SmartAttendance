package com.scibots.smartattendance.helper;

import java.util.concurrent.locks.ReentrantLock;

public class AudioTrainHelper {
    public static final String TAG = "AudioTrainHelper";
    public static final String MODEL_FILEPATH = "file:///android_asset/tf_model.pb";
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 500;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final String INPUT_DATA_NAME_FIRST_AUDIO = "input_1:0";
    private static final String INPUT_DATA_NAME_SECOND_AUDIO = "input_2:0";
    private static final String OUTPUT_SCORES_NAME_NEWMODEL = "dense_2/Sigmoid:0";
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
//    private TensorFlowInferenceInterface inferenceInterface;
}
