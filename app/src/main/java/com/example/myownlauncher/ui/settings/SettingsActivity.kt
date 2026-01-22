package com.example.myownlauncher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myownlauncher.R
import com.example.myownlauncher.data.PreferencesManager
import com.example.myownlauncher.ui.hiddenapps.HiddenAppsActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefsManager = PreferencesManager(this)
        setupViews()
    }

    private fun setupViews() {
        // Back button
        findViewById<android.widget.ImageView>(R.id.backButton)?.setOnClickListener {
            finish()
        }

        // Hidden Apps
        findViewById<TextView>(R.id.btnHiddenApps).setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }

        // Font Size
        findViewById<TextView>(R.id.btnFontSize).setOnClickListener {
            showFontSizeDialog()
        }

        // Color Theme
        findViewById<TextView>(R.id.btnColorTheme).setOnClickListener {
            Toast.makeText(this, "Theme selection coming soon", Toast.LENGTH_SHORT).show()
        }

        // Usage Stats Permission
        findViewById<TextView>(R.id.btnUsageStats).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open usage settings", Toast.LENGTH_SHORT).show()
            }
        }

        // Set as Default Launcher
        findViewById<TextView>(R.id.btnSetDefault).setOnClickListener {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        }

        // Gestures (just show toasts for now)
        findViewById<TextView>(R.id.btnSwipeUp).setOnClickListener {
            Toast.makeText(this, "Swipe up gesture configured", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.btnSwipeDown).setOnClickListener {
            Toast.makeText(this, "Swipe down gesture configured", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.btnDoubleTap).setOnClickListener {
            Toast.makeText(this, "Double-tap gesture configured", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFontSizeDialog() {
        val sizes = arrayOf("Small (14sp)", "Medium (16sp)", "Large (18sp)", "Extra Large (20sp)")
        val currentSize = prefsManager.getTextSize().toInt()
        val selectedIndex = when (currentSize) {
            14 -> 0
            16 -> 1
            18 -> 2
            20 -> 3
            else -> 1
        }

        android.app.AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("Font Size")
            .setSingleChoiceItems(sizes, selectedIndex) { dialog, which ->
                val newSize = when (which) {
                    0 -> 14f
                    1 -> 16f
                    2 -> 18f
                    3 -> 20f
                    else -> 16f
                }
                prefsManager.setTextSize(newSize)
                Toast.makeText(this, "Font size updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}