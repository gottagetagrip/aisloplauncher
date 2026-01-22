package com.example.myownlauncher.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HIDDEN_APPS = "hidden_apps"
        private const val KEY_APP_CUSTOMIZATIONS = "app_customizations"
        private const val KEY_GESTURES = "gestures"
        private const val KEY_SHOW_APP_ICONS = "show_app_icons"
        private const val KEY_TEXT_SIZE = "text_size"
        private const val KEY_SORT_MODE = "sort_mode"
    }

    // Hidden Apps Management
    fun getHiddenApps(): Set<String> {
        return prefs.getStringSet(KEY_HIDDEN_APPS, emptySet()) ?: emptySet()
    }

    fun hideApp(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        hidden.add(packageName)
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
    }

    fun unhideApp(packageName: String) {
        val hidden = getHiddenApps().toMutableSet()
        hidden.remove(packageName)
        prefs.edit().putStringSet(KEY_HIDDEN_APPS, hidden).apply()
    }

    fun isAppHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    // App Customization (colors, labels)
    fun saveAppCustomization(customization: AppCustomization) {
        val customizations = getAppCustomizations().toMutableMap()
        customizations[customization.packageName] = customization
        val json = gson.toJson(customizations)
        prefs.edit().putString(KEY_APP_CUSTOMIZATIONS, json).apply()
    }

    fun getAppCustomization(packageName: String): AppCustomization? {
        return getAppCustomizations()[packageName]
    }

    fun getAppCustomizations(): Map<String, AppCustomization> {
        val json = prefs.getString(KEY_APP_CUSTOMIZATIONS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, AppCustomization>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    fun deleteAppCustomization(packageName: String) {
        val customizations = getAppCustomizations().toMutableMap()
        customizations.remove(packageName)
        val json = gson.toJson(customizations)
        prefs.edit().putString(KEY_APP_CUSTOMIZATIONS, json).apply()
    }

    // Gesture Configuration
    fun saveGestureConfig(config: GestureConfig) {
        val json = gson.toJson(config)
        prefs.edit().putString(KEY_GESTURES, json).apply()
    }

    fun getGestureConfig(): GestureConfig {
        val json = prefs.getString(KEY_GESTURES, null) ?: return GestureConfig()
        return gson.fromJson(json, GestureConfig::class.java) ?: GestureConfig()
    }

    // Display Settings
    fun setShowAppIcons(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_APP_ICONS, show).apply()
    }

    fun shouldShowAppIcons(): Boolean {
        return prefs.getBoolean(KEY_SHOW_APP_ICONS, false)
    }

    fun setTextSize(size: Float) {
        prefs.edit().putFloat(KEY_TEXT_SIZE, size).apply()
    }

    fun getTextSize(): Float {
        return prefs.getFloat(KEY_TEXT_SIZE, 16f)
    }

    // Sort Mode
    fun setSortMode(mode: SortMode) {
        prefs.edit().putString(KEY_SORT_MODE, mode.name).apply()
    }

    fun getSortMode(): SortMode {
        val mode = prefs.getString(KEY_SORT_MODE, SortMode.ALPHABETICAL.name)
        return SortMode.valueOf(mode ?: SortMode.ALPHABETICAL.name)
    }
}

enum class SortMode {
    ALPHABETICAL,
    MOST_USED,
    RECENTLY_USED,
    CUSTOM
}