package com.example.myownlauncher.ui.customize

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myownlauncher.R
import com.example.myownlauncher.data.AppCustomization
import com.example.myownlauncher.data.PreferencesManager

class AppCustomizeActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private var packageName: String = ""
    private var appName: String = ""
    
    private var selectedBackgroundColor: Int? = null
    private var selectedTextColor: Int? = null

    private lateinit var previewContainer: LinearLayout
    private lateinit var previewText: TextView
    private lateinit var customLabelInput: EditText
    private lateinit var titleText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_customize)

        packageName = intent.getStringExtra("package_name") ?: ""
        appName = intent.getStringExtra("app_name") ?: ""

        if (packageName.isEmpty()) {
            finish()
            return
        }

        prefsManager = PreferencesManager(this)
        setupViews()
        loadExistingCustomization()
    }

    private fun setupViews() {
        titleText = findViewById(R.id.titleText)
        titleText.text = "Customize"

        previewContainer = findViewById(R.id.previewContainer)
        previewText = findViewById(R.id.previewText)
        customLabelInput = findViewById(R.id.customLabelInput)
        
        // Back button
        findViewById<android.widget.ImageView>(R.id.backButton)?.setOnClickListener {
            finish()
        }

        previewText.text = appName

        // Custom label listener
        customLabelInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updatePreview()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Background color buttons
        findViewById<TextView>(R.id.btnBgGreen).setOnClickListener { setBackgroundColor(0xFF4CAF50.toInt()) }
        findViewById<TextView>(R.id.btnBgBlue).setOnClickListener { setBackgroundColor(0xFF2196F3.toInt()) }
        findViewById<TextView>(R.id.btnBgRed).setOnClickListener { setBackgroundColor(0xFFF44336.toInt()) }
        findViewById<TextView>(R.id.btnBgYellow).setOnClickListener { setBackgroundColor(0xFFFFEB3B.toInt()) }
        findViewById<TextView>(R.id.btnBgPurple).setOnClickListener { setBackgroundColor(0xFF9C27B0.toInt()) }
        findViewById<TextView>(R.id.btnBgOrange).setOnClickListener { setBackgroundColor(0xFFFF9800.toInt()) }
        findViewById<TextView>(R.id.btnBgGray).setOnClickListener { setBackgroundColor(0xFF666666.toInt()) }
        findViewById<TextView>(R.id.btnBgClear).setOnClickListener { 
            selectedBackgroundColor = null
            updatePreview()
        }

        // Text color buttons
        findViewById<TextView>(R.id.btnTextWhite).setOnClickListener { setTextColor(Color.WHITE) }
        findViewById<TextView>(R.id.btnTextBlack).setOnClickListener { setTextColor(Color.BLACK) }
        findViewById<TextView>(R.id.btnTextDefault).setOnClickListener { 
            selectedTextColor = null
            updatePreview()
        }

        // Save button
        findViewById<TextView>(R.id.btnSave).setOnClickListener {
            saveCustomization()
        }

        // Reset button
        findViewById<TextView>(R.id.btnReset).setOnClickListener {
            resetCustomization()
        }
    }

    private fun loadExistingCustomization() {
        val customization = prefsManager.getAppCustomization(packageName)
        if (customization != null) {
            selectedBackgroundColor = customization.backgroundColor
            selectedTextColor = customization.textColor
            customLabelInput.setText(customization.customLabel ?: "")
            updatePreview()
        }
    }

    private fun setBackgroundColor(color: Int) {
        selectedBackgroundColor = color
        updatePreview()
    }

    private fun setTextColor(color: Int) {
        selectedTextColor = color
        updatePreview()
    }

    private fun updatePreview() {
        val customLabel = customLabelInput.text.toString()
        previewText.text = customLabel.ifEmpty { appName }
        
        selectedBackgroundColor?.let {
            previewContainer.setBackgroundColor(it)
        } ?: run {
            previewContainer.setBackgroundColor(Color.TRANSPARENT)
        }
        
        selectedTextColor?.let {
            previewText.setTextColor(it)
        } ?: run {
            previewText.setTextColor(Color.WHITE)
        }
    }

    private fun saveCustomization() {
        val customLabel = customLabelInput.text.toString().takeIf { it.isNotEmpty() }
        
        val customization = AppCustomization(
            packageName = packageName,
            backgroundColor = selectedBackgroundColor,
            textColor = selectedTextColor,
            customLabel = customLabel
        )
        
        prefsManager.saveAppCustomization(customization)
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetCustomization() {
        prefsManager.deleteAppCustomization(packageName)
        Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show()
        finish()
    }
}