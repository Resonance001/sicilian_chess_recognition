package com.example.thesis2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChessCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "Main Activity";
    private static CameraBridgeViewBase javaCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    javaCameraView.setMaxFrameSize(352, 288);
                    javaCameraView.enableFpsMeter();
                    javaCameraView.enableView();
                }
                break;
                case LoaderCallbackInterface.INIT_FAILED: {
                    Log.i(TAG, "Initialization Failed");
                }
                break;
                case LoaderCallbackInterface.INSTALL_CANCELED: {
                    Log.i(TAG, "Installation Cancelled");
                }
                break;
                case LoaderCallbackInterface.INCOMPATIBLE_MANAGER_VERSION: {
                    Log.i(TAG, "Incompatible Version");
                }
                break;
                case LoaderCallbackInterface.MARKET_ERROR: {
                    Log.i(TAG, "Market Error");
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    DrawerLayout nav_drawer;
    NavigationView navigationView;
    Toolbar toolbar;

    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_chess_camera);

        initializeViews();
        setListeners();
        setViewEdge();

        javaCameraView.getLayoutParams().width = 592;
        javaCameraView.getLayoutParams().height = 727;
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        javaCameraView.setBackground(ContextCompat.getDrawable(this, R.drawable.border_full));

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("SICILIAN");
        toolbar.setOnMenuItemClickListener(new ToolbarListener());

        nav_drawer = findViewById(R.id.nav_drawer);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                nav_drawer, toolbar, R.string.open, R.string.close);

        nav_drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_available,
                    Toast.LENGTH_LONG).show();
            finish();
        }
        ((BluetoothApp) this.getApplicationContext()).mChatService.setHandler(mHandler);
    }

    private RelativeLayout mLayoutThreshold, back_dim_layout;
    private LinearLayout mLayoutAdditionalThreshold;
    private Button mButton1, mButton2, mButton3, mButtonCannyLightP;
    private TextView mTextLowerThreshold, mTextUpperThreshold, mTextRatioThreshold, mTextViewDisplay;
    private SeekBar mBarLower, mBarUpper, mBarRatio;
    private Boolean isHeaderChanged = true;

    private void initializeViews() {
        javaCameraView = findViewById(R.id.javacamera);

        toolbar = findViewById(R.id.toolbar);
        mTextViewDisplay = findViewById(R.id.test);

        back_dim_layout = findViewById(R.id.bac_dim_layout);
        mLayoutAdditionalThreshold = findViewById(R.id.additional_threshold);
        mLayoutThreshold = findViewById(R.id.layout);
        mButton1 = findViewById(R.id.button_1);
        mButton2 = findViewById(R.id.button_2);
        mButton3 = findViewById(R.id.button_3);

        mButtonCannyLightP = findViewById(R.id.button_canny_or_lightP);

        mTextLowerThreshold = findViewById(R.id.text_lower_threshold);
        mTextUpperThreshold = findViewById(R.id.text_upper_threshold);
        mTextRatioThreshold = findViewById(R.id.text_ratio);

        mBarLower = findViewById(R.id.bar_lower_threshold);
        mBarUpper = findViewById(R.id.bar_upper_threshold);
        mBarRatio = findViewById(R.id.bar_ratio);
    }

    private void setListeners() {
        mButton1.setOnClickListener(new ButtonListener());
        mButton2.setOnClickListener(new ButtonListener());
        mButton3.setOnClickListener(new ButtonListener());
        mButtonCannyLightP.setOnClickListener(new ButtonListener());

        mBarLower.setMax(255);
        mBarUpper.setMax(255);
        mBarRatio.setMax(160);

        mBarLower.setOnSeekBarChangeListener(new SeekBarListener());
        mBarUpper.setOnSeekBarChangeListener(new SeekBarListener());
        mBarRatio.setOnSeekBarChangeListener(new SeekBarListener());
    }

    private void setViewEdge() {
        mButtonCannyLightP.setVisibility(View.VISIBLE);
        mButtonCannyLightP.setText(getApplicationContext().getString(R.string.Canny));

        mLayoutThreshold.setVisibility(View.VISIBLE);

        mButton1.setText(getApplicationContext().getString(R.string.Canny));
        mButton2.setVisibility(View.GONE);
        mButton3.setVisibility(View.GONE);

        mLayoutAdditionalThreshold.setVisibility(View.GONE);

        mBarLower.setMax(256);
        mBarUpper.setMax(256);
        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(),
                SharedPref.CANNY_LOWER, 80));
        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.CANNY_UPPER, 130));
    }

    private void setViewProcess() {
        mButtonCannyLightP.setVisibility(View.VISIBLE);

        mLayoutThreshold.setVisibility(View.VISIBLE);

        mButton1.setText(getApplicationContext().getString(R.string.Hue));
        mButton2.setVisibility(View.VISIBLE);
        mButton3.setVisibility(View.VISIBLE);

        mLayoutAdditionalThreshold.setVisibility(View.VISIBLE);
        mTextViewDisplay.setVisibility(View.GONE);

        mBarLower.setMax(180);
        mBarUpper.setMax(180);

        if (isHeaderChanged) {
            setLightPieceConfig();
        } else {
            setDarkPieceConfig();
        }
    }

    private void setLightPieceConfig() {
        colorIndicator = 0;

        mButtonCannyLightP.setText(getApplicationContext().getString(R.string.Light));

        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_LOWER_LIGHT, 18));
        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_UPPER_LIGHT, 48));

        mBarRatio.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.RATIO_THRESHOLD_LIGHT, 30));
    }

    private void setDarkPieceConfig() {
        colorIndicator = 1;

        mButtonCannyLightP.setText(getApplicationContext().getString(R.string.Dark));

        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_LOWER_DARK, 0));
        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_UPPER_DARK, 180));

        mBarRatio.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.RATIO_THRESHOLD_DARK, 90));
    }

    private int mView = 0;
    private static final int VIEW_EDGE = 0;
    private static final int VIEW_LINE = 1;
    private static final int VIEW_COORDINATE = 2;
    private static final int VIEW_PROCESS = 4;
    private static final int VIEW_FINAL = 5;

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_edge:
                mView = VIEW_EDGE;
                isHeaderChanged = true;
                mTextViewDisplay.setVisibility(View.GONE);
                setViewEdge();
                break;
            case R.id.view_line:
                mView = VIEW_LINE;
                mButtonCannyLightP.setText("Line Detection");
                mLayoutThreshold.setVisibility(View.GONE);
                mTextViewDisplay.setVisibility(View.GONE);
                break;
            case R.id.view_coordinate:
                mView = VIEW_COORDINATE;
                isHeaderChanged = true;
                mButtonCannyLightP.setText("Coordinate Detection");
                mLayoutThreshold.setVisibility(View.GONE);
                mTextViewDisplay.setVisibility(View.VISIBLE);
                break;
            case R.id.view_process:
                mView = VIEW_PROCESS;
                setViewProcess();
                mTextViewDisplay.setVisibility(View.GONE);
                break;
            case R.id.view_final:
                mView = VIEW_FINAL;
                mButtonCannyLightP.setText("Final");
                mLayoutThreshold.setVisibility(View.GONE);
                mTextViewDisplay.setVisibility(View.VISIBLE);
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                nav_drawer.closeDrawer(GravityCompat.START);
            }
        }, 500);

        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_bar, menu);
        return true;
    }

    boolean isTorchOn = true;

    private class ToolbarListener implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_help) {
                help();
            }
            if (item.getItemId() == R.id.action_torch) {
                if (isTorchOn) {
                    if (javaCameraView instanceof JavaCameraView)
                        ((JavaCameraView) javaCameraView).turnOffFlash();
                } else {
                    if (javaCameraView instanceof JavaCameraView)
                        ((JavaCameraView) javaCameraView).turnOnFlash();
                }
                isTorchOn = !isTorchOn;
            }
            return false;
        }
    }

    private void help() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams")
        View popupView = layoutInflater.inflate(R.layout.popup_help, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                false);

        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(-1);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        switch (mView) {
            case VIEW_EDGE:
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_title)).setText(getString(R.string.help_edge_title));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text)).setText(getString(R.string.help_edge));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_end)).setText(getString(R.string.help_edge_end));
                break;
            case VIEW_LINE:
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_title)).setText(getString(R.string.help_line_title));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text)).setText(getString(R.string.help_line));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_end)).setText(getString(R.string.help_line_end));
                break;
            case VIEW_COORDINATE:
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_title)).setText(getString(R.string.help_coordinate_title));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text)).setText(getString(R.string.help_coordinate));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_end)).setText(getString(R.string.help_coordinate_end));
                break;
            case VIEW_PROCESS:
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_title)).setText(getString(R.string.help_process_title));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text)).setText(getString(R.string.help_process));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_end)).setText(getString(R.string.help_process_end));
                break;
            case VIEW_FINAL:
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_title)).setText(getString(R.string.help_final_title));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text)).setText(getString(R.string.help_final));
                ((TextView) popupWindow.getContentView().findViewById(R.id.help_text_end)).setText(getString(R.string.help_final_end));
                break;
            default:

        }

        back_dim_layout.setVisibility(View.VISIBLE);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    back_dim_layout.setVisibility(View.GONE);
                    popupWindow.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    int hsvIndicator = 0, colorIndicator = 0;
    boolean hasMoved = false;

    private void sendMessage(String message) {
        if (((BluetoothApp) this.getApplicationContext()).mChatService.getState()
                != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            ((BluetoothApp) this.getApplication()).mChatService.write(send);
        }
    }

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_1:
                    if (mView == VIEW_PROCESS) {
                        hsvIndicator = 0;

                        mBarLower.setMax(180);
                        mBarUpper.setMax(180);

                        if (isHeaderChanged) {
                            mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_LOWER_LIGHT, 18));
                            mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_UPPER_LIGHT, 48));
                        } else {
                            mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_LOWER_DARK, 0));
                            mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.HUE_UPPER_DARK, 180));
                        }
                    }
                    break;
                case R.id.button_2:
                    hsvIndicator = 1;

                    mBarLower.setMax(255);
                    mBarUpper.setMax(255);

                    if (isHeaderChanged) {
                        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.SATURATION_LOWER_LIGHT, 28));
                        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.SATURATION_UPPER_LIGHT, 255));
                    } else {
                        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.SATURATION_LOWER_DARK, 0));
                        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.SATURATION_UPPER_DARK, 70));
                    }
                    break;
                case R.id.button_3:
                    hsvIndicator = 2;

                    mBarLower.setMax(255);
                    mBarUpper.setMax(255);

                    if (isHeaderChanged) {
                        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.VALUE_LOWER_LIGHT, 149));
                        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.VALUE_UPPER_LIGHT, 255));
                    } else {
                        mBarLower.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.VALUE_LOWER_DARK, 0));
                        mBarUpper.setProgress(SharedPref.getIntPref(getApplicationContext(), SharedPref.VALUE_UPPER_DARK, 30));
                    }
                    break;
                case R.id.button_canny_or_lightP:
                    isHeaderChanged = !isHeaderChanged;

                    if (mView == VIEW_EDGE) {
                        if (isHeaderChanged) {
                            setViewEdge();
                        } else {
                            mButtonCannyLightP.setText(getApplicationContext().getString(R.string.Blur));
                            mLayoutThreshold.setVisibility(View.GONE);
                        }
                    }

                    if (mView == VIEW_COORDINATE) {
                        if (isHeaderChanged) {
                            mButtonCannyLightP.setText("Coordinate Detection");
                        } else {
                            mButtonCannyLightP.setText("Square Extraction");
                        }
                    }

                    if (mView == VIEW_PROCESS) {
                        if (isHeaderChanged) {
                            setLightPieceConfig();
                        } else {
                            setDarkPieceConfig();
                        }
                    }
                    break;
            }
        }
    }

    int lowerThresh, upperThresh;
    double ratio;

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.bar_lower_threshold:
                    lowerThresh = progress;
                    mTextLowerThreshold.setText(getString(R.string.lower_thresh, progress));
                    break;
                case R.id.bar_upper_threshold:
                    upperThresh = progress;
                    mTextUpperThreshold.setText(getString(R.string.upper_thresh, progress));
                    break;
                case R.id.bar_ratio:
                    lowerThresh = progress;
                    ratio = progress * 0.001;
                    mTextRatioThreshold.setText(getString(R.string.ratio_thresh, ratio));

                    String thresh = null;
                    switch (colorIndicator) {
                        case 0:
                            thresh = SharedPref.RATIO_THRESHOLD_LIGHT;
                            break;
                        case 1:
                            thresh = SharedPref.RATIO_THRESHOLD_DARK;
                            break;
                    }
                    SharedPref.setIntPref(getApplicationContext(), thresh, lowerThresh);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // implementation of abstract method
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            String lower = null, upper = null;
            if (mView == VIEW_EDGE) {
                if (isHeaderChanged) {
                    lower = SharedPref.CANNY_LOWER;
                    upper = SharedPref.CANNY_UPPER;
                }
            } else if (mView == VIEW_PROCESS && seekBar.getId() != R.id.bar_ratio) {
                switch (hsvIndicator) {
                    case 0:
                        if (isHeaderChanged) {
                            lower = SharedPref.HUE_LOWER_LIGHT;
                            upper = SharedPref.HUE_UPPER_LIGHT;
                        } else {
                            lower = SharedPref.HUE_LOWER_DARK;
                            upper = SharedPref.HUE_UPPER_DARK;
                        }
                        break;
                    case 1:
                        if (isHeaderChanged) {
                            lower = SharedPref.SATURATION_LOWER_LIGHT;
                            upper = SharedPref.SATURATION_UPPER_LIGHT;
                        } else {
                            lower = SharedPref.SATURATION_LOWER_DARK;
                            upper = SharedPref.SATURATION_UPPER_DARK;
                        }
                        break;
                    case 2:
                        if (isHeaderChanged) {
                            lower = SharedPref.VALUE_LOWER_LIGHT;
                            upper = SharedPref.VALUE_UPPER_LIGHT;
                        } else {
                            lower = SharedPref.VALUE_LOWER_DARK;
                            upper = SharedPref.VALUE_UPPER_DARK;
                        }
                        break;
                }
            }
            SharedPref.setIntPref(getApplicationContext(), lower, lowerThresh);
            SharedPref.setIntPref(getApplicationContext(), upper, upperThresh);
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainMenu.MESSAGE_WRITE:
                    Toast.makeText(getApplicationContext(),
                            "Sent", Toast.LENGTH_SHORT).show();
                    break;
                case MainMenu.MESSAGE_READ:
                    Toast.makeText(getApplicationContext(),
                            "Received!", Toast.LENGTH_SHORT).show();
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (readMessage.equals(SharedPref.HAS_MOVED)) {
                        hasMoved = true;
                    } else {
                        promotedPiece = readMessage;
                    }
                    break;
                case MainMenu.MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName;
                    mConnectedDeviceName = msg.getData().getString(MainMenu.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MainMenu.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(MainMenu.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");

        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        if (javaCameraView != null)
            javaCameraView.disableView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV library found inside package. Using it.");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "Internal OpenCV library not found. "
                    + "Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,
                    this, mLoaderCallback);
        }
    }

    // Matrices for Pre-processing and Edge Detection
    private Mat mRgba;
    private Mat mGray;
    private Mat mIntermediateMat;
    private Mat mIntermediateMat2;
    private Mat mCloseMorph;

    private Mat mLab;
    private Mat mDestImage;
    private Mat mGray2;

    // Matrices for Line Detection
    List<MatOfPoint> contours;
    private Mat hierarchy;
    private Mat box;
    private Mat sub_edges;
    private Mat lines;

    // Matrices for Color Detection
    private Mat thr;
    private Mat hsv;
    private Mat thrLight;
    private Mat thrDark;

    private static final short CLAHE_CLIP_LIMIT = 4;
    private static final short HOUGH_ACCUMULATOR_THRESHOLD = 40;
    private static final double RHO_THRESHOLD = 15;

    private static ArrayList<Point> points = new ArrayList<>();
    private int counter = 0;

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat2 = new Mat(height, width, CvType.CV_8UC4);
        mCloseMorph = Imgproc.getStructuringElement(
                Imgproc.CV_SHAPE_ELLIPSE, new Size(3, 3));

        mLab = new Mat();
        mDestImage = new Mat();
        mGray2 = new Mat();

        contours = new ArrayList<>();
        hierarchy = new Mat();
        box = new Mat();
        sub_edges = new Mat();
        lines = new Mat();

        thr = new Mat();
        hsv = new Mat();
        thrLight = new Mat();
        thrDark = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
        mIntermediateMat2.release();
        mCloseMorph.release();

        mLab.release();
        mDestImage.release();
        mGray2.release();

        hierarchy.release();
        box.release();
        sub_edges.release();
        lines.release();

        thr.release();
        hsv.release();
        thrLight.release();
        thrDark.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        counter++;
        if (counter < 3) {
            return null;
        }
        counter = 0;

        final int view = mView;
        switch (view) {
            case VIEW_EDGE: {
                mRgba = inputFrame.rgba();

                Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_RGB2Lab);

                List<Mat> channels = new LinkedList<>();
                Core.split(mLab, channels);

                CLAHE clahe = Imgproc.createCLAHE();
                clahe.setClipLimit(CLAHE_CLIP_LIMIT);
                clahe.setTilesGridSize(new Size(8, 8));

                clahe.apply(channels.get(0), mDestImage);
                mDestImage.copyTo(channels.get(0));
                Core.merge(channels, mLab);

                Imgproc.cvtColor(mLab, mRgba, Imgproc.COLOR_Lab2RGB);
                mLab.release();

                Imgproc.cvtColor(mRgba, mGray2, Imgproc.COLOR_RGB2GRAY);
                Imgproc.bilateralFilter(mGray2, mGray, 5, 60, 120);

                if (!isHeaderChanged) return mGray;

                Imgproc.Canny(mGray, mIntermediateMat,
                        SharedPref.getIntPref(this, SharedPref.CANNY_LOWER, 210),
                        SharedPref.getIntPref(this, SharedPref.CANNY_UPPER, 256));
                Imgproc.morphologyEx(mIntermediateMat, mIntermediateMat2, Imgproc.MORPH_CLOSE, mCloseMorph);

                mGray.release();
                return mIntermediateMat2;
            }
            case VIEW_LINE: {
                mRgba = inputFrame.rgba();

                Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_RGB2Lab);

                List<Mat> channels = new LinkedList<>();
                Core.split(mLab, channels);

                CLAHE clahe = Imgproc.createCLAHE();
                clahe.setClipLimit(CLAHE_CLIP_LIMIT);
                clahe.setTilesGridSize(new Size(8, 8));

                clahe.apply(channels.get(0), mDestImage);
                mDestImage.copyTo(channels.get(0));
                Core.merge(channels, mLab);

                Imgproc.cvtColor(mLab, mRgba, Imgproc.COLOR_Lab2RGB);
                mLab.release();

                Imgproc.cvtColor(mRgba, mGray2, Imgproc.COLOR_RGB2GRAY);
                Imgproc.bilateralFilter(mGray2, mGray, 5, 60, 120);
                Imgproc.Canny(mGray, mIntermediateMat,
                        SharedPref.getIntPref(this, SharedPref.CANNY_LOWER, 210),
                        SharedPref.getIntPref(this, SharedPref.CANNY_UPPER, 256));
                Imgproc.morphologyEx(mIntermediateMat, mIntermediateMat2,
                        Imgproc.MORPH_CLOSE, mCloseMorph);

                mGray.release();

                Imgproc.findContours(mIntermediateMat2, contours, hierarchy,
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                hierarchy.release();

                if (contours.size() > 0) {
                    double best_area = 0;
                    int best_idx = 0;

                    int idx = 0;
                    for (MatOfPoint contour : contours) {
                        double area = Imgproc.contourArea(contour);
                        if (area > best_area) {
                            best_area = area;
                            best_idx = idx;
                        }
                        idx++;
                    }

                    MatOfPoint best_contour = contours.get(best_idx);
                    RotatedRect best_rect = Imgproc.minAreaRect(
                            new MatOfPoint2f(best_contour.toArray()));

                    Imgproc.boxPoints(best_rect, box);
                    Rect bbox = Imgproc.boundingRect(best_contour);

                    double total_len = Math.min(Math.abs(bbox.tl().x - bbox.br().x),
                            Math.abs(bbox.tl().y - bbox.br().y));
                    double max_line_gap = total_len * 0.9;
                    double min_line_length = total_len * 0.65;

                    Mat sub_edges = mIntermediateMat.submat(bbox);

                    Imgproc.HoughLinesP(sub_edges, lines, 1, 1 * Math.PI / 180,
                            HOUGH_ACCUMULATOR_THRESHOLD, min_line_length, max_line_gap);

                    mIntermediateMat.release();
                    sub_edges.release();

                    for (int i = 0; i < lines.rows(); i++) {
                        double[] line = lines.get(i, 0);

                        Imgproc.line(mRgba, new Point(line[0] + bbox.x, line[1] + bbox.y),
                                new Point(line[2] + bbox.x, line[3] + bbox.y),
                                new Scalar(0, 255, 0), 1);
                    }
                    lines.release();

                    MatOfPoint box_points = new MatOfPoint(
                            new Point(box.get(0, 0)[0], box.get(0, 1)[0]),
                            new Point(box.get(1, 0)[0], box.get(1, 1)[0]),
                            new Point(box.get(2, 0)[0], box.get(2, 1)[0]),
                            new Point(box.get(3, 0)[0], box.get(3, 1)[0])
                    );

                    List<MatOfPoint> box_contours = new ArrayList<>();
                    box_contours.add(box_points);

                    Imgproc.drawContours(mRgba, box_contours, 0, new Scalar(255, 0, 0), 2);

                    MatOfPoint bbox_points = new MatOfPoint(
                            bbox.tl(),
                            new Point(bbox.br().x, bbox.tl().y),
                            bbox.br(),
                            new Point(bbox.tl().x, bbox.br().y)
                    );
                    box_contours.add(bbox_points);
                    Imgproc.drawContours(mRgba, box_contours, 1, new Scalar(0, 255, 0), 2);
                }

                return mRgba;
            }
            case VIEW_COORDINATE: {
                points.clear();
                contours.clear();

                mRgba = inputFrame.rgba();

                Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_RGB2Lab);

                List<Mat> channels = new LinkedList<>();
                Core.split(mLab, channels);

                CLAHE clahe = Imgproc.createCLAHE();
                clahe.setClipLimit(CLAHE_CLIP_LIMIT);
                clahe.setTilesGridSize(new Size(8, 8));

                clahe.apply(channels.get(0), mDestImage);
                mDestImage.copyTo(channels.get(0));
                Core.merge(channels, mLab);

                Imgproc.cvtColor(mLab, mRgba, Imgproc.COLOR_Lab2RGB);
                mLab.release();

                Imgproc.cvtColor(mRgba, mGray2, Imgproc.COLOR_RGB2GRAY);
                Imgproc.bilateralFilter(mGray2, mGray, 5, 60, 120);
                Imgproc.Canny(mGray, mIntermediateMat,
                        SharedPref.getIntPref(this, SharedPref.CANNY_LOWER, 210),
                        SharedPref.getIntPref(this, SharedPref.CANNY_UPPER, 256));
                Imgproc.morphologyEx(mIntermediateMat, mIntermediateMat2,
                        Imgproc.MORPH_CLOSE, mCloseMorph);
                mGray.release();

                Imgproc.findContours(mIntermediateMat2, contours, hierarchy,
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                hierarchy.release();

                if (contours.size() > 0) {
                    double best_area = 0;
                    int best_idx = 0;

                    int idx = 0;
                    for (MatOfPoint contour : contours) {
                        double area = Imgproc.contourArea(contour);
                        if (area > best_area) {
                            best_area = area;
                            best_idx = idx;
                        }
                        idx++;
                    }

                    MatOfPoint best_contour = contours.get(best_idx);
                    RotatedRect best_rect = Imgproc.minAreaRect(
                            new MatOfPoint2f(best_contour.toArray()));

                    Imgproc.boxPoints(best_rect, box);
                    Rect bbox = Imgproc.boundingRect(best_contour);

                    double center_x = (bbox.tl().x + bbox.br().x) / 2;
                    double center_y = (bbox.tl().y + bbox.br().y) / 2;

                    Imgproc.line(mRgba, new Point(0, mRgba.height() * 0.5), new Point(mRgba.width(), mRgba.height() * 0.5), new Scalar(0, 0, 0));
                    Imgproc.line(mRgba, new Point(mRgba.width() * 0.5, 0), new Point(mRgba.width() * 0.5, mRgba.height()), new Scalar(0, 0, 0));

                    drawCoordinates(mRgba, new Scalar(255, 0, 0), bbox.tl(), bbox.br(), new Point(bbox.br().x, bbox.tl().y),
                            new Point(bbox.tl().x, bbox.br().y), new Point(center_x, center_y));

                    double total_len = Math.min(Math.abs(bbox.tl().x - bbox.br().x), Math.abs(bbox.tl().y - bbox.br().y));
                    double max_line_gap = total_len * 0.9;
                    double min_line_length = total_len * 0.65;

                    sub_edges = mIntermediateMat.submat(bbox);

                    Imgproc.HoughLinesP(sub_edges, lines, 1, Math.PI / 180,
                            HOUGH_ACCUMULATOR_THRESHOLD, min_line_length, max_line_gap);

                    mIntermediateMat.release();
                    sub_edges.release();

                    if (lines.rows() > 2) {
                        ArrayList<double[]> vertical_list = new ArrayList<>();
                        ArrayList<Double> vertical_rhos = new ArrayList<>();

                        ArrayList<double[]> horizontal_list = new ArrayList<>();
                        ArrayList<Double> horizontal_rhos = new ArrayList<>();

                        for (int i = 0; i < lines.rows(); i++) {
                            double[] line = lines.get(i, 0);
                            double x1 = line[0];
                            double y1 = line[1];
                            double x2 = line[2];
                            double y2 = line[3];

                            double theta = Math.atan2(x2 - x1, y2 - y1);
                            double rho = x1 * Math.cos(theta) + y1 * Math.sin(theta);

                            if (LineIsVertical(theta)) {
                                if (LineIsUnique(rho, vertical_rhos)) {
                                    vertical_list.add(line);
                                    vertical_rhos.add(rho);
                                }
                            } else if (LineIsHorizontal(theta)) {
                                if (LineIsUnique(rho, horizontal_rhos)) {
                                    horizontal_list.add(line);
                                    horizontal_rhos.add(rho);
                                }
                            } else break;
                        }
                        lines.release();

                        for (double[] line : vertical_list) {
                            Imgproc.line(mRgba, new Point(line[0] + bbox.x, line[1] + bbox.y),
                                    new Point(line[2] + bbox.x, line[3] + bbox.y),
                                    new Scalar(0, 255, 0), 1);
                        }
                        for (double[] line : horizontal_list) {
                            Imgproc.line(mRgba, new Point(line[0] + bbox.x, line[1] + bbox.y),
                                    new Point(line[2] + bbox.x, line[3] + bbox.y),
                                    new Scalar(255, 0, 0), 1);
                        }

                        for (double[] v : vertical_list) {
                            double a1 = v[0];
                            double b1 = v[1];
                            double a2 = v[2];
                            double b2 = v[3];

                            for (double[] h : horizontal_list) {
                                double x, y, m1, m2;

                                double x1 = h[0];
                                double y1 = h[1];
                                double x2 = h[2];
                                double y2 = h[3];

                                m2 = (y2 - y1) / (x2 - x1);

                                if (a2 - a1 == 0) {
                                    x = a2;
                                } else {
                                    m1 = (b2 - b1) / (a2 - a1);
                                    x = (m1 * a1 - m2 * x1 + y1 - b1) / (m1 - m2);
                                }

                                y = m2 * (x - x1) + y1;

                                points.add(new Point(x + bbox.x, y + bbox.y));
                            }
                        }

                        if (points.size() > 81) {
                            points.sort((a, b) -> Double.compare(
                                    Math.max(Math.abs(center_x - a.x), Math.abs(center_y - a.y)),
                                    Math.max(Math.abs(center_x - b.x), Math.abs(center_y - b.y))
                            ));
                            points.subList(81, points.size()).clear();
                        }

                        for (Point point : points)
                            Imgproc.circle(mRgba, point, 2, new Scalar(0, 0, 255));

                        mTextViewDisplay.setText("Number of Coordinates: " + points.size());

                        if (points.size() > 0) {
                            Collections.sort(points, new Comparator<Point>() {
                                @Override
                                public int compare(Point a, Point b) {
                                    return Double.compare(a.x, b.x);
                                }
                            });

                            double length = points.get(points.size() - 1).x - points.get(0).x;
                            double threshold = length / 16;

                            Collections.sort(points, new Comparator<Point>() {
                                @Override
                                public int compare(Point a, Point b) {
                                    if (Math.abs(a.x - b.x) < threshold)
                                        return Double.compare(b.y, a.y);
                                    else return 0;
                                }
                            });

                            if (!isHeaderChanged && points.size() == 81) {
                                int ctr = 0;
                                for (int i = 1; i < 72; i++) {
                                    if (i % 9 == 0) continue;
                                    ctr++;
                                    double offset = 3.0;

                                    Point tr = new Point(points.get(i).x + offset, points.get(i).y + offset);
                                    Point bl = new Point(points.get(i + 8).x - offset, points.get(i + 8).y - offset);

                                    Mat square = mRgba.submat(new Rect(tr, bl));
                                    switch (ctr) {
                                        case 1:
                                            square.setTo(new Scalar(255, 0, 0));
                                            break;
                                        case 2:
                                            square.setTo(new Scalar(0, 255, 0));
                                            break;
                                        case 3:
                                            square.setTo(new Scalar(0, 0, 255));
                                            break;
                                        case 4:
                                            square.setTo(new Scalar(255, 255, 0));
                                            break;
                                        case 5:
                                            square.setTo(new Scalar(0, 255, 255));
                                            ctr = 0;
                                            break;
                                    }
                                    square.release();
                                }
                            }
                        }
                    }
                }
                return mRgba;
            }
            case VIEW_PROCESS: {
                mRgba = inputFrame.rgba();

                Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_RGB2Lab);

                List<Mat> channels = new LinkedList<>();
                Core.split(mLab, channels);

                CLAHE clahe = Imgproc.createCLAHE();
                clahe.setClipLimit(CLAHE_CLIP_LIMIT);
                clahe.setTilesGridSize(new Size(8, 8));

                clahe.apply(channels.get(0), mDestImage);
                mDestImage.copyTo(channels.get(0));
                Core.merge(channels, mLab);

                Imgproc.cvtColor(mLab, mRgba, Imgproc.COLOR_Lab2RGB);
                mLab.release();

                if (isHeaderChanged) {
                    Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV);
                    Core.inRange(hsv,
                            new Scalar(
                                    SharedPref.getIntPref(this, SharedPref.HUE_LOWER_LIGHT, 18),
                                    SharedPref.getIntPref(this, SharedPref.SATURATION_LOWER_LIGHT, 28),
                                    SharedPref.getIntPref(this, SharedPref.VALUE_LOWER_LIGHT, 149)),
                            new Scalar(
                                    SharedPref.getIntPref(this, SharedPref.HUE_UPPER_LIGHT, 48),
                                    SharedPref.getIntPref(this, SharedPref.SATURATION_UPPER_LIGHT, 255),
                                    SharedPref.getIntPref(this, SharedPref.VALUE_UPPER_LIGHT, 255)),
                            thr);
                } else {
                    Mat mask = new Mat();
                    Mat remove = new Mat();

                    Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV);
                    Core.inRange(hsv, new Scalar(35, 0, 0), new Scalar(85, 255, 255), mask);
                    Core.bitwise_not(mask, mask);
                    mRgba.copyTo(remove, mask);

                    Imgproc.cvtColor(remove, hsv, Imgproc.COLOR_RGB2HSV);

                    Core.inRange(hsv,
                            new Scalar(
                                    SharedPref.getIntPref(this, SharedPref.HUE_LOWER_DARK, 0),
                                    SharedPref.getIntPref(this, SharedPref.SATURATION_LOWER_DARK, 1),
                                    SharedPref.getIntPref(this, SharedPref.VALUE_LOWER_DARK, 0)),
                            new Scalar(
                                    SharedPref.getIntPref(this, SharedPref.HUE_UPPER_DARK, 180),
                                    SharedPref.getIntPref(this, SharedPref.SATURATION_UPPER_DARK, 70),
                                    SharedPref.getIntPref(this, SharedPref.VALUE_UPPER_DARK, 30)),
                            thr);
                }
                mGray.release();
                hsv.release();

                return thr;
            }
            case VIEW_FINAL: {
                points.clear();
                contours.clear();

                mRgba = inputFrame.rgba();

                Imgproc.cvtColor(mRgba, mLab, Imgproc.COLOR_RGB2Lab);

                List<Mat> channels = new LinkedList<>();
                Core.split(mLab, channels);

                CLAHE clahe = Imgproc.createCLAHE();
                clahe.setClipLimit(CLAHE_CLIP_LIMIT);
                clahe.setTilesGridSize(new Size(8, 8));

                clahe.apply(channels.get(0), mDestImage);
                mDestImage.copyTo(channels.get(0));
                Core.merge(channels, mLab);

                Imgproc.cvtColor(mLab, mRgba, Imgproc.COLOR_Lab2RGB);
                mLab.release();

                Imgproc.cvtColor(mRgba, mGray2, Imgproc.COLOR_RGB2GRAY);
                Imgproc.bilateralFilter(mGray2, mGray, 5, 60, 120);
                Imgproc.Canny(mGray, mIntermediateMat,
                        SharedPref.getIntPref(this, SharedPref.CANNY_LOWER, 210),
                        SharedPref.getIntPref(this, SharedPref.CANNY_UPPER, 256));
                Imgproc.morphologyEx(mIntermediateMat, mIntermediateMat2,
                        Imgproc.MORPH_CLOSE, mCloseMorph);
                mGray.release();

                Imgproc.findContours(mIntermediateMat2, contours, hierarchy,
                        Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
                hierarchy.release();

                if (contours.size() > 0) {
                    double best_area = 0;
                    int best_idx = 0;

                    int idx = 0;
                    for (MatOfPoint contour : contours) {
                        double area = Imgproc.contourArea(contour);
                        if (area > best_area) {
                            best_area = area;
                            best_idx = idx;
                        }
                        idx++;
                    }

                    MatOfPoint best_contour = contours.get(best_idx);
                    RotatedRect best_rect = Imgproc.minAreaRect(
                            new MatOfPoint2f(best_contour.toArray()));

                    Imgproc.boxPoints(best_rect, box);
                    Rect bbox = Imgproc.boundingRect(best_contour);

                    double center_x = (bbox.tl().x + bbox.br().x) / 2;
                    double center_y = (bbox.tl().y + bbox.br().y) / 2;

                    Imgproc.line(mRgba, new Point(0, mRgba.height() * 0.5),
                            new Point(mRgba.width(), mRgba.height() * 0.5), new Scalar(0, 0, 0));
                    Imgproc.line(mRgba, new Point(mRgba.width() * 0.5, 0),
                            new Point(mRgba.width() * 0.5, mRgba.height()), new Scalar(0, 0, 0));

                    drawCoordinates(mRgba, new Scalar(255, 0, 0), bbox.tl(), bbox.br(), new Point(bbox.br().x, bbox.tl().y),
                            new Point(bbox.tl().x, bbox.br().y), new Point(center_x, center_y));

                    double total_len = Math.min(Math.abs(bbox.tl().x - bbox.br().x), Math.abs(bbox.tl().y - bbox.br().y));
                    double max_line_gap = total_len * 0.9;
                    double min_line_length = total_len * 0.65;

                    sub_edges = mIntermediateMat.submat(bbox);

                    Imgproc.HoughLinesP(sub_edges, lines, 1, Math.PI / 180,
                            HOUGH_ACCUMULATOR_THRESHOLD, min_line_length, max_line_gap);

                    mIntermediateMat.release();
                    sub_edges.release();

                    if (lines.rows() > 2) {
                        ArrayList<double[]> vertical_list = new ArrayList<>();
                        ArrayList<Double> vertical_rhos = new ArrayList<>();

                        ArrayList<double[]> horizontal_list = new ArrayList<>();
                        ArrayList<Double> horizontal_rhos = new ArrayList<>();

                        for (int i = 0; i < lines.rows(); i++) {
                            double[] line = lines.get(i, 0);
                            double x1 = line[0];
                            double y1 = line[1];
                            double x2 = line[2];
                            double y2 = line[3];

                            double theta = Math.atan2(x2 - x1, y2 - y1);
                            double rho = x1 * Math.cos(theta) + y1 * Math.sin(theta);

                            if (LineIsVertical(theta)) {
                                if (LineIsUnique(rho, vertical_rhos)) {
                                    vertical_list.add(line);
                                    vertical_rhos.add(rho);
                                }
                            } else if (LineIsHorizontal(theta)) {
                                if (LineIsUnique(rho, horizontal_rhos)) {
                                    horizontal_list.add(line);
                                    horizontal_rhos.add(rho);
                                }
                            } else break;
                        }
                        lines.release();

                        for (double[] line : vertical_list) {
                            Imgproc.line(mRgba, new Point(line[0] + bbox.x, line[1] + bbox.y),
                                    new Point(line[2] + bbox.x, line[3] + bbox.y),
                                    new Scalar(0, 255, 0), 1);
                        }
                        for (double[] line : horizontal_list) {
                            Imgproc.line(mRgba, new Point(line[0] + bbox.x, line[1] + bbox.y),
                                    new Point(line[2] + bbox.x, line[3] + bbox.y),
                                    new Scalar(255, 0, 0), 1);
                        }

                        for (double[] v : vertical_list) {
                            double a1 = v[0];
                            double b1 = v[1];
                            double a2 = v[2];
                            double b2 = v[3];

                            for (double[] h : horizontal_list) {
                                double x, y, m1, m2;

                                double x1 = h[0];
                                double y1 = h[1];
                                double x2 = h[2];
                                double y2 = h[3];

                                m2 = (y2 - y1) / (x2 - x1);

                                if (a1 == a2) {
                                    x = a2;
                                } else {
                                    m1 = (b2 - b1) / (a2 - a1);
                                    x = (m1 * a1 - m2 * x1 + y1 - b1) / (m1 - m2);
                                }
                                y = m2 * (x - x1) + y1;

                                points.add(new Point(x + bbox.x, y + bbox.y));
                            }
                        }

                        if (points.size() > 81) {
                            points.sort((a, b) ->
                                    Double.compare(
                                            Math.max(Math.abs(center_x - a.x),
                                                    Math.abs(center_y - a.y)),
                                            Math.max(Math.abs(center_x - b.x),
                                                    Math.abs(center_y - b.y))
                                    ));
                            points.subList(81, points.size()).clear();
                        }

                        if (points.size() > 0) {
                            Collections.sort(points, new Comparator<Point>() {
                                @Override
                                public int compare(Point a, Point b) {
                                    return Double.compare(a.x, b.x);
                                }
                            });

                            double length = points.get(points.size() - 1).x - points.get(0).x;
                            double threshold = length / 16;

                            Collections.sort(points, new Comparator<Point>() {
                                @Override
                                public int compare(Point a, Point b) {
                                    if (Math.abs(a.x - b.x) < threshold)
                                        return Double.compare(b.y, a.y);
                                    else return 0;
                                }
                            });

                            if (points.size() == 81) {
                                Mat square;

                                int piece, piece_dark = 0, piece_light = 0;

                                double ratio_light = SharedPref.getIntPref(this, SharedPref.RATIO_THRESHOLD_LIGHT, 30);
                                double ratio_dark = SharedPref.getIntPref(this, SharedPref.RATIO_THRESHOLD_DARK, 80);
                                ratio_light /= 1000;
                                ratio_dark /= 1000;

                                double offset = 3.0;

                                Point point1 = new Point(points.get(1).x + offset, points.get(1).y + offset);
                                Point point2 = new Point(points.get(1 + 8).x - offset, points.get(9).y - offset);

                                double area = (point2.x - point1.x) * (point2.y - point1.y);
                                int area_light = (int) (area * ratio_light);
                                int area_dark = (int) (area * ratio_dark);

                                Mat mask = new Mat();
                                Mat remove = new Mat();
                                Mat hsv_dark = new Mat();

                                Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV);
                                Core.inRange(hsv, new Scalar(35, 0, 0), new Scalar(85, 255, 255), mask);
                                Core.bitwise_not(mask, mask);
                                mRgba.copyTo(remove, mask);

                                Imgproc.cvtColor(remove, hsv_dark, Imgproc.COLOR_RGB2HSV);
                                Imgproc.cvtColor(mRgba, hsv, Imgproc.COLOR_RGB2HSV);

                                Core.inRange(hsv,
                                        new Scalar(
                                                SharedPref.getIntPref(this, SharedPref.HUE_LOWER_LIGHT, 18),
                                                SharedPref.getIntPref(this, SharedPref.SATURATION_LOWER_LIGHT, 28),
                                                SharedPref.getIntPref(this, SharedPref.VALUE_LOWER_LIGHT, 149)),
                                        new Scalar(
                                                SharedPref.getIntPref(this, SharedPref.HUE_UPPER_LIGHT, 48),
                                                SharedPref.getIntPref(this, SharedPref.SATURATION_UPPER_LIGHT, 255),
                                                SharedPref.getIntPref(this, SharedPref.VALUE_UPPER_LIGHT, 255)),
                                        thrLight);

                                Core.inRange(hsv_dark,
                                        new Scalar(
                                                SharedPref.getIntPref(this, SharedPref.HUE_LOWER_DARK, 0),
                                                SharedPref.getIntPref(this, SharedPref.SATURATION_LOWER_DARK, 1),
                                                SharedPref.getIntPref(this, SharedPref.VALUE_LOWER_DARK, 0)),
                                        new Scalar(
                                                SharedPref.getIntPref(this, SharedPref.HUE_UPPER_DARK, 180),
                                                SharedPref.getIntPref(this, SharedPref.SATURATION_UPPER_DARK, 70),
                                                SharedPref.getIntPref(this, SharedPref.VALUE_UPPER_DARK, 30)),
                                        thrDark);

                                hsv.release();

                                int j = 0, k = 0;
                                k--;

                                for (int i = 1; i < 72; i++) {
                                    if (i % 9 == 0) continue;

                                    {
                                        k++;
                                        if (k == 8) {
                                            k = 0;
                                            j++;
                                        }
                                    }

                                    Point tr = new Point(points.get(i).x + offset, points.get(i).y + offset);
                                    Point bl = new Point(points.get(i + 8).x - offset, points.get(i + 8).y - offset);

                                    square = thrLight.submat(new Rect(tr, bl));
                                    if (Core.countNonZero(square) > area_light) {
                                        piece_light++;
                                        present_array[j][k] = 1;
                                        continue;
                                    }

                                    square = thrDark.submat(new Rect(tr, bl));
                                    if (Core.countNonZero(square) > area_dark) {
                                        piece_dark++;
                                        present_array[j][k] = 2;
                                        continue;
                                    }

                                    present_array[j][k] = 0;
                                }
                                thrLight.release();
                                thrDark.release();

                                piece = piece_dark + piece_light;

                                String text = "Light Pieces: " + piece_light + "| Dark Pieces: "
                                        + piece_dark + "| Total Pieces: " + piece + "\n"
                                        + "Move: " + currMove;
                                mTextViewDisplay.setText(text);

                                if (hasMoved) {
                                    compareArray();
                                    convertToASCII();
                                    move();

                                    if (isCaptured) {
                                        if (moves % 2 == 0) {
                                            if (piece_light == old_piece_light - 1
                                                    && piece_dark == old_piece_dark) {
                                                hasMoved = false;
                                            }
                                        } else {
                                            if (piece_light == old_piece_light
                                                    && piece_dark == old_piece_dark - 1) {
                                                hasMoved = false;
                                            }
                                        }
                                    } else {
                                        if (piece_light == old_piece_light
                                                || piece_dark == old_piece_dark) {
                                            hasMoved = false;
                                        }
                                    }
                                    if (noMove) {
                                        hasMoved = true;
                                        noMove = false;
                                    }

                                    if (!hasMoved) {
                                        if (isPromoted) {
                                            while (promotedPiece == null) {
                                                try {
                                                    Thread.sleep(300);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            move.append("==").append(promotedPiece);
                                        }
                                        move.append(moves % 2 != 0 ? ", " : " ");

                                        for (int a = 0; a < present_array.length; ++a)
                                            System.arraycopy(
                                                    present_array[a], 0,
                                                    previous_array[a], 0,
                                                    previous_array[a].length);

                                        old_piece_light = piece_light;
                                        old_piece_dark = piece_dark;

                                        convertFromASCII();
                                        updatePieceArray();

                                        hasMoved = false;
                                        sendMessage(move.toString());
                                    }
                                    occupied = 0;
                                    unoccupied = 0;
                                    capture = 0;
                                    enPassant[0] = 8;
                                    enPassant[1] = 8;

                                    currMove.append(move);
                                }
                            }

                            for (Point point : points)
                                Imgproc.circle(mRgba, point, 2, new Scalar(0, 0, 255));
                        }
                    }
                }
                System.gc();
                return mRgba;
            }
            default:
                return inputFrame.rgba();
        }
    }

    private static boolean LineIsVertical(double theta) {
        return Math.abs(theta - Math.PI) <= 1.75 * Math.PI / 180 ||
                Math.abs(theta) <= 1.75 * Math.PI / 180;
    }

    private static boolean LineIsHorizontal(double theta) {
        return Math.abs(theta - Math.PI / 2) <= 1.75 * Math.PI / 180 ||
                Math.abs(theta + Math.PI / 2) <= 1.75 * Math.PI / 180;
    }

    private static boolean LineIsUnique(double rho, ArrayList<Double> rhos) {
        for (double other_rho : rhos) {
            if (Math.abs(rho - other_rho) < RHO_THRESHOLD)
                return false;
        }
        return true;
    }

    private static void drawCoordinates(Mat img, Scalar color, Point... points) {
        int radius = 2;
        for (Point point : points) {
            Imgproc.circle(img, point, radius, color);
        }
    }

    int old_piece_light = 16;
    int old_piece_dark = 16;
    int column, row;
    int piece = 0, occupied = 0, unoccupied = 0;
    int capture = 0;
    int moves = 0;
    int[] enPassant = {8, 8};
    boolean isCaptured = true;
    boolean noMove = false;
    boolean isPromoted = false;
    String promotedPiece = null;
    StringBuilder move = new StringBuilder();
    StringBuilder currMove = new StringBuilder();

    private void compareArray() {
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                if (present_array[i][j] != previous_array[i][j]) {
                    if (present_array[i][j] == 0 && previous_array[i][j] != 0) {
                        piece = piece_array[i][j];
                        unoccupied++;

                        enPassant[enPassant[0] == 8 ? 0 : 1] = j;
                    }
                    if (present_array[i][j] != 0 && previous_array[i][j] == 0) {
                        row = i;
                        column = j;
                        occupied++;
                    }
                    if (present_array[i][j] != 0 && previous_array[i][j] != 0) {
                        row = i;
                        column = j;
                        capture++;
                    }
                }
            }
        }
    }

    private void updatePieceArray() {
        if (isPromoted) {
            int piece;
            switch (promotedPiece) {
                case SharedPref.PROMOTE_ROOK:
                    piece = 82;
                    break;
                case SharedPref.PROMOTE_BISHOP:
                    piece = 66;
                    break;
                case SharedPref.PROMOTE_KNIGHT:
                    piece = 78;
                    break;
                case SharedPref.PROMOTE_QUEEN:
                default:
                    piece = 81;
            }
            piece_array[row][column] = piece;
            isPromoted = false;
            promotedPiece = null;
        } else if (occupied == 2 && unoccupied == 2) {
            int rank = row == 0 ? 0 : 7;
            if (column > 4) {
                piece_array[rank][6] = 75;
                piece_array[rank][5] = 82;
            } else {
                piece_array[rank][2] = 75;
                piece_array[rank][3] = 82;
            }
        } else {
            piece_array[row][column] = piece;
        }
    }

    private void convertFromASCII() {
        row -= 56;
        row = -row;
        column -= 97;
    }

    private void convertToASCII() {
        row = -row;
        row += 56;
        column += 97;

        enPassant[0] += 97;
        enPassant[1] += 97;
    }

    private void move() {
        move.setLength(0);
        moves++;

        if (moves % 2 != 0) {
            int num = (int) Math.ceil(moves / 2.0);
            move.append(num).append(". ");
        }

        if (capture == 1) {
            isCaptured = true;

            if (piece == 32) {
                if (moves % 2 == 0) {
                    if (row == 49) {
                        sendMessage(SharedPref.PAWN_PROMOTION);
                        isPromoted = true;
                    }
                } else {
                    if (row == 56) {
                        sendMessage(SharedPref.PAWN_PROMOTION);
                        isPromoted = true;
                    }
                }
                move.append((char) enPassant[0]);
            } else {
                move.append((char) piece);
            }
            move.append("x");
            move.append((char) column);
            move.append((char) row);
        } else if ((occupied == 1 && unoccupied == 2)) {
            isCaptured = true;

            move.append(enPassant[0] != column ?
                    (char) enPassant[0] : (char) enPassant[1]);
            move.append("x");
            move.append((char) column);
            move.append((char) row);
        } else if (occupied == 1 && unoccupied == 1) {
            isCaptured = false;

            if (piece == 32) {
                if (moves % 2 == 0) {
                    if (row == 49) {
                        sendMessage(SharedPref.PAWN_PROMOTION);
                        isPromoted = true;
                    }
                } else {
                    if (row == 56) {
                        sendMessage(SharedPref.PAWN_PROMOTION);
                        isPromoted = true;
                    }
                }
            }
            move.append((char) piece);
            move.append((char) column);
            move.append((char) row);
        } else if (occupied == 2 && unoccupied == 2) {
            isCaptured = false;

            move.append(column > 101 ? "0-0" : "0-0-0");
        } else {
            noMove = true;
            moves--;
        }
    }

    int[][] piece_array = {
            {82, 78, 66, 81, 75, 66, 78, 82},
            {32, 32, 32, 32, 32, 32, 32, 32},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {32, 32, 32, 32, 32, 32, 32, 32},
            {82, 78, 66, 81, 75, 66, 78, 82},
    };

    int[][] previous_array = {
            {2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
    };

    int[][] present_array = {
            {2, 2, 2, 2, 2, 2, 2, 2},
            {2, 2, 2, 2, 2, 2, 2, 2},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0},
            {1, 1, 1, 1, 1, 1, 1, 1},
            {1, 1, 1, 1, 1, 1, 1, 1},
    };
}