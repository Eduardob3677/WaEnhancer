package com.wmods.wppenhacer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rikka.material.app.LocaleDelegate;

/**
 * WaEnhancer Application class.
 * <p>
 * Handles global application initialization including theme mode selection.
 * <p>
 * Theme Mode Implementation (Material3 / Material Design):
 * <ul>
 *   <li>Mode 0 (Auto): Follows system theme using AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
 *       - On Android 10+ (API 29): Respects system-wide dark mode setting
 *       - On Android 12+ (API 31): Also supports dynamic colors from wallpaper
 *       - On Android 16+: Full Material You support with predictable back animations</li>
 *   <li>Mode 1 (Dark): Forces dark theme using AppCompatDelegate.MODE_NIGHT_YES</li>
 *   <li>Mode 2 (Light): Forces light theme using AppCompatDelegate.MODE_NIGHT_NO</li>
 * </ul>
 * <p>
 * The theme preference is stored in SharedPreferences under key "thememode".
 * When the preference changes, BasePreferenceFragment.chanceStates() calls setThemeMode().
 * <p>
 * Resource qualifiers (-night) are used to provide theme-specific resources:
 * <ul>
 *   <li>values/styles.xml: AppTheme inherits from Theme.Light</li>
 *   <li>values-night/theme.xml: AppTheme inherits from Theme (dark)</li>
 * </ul>
 *
 * @see BaseActivity for theme application in activities
 * @see com.wmods.wppenhacer.ui.fragments.base.BasePreferenceFragment for theme preference handling
 */
public class App extends Application {

    private static App instance;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final Handler MainHandler = new Handler(Looper.getMainLooper());

    public static void showRequestStoragePermission(Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        builder.setTitle(R.string.storage_permission);
        builder.setMessage(R.string.permission_storage);
        builder.setPositiveButton(R.string.allow, (dialog, which) -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
                activity.startActivity(intent);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });
        builder.setNegativeButton(R.string.deny, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @SuppressLint("ApplySharedPref")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        var sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        var mode = Integer.parseInt(sharedPreferences.getString("thememode", "0"));
        // Apply theme mode on app startup - Material3 theme will be applied based on this setting
        setThemeMode(mode);
        changeLanguage(this);
    }

    /**
     * Sets the application-wide theme mode using AppCompatDelegate.
     * <p>
     * This method controls the day/night theme switching for the entire app.
     * Material3 themes are automatically applied based on the selected mode:
     * <ul>
     *   <li>Mode 0: Follow system - respects Android system dark mode setting</li>
     *   <li>Mode 1: Dark mode - always uses dark Material3 theme</li>
     *   <li>Mode 2: Light mode - always uses light Material3 theme</li>
     * </ul>
     * <p>
     * On Android 12+, Material You dynamic colors are automatically applied
     * regardless of the theme mode selection.
     *
     * @param mode Theme mode: 0 = follow system, 1 = dark, 2 = light
     */
    public static void setThemeMode(int mode) {
        switch (mode) {
            case 0:
                // Auto/System mode: Theme follows system dark mode setting
                // On Android 10+: Uses system-wide dark mode
                // On Android 12+: Also supports Material You dynamic colors
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                // Dark mode: Always use dark theme
                // Uses values-night/ resources and dark Material3 colors
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                // Light mode: Always use light theme
                // Uses values/ resources and light Material3 colors
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }


    public static App getInstance() {
        return instance;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Handler getMainHandler() {
        return MainHandler;
    }


    public void restartApp(String packageWpp) {
        Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".WHATSAPP.RESTART");
        intent.putExtra("PKG", packageWpp);
        sendBroadcast(intent);
    }

    public static void changeLanguage(Context context) {
        var force = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("force_english", false);
        LocaleDelegate.setDefaultLocale(force ? Locale.ENGLISH : Locale.getDefault());
        var res = context.getResources();
        var config = res.getConfiguration();
        config.setLocale(LocaleDelegate.getDefaultLocale());
        //noinspection deprecation
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    public static File getWaEnhancerFolder() {
        var download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        var waEnhancerFolder = new File(download, "WaEnhancer");
        if (!waEnhancerFolder.exists()) waEnhancerFolder.mkdirs();
        return waEnhancerFolder;
    }

    public static boolean isOriginalPackage() {
        //noinspection ConstantValue
        return BuildConfig.APPLICATION_ID.equals("com.wmods.wppenhacer");
    }

}
