package com.dyson.tech.touchgesture.presenter;

import static android.content.Context.WIFI_SERVICE;
import static com.dyson.tech.touchgesture.presenter.ItemsFeaturePresenter.ActionActivity.KEY_FOR_SETTING;
import static com.dyson.tech.touchgesture.presenter.ItemsFeaturePresenter.ActionActivity.VALUE_LOCK_ROTATION;
import static com.dyson.tech.touchgesture.presenter.ItemsFeaturePresenter.ActionActivity.VALUE_MANAGER_WRITE_SETTING;
import static com.dyson.tech.touchgesture.presenter.ItemsFeaturePresenter.ActionActivity.VALUE_OPEN_ROTATION;

import android.Manifest;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dyson.tech.touchgesture.R;

public class ItemsFeaturePresenter {
    public final static int TURN_ON_BLUETOOTH = 1;
    public final static int TURN_OFF_BLUETOOTH = 0;

    private final Context context;
    private Intent blueToothIntent;

    public ItemsFeaturePresenter(Context context) {
        this.context = context;
    }

    public boolean isModeDeviceChangeGranted() {
        NotificationManager n = (NotificationManager) context
                .getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                n.isNotificationPolicyAccessGranted();
    }

    public void giveModeDevicePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(intent);
            } catch (ActivityNotFoundException exception) {
                exception.printStackTrace();
            }
        }
    }

    public void execChangeMode(int mode) {
        if (!isModeDeviceChangeGranted()) {
            giveModeDevicePermission();
            return;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mode == AudioManager.RINGER_MODE_SILENT) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
        } else if (mode == AudioManager.RINGER_MODE_VIBRATE) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
        } else if (mode == AudioManager.RINGER_MODE_NORMAL) {
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }
    }


    public void backToHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startMain);
    }


    public void actionBluetooth(BluetoothAdapter adapter, int status) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,
                    context.getString(R.string.please_get_bluetooth_connect_permission_in_permission_list),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (status == TURN_ON_BLUETOOTH) {
            if (!adapter.isEnabled()) {
                blueToothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                blueToothIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
                context.startActivity(blueToothIntent);
            }
        } else if (status == TURN_OFF_BLUETOOTH) {
            if (adapter.isEnabled())
                adapter.disable();
        }
    }

    public void actionVolume(int volumeType) {
        AudioManager audioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                volumeType, AudioManager.FLAG_SHOW_UI);
    }

    public void actionFlash(boolean isEnabled) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.setTorchMode(cameraManager.getCameraIdList()[0], isEnabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void intentToGPS() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    public void Rotation() {
        if (Settings.System.canWrite(context.getApplicationContext())) {
            Intent intent_Rotation = new Intent(context, ActionActivity.class);
            intent_Rotation.putExtra(KEY_FOR_SETTING, VALUE_OPEN_ROTATION);
            intent_Rotation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent_Rotation);
        } else {
            openAndroidPermissionsMenu();
        }
    }

    public void LockRotation() {
        if (Settings.System.canWrite(context.getApplicationContext())) {
            Intent intentLockRotation = new Intent(context, ActionActivity.class);
            intentLockRotation.putExtra(KEY_FOR_SETTING, VALUE_LOCK_ROTATION);
            intentLockRotation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentLockRotation);
        } else {
            openAndroidPermissionsMenu();
        }
    }

    private void openAndroidPermissionsMenu() {
        Intent intent = new Intent(context, ActionActivity.class);
        intent.putExtra(KEY_FOR_SETTING, VALUE_MANAGER_WRITE_SETTING);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void changeWifiStatus(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
           WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
            wifiManager.setWifiEnabled(!wifiManager.isWifiEnabled());
        } else {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            panelIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
            context.startActivity(panelIntent);
        }
    }

    public static class ActionActivity extends AppCompatActivity {

        public static final String KEY_FOR_SETTING = "setting_manager";
        private static final int DEFAULT_VALUE = 0;
        public static final int VALUE_LOCK_ROTATION = 2;
        public static final int VALUE_OPEN_ROTATION = 3;
        public static final int VALUE_MANAGER_WRITE_SETTING = 4;

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            startIntent();
            finish();
        }

        private void startIntent() {
            switch (getValueKey()) {
                case VALUE_LOCK_ROTATION:
                    setRotationScreenFromSettings(ActionActivity.this, false);
                    break;
                case VALUE_OPEN_ROTATION:
                    setRotationScreenFromSettings(ActionActivity.this, true);
                    break;
                case VALUE_MANAGER_WRITE_SETTING:
                    intentToManagerWriteSetting();
                    break;
            }
        }

        private int getValueKey() {
            Intent intent = getIntent();
            return intent.getIntExtra(KEY_FOR_SETTING, DEFAULT_VALUE);
        }

        public void setRotationScreenFromSettings(Context context, boolean enabled) {

            try {
                if (Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) == 1) {
                    Display defaultDisplay = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    Settings.System.putInt(context.getContentResolver(), Settings.System.USER_ROTATION, defaultDisplay.getRotation());
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                } else {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
                }

                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, enabled ? 1 : 0);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
        }

        private void intentToManagerWriteSetting() {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getApplication().getPackageName()));
            startActivity(intent);
        }
    }
}
