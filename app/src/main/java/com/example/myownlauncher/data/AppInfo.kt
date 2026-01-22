package com.example.myownlauncher.data

import android.graphics.drawable.Drawable

/**
 * Represents an installed application
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    var isHidden: Boolean = false,
    var backgroundColor: Int? = null, // Custom background color
    var textColor: Int? = null,       // Custom text color
    var customLabel: String? = null,   // Custom app label
    var usageTime: Long = 0L,          // Total usage time in milliseconds
    var lastUsed: Long = 0L            // Last time app was opened
) : Comparable<AppInfo> {
    override fun compareTo(other: AppInfo): Int {
        return appName.compareTo(other.appName, ignoreCase = true)
    }
    
    fun getDisplayName(): String = customLabel ?: appName
}

/**
 * App customization settings stored in SharedPreferences
 */
data class AppCustomization(
    val packageName: String,
    val backgroundColor: Int? = null,
    val textColor: Int? = null,
    val customLabel: String? = null,
    val isHidden: Boolean = false
)

/**
 * Gesture configuration
 */
data class GestureConfig(
    val swipeUp: GestureAction = GestureAction.NONE,
    val swipeDown: GestureAction = GestureAction.NONE,
    val swipeLeft: GestureAction = GestureAction.NONE,
    val swipeRight: GestureAction = GestureAction.NONE,
    val doubleTap: GestureAction = GestureAction.NONE
)

enum class GestureAction {
    NONE,
    OPEN_NOTIFICATIONS,
    OPEN_QUICK_SETTINGS,
    OPEN_APP_DRAWER,
    OPEN_RECENT_APPS,
    OPEN_CAMERA,
    OPEN_SEARCH,
    CUSTOM_APP
}

/**
 * App usage statistics
 */
data class AppUsageInfo(
    val packageName: String,
    val totalTimeInForeground: Long,
    val lastTimeUsed: Long,
    val launchCount: Int
)