package com.example.myownlauncher.ui.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myownlauncher.R
import com.example.myownlauncher.data.PreferencesManager
import com.example.myownlauncher.data.SortMode
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
        title = "Settings"

        // Show App Icons Toggle
        val showIconsCheckbox = findViewById<CheckBox>(R.id.checkboxShowIcons)
        showIconsCheckbox.isChecked = prefsManager.shouldShowAppIcons()
        showIconsCheckbox.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setShowAppIcons(isChecked)
            Toast.makeText(this, "Restart launcher to see changes", Toast.LENGTH_SHORT).show()
        }

        // Text Size Slider
        val textSizeSeekBar = findViewById<SeekBar>(R.id.seekBarTextSize)
        val textSizeLabel = findViewById<TextView>(R.id.textSizeLabel)
        val currentSize = prefsManager.getTextSize()
        textSizeSeekBar.progress = (currentSize - 12).toInt()
        textSizeLabel.text = "Text Size: ${currentSize.toInt()}sp"

        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress + 12f
                textSizeLabel.text = "Text Size: ${size.toInt()}sp"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val size = (seekBar?.progress ?: 0) + 12f
                prefsManager.setTextSize(size)
                Toast.makeText(this@SettingsActivity, "Restart launcher to see changes", Toast.LENGTH_SHORT).show()
            }
        })

        // Sort Mode Buttons
        val btnSortAlpha = findViewById<Button>(R.id.btnSortAlpha)
        val btnSortMostUsed = findViewById<Button>(R.id.btnSortMostUsed)
        val btnSortRecent = findViewById<Button>(R.id.btnSortRecent)

        // Highlight current sort mode
        when (prefsManager.getSortMode()) {
            SortMode.ALPHABETICAL -> btnSortAlpha.setBackgroundColor(getColor(R.color.selected))
            SortMode.MOST_USED -> btnSortMostUsed.setBackgroundColor(getColor(R.color.selected))
            SortMode.RECENTLY_USED -> btnSortRecent.setBackgroundColor(getColor(R.color.selected))
            else -> {}
        }

        btnSortAlpha.setOnClickListener {
            prefsManager.setSortMode(SortMode.ALPHABETICAL)
            Toast.makeText(this, "Sort: Alphabetical", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSortMostUsed.setOnClickListener {
            prefsManager.setSortMode(SortMode.MOST_USED)
            Toast.makeText(this, "Sort: Most Used", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnSortRecent.setOnClickListener {
            prefsManager.setSortMode(SortMode.RECENTLY_USED)
            Toast.makeText(this, "Sort: Recently Used", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Hidden Apps Button
        findViewById<Button>(R.id.btnHiddenApps).setOnClickListener {
            startActivity(Intent(this, HiddenAppsActivity::class.java))
        }

        // Usage Stats Permission
        findViewById<Button>(R.id.btnUsageStats).setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open usage settings", Toast.LENGTH_SHORT).show()
            }
        }

        // Set as Default Launcher
        findViewById<Button>(R.id.btnSetDefault).setOnClickListener {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            startActivity(intent)
        }
    }
}