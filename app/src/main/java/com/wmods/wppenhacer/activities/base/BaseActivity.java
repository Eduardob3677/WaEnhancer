package com.wmods.wppenhacer.activities.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wmods.wppenhacer.R;

/**
 * Base Activity for all WaEnhancer activities.
 * <p>
 * Handles Material3 theme application with the following theme layers:
 * <ol>
 *   <li>AppTheme: Base theme (Light or Dark depending on system/preference)
 *       - Day mode: Theme.Light -> Material3.DynamicColors.Light.Rikka
 *       - Night mode: Theme -> Material3.DynamicColors.Dark.Rikka</li>
 *   <li>Rikka Material3 Preference overlay: Styling for preference screens</li>
 *   <li>ThemeOverlay: Custom preference styling (dividers, etc.)</li>
 *   <li>ThemeOverlay.MaterialGreen: Green accent color from Material Theme Builder</li>
 * </ol>
 * <p>
 * Theme Mode Selection:
 * <ul>
 *   <li>The actual theme mode (light/dark/auto) is set in App.java using AppCompatDelegate</li>
 *   <li>Android's resource qualifiers (-night) automatically select the correct theme variant</li>
 *   <li>Dynamic colors (Material You) are enabled on Android 12+ through the theme hierarchy</li>
 * </ul>
 * <p>
 * Material3 Features:
 * <ul>
 *   <li>Dynamic colors from wallpaper (Android 12+)</li>
 *   <li>Automatic dark/light theme switching</li>
 *   <li>Material3 typography and color tokens</li>
 *   <li>Tonal elevation and surface colors</li>
 * </ul>
 *
 * @see com.wmods.wppenhacer.App#setThemeMode(int) for theme mode switching
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Apply Material3 theme stack
        // 1. Base theme: AppTheme - uses Material3 DynamicColors with Rikka compatibility
        //    In light mode: inherits from Theme.Light
        //    In dark mode: inherits from Theme (dark) via -night qualifier
        setTheme(R.style.AppTheme);

        // 2. Rikka Material3 Preference overlay - provides Material3 styling for preferences
        getTheme().applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true);

        // 3. Custom overlay - removes dividers and customizes preference appearance
        getTheme().applyStyle(R.style.ThemeOverlay, true);

        // 4. Material Green overlay - applies green accent from Material Theme Builder
        //    Uses Light or Dark variant based on current theme mode
        getTheme().applyStyle(R.style.ThemeOverlay_MaterialGreen, true);

        super.onCreate(savedInstanceState);
    }
}
