package com.dyson.tech.touchgesture.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.Prediction;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.model.Apps;
import com.dyson.tech.touchgesture.view.AuthenticationCallBack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GestureFilesHelper {
    private Context context;

    public GestureFilesHelper(Context context) {
        this.context = context;
    }

    public static final String PATH_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String PACKAGE_APP = "com.dyson.tech.touchgesture";
    public static final String FILE_APP = "/Android/data/" + PACKAGE_APP + "/files/";
    public static final String FILE_TYPE = ".txt";

    private File gesFile;

    public void getAllFile(AuthenticationCallBack.GetGestureFilesCallBack callBack) {
        Log.e("DEBUG", "getAllFile: " + Environment.getExternalStorageDirectory().getAbsolutePath());
        File mStoreFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + FILE_APP);
        if (!mStoreFile.exists()) {
            mStoreFile.mkdirs();
        }

        File[] listFiles = mStoreFile.listFiles();
        if (listFiles != null) {

            try {
                List<Apps> appsList = getListGestureApps(listFiles);
                if (appsList != null && appsList.size() > 0) {
                    callBack.allFile(appsList);
                } else {
                    callBack.nothingFile(context.getString(R.string.not_gesture_in_here));
                }
            } catch (PackageManager.NameNotFoundException e) {
                throw new RuntimeException(e);
            }

        } else {
            callBack.nothingFile(context.getString(R.string.not_gesture_in_here));
        }
    }

    private List<Apps> getListGestureApps(File[] files) throws PackageManager.NameNotFoundException {
        List<Apps> appsList = new ArrayList<>();
        for (File f : files) {
            if (f.isFile() && f.getName().contains(FILE_TYPE)) {
                GestureLibrary gestureLib = GestureLibraries.fromFile(f.getAbsolutePath());
                if (gestureLib != null && gestureLib.load()) {
                    for (String pkName : gestureLib.getGestureEntries()) {
                        for (Gesture gesture : gestureLib.getGestures(pkName)) {

                            Drawable iconApp = context.getApplicationContext().getPackageManager()
                                    .getApplicationIcon(pkName);

                            appsList.add(new Apps(iconApp, f.getName(), pkName, gesture));
                        }
                    }
                }
            }
        }
        return appsList;
    }

    public void checkGestureEqual(Gesture gesture,
                                  AuthenticationCallBack.CheckGestureEqualCallBack callBack) {
        File mStoreFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + FILE_APP);
        if (!mStoreFile.exists()) {
            mStoreFile.mkdirs();
        }
        File[] files = mStoreFile.listFiles();
        if (files != null) {
            for (File f : files) {

                if (f.isFile() && f.getName().contains(FILE_TYPE)) {

                    GestureLibrary objGestureLib = GestureLibraries.fromFile(f.getAbsolutePath());
                    objGestureLib.load();

                    List<Prediction> objPrediction = objGestureLib.recognize(gesture);
                    if (objPrediction.size() > 0 && objPrediction.get(0).score > 2.0) {
                        Intent intentLauncher = context.getPackageManager()
                                .getLaunchIntentForPackage(objPrediction.get(0).name);
                        if (intentLauncher != null) {
                            context.startActivity(intentLauncher);
                            callBack.isEqual();
                            break;
                        }
                    }
                }
            }
        }
    }

    public static boolean isExistGestureAppFile(Apps app) {
        File storeFile = new File(PATH_ROOT + FILE_APP);
        File gesFile = new File(storeFile, app.getName() + FILE_TYPE);
        return gesFile.exists();
    }

    public void addToFile(Apps app,
                          AuthenticationCallBack.AddGestureCallBack callBack) {
        File storeFile = new File(PATH_ROOT + FILE_APP);

        String fileName = app.getName().replace(" ", "_");
        gesFile = new File(storeFile, fileName + FILE_TYPE);

        if (!storeFile.exists()) {
            storeFile.mkdirs();
        }

        execCreateGestureFile(app, callBack);
    }

    private void execCreateGestureFile(Apps app,
                                       AuthenticationCallBack.AddGestureCallBack callBack) {
        Gesture gesture = app.getGesture();
        if (gesture == null) {
            callBack.addFail(context.getString(R.string.please_draw_something));
            return;
        }

        FileWriter writer = null;
        try {
            if(!gesFile.createNewFile()){
                Log.e("DEBUG", "Cannot created :"+gesFile.getAbsolutePath() );
            }

            writer = new FileWriter(gesFile);
            Log.e("DEBUG", "execCreateGestureFile: " + gesFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (saveGestureToFile(app, gesFile)) {

            try {
                if (writer != null)
                    writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            callBack.addSuccess(context.getString(R.string.add_gesture_success));
        } else {
            callBack.addFail(context.getString(R.string.something_went_wrong));
        }
    }

    public void editGesture(Apps gestureApps,
                            AuthenticationCallBack.EditGestureFileCallBack callBack) {
        File file = new File(PATH_ROOT + FILE_APP + gestureApps.getName());
        if (clearTheFile(file)) {
            if (gestureApps.getGesture() == null) {
                callBack.editFail(context.getString(R.string.edit_gesture_fail));
                return;
            }
            if (saveGestureToFile(gestureApps, file)) {
                callBack.editSuccess(gestureApps, context.getString(R.string.edit_gesture_success));
            } else {
                callBack.editFail(context.getString(R.string.edit_gesture_fail));
            }
        }
    }

    private boolean saveGestureToFile(Apps app, File file) {
        GestureLibrary gestureLibraries = GestureLibraries.fromFile(file);
        gestureLibraries.addGesture(app.getStrPackage(), app.getGesture());
        return gestureLibraries.save();
    }

    public boolean clearTheFile(File file) {
        try {
            FileWriter fwOb = new FileWriter(file, false);
            PrintWriter pwOb = new PrintWriter(fwOb, false);
            pwOb.flush();
            pwOb.close();
            fwOb.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDeletedFile(Apps gestureApps) {
        File file = new File(PATH_ROOT + FILE_APP + gestureApps.getName());
        return file.delete();
    }
}
