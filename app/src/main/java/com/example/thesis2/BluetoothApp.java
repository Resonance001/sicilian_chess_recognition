package com.example.thesis2;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/** Application class.
 *
 * Store BluetoothService object so that the connection remains
 * after destroying MainMenu activity
 */

public class BluetoothApp extends Application {
    public BluetoothService mChatService;
    BluetoothApp mBluetoothApp;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private String[] name = {"Classical", "Rapid", "Blitz", "Custom 1", "Custom 2"};
    private long[] time = {5400000, 1500000, 300000, 180000, 180000};
    private char[] index = {(char) 49, (char) 50, (char) 51, (char) 52, (char) 53};

    @Override
    public void onCreate()
    {
        super.onCreate();
        mBluetoothApp =  (BluetoothApp) getApplicationContext();
        mChatService = new BluetoothService();
        mChatService.setHandler(mHandler);

        for (int idx = 0; idx < index.length; idx++){
            SharedPref.setStrPref(this, SharedPref.getFormatName(index[idx]), name[idx]);
            SharedPref.setLongPref(this, SharedPref.getFormatKey(index[idx]), time[idx]);
        }
    }
}
