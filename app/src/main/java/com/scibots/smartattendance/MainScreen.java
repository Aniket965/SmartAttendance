package com.scibots.smartattendance;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
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
import com.scibots.smartattendance.helper.TrainHelper;
import com.scibots.smartattendance.ui.login.LoginActivity;
import com.scibots.smartattendance.views.CvCameraPreview;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_objdetect;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_PLAIN;
import static org.bytedeco.javacpp.opencv_core.LINE_8;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.putText;
import static org.bytedeco.javacpp.opencv_imgproc.rectangle;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
//import static org.bytedeco.javacv.android.recognize.example.TrainHelper.ACCEPT_LEVEL;

public class MainScreen extends AppCompatActivity implements CvCameraPreview.CvCameraViewListener {
    public Button mark;
    private String TAG = "MAIN_SCREEN";
    private CvCameraPreview cameraView;
    private CascadeClassifier faceDetector;
    private String[] nomes = {"", "You"};
    private int absoluteFaceSize = 0;
    boolean takePhoto;
    opencv_face.FaceRecognizer faceRecognizer = opencv_face.EigenFaceRecognizer.create();
    boolean trained;
    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, 1 );
            }
        }
        cameraView = (CvCameraPreview) findViewById(R.id.camera_view);
        cameraView.setCvCameraViewListener(this);

        AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    faceDetector = TrainHelper.loadClassifierCascade(MainScreen.this, R.raw.frontalface);
//                    if(TrainHelper.isTrained(getBaseContext())) {
//                        File folder = new File(getFilesDir(), TrainHelper.TRAIN_FOLDER);
//                        File f = new File(folder, TrainHelper.EIGEN_FACES_CLASSIFIER);
////                        faceRecognizer.load(f.getAbsolutePath());
//                        faceRecognizer.read(f.getAbsolutePath());
//                        trained = true;
//                    }
                } catch (Exception e) {
                    Log.d(TAG, e.getLocalizedMessage(), e);
                }
                return null;
            }

//            @Override
//            protected void onPostExecute(Void aVoid) {
//                super.onPostExecute(aVoid);
//                findViewById(R.id.btPhoto).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        takePhoto = true;
//                    }
//                });
//                findViewById(R.id.btTrain).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        train();
//                    }
//                });
//                findViewById(R.id.btReset).setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        try {
//                            TrainHelper.reset(getBaseContext());
//                            Toast.makeText(getBaseContext(), "Reseted with sucess.", Toast.LENGTH_SHORT).show();
//                            finish();
//                        }catch (Exception e) {
//                            Log.d(TAG, e.getLocalizedMessage(), e);
//                        }
//                    }
//                });
        };
        asyncTask.execute();


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

    void showDetectedFace(opencv_core.RectVector faces, Mat rgbaMat) {
        int x = faces.get(0).x();
        int y = faces.get(0).y();
        int w = faces.get(0).width();
        int h = faces.get(0).height();

        rectangle(rgbaMat, new opencv_core.Point(x, y), new opencv_core.Point(x + w, y + h), opencv_core.Scalar.GREEN, 2, LINE_8, 0);
    }


    private boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public  Boolean markTodaysAttendance() {
        // TODO:    NIKHIL YHA DAAL
        // return true if authenticated
        // return false if not authenticated or not sensor not available
        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (width * 0.32f);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public opencv_core.Mat onCameraFrame(opencv_core.Mat rgbaMat) {

        if (absoluteFaceSize == 0) {
            int height = rgbaMat.rows();
            float mRelativeFaceSize = 0.2f;
            if (Math.round(height * mRelativeFaceSize) > 0) {
                absoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
        }
        if (faceDetector != null) {
            Mat greyMat = new Mat(rgbaMat.rows(), rgbaMat.cols());
            cvtColor(rgbaMat, greyMat, CV_BGR2GRAY);
            opencv_core.RectVector faces = new opencv_core.RectVector();

            faceDetector.detectMultiScale(greyMat, faces, 1.1f, 2, 2,
                  new opencv_core.Size(absoluteFaceSize, absoluteFaceSize), new opencv_core.Size());

            if (faces.size() == 1) {
                showDetectedFace(faces, rgbaMat);
//                if(takePhoto) {
//                    capturePhoto(rgbaMat);
//                    alertRemainingPhotos();
//                }
//                if(trained) {
//                    recognize(faces.get(0), greyMat, rgbaMat);
//                }else{
//                    noTrainedLabel(faces.get(0), rgbaMat);
//                }
            }
            greyMat.release();
        }
        return rgbaMat;
    }
}
