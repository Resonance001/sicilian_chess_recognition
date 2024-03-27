package com.example.thesis2;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * MainMenu class.
 *
 * Launcher activity. Instantiate bluetooth connection here.
 * ChessClock > Holds the clock module on one device
 * ChessCamera > Holds the camera module on another device
 */

public class MainMenu extends AppCompatActivity{

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 10;
    private static final int BLUETOOTH_PERMISSION_CODE = 200;

    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;

    public boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if (hasAllPermissionsGranted(grantResults)) {
                    Toast.makeText(this, R.string.camera_perms_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.camera_perms_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }

            case REQUEST_EXTERNAL_STORAGE_PERMISSION:
                if (hasAllPermissionsGranted(grantResults)) {
                    Toast.makeText(this, R.string.storage_perms_granted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.storage_perms_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainMenu.this,
                        new String[]{
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        }, BLUETOOTH_PERMISSION_CODE);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.CAMERA
                    }, REQUEST_CAMERA_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, REQUEST_EXTERNAL_STORAGE_PERMISSION);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("SICILIAN");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_available,
                    Toast.LENGTH_LONG).show();
            finish();
        }
        ((BluetoothApp)this.getApplicationContext()).mChatService.setHandler(mHandler);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_DEVICE_NAME){
                String mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(MainMenu.this, "Connected to "
                        + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public synchronized void onResume() {
        super.onResume();
        if (((BluetoothApp)this.getApplicationContext()).mChatService != null) {
            if (((BluetoothApp)this.getApplicationContext()).mChatService.getState() == BluetoothService.STATE_NONE) {
                ((BluetoothApp)this.getApplicationContext()).mChatService.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (((BluetoothApp)this.getApplicationContext()).mChatService != null)
            ((BluetoothApp)this.getApplicationContext()).mChatService.stop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    @SuppressWarnings("ConstantConditions")
                    String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    ((BluetoothApp)this.getApplicationContext()).mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, R.string.bt_enabled, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    public void ensureDiscoverable(View v) {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
            startActivity(discoverableIntent);
        }
    }

    public void connect(View v) {
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    public void startActivity(View v){
        if(v.getId() == R.id.rc) {
            Intent i = new Intent(MainMenu.this, ChessClock.class);
            startActivity(i);
        }
        if(v.getId() == R.id.cam) {
            Intent i = new Intent(MainMenu.this, ChessCamera.class);
            startActivity(i);
        }
    }
}
