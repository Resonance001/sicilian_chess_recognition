package com.example.thesis2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clock Module.
 *
 * Acts as a chess clock with a score sheet - like display
 */

public class ChessClock extends AppCompatActivity {
    private static final String TAG = "ChessClock";

    private static final long mCountDownInterval = 1000;
    private static long mTimeLeftWhite = 600000;
    private static long mTimeLeftBlack = 600000;
    private int increment = 0;

    private RelativeLayout back_dim_layout;
    private TextView mTextViewWhite;
    private TextView mTextViewBlack;
    private TextView mTextViewScore;
    private Button mButtonWhite;
    private Button mButtonBlack;

    private boolean mIsWhitePaused, mIsBlackPaused;

    public CountDownTimer mTimerWhite, mTimerBlack;
    private Toolbar toolbar;

    BluetoothAdapter mBluetoothAdapter;
    StringBuilder scoreSheet = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chess_clock);

        initializeViews();
        getTime();

        setFormat(mTimeLeftWhite, mTextViewWhite);
        setFormat(mTimeLeftBlack, mTextViewBlack);

        mButtonWhite.setOnClickListener(new ButtonListener());
        mButtonBlack.setOnClickListener(new ButtonListener());

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("SICILIAN");
        toolbar.setOnMenuItemClickListener(new ChessClock.ToolbarListener());

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_available, Toast.LENGTH_LONG).show();
            finish();
        }
        ((BluetoothApp) this.getApplicationContext()).mChatService.setHandler(mHandler);
    }

    boolean isWhite;

    private class ButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.button_white:
                    isWhite = true;
                    break;
                case R.id.button_black:
                    isWhite = false;
            }
            sendMessage(SharedPref.HAS_MOVED);
            mIsWhitePaused = isWhite;
            mIsBlackPaused = !isWhite;
            mButtonWhite.setEnabled(!mIsWhitePaused);
            mButtonBlack.setEnabled(!mIsBlackPaused);

            if (isWhite) {
                if (mTimerWhite != null) mTimerWhite.cancel();

                mTimeLeftWhite += increment;
                setFormat(mTimeLeftWhite, mTextViewWhite);
                startTimerBlack();
            } else {
                if (mTimerBlack != null) mTimerBlack.cancel();

                mTimeLeftBlack += increment;
                setFormat(mTimeLeftBlack, mTextViewBlack);
                startTimerWhite();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimerBlack != null) mTimerBlack.cancel();
        if (mTimerWhite != null) mTimerWhite.cancel();
    }

    boolean whiteMoved;

    private void freezeClock() {
        if (mIsWhitePaused) {
            whiteMoved = true;
            mButtonBlack.setEnabled(false);

            mIsBlackPaused = true;
            startTimerBlack();

        } else {
            whiteMoved = false;
            mButtonWhite.setEnabled(false);

            mIsWhitePaused = true;
            startTimerWhite();
        }
    }

    private void unfreezeClock() {
        if (whiteMoved) {
            mIsBlackPaused = false;
            mButtonBlack.setEnabled(true);
            startTimerBlack();
        } else {
            mIsWhitePaused = false;
            mButtonWhite.setEnabled(true);
            startTimerWhite();
        }
    }

    private void callPopup() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams")
        View popupView = layoutInflater.inflate(R.layout.popup_window, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                false);

        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(-1);

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        freezeClock();

        back_dim_layout.setVisibility(View.VISIBLE);

        ((Button) popupView.findViewById(R.id.queen))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        sendMessage(SharedPref.PROMOTE_QUEEN);
                        unfreezeClock();

                        back_dim_layout.setVisibility(View.GONE);
                        popupWindow.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Promoted", Toast.LENGTH_LONG).show();
                    }
                });


        ((Button) popupView.findViewById(R.id.rook))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        sendMessage(SharedPref.PROMOTE_ROOK);
                        unfreezeClock();

                        back_dim_layout.setVisibility(View.GONE);
                        popupWindow.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Promoted", Toast.LENGTH_LONG).show();
                    }
                });

        ((Button) popupView.findViewById(R.id.bishop))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        sendMessage(SharedPref.PROMOTE_BISHOP);
                        unfreezeClock();

                        back_dim_layout.setVisibility(View.GONE);
                        popupWindow.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Promoted", Toast.LENGTH_LONG).show();
                    }
                });

        ((Button) popupView.findViewById(R.id.knight))
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View arg0) {
                        sendMessage(SharedPref.PROMOTE_KNIGHT);
                        unfreezeClock();

                        back_dim_layout.setVisibility(View.GONE);
                        popupWindow.dismiss();
                        Toast.makeText(getApplicationContext(),
                                "Promoted", Toast.LENGTH_LONG).show();
                    }
                });

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                    Toast.makeText(ChessClock.this, "Choose a piece!", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }

    private void sendMessage(String message) {
        if (((BluetoothApp) this.getApplicationContext()).mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        if (message.length() > 0) {
            byte[] send = message.getBytes();
            ((BluetoothApp) this.getApplicationContext()).mChatService.write(send);
            Toast.makeText(getApplicationContext(), "Sent!", Toast.LENGTH_LONG).show();
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainMenu.MESSAGE_WRITE:
                    Toast.makeText(getApplicationContext(), "Sent!",
                            Toast.LENGTH_SHORT).show();
                    break;
                case MainMenu.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if (!readMessage.equals(SharedPref.PAWN_PROMOTION)) {
                        scoreSheet.append(readMessage);
                    } else {
                        callPopup();
                    }
                    mTextViewScore.setText(scoreSheet);
                    break;
                case MainMenu.MESSAGE_DEVICE_NAME:
                    String mConnectedDeviceName;
                    mConnectedDeviceName = msg.getData().getString(MainMenu.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MainMenu.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(MainMenu.TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chess_clock_bar, menu);
        return true;
    }

    private class ToolbarListener implements Toolbar.OnMenuItemClickListener {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.start) {
                startActivity(new Intent(ChessClock.this, SelectTimer.class));
                finish();
            }
            if (item.getItemId() == R.id.resign) {
                scoreSheet.append(mIsBlackPaused ? "0-1" : "1-0");
                String text = mIsBlackPaused ? "White resigns" : "Black resigns";
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                mTextViewScore.setText(scoreSheet);
            }
            if (item.getItemId() == R.id.draw) {
                Toast.makeText(getApplicationContext(), "Draw agreed", Toast.LENGTH_SHORT).show();
                scoreSheet.append(" 1/2 - 1/2");
                mTextViewScore.setText(scoreSheet);
            }
            if (item.getItemId() == R.id.save) {
                scoreSheet.append(System.lineSeparator());
                saveFile();
            }
            return false;
        }
    }

    private static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public void saveFile() {
        if (isExternalStorageWritable()) {
            BufferedWriter writer = null;

            File root = new File(getApplicationContext().getExternalFilesDir(null), "files");
            if (!root.exists()) {
                Log.e(TAG, "File does not exist");
                if (!root.mkdirs()) {
                    Log.e(TAG, "Failed to create directory!");
                }
            } else {
                Log.e(TAG, "File exists");
            }

            try {
                File file = new File(root, "data.txt");
                FileWriter filewriter = new FileWriter(file, true);

                writer = new BufferedWriter(filewriter);
                writer.write(scoreSheet.toString());
                writer.write(System.lineSeparator());

                Toast.makeText(getApplicationContext(), R.string.saved, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "IO exception");
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                        Toast.makeText(getApplicationContext(), R.string.success, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Toast.makeText(this, R.string.storage_inaccessible, Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        mTextViewWhite = findViewById(R.id.text_view_white);
        mTextViewBlack = findViewById(R.id.text_view_black);
        mTextViewScore = findViewById(R.id.text_view_score);

        mButtonWhite = findViewById(R.id.button_white);
        mButtonBlack = findViewById(R.id.button_black);

        toolbar = findViewById(R.id.toolbar);
        back_dim_layout = findViewById(R.id.bac_dim_layout);
    }

    private void getTime() {
        mTimeLeftWhite = SharedPref.getLongPref(this, SharedPref.MAIN_TIME, 600000);
        mTimeLeftBlack = SharedPref.getLongPref(this, SharedPref.MAIN_TIME, 600000);
        increment = SharedPref.getIntPref(this, SharedPref.INCREMENT, 0);
        increment *= 1000;
    }

    private void startTimerWhite() {
        mTimerWhite = new CountDownTimer(mTimeLeftWhite, mCountDownInterval) {
            public void onTick(long timeToFinishWhite) {
                setFormat(timeToFinishWhite, mTextViewWhite);
                mTimeLeftWhite = timeToFinishWhite;
            }

            public void onFinish() {
                Toast.makeText(ChessClock.this,
                        "Black wins!", Toast.LENGTH_LONG).show();
                scoreSheet.append("0-1");
                mButtonWhite.setEnabled(false);
                mButtonBlack.setEnabled(false);
            }
        }.start();
    }

    private void startTimerBlack() {
        mTimerBlack = new CountDownTimer(mTimeLeftBlack, mCountDownInterval) {
            public void onTick(long timeToFinishBlack) {
                setFormat(timeToFinishBlack, mTextViewBlack);
                mTimeLeftBlack = timeToFinishBlack;
            }

            public void onFinish() {
                Toast.makeText(ChessClock.this,
                        "White wins!", Toast.LENGTH_LONG).show();
                scoreSheet.append("1-0");
                mButtonWhite.setEnabled(false);
                mButtonBlack.setEnabled(false);
            }
        }.start();
    }

    private void setFormat(long time, TextView display) {
        long seconds = time / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (minutes > 0)
            seconds = seconds % 60;
        if (hours > 0)
            minutes = minutes % 60;

        String formatTime = String.format("%s : %s : %s", formatText(hours), formatText(minutes), formatText(seconds));
        display.setText(formatTime);
    }

    private String formatText(long value) {
        if (value < 10)
            return "0" + value;

        return value + "";
    }
}
