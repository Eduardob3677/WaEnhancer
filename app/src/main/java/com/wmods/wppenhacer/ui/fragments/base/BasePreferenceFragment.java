package com.wmods.wppenhacer.ui.fragments.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.wmods.wppenhacer.App;
import com.wmods.wppenhacer.BuildConfig;
import com.wmods.wppenhacer.xposed.utils.Utils;

import java.util.Objects;

import rikka.material.preference.MaterialSwitchPreference;

/**
 * Base PreferenceFragment for all preference screens in WaEnhancer.
 * <p>
 * Handles preference changes including theme mode selection.
 * <p>
 * Theme Mode Handling:
 * When the "thememode" preference changes, this fragment calls App.setThemeMode()
 * to update the AppCompatDelegate night mode setting. This triggers an automatic
 * recreation of activities to apply the new theme.
 * <p>
 * Material3 Integration:
 * - Uses rikka.material.preference for Material3-styled preferences
 * - Theme overlays are applied in BaseActivity.onCreate()
 * - Dynamic colors are supported on Android 12+ through the theme hierarchy
 *
 * @see com.wmods.wppenhacer.App#setThemeMode(int)
 * @see com.wmods.wppenhacer.activities.base.BaseActivity
 */
public abstract class BasePreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected SharedPreferences mPrefs;

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        mPrefs.registerOnSharedPreferenceChangeListener(this);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                    getParentFragmentManager().popBackStack();
                } else {
                    requireActivity().finish();
                }
            }
        });
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        chanceStates(null);
        monitorPreference();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
        Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".MANUAL_RESTART");
        App.getInstance().sendBroadcast(intent);
        chanceStates(s);
    }

    private void setPreferenceState(String key, boolean enabled) {
        var pref = findPreference(key);
        if (pref != null) {
            pref.setEnabled(enabled);
            if (pref instanceof MaterialSwitchPreference && !enabled) {
                ((MaterialSwitchPreference) pref).setChecked(false);
            }
        }
    }

    private void monitorPreference() {
        var downloadstatus = (MaterialSwitchPreference) findPreference("downloadstatus");

        if (downloadstatus != null) {
            downloadstatus.setOnPreferenceChangeListener((preference, newValue) -> checkStoragePermission(newValue));
        }

        var downloadviewonce = (MaterialSwitchPreference) findPreference("downloadviewonce");
        if (downloadviewonce != null) {
            downloadviewonce.setOnPreferenceChangeListener((preference, newValue) -> checkStoragePermission(newValue));
        }
    }

    private boolean checkStoragePermission(Object newValue) {
        if (newValue instanceof Boolean && (Boolean) newValue) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) || (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                App.showRequestStoragePermission(requireActivity());
                return false;
            }
        }
        return true;
    }

    @SuppressLint("ApplySharedPref")
    private void chanceStates(String key) {

        var lite_mode = mPrefs.getBoolean("lite_mode", false);

        if (lite_mode) {
            setPreferenceState("wallpaper", false);
            setPreferenceState("custom_filters", false);
        }

        // Handle theme mode changes
        // Material3 Theme Mode Selection:
        // - Mode 0: Follow system theme (uses MODE_NIGHT_FOLLOW_SYSTEM)
        // - Mode 1: Force dark theme (uses MODE_NIGHT_YES)
        // - Mode 2: Force light theme (uses MODE_NIGHT_NO)
        // On Android 12+, dynamic colors are automatically applied regardless of mode
        if (Objects.equals(key, "thememode")) {
            var mode = Integer.parseInt(mPrefs.getString("thememode", "0"));
            App.setThemeMode(mode);
        }

        if (Objects.equals(key, "force_english")) {
            mPrefs.edit().commit();
            Utils.doRestart(requireContext());
        }

        var igstatus = mPrefs.getBoolean("igstatus", false);
        setPreferenceState("oldstatus", !igstatus);

        var oldstatus = mPrefs.getBoolean("oldstatus", false);
        setPreferenceState("verticalstatus", !oldstatus);
        setPreferenceState("channels", !oldstatus);
        setPreferenceState("removechannel_rec", !oldstatus);
        setPreferenceState("status_style", !oldstatus);
        setPreferenceState("igstatus", !oldstatus);

        var channels = mPrefs.getBoolean("channels", false);
        setPreferenceState("removechannel_rec", !channels && !oldstatus);

        var freezelastseen = mPrefs.getBoolean("freezelastseen", false);
        setPreferenceState("show_freezeLastSeen", !freezelastseen);
        setPreferenceState("showonlinetext", !freezelastseen);
        setPreferenceState("dotonline", !freezelastseen);


        var separategroups = mPrefs.getBoolean("separategroups", false);
        setPreferenceState("filtergroups", !separategroups);

        var filtergroups = mPrefs.getBoolean("filtergroups", false);
        setPreferenceState("separategroups", !filtergroups);


        var callBlockContacts = findPreference("call_block_contacts");
        var callWhiteContacts = findPreference("call_white_contacts");
        if (callBlockContacts != null && callWhiteContacts != null) {
            var callType = Integer.parseInt(mPrefs.getString("call_privacy", "0"));
            switch (callType) {
                case 3:
                    callBlockContacts.setEnabled(true);
                    callWhiteContacts.setEnabled(false);
                    break;
                case 4:
                    callWhiteContacts.setEnabled(true);
                    callBlockContacts.setEnabled(false);
                    break;
                default:
                    callWhiteContacts.setEnabled(false);
                    callBlockContacts.setEnabled(false);
                    break;
            }

        }
    }

    public void setDisplayHomeAsUpEnabled(boolean enabled) {
        if (getActivity() == null) return;
        var actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(enabled);
        }
    }
}
