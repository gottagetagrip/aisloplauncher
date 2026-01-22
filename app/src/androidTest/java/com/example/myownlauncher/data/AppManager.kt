package com.example.myownlauncher.data

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class AppManager(private val context: Context) {
    private val packageManager = context.packageManager
    private val prefsManager = PreferencesManager(context)

    /**
     * Get all installed launchable apps
     */
    suspend fun getAllApps(includeHidden: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val apps = packageManager.queryIntentActivities(intent, 0).mapNotNull { resolveInfo ->
            try {
                val packageName = resolveInfo.activityInfo.packageName
                val appName = resolveInfo.loadLabel(packageManager).toString()
                val icon = resolveInfo.loadIcon(packageManager)
                
                // Get customization
                val customization = prefsManager.getAppCustomization(packageName)
                
                AppInfo(
                    packageName = packageName,
                    appName = appName,
                    icon = icon,
                    isHidden = customization?.isHidden ?: false,
                    backgroundColor = customization?.backgroundColor,
                    textColor = customization?.textColor,
                    customLabel = customization?.customLabel
                )
            } catch (e: Exception) {
                null
            }
        }

        // Filter hidden apps if needed
        val filteredApps = if (includeHidden) apps else apps.filter { !it.isHidden }
        
        // Apply usage stats
        applyUsageStats(filteredApps)
    }

    /**
     * Apply usage statistics to apps
     */
    private suspend fun applyUsageStats(apps: List<AppInfo>): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            if (usageStatsManager != null) {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1) // Last month
                
                val stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    calendar.timeInMillis,
                    System.currentTimeMillis()
                )

                val usageMap = stats.associateBy { it.packageName }
                
                apps.forEach { app ->
                    usageMap[app.packageName]?.let { usageStats ->
                        app.usageTime = usageStats.totalTimeInForeground
                        app.lastUsed = usageStats.lastTimeUsed
                    }
                }
            }
        } catch (e: Exception) {
            // Usage stats permission not granted
        }
        
        apps
    }

    /**
     * Launch an app by package name
     */
    fun launchApp(packageName: String) {
        try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Uninstall an app
     */
    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Open app info page
     */
    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Sort apps based on preference
     */
    fun sortApps(apps: List<AppInfo>, sortMode: SortMode = prefsManager.getSortMode()): List<AppInfo> {
        return when (sortMode) {
            SortMode.ALPHABETICAL -> apps.sortedBy { it.getDisplayName().lowercase() }
            SortMode.MOST_USED -> apps.sortedByDescending { it.usageTime }
            SortMode.RECENTLY_USED -> apps.sortedByDescending { it.lastUsed }
            SortMode.CUSTOM -> apps // User-defined order would be stored separately
        }
    }

    /**
     * Search apps by name
     */
    fun searchApps(apps: List<AppInfo>, query: String): List<AppInfo> {
        if (query.isBlank()) return apps
        
        return apps.filter { app ->
            app.getDisplayName().contains(query, ignoreCase = true) ||
            app.packageName.contains(query, ignoreCase = true)
        }
    }
}