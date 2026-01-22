package com.example.myownlauncher.ui.hiddenapps

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myownlauncher.R
import com.example.myownlauncher.data.AppManager
import com.example.myownlauncher.data.PreferencesManager
import com.example.myownlauncher.ui.adapters.AppListAdapter
import kotlinx.coroutines.launch

class HiddenAppsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appManager: AppManager
    private lateinit var prefsManager: PreferencesManager
    private lateinit var adapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)

        title = "Hidden Apps"
        
        appManager = AppManager(this)
        prefsManager = PreferencesManager(this)

        setupViews()
        loadHiddenApps()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)

        adapter = AppListAdapter(
            showIcons = prefsManager.shouldShowAppIcons(),
            onAppClick = { app ->
                // Don't launch, just show unhide option
                unhideApp(app.packageName, app.getDisplayName())
            },
            onAppLongClick = { app ->
                unhideApp(app.packageName, app.getDisplayName())
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadHiddenApps() {
        lifecycleScope.launch {
            val allApps = appManager.getAllApps(includeHidden = true)
            val hiddenApps = allApps.filter { it.isHidden }
            
            if (hiddenApps.isEmpty()) {
                Toast.makeText(this@HiddenAppsActivity, "No hidden apps", Toast.LENGTH_SHORT).show()
            }
            
            adapter.submitList(hiddenApps)
        }
    }

    private fun unhideApp(packageName: String, appName: String) {
        prefsManager.unhideApp(packageName)
        Toast.makeText(this, "$appName unhidden", Toast.LENGTH_SHORT).show()
        loadHiddenApps()
    }
}