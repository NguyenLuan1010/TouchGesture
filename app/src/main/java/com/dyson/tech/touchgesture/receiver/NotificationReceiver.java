package com.dyson.tech.touchgesture.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.service.HomeButtonService;
import com.dyson.tech.touchgesture.service.ServiceBuilder;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(ServiceBuilder.ACTION_CLICK, 0);
        if (action == R.id.tv_close_button) {
            context.stopService(new Intent(context.getApplicationContext(),
                    HomeButtonService.class));
        }
    }
}
