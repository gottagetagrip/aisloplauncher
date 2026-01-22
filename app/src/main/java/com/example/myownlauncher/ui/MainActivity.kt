package com.example.myownlauncher.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myownlauncher.R
import com.example.myownlauncher.data.AppInfo
import com.example.myownlauncher.data.AppManager
import com.example.myownlauncher.data.PreferencesManager
import com.example.myownlauncher.data.GestureAction
import com.example.myownlauncher.ui.adapters.AppListAdapter
import com.example.myownlauncher.ui.customize.AppCustomizeActivity
import com.example.myownlauncher.ui.settings.SettingsActivity
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var appManager: AppManager
    private lateinit var prefsManager: PreferencesManager
    private lateinit var adapter: AppListAdapter
    private lateinit var gestureDetector: GestureDetector

    private var allApps = listOf<AppInfo>()
    private var filteredApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appManager = AppManager(this)
        prefsManager = PreferencesManager(this)

        setupViews()
        setupGestureDetector()
        loadApps()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)

        // Setup RecyclerView
        adapter = AppListAdapter(
            showIcons = prefsManager.shouldShowAppIcons(),
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
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Settings button (optional - can be gesture-based too)
        findViewById<View>(R.id.settingsButton)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val SWIPE_THRESHOLD = 100
            private val SWIPE_VELOCITY_THRESHOLD = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false

                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                if (abs(diffX) > abs(diffY)) {
                    // Horizontal swipe
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                } else {
                    // Vertical swipe
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown()
                        } else {
                            onSwipeUp()
                        }
                        return true
                    }
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                onDoubleTapGesture()
                return true
            }
        })

        // Apply gesture detector to the main layout
        findViewById<View>(R.id.mainLayout).setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
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
            "Customize",
            "Hide App",
            "App Info",
            "Uninstall"
        )

        AlertDialog.Builder(this)
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

    // Gesture handlers based on user preferences
    private fun onSwipeUp() {
        handleGesture(prefsManager.getGestureConfig().swipeUp)
    }

    private fun onSwipeDown() {
        handleGesture(prefsManager.getGestureConfig().swipeDown)
    }

    private fun onSwipeLeft() {
        handleGesture(prefsManager.getGestureConfig().swipeLeft)
    }

    private fun onSwipeRight() {
        handleGesture(prefsManager.getGestureConfig().swipeRight)
    }

    private fun onDoubleTapGesture() {
        handleGesture(prefsManager.getGestureConfig().doubleTap)
    }

    private fun handleGesture(action: GestureAction) {
        when (action) {
            GestureAction.OPEN_NOTIFICATIONS -> {
                try {
                    val intent = Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
                    sendBroadcast(intent)
                    
                    @Suppress("DEPRECATION")
                    val statusBarService = getSystemService("statusbar")
                    val statusBarClass = Class.forName("android.app.StatusBarManager")
                    val method = statusBarClass.getMethod("expandNotificationsPanel")
                    method.invoke(statusBarService)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            GestureAction.OPEN_SEARCH -> {
                searchEditText.requestFocus()
            }
            GestureAction.NONE -> {
                // Do nothing
            }
            else -> {
                Toast.makeText(this, "Gesture: $action", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps() // Reload in case apps were customized or hidden
    }

    override fun onBackPressed() {
        if (searchEditText.text.isNotEmpty()) {
            searchEditText.text.clear()
        } else {
            // Don't exit launcher on back press
        }
    }
}