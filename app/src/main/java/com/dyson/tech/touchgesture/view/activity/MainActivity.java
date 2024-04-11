package com.dyson.tech.touchgesture.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dyson.tech.touchgesture.R;
import com.dyson.tech.touchgesture.data.SettingSharedPref;
import com.dyson.tech.touchgesture.utils.ChangeScreen;
import com.dyson.tech.touchgesture.view.fragment.AppsOnDeviceFragment;
import com.dyson.tech.touchgesture.view.fragment.GesturesListFragment;
import com.dyson.tech.touchgesture.view.fragment.HomeFragment;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    @SuppressLint("StaticFieldLeak")
    private static LinearLayout toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        ChangeScreen.init().replace(MainActivity.this, new HomeFragment(), false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        FragmentManager manager = getSupportFragmentManager();
        Fragment currentFragment = manager.findFragmentById(R.id.main_layout);
        setToolBar(!(currentFragment instanceof GesturesListFragment));

        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initWindow();
        SettingSharedPref settingSharedPref = SettingSharedPref.getInstance(this);
        String language = settingSharedPref.getLanguage();
        if (language != null) {
            setLanguage(language);
        } else {
            setLanguage("en");
        }
        initItem();

    }

    private void setLanguage(String languageCode) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    private void initWindow() {
        Window window = getWindow();
        //Full screen + lock screen + always-on display
        window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.layoutInDisplayCutoutMode = WindowManager
                    .LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }
        window.setStatusBarColor(getResources().getColor(R.color.orange));
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void initItem() {
        ImageView imgMenu = findViewById(R.id.img_menu);
        drawerLayout = findViewById(R.id.navigation_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        toolBar = findViewById(R.id.tool_bar);

        imgMenu.setOnClickListener(view -> {
            drawerLayout.open();
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_rate_app:
                clickRateApp();
                break;
            case R.id.nav_share:
                clickShareApp();
                break;
            case R.id.nav_contact:
                sendEmail();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void clickRateApp() {
        try {
            Log.e("DEBUG", "clickRateApp: "+getPackageName() );
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void clickShareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plan");
        String body = "Download this app";
        String sub = "https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_TEXT, sub);
        startActivity(Intent.createChooser(intent, "Share using"));
    }

    private void sendEmail(){
        Intent intent= new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL,new String[]{"dysontechnology1010@gmail.com"});
        intent.setType("message/rfc822");
        startActivity(Intent.createChooser(intent,"Choose email client: "));
    }

    public static void setToolBar(boolean isShown) {
        if (isShown) {
            toolBar.setVisibility(View.VISIBLE);
        } else {
            toolBar.setVisibility(View.GONE);
        }
    }

}