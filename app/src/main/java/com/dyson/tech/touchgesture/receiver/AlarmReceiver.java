package com.dyson.tech.touchgesture.receiver;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.IdRes;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.NoteDBHelper;
import com.dyson.tech.touchgesture.model.Notes;
import com.dyson.tech.touchgesture.utils.TimeHelper;
import com.dyson.tech.touchgesture.view.activity.MainActivity;
import com.dyson.tech.touchgesture.view.fragment.HomeFragment;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String NOTES_TRANSFER = "NOTES_TRANSFER";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            int noteId = intent.getIntExtra(NOTES_TRANSFER, -1);
            Notes note = NoteDBHelper.init(context).notesDAO().getNoteById(noteId);
            buildNotification(context, note);
        }
    }

    public void buildNotification(Context context, Notes note) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel(context);
        }
        setNotificationAttribute(context, note);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void notificationChannel(Context context) {
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(context.getString(R.string.package_app),
                context.getString(R.string.app_name), importance);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes. CONTENT_TYPE_SONIFICATION )
                .setUsage(AudioAttributes. USAGE_ALARM )
                .build() ;
        channel.setSound(getSound(context),audioAttributes);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint({"ObsoleteSdkInt", "UnspecifiedImmutableFlag"})
    private void setNotificationAttribute(Context context, Notes note) {

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent
                    .getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent
                    .getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes. CONTENT_TYPE_SONIFICATION )
                .setUsage(AudioAttributes. USAGE_ALARM )
                .build() ;

        Notification
                notification = new NotificationCompat.Builder(context.getApplicationContext(),
                context.getString(R.string.package_app))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(context.getString(R.string.dont_forget_your_task))
                .setContentText(note.getTitle() + " " + context.getString(R.string.at) + " " +
                        TimeHelper.init().getTime(note.getTimeStart(), TimeHelper.TIME_FORMAT))
                .setSound(getSound(context))
                .setSmallIcon(R.drawable.baseline_notifications_active_24)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(2, notification);
    }

    private Uri getSound(Context context) {
        return Uri.parse("android.resource://"
                + context.getApplicationContext().getPackageName()
                + "/" + R.raw.sound_notification);
    }
}
