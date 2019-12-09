package com.scibots.smartattendance.helper;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
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
    private TensorFlowInferenceInterface inferenceInterface;
    private AudioRecord record;

    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    public Context context;
    public Boolean istrained = false;


    public AudioTrainHelper(Context context) {
        this.context = context;
    }

    public void start() {

    }

    public void train() {
        startRecording(true);
//        startTraining();
        startRecognition();

    }

    private void startTraining() {
        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[]floatInputBuffer = new float[RECORDING_LENGTH];

        recordingBufferLock.lock();
        try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
        } finally {
                recordingBufferLock.unlock();
        }


        for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] =  inputBuffer[i] / 32767.0f;
        }
        Log.d("AUDIOTRAINHELPER",inputBuffer.toString());
    }

    public synchronized void startRecording(Boolean istraining) {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {

                            @Override
                            public void run() {
                                record(istraining);
                            }
                        });
        recordingThread.start();
        recordingThread = null;
    }

    private void record(Boolean istraining) {
        Log.d(TAG,"Recording");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];
        Log.d(TAG,"buffer size ->" + bufferSize);
        record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "Audio Record can't initialize!");
            return;
        }


        record.startRecording();

        Log.v(TAG, "Start recording");


        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);


            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;


            } catch (Exception e) {
                shouldContinue = false;
            } finally {
                recordingBufferLock.unlock();

            }
        }

        try {
            record.stop();
            record.release();
        } catch (Exception e) {
            Log.d(TAG,"exception");
        }


    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    recognize();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
        recognitionThread.start();
    }
    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;

    }

    private  int argmax(float[] outputScores ) {
        int maxi = 0;
        float max = Integer.MIN_VALUE;

        for (int i = 0;i<outputScores.length;i++) {
            if(outputScores[i]> max) {
                maxi = i;
                max = outputScores[i];
            }
        }
        return maxi;
    }

    private void recognize() throws IOException {
        Log.v(TAG, "Start recognition");

        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[]floatInputBuffer = new float[RECORDING_LENGTH];
        float[]floatInputBuffer_prev = new float[RECORDING_LENGTH];
        float[]floatInputBuffer_prev2 = new float[RECORDING_LENGTH];
        float[] floatInputBuffer_newmodel = new float[RECORDING_LENGTH * 3];


        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            long startTime = new Date().getTime();
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }


            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] =  inputBuffer[i] / 32767.0f;
            }

            Log.d("AUDIOTRAINHELPER", Arrays.toString(inputBuffer));


        }


        try {
            // We don't need to run too frequently, so snooze for a bit.
            Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
        } catch (InterruptedException e) {
            // Ignore
        }

    }

}
