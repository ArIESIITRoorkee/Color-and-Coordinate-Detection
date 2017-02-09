package com.example.gourav.quadcopto;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import android.view.SurfaceView;
import android.widget.Button;

import java.util.List;

import static org.opencv.imgproc.Imgproc.contourMoments;


public class Quadcopter extends AppCompatActivity implements CvCameraViewListener2 {

    private static final String  TAG  = " Quadcopter " ;

    public int a = 0 ;
    private Handler h ;
    private Mat  mRgba;
    private Scalar   mBlobColorHsv;
    private Color_detector       mDetector;
    private Scalar   CONTOUR_COLOR;
    double x,y;
    public int mCameraViewWidth,mCameraViewHeight ;
    public String LeftnRight = "Initializing..." ;
    public String UpnDown = "Initializing..." ;
    TextView textView2, textView1 ;
    private CameraBridgeViewBase mOpenCvCameraView;


    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.copter_layout);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            Log.d(TAG, "Everything should be fine with using the camera.");
        } else {
            Log.d(TAG, "Requesting permission to use the camera.");
            String[] CAMERA_PERMISSONS = {
                    Manifest.permission.CAMERA
            };
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSONS, 0);
        }

        final Button red =(Button) findViewById(R.id.red);
        red.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {

                mBlobColorHsv.val[0] = 250.437 ;
                mBlobColorHsv.val[1] = 162.296 ;
                mBlobColorHsv.val[2] = 198.234 ;
                mDetector.setHsvColor(mBlobColorHsv);
            }
        });

        final Button green =(Button) findViewById(R.id.green);
        green.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mBlobColorHsv.val[0] = 77.90625 ;
                mBlobColorHsv.val[1] = 172.0782; ;
                mBlobColorHsv.val[2] = 162.2343 ;
                mDetector.setHsvColor(mBlobColorHsv);

            }
        });
        final Button blue =(Button) findViewById(R.id.blue);
        blue.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {

                mBlobColorHsv.val[0] = 164.703;
                mBlobColorHsv.val[1] = 124.718;
                mBlobColorHsv.val[2] = 150.203;
                mDetector.setHsvColor(mBlobColorHsv);
            }
        });
        final Button yellow =(Button) findViewById(R.id.yellow);
        yellow.setOnClickListener(new View.OnClickListener() {
            //@Override
            public void onClick(View v) {

                mBlobColorHsv.val[0] = 42.546;
                mBlobColorHsv.val[1] = 203.75;;
                mBlobColorHsv.val[2] = 199.75;
                mDetector.setHsvColor(mBlobColorHsv);
            }
        });


        textView1 = (TextView)findViewById(R.id.Move1);
        textView2 = (TextView)findViewById(R.id.Move2);
        h=new Handler();
        Runnable runnable =new Runnable() {
            @Override
            public void run() {

                textView1.setText(LeftnRight);
                textView2.setText(UpnDown);

                h.postDelayed(this, 100);
            }
        }; h.postDelayed(runnable,100);




    }


    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new Color_detector();
        mBlobColorHsv = new Scalar(255);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        mCameraViewHeight =  height ;
        mCameraViewWidth = width ;
        Log.i(TAG,"width = " + width + "       height = " + height) ;
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        mDetector.process(mRgba);
        List<MatOfPoint> contours = mDetector.getContours();
        Log.e(TAG, "Contours count: " + contours.size());
        int n = contours.size() ;
        if(n == 0){
            LeftnRight = "Color not detected" ;
            UpnDown = "Color not detected" ;
        }
        else {
            MatOfPoint largestContour = contours.get(0);
            double largestArea = Imgproc.contourArea(largestContour);

            for (int i = 1; i < contours.size(); ++i) {
                MatOfPoint currentContour = contours.get(0);
                double currentArea = Imgproc.contourArea(currentContour);
                if (currentArea > largestArea) {
                    largestArea = currentArea;
                    largestContour = currentContour;
                }
            }
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);


            Moments moments = contourMoments(largestContour);

            double aveX = moments.get_m10() / moments.get_m00();
            double aveY = moments.get_m01() / moments.get_m00();
            double centreX = aveX * mCameraViewWidth;
            double centreY = (aveY + 1) / 2 * mCameraViewHeight;
            Imgproc.circle(mRgba, new Point(centreX, centreY), 5, CONTOUR_COLOR, -1);
            x = centreX;
            y = centreY;
            Log.i(TAG, "///////// x = " + x + " y = " + y);
//                Log.i(TAG, "///////// Maxx = " + maxX + " MAxy = " + maxY);
            if((x-293180) > 60000){
                Log.i(TAG," -----------------Move right ");
                LeftnRight = "Move Right" ;

            }
            else {
                if((x-293180) < -60000) {
                    Log.i(TAG, "Move left ");
                    LeftnRight = "Move Left" ;

                }
                else{
                    Log.i(TAG,"Dont move Left or Right");
                    LeftnRight = "Dont move Left or Right" ;

                }
            }
            if((y-85021) > 25000){
                Log.i(TAG,"Move Backward");
                UpnDown = "Move Backward" ;

            }
            else {
                if((y-85021) < -25000) {
                    Log.i(TAG, " Move Forward ");
                    UpnDown = " Move Forward " ;
                }
                else{
                    Log.i(TAG," Stop");
                    UpnDown = " Dont move Forward or Backward " ;

                }
            }

        } // else ( n == 0 )


        return mRgba;
    } // onCameraFrame


} //class Quadcopter


