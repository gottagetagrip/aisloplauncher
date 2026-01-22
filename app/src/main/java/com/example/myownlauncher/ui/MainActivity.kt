package com.example.myownlauncher.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myownlauncher.R
import com.example.myownlauncher.data.*
import com.example.myownlauncher.ui.adapters.AppListAdapter
import com.example.myownlauncher.ui.customize.AppCustomizeActivity
import com.example.myownlauncher.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var clockText: TextView
    private lateinit var dateText: TextView
    private lateinit var dayProgressBar: TextView
    private lateinit var yearProgressBar: TextView
    private lateinit var fastScrollView: View
    private lateinit var appManager: AppManager
    private lateinit var prefsManager: PreferencesManager
    private lateinit var adapter: AppListAdapter

    private var allApps = listOf<AppInfo>()
    private var filteredApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appManager = AppManager(this)
        prefsManager = PreferencesManager(this)

        setupViews()
        loadApps()
        startClock()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        clockText = findViewById(R.id.clockText)
        dateText = findViewById(R.id.dateText)
        dayProgressBar = findViewById(R.id.dayProgressBar)
        yearProgressBar = findViewById(R.id.yearProgressBar)
        fastScrollView = findViewById(R.id.fastScrollView)

        // Setup RecyclerView
        adapter = AppListAdapter(
            showIcons = prefsManager.shouldShowAppIcons(),
            textSize = prefsManager.getTextSize(),
            onAppClick = { app -> launchApp(app) },
            onAppLongClick = { app -> showAppOptionsDialog(app) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Setup search
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s.toString())
                // Show/hide clock based on search
                val clockContainer = findViewById<View>(R.id.clockContainer)
                clockContainer.visibility = if (s.isNullOrEmpty()) View.VISIBLE else View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Settings icon
        findViewById<View>(R.id.settingsIcon)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Update progress bars
        updateProgressBars()
    }

    private fun startClock() {
        val handler = android.os.Handler(mainLooper)
        val updateClock = object : Runnable {
            override fun run() {
                updateClock()
                handler.postDelayed(this, 1000) // Update every second
            }
        }
        handler.post(updateClock)
    }

    private fun updateClock() {
        val calendar = Calendar.getInstance()
        
        // Time format (24h)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        clockText.text = timeFormat.format(calendar.time)
        
        // Date format
        val dateFormat = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault())
        dateText.text = dateFormat.format(calendar.time)
    }

    private fun updateProgressBars() {
        val calendar = Calendar.getInstance()
        
        // Day progress (0-24 hours)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val dayPercent = ((hour * 60 + minute) / 1440.0 * 100).toInt()
        dayProgressBar.text = buildProgressBar(dayPercent, "Day")
        
        // Year progress (1-365 days)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val totalDays = if (calendar.isLeapYear(calendar.get(Calendar.YEAR))) 366 else 365
        val yearPercent = (dayOfYear.toDouble() / totalDays * 100).toInt()
        yearProgressBar.text = buildProgressBar(yearPercent, "Year")
    }

    private fun buildProgressBar(percent: Int, label: String): String {
        val barLength = 20
        val filled = (percent * barLength / 100).coerceIn(0, barLength)
        val bar = "█".repeat(filled) + "░".repeat(barLength - filled)
        return "$label: [$bar] $percent%"
    }

    private fun Calendar.isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun loadApps() {
        lifecycleScope.launch {
            allApps = appManager.getAllApps(includeHidden = false)
            allApps = appManager.sortApps(allApps)
            filteredApps = allApps
            adapter.submitList(filteredApps)
        }
    }

    private fun filterApps(query: String) {
        filteredApps = appManager.searchApps(allApps, query)
        adapter.submitList(filteredApps)
    }

    private fun launchApp(app: AppInfo) {
        appManager.launchApp(app.packageName)
    }

    private fun showAppOptionsDialog(app: AppInfo) {
        val options = arrayOf(
            "Customize color",
            "Hide app",
            "App info",
            "Uninstall"
        )

        AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle(app.getDisplayName())
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCustomizeActivity(app)
                    1 -> hideApp(app)
                    2 -> appManager.openAppInfo(app.packageName)
                    3 -> appManager.uninstallApp(app.packageName)
                }
            }
            .show()
    }

    private fun openCustomizeActivity(app: AppInfo) {
        val intent = Intent(this, AppCustomizeActivity::class.java).apply {
            putExtra("package_name", app.packageName)
            putExtra("app_name", app.getDisplayName())
        }
        startActivity(intent)
    }

    private fun hideApp(app: AppInfo) {
        prefsManager.hideApp(app.packageName)
        Toast.makeText(this, "${app.getDisplayName()} hidden", Toast.LENGTH_SHORT).show()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        updateProgressBars()
    }

    override fun onBackPressed() {
        if (searchEditText.text.isNotEmpty()) {
            searchEditText.text.clear()
        }
        // Don't exit on back press
    }
}