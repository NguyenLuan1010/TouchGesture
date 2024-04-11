package com.dyson.tech.touchgesture.service;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.GestureOverlayView;
import android.graphics.PixelFormat;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.adapter.TodayNotesAdapter;
import com.dyson.tech.touchgesture.data.GestureFilesHelper;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.presenter.ItemsFeaturePresenter;
import com.dyson.tech.touchgesture.receiver.NotificationReceiver;
import com.dyson.tech.touchgesture.view.activity.MainActivity;
import com.dyson.tech.touchgesture.view.fragment.HomeFragment;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class ServiceBuilder implements LifecycleObserver {
    public static final String ACTION_CLICK = "ACTION_CLICK";
    private final Service service;
    private final ItemsFeaturePresenter itemsFeature;

    public ServiceBuilder(Service service) {
        this.service = service;
        itemsFeature = new ItemsFeaturePresenter(service);
    }

    ////////////////////////////////////BUILD NOTIFICATION////////////////////////////////////////

    public void buildNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel();
        }
        notificationControl();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationChannel() {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(service.getString(R.string.package_app),
                service.getString(R.string.app_name), importance);

        NotificationManager notificationManager = service.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private void notificationControl() {
        Intent intent = new Intent(service, HomeFragment.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(service, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        @SuppressLint("RemoteViewLayout")
        RemoteViews notificationLayout = new RemoteViews(service.getPackageName(),
                R.layout.custom_notification);

        notificationLayout.setOnClickPendingIntent(R.id.tv_close_button,
                onButtonNotificationClick(R.id.tv_close_button));

        setNotificationAttribute(pendingIntent, notificationLayout);
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent onButtonNotificationClick(@IdRes int id) {

        Intent intent = new Intent(service, NotificationReceiver.class);

        intent.putExtra(ACTION_CLICK, id);

        return PendingIntent
                .getBroadcast(service, id, intent, PendingIntent.FLAG_IMMUTABLE);
    }

    private void setNotificationAttribute(PendingIntent pendingIntent,
                                          RemoteViews notificationLayout) {

        Notification
                notification = new NotificationCompat.Builder(service.getApplicationContext(),
                service.getString(R.string.package_app))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCustomContentView(notificationLayout)
                .build();

        notification.flags = Notification.FLAG_NO_CLEAR;

        NotificationManager notificationManager =
                (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification);
        service.startForeground(1, notification);
    }

    ////////////////////////////////////BUILD HOME BUTTON LAYOUT////////////////////////////////////

    private int LAYOUT_FLAG;
    private final int TYPE_ICON = 0;
    private final int TYPE_FIRST_MENU_TASK = 1;
    private final int TYPE_GESTURE_VIEW = 2;
    private final int TYPE_SECOND_MENU_TASK = 3;
    private final int TYPE_ADD_NOTE_TASK = 4;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams paramsIcon;
    private View homeIcon;
    private View secondMenuTask;
    private View drawGestureView;
    private int currentState;
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

    public void buildHomeButton() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            mWindowManager = service.getSystemService(WindowManager.class);
        } else {
            mWindowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);
        }
        setViewForButton();
        showHomeButton();
        setActionMoveView();
    }

    @SuppressLint({"RtlHardcoded", "InflateParams"})
    private void setViewForButton() {
        homeIcon = LayoutInflater.from(service).inflate(R.layout.home_button, null);
        addButtonToWindow();

        paramsIcon.gravity = Gravity.TOP | Gravity.LEFT;
        paramsIcon.x = 50;
        paramsIcon.y = 300;
    }

    private void addButtonToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        paramsIcon = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    @SuppressLint("ResourceType")
    private void showHomeButton() {

        if (currentState == TYPE_FIRST_MENU_TASK) {
            mWindowManager.removeView(firstMenuTask);
        } else if (currentState == TYPE_GESTURE_VIEW) {
            mWindowManager.removeView(drawGestureView);
        } else if (currentState == TYPE_SECOND_MENU_TASK) {
            mWindowManager.removeView(secondMenuTask);
        }

//        else if (currentState == TYPE_ADD_NOTE_TASK) {
//            mWindowManager.removeView(notesView);
//        }

        if (!homeIcon.isShown()) {
            SettingSharedPref mSetting = SettingSharedPref.getInstance(service);

            AppCompatImageView buttonImage = homeIcon.findViewById(R.id.img_home_button);
            buttonImage.setImageResource(mSetting.getBtnHomeTheme());
            Log.e("DEBUG", "showHomeButton: "+ mSetting.getBtnHomeTheme());
            mSetting.setOnBtnHomeChangeListener(value -> {
                if (currentState == TYPE_ICON)
                    mWindowManager.removeView(homeIcon);

                setViewForButton();
                showHomeButton();
                setActionMoveView();
            });

            mWindowManager.addView(homeIcon, paramsIcon);
            currentState = TYPE_ICON;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setActionMoveView() {
        homeIcon.findViewById(R.id.root_container)
                .setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            actionDown(event);
                            return true;
                        case MotionEvent.ACTION_UP:
                            actionUp(event);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            moveAction(event);
                            return true;
                    }

                    return false;
                });
    }

    private void actionDown(MotionEvent event) {
        //remember the initial position.
        initialX = paramsIcon.x;
        initialY = paramsIcon.y;

        //get the touch location
        initialTouchX = event.getRawX();
        initialTouchY = event.getRawY();
    }

    private void actionUp(MotionEvent event) {
        int Xdiff = (int) (event.getRawX() - initialTouchX);
        int Ydiff = (int) (event.getRawY() - initialTouchY);

        if (Xdiff < 10 && Ydiff < 10) {
            showFirstMenuTask();
            clickOutsideFirstMenu();
        }

    }

    private void moveAction(MotionEvent event) {
        //Calculate the X and Y coordinates of the view.
        paramsIcon.x = initialX + (int) (event.getRawX() - initialTouchX);
        paramsIcon.y = initialY + (int) (event.getRawY() - initialTouchY);

        //Update the layout with new X & Y coordinate
        mWindowManager.updateViewLayout(homeIcon, paramsIcon);
    }

    ////////////////////////////////////BUILD FIRST MENU LAYOUT////////////////////////////////////

    private WindowManager.LayoutParams paramsFirstMenuTask;
    private View firstMenuTask;
    private LinearLayout btnLock,
            btnGestureView,
            btnRingNormal,
            btnRingSilent,
            btnRingRang,
            btnSetting,
            btnHome;

    public void buildFirstMenu() {
        firstMenuTask = LayoutInflater.from(service).inflate(R.layout.first_menu_items, null);
        initFirstMenuItem();
        getStatusMode();
        addFirstMenuToWindow();
    }

    private void initFirstMenuItem() {
        btnHome = firstMenuTask.findViewById(R.id.Home_Button);
        btnHome.setOnClickListener(onClickOnFirstMenu);
        btnGestureView = firstMenuTask.findViewById(R.id.Gesture_View);
        btnGestureView.setOnClickListener(onClickOnFirstMenu);
        btnLock = firstMenuTask.findViewById(R.id.notes_setting_layout);
        btnLock.setOnClickListener(onClickOnFirstMenu);
        btnRingRang = firstMenuTask.findViewById(R.id.Ring_Rang);
        btnRingRang.setOnClickListener(onClickOnFirstMenu);
        btnRingNormal = firstMenuTask.findViewById(R.id.Ring_Normal);
        btnRingNormal.setOnClickListener(onClickOnFirstMenu);
        btnRingSilent = firstMenuTask.findViewById(R.id.Ring_Silent);
        btnRingSilent.setOnClickListener(onClickOnFirstMenu);
        btnSetting = firstMenuTask.findViewById(R.id.Setting);
        btnSetting.setOnClickListener(onClickOnFirstMenu);
    }

    private void addFirstMenuToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        paramsFirstMenuTask = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }


    private void showFirstMenuTask() {
        if (currentState == TYPE_ICON) {
            mWindowManager.removeView(homeIcon);
        } else if (currentState == TYPE_GESTURE_VIEW) {
            mWindowManager.removeView(drawGestureView);
        } else if (currentState == TYPE_SECOND_MENU_TASK) {
            mWindowManager.removeView(secondMenuTask);
        }

//        else if (currentState == TYPE_ADD_NOTE_TASK) {
//            mWindowManager.removeView(notesView);
//        }

        if (!firstMenuTask.isShown()) {
            mWindowManager.addView(firstMenuTask, paramsFirstMenuTask);
            currentState = TYPE_FIRST_MENU_TASK;
        }
        getBlueToothStatus();
        getFlashStatus();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void clickOutsideFirstMenu() {
        firstMenuTask.findViewById(R.id.first_menu_task).setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_OUTSIDE:
                    getStatusMode();
//                    buildItemsTasks_presenter.CheckLocationStatus();
                    showHomeButton();

                    break;
            }
            return false;
        });
    }

    View.OnClickListener onClickOnFirstMenu = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.Home_Button:
                    itemsFeature.backToHomeScreen();
                    showHomeButton();
                    break;
                case R.id.Gesture_View:
                    showDrawGestureView();
                    clickOutSideDrawGestureView();
                    break;
                case R.id.notes_setting_layout:
                    Intent intent = new Intent(service.getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    service.startActivity(intent);
                    showHomeButton();
//                    showAddNoteTask();
//                    clickOutsideAddNote();
                    break;
                case R.id.Ring_Rang:
                    itemsFeature.execChangeMode(AudioManager.RINGER_MODE_SILENT);
                    getStatusMode();
                    break;
                case R.id.Ring_Normal:
                    itemsFeature.execChangeMode(AudioManager.RINGER_MODE_VIBRATE);
                    getStatusMode();
                    break;
                case R.id.Ring_Silent:
                    itemsFeature.execChangeMode(AudioManager.RINGER_MODE_NORMAL);
                    getStatusMode();
                    break;
                case R.id.Setting:
                    showSecondMenuTask();
                    clickOutSideSecondMenuTasks();
                    break;
            }
        }
    };

    private void getStatusMode() {
        AudioManager audioManager = (AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {

            btnRingNormal.setVisibility(View.GONE);
            btnRingRang.setVisibility(View.GONE);
            btnRingSilent.setVisibility(View.VISIBLE);

        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {

            btnRingNormal.setVisibility(View.GONE);
            btnRingRang.setVisibility(View.VISIBLE);
            btnRingSilent.setVisibility(View.GONE);

        } else if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {

            btnRingNormal.setVisibility(View.VISIBLE);
            btnRingRang.setVisibility(View.GONE);
            btnRingSilent.setVisibility(View.GONE);

        }
    }

//    ////////////////////////////////////BUILD ADD NOTE LAYOUT///////////////////////////////////////
//    private WindowManager.LayoutParams paramsAddNoteTask;
//    private View notesView;
//
//    public void buildAddNote() {
//        notesView = LayoutInflater.from(service).inflate(R.layout.add_note_view, null);
//        addAddNoteWindow();
//        initAddNoteView();
//    }
//
//    private void addAddNoteWindow() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
//        } else {
//            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
//        }
//
//        paramsAddNoteTask = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.MATCH_PARENT,
//                WindowManager.LayoutParams.WRAP_CONTENT,
//                LAYOUT_FLAG,
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
//                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
//                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
//                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
//                PixelFormat.TRANSLUCENT);
//
//        paramsAddNoteTask.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
//    }
//
//    private void showAddNoteTask() {
//        if (currentState == TYPE_ICON) {
//            mWindowManager.removeView(homeIcon);
//        } else if (currentState == TYPE_GESTURE_VIEW) {
//            mWindowManager.removeView(drawGestureView);
//        } else if (currentState == TYPE_SECOND_MENU_TASK) {
//            mWindowManager.removeView(secondMenuTask);
//        } else if (currentState == TYPE_FIRST_MENU_TASK) {
//            mWindowManager.removeView(firstMenuTask);
//        }
//
//        if (!notesView.isShown()) {
//            mWindowManager.addView(notesView, paramsAddNoteTask);
//            currentState = TYPE_ADD_NOTE_TASK;
//        }
//    }
//
//    @SuppressLint("ClickableViewAccessibility")
//    private void clickOutsideAddNote() {
//        notesView.findViewById(R.id.note_layout).setOnTouchListener((v, event) -> {
//
//            switch (event.getAction()) {
//
//                case MotionEvent.ACTION_DOWN:
//                case MotionEvent.ACTION_MOVE:
//                    return false;
//                case MotionEvent.ACTION_OUTSIDE:
//                    showHomeButton();
//                    break;
//            }
//            return false;
//        });
//    }
//
//    RelativeLayout parent;
//
//    private void initAddNoteView() {
//        ListView rcvNotes = notesView.findViewById(R.id.rcv_notes_list);
//
//        LifecycleOwner lifecycleOwner = ProcessLifecycleOwner.get();
//        lifecycleOwner.getLifecycle().addObserver(this);
//
//        parent = notesView.findViewById(R.id.note_layout);
//        LinearLayout note = notesView.findViewById(R.id.note_item_layout);
//
//        NoteDBHelper.init(service).notesDAO().getAllNotes().observe(lifecycleOwner, new Observer<List<Notes>>() {
//            @Override
//            public void onChanged(List<Notes> notes) {
//                for (Notes notes1 : notes) {
//                    parent.addView(note);
//                }
////                NotesCustomAdapter adapter = new NotesCustomAdapter(service,notes);
////                rcvNotes.setAdapter(adapter);
//            }
//        });
//    }
//


    ////////////////////////////////////BUILD SECOND MENU LAYOUT////////////////////////////////////
    private WindowManager.LayoutParams paramsSecondMenuTask;

    private LinearLayout btnWifi, btnTurnOnBluetooth, btnTurnOffBluetooth, btnVolumeUp,
            btnVolumeDown, btnFlashOn, btnFlashOff, btnTurnOnLocation, btnTurnOffLocation,
            btnLockRotation, btnRotation, btnBackToFistMenu, btnFlashEnable;

    private BluetoothAdapter bluetoothAdapter;

    public void buildSecondTaskView() {
        secondMenuTask = LayoutInflater.from(service).inflate(R.layout.second_menu_items, null);
        initSecondMenuItems();
        addSecondMenuToWindow();
    }

    private void initSecondMenuItems() {
        btnWifi = secondMenuTask.findViewById(R.id.Wifi);
        btnWifi.setOnClickListener(onClickOnSecondMenu);
        btnBackToFistMenu = secondMenuTask.findViewById(R.id.BackToFirstMenu);
        btnBackToFistMenu.setOnClickListener(onClickOnSecondMenu);
        btnTurnOnBluetooth = secondMenuTask.findViewById(R.id.TurnOnBluetooth);
        btnTurnOnBluetooth.setOnClickListener(onClickOnSecondMenu);
        btnTurnOffBluetooth = secondMenuTask.findViewById(R.id.TurnOffBluetooth);
        btnTurnOffBluetooth.setOnClickListener(onClickOnSecondMenu);
        btnVolumeDown = secondMenuTask.findViewById(R.id.Volume_Down);
        btnVolumeDown.setOnClickListener(onClickOnSecondMenu);
        btnVolumeUp = secondMenuTask.findViewById(R.id.Volume_Up);
        btnVolumeUp.setOnClickListener(onClickOnSecondMenu);
        btnFlashOff = secondMenuTask.findViewById(R.id.FlashLightOff);
        btnFlashOff.setOnClickListener(onClickOnSecondMenu);
        btnFlashOn = secondMenuTask.findViewById(R.id.FlashLightOn);
        btnFlashOn.setOnClickListener(onClickOnSecondMenu);
        btnFlashEnable = secondMenuTask.findViewById(R.id.FlashLightEnabled);
        btnFlashEnable.setOnClickListener(onClickOnSecondMenu);
        btnTurnOnLocation = secondMenuTask.findViewById(R.id.TurnOnLocation);
        btnTurnOnLocation.setOnClickListener(onClickOnSecondMenu);
        btnTurnOffLocation = secondMenuTask.findViewById(R.id.TurnOffLocation);
        btnTurnOffLocation.setOnClickListener(onClickOnSecondMenu);
        btnRotation = secondMenuTask.findViewById(R.id.Rotation);
        btnRotation.setOnClickListener(onClickOnSecondMenu);
        btnLockRotation = secondMenuTask.findViewById(R.id.Lock_Rotation);
        btnLockRotation.setOnClickListener(onClickOnSecondMenu);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        getBlueToothStatus();
        getFlashStatus();
        hasCamera();
        getLocationStatus();
    }

    private void addSecondMenuToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        paramsSecondMenuTask = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private void showSecondMenuTask() {
        if (currentState == TYPE_FIRST_MENU_TASK) {
            mWindowManager.removeView(firstMenuTask);
        } else if (currentState == TYPE_GESTURE_VIEW) {
            mWindowManager.removeView(drawGestureView);
        } else if (currentState == TYPE_ICON) {
            mWindowManager.removeView(homeIcon);
        }
//        else if (currentState == TYPE_ADD_NOTE_TASK) {
//            mWindowManager.removeView(notesView);
//        }
        if (!secondMenuTask.isShown()) {
            mWindowManager.addView(secondMenuTask, paramsSecondMenuTask);
            currentState = TYPE_SECOND_MENU_TASK;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void clickOutSideSecondMenuTasks() {
        secondMenuTask.findViewById(R.id.second_menu_tasks).setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_OUTSIDE:
                    showHomeButton();
                    break;
            }
            return false;
        });

    }

    private static boolean isFlashActive = false;

    private final View.OnClickListener onClickOnSecondMenu = new View.OnClickListener() {

        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {

            switch (v.getId()) {
                case R.id.Wifi:
                    itemsFeature.changeWifiStatus();
                    showHomeButton();
                    break;
                case R.id.BackToFirstMenu:
                    showFirstMenuTask();
                    break;
                case R.id.TurnOnBluetooth:
                    itemsFeature.actionBluetooth(bluetoothAdapter, ItemsFeaturePresenter.TURN_OFF_BLUETOOTH);
                    showHomeButton();
                    break;
                case R.id.TurnOffBluetooth:
                    itemsFeature.actionBluetooth(bluetoothAdapter, ItemsFeaturePresenter.TURN_ON_BLUETOOTH);
                    showHomeButton();
                    break;
                case R.id.Volume_Down:
                    itemsFeature.actionVolume(AudioManager.ADJUST_LOWER);
                    break;
                case R.id.Volume_Up:
                    itemsFeature.actionVolume(AudioManager.ADJUST_RAISE);
                    break;
                case R.id.FlashLightOff:
                case R.id.FlashLightOn:
                    if (isFlashActive) {
                        btnFlashOff.setVisibility(View.VISIBLE);
                        btnFlashOn.setVisibility(View.GONE);
                        itemsFeature.actionFlash(false);
                        isFlashActive = false;
                    } else {
                        btnFlashOff.setVisibility(View.GONE);
                        btnFlashOn.setVisibility(View.VISIBLE);
                        itemsFeature.actionFlash(true);
                        isFlashActive = true;
                    }
                    break;
                case R.id.FlashLightEnabled:
                    Toast.makeText(service, service.getString(R.string.this_device_not_flash),
                            Toast.LENGTH_LONG).show();
                    showHomeButton();
                    break;
                case R.id.TurnOnLocation:
                case R.id.TurnOffLocation:

                    itemsFeature.intentToGPS();
                    showHomeButton();
                    break;

                case R.id.Rotation:
                    btnRotation.setVisibility(View.GONE);
                    btnLockRotation.setVisibility(View.VISIBLE);
                    itemsFeature.Rotation();
                    break;
                case R.id.Lock_Rotation:
                    btnRotation.setVisibility(View.VISIBLE);
                    btnLockRotation.setVisibility(View.GONE);
                    itemsFeature.LockRotation();
                    break;
            }
        }
    };

    private void getBlueToothStatus() {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                btnTurnOffBluetooth.setVisibility(View.GONE);
                btnTurnOnBluetooth.setVisibility(View.VISIBLE);
            } else {
                btnTurnOffBluetooth.setVisibility(View.VISIBLE);
                btnTurnOnBluetooth.setVisibility(View.GONE);
            }
        }
    }

    private void getFlashStatus() {
        if (isFlashActive) {
            btnFlashOff.setVisibility(View.GONE);
            btnFlashOn.setVisibility(View.VISIBLE);
        } else {
            btnFlashOff.setVisibility(View.VISIBLE);
            btnFlashOn.setVisibility(View.GONE);
        }
    }

    private void hasCamera() {
        if (service.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            if (service.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                btnFlashEnable.setVisibility(View.GONE);
            } else {
                btnFlashEnable.setVisibility(View.VISIBLE);
                btnFlashOn.setVisibility(View.GONE);
                btnFlashOff.setVisibility(View.GONE);
            }
        }
    }

    private void getLocationStatus() {
        LocationManager locationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            return;
        }
        boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            btnTurnOnLocation.setVisibility(View.VISIBLE);
            btnTurnOffLocation.setVisibility(View.GONE);
        } else {
            btnTurnOnLocation.setVisibility(View.GONE);
            btnTurnOffLocation.setVisibility(View.VISIBLE);
        }
    }


    ////////////////////////////////////BUILD SECOND MENU LAYOUT////////////////////////////////////

    private WindowManager.LayoutParams paramGestureView;

    public void buildDrawGestureView() {
        drawGestureView = LayoutInflater.from(service).inflate(R.layout.gesture_view, null);
        initGestureViewItem();
        addGestureViewToWindow();
    }

    private void initGestureViewItem() {
        GestureFilesHelper helper = new GestureFilesHelper(service);
        GestureOverlayView gestureOverlayView = drawGestureView.findViewById(R.id.gesture_view);
        gestureOverlayView.addOnGesturePerformedListener((overlay, gesture) -> {
            helper.checkGestureEqual(gesture, this::showHomeButton);
        });
    }

    private void addGestureViewToWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        paramGestureView = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
    }

    private void showDrawGestureView() {
        if (currentState == TYPE_FIRST_MENU_TASK) {
            mWindowManager.removeView(firstMenuTask);
        } else if (currentState == TYPE_SECOND_MENU_TASK) {
            mWindowManager.removeView(secondMenuTask);
        } else if (currentState == TYPE_ICON) {
            mWindowManager.removeView(homeIcon);
        }
//        else if (currentState == TYPE_ADD_NOTE_TASK) {
//            mWindowManager.removeView(notesView);
//        }
        if (!drawGestureView.isShown()) {
            mWindowManager.addView(drawGestureView, paramGestureView);
            currentState = TYPE_GESTURE_VIEW;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void clickOutSideDrawGestureView() {
        drawGestureView.findViewById(R.id.draw_gesture).setOnTouchListener((v, event) -> {

            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    return false;
                case MotionEvent.ACTION_OUTSIDE:
                    showHomeButton();
                    break;
            }
            return false;
        });

    }

    public void removeViewInWindow() {
        if (homeIcon.isShown()) {
            mWindowManager.removeView(homeIcon);
        }
        if (firstMenuTask.isShown()) {
            mWindowManager.removeView(firstMenuTask);
        }

        if (secondMenuTask.isShown()) {
            mWindowManager.removeView(secondMenuTask);
        }

        if (drawGestureView.isShown()) {
            mWindowManager.removeView(drawGestureView);
        }
    }
}
