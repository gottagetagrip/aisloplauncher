package com.example.myownlauncher.ui.customize

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myownlauncher.R
import com.example.myownlauncher.data.AppCustomization
import com.example.myownlauncher.data.PreferencesManager

class AppCustomizeActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager
    private var packageName: String = ""
    private var appName: String = ""
    
    private var selectedBackgroundColor: Int? = null
    private var selectedTextColor: Int? = null

    private lateinit var previewCard: CardView
    private lateinit var previewText: TextView
    private lateinit var customLabelInput: EditText
    
    private lateinit var bgRedSeekBar: SeekBar
    private lateinit var bgGreenSeekBar: SeekBar
    private lateinit var bgBlueSeekBar: SeekBar
    
    private lateinit var textRedSeekBar: SeekBar
    private lateinit var textGreenSeekBar: SeekBar
    private lateinit var textBlueSeekBar: SeekBar
    
    private lateinit var bgColorPreview: CardView
    private lateinit var textColorPreview: CardView

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
        title = "Customize $appName"

        previewCard = findViewById(R.id.previewCard)
        previewText = findViewById(R.id.previewText)
        customLabelInput = findViewById(R.id.customLabelInput)
        
        bgRedSeekBar = findViewById(R.id.bgRedSeekBar)
        bgGreenSeekBar = findViewById(R.id.bgGreenSeekBar)
        bgBlueSeekBar = findViewById(R.id.bgBlueSeekBar)
        
        textRedSeekBar = findViewById(R.id.textRedSeekBar)
        textGreenSeekBar = findViewById(R.id.textGreenSeekBar)
        textBlueSeekBar = findViewById(R.id.textBlueSeekBar)
        
        bgColorPreview = findViewById(R.id.bgColorPreview)
        textColorPreview = findViewById(R.id.textColorPreview)

        // Quick color buttons for background
        findViewById<Button>(R.id.btnBgGreen).setOnClickListener { setBackgroundColor(0xFF4CAF50.toInt()) }
        findViewById<Button>(R.id.btnBgBlue).setOnClickListener { setBackgroundColor(0xFF2196F3.toInt()) }
        findViewById<Button>(R.id.btnBgRed).setOnClickListener { setBackgroundColor(0xFFF44336.toInt()) }
        findViewById<Button>(R.id.btnBgYellow).setOnClickListener { setBackgroundColor(0xFFFFEB3B.toInt()) }
        findViewById<Button>(R.id.btnBgPurple).setOnClickListener { setBackgroundColor(0xFF9C27B0.toInt()) }
        findViewById<Button>(R.id.btnBgOrange).setOnClickListener { setBackgroundColor(0xFFFF9800.toInt()) }
        findViewById<Button>(R.id.btnBgClear).setOnClickListener { 
            selectedBackgroundColor = null
            updatePreview()
        }

        // Quick color buttons for text
        findViewById<Button>(R.id.btnTextWhite).setOnClickListener { setTextColor(Color.WHITE) }
        findViewById<Button>(R.id.btnTextBlack).setOnClickListener { setTextColor(Color.BLACK) }
        findViewById<Button>(R.id.btnTextDefault).setOnClickListener { 
            selectedTextColor = null
            updatePreview()
        }

        // Setup seekbar listeners
        val bgSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    selectedBackgroundColor = Color.rgb(
                        bgRedSeekBar.progress,
                        bgGreenSeekBar.progress,
                        bgBlueSeekBar.progress
                    )
                    updatePreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        val textSeekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    selectedTextColor = Color.rgb(
                        textRedSeekBar.progress,
                        textGreenSeekBar.progress,
                        textBlueSeekBar.progress
                    )
                    updatePreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        }

        bgRedSeekBar.setOnSeekBarChangeListener(bgSeekBarListener)
        bgGreenSeekBar.setOnSeekBarChangeListener(bgSeekBarListener)
        bgBlueSeekBar.setOnSeekBarChangeListener(bgSeekBarListener)
        
        textRedSeekBar.setOnSeekBarChangeListener(textSeekBarListener)
        textGreenSeekBar.setOnSeekBarChangeListener(textSeekBarListener)
        textBlueSeekBar.setOnSeekBarChangeListener(textSeekBarListener)

        // Save button
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveCustomization()
        }

        // Reset button
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetCustomization()
        }

        previewText.text = appName
    }

    private fun loadExistingCustomization() {
        val customization = prefsManager.getAppCustomization(packageName)
        if (customization != null) {
            selectedBackgroundColor = customization.backgroundColor
            selectedTextColor = customization.textColor
            customLabelInput.setText(customization.customLabel ?: "")
            
            customization.backgroundColor?.let { color ->
                bgRedSeekBar.progress = Color.red(color)
                bgGreenSeekBar.progress = Color.green(color)
                bgBlueSeekBar.progress = Color.blue(color)
            }
            
            customization.textColor?.let { color ->
                textRedSeekBar.progress = Color.red(color)
                textGreenSeekBar.progress = Color.green(color)
                textBlueSeekBar.progress = Color.blue(color)
            }
            
            updatePreview()
        }
    }

    private fun setBackgroundColor(color: Int) {
        selectedBackgroundColor = color
        bgRedSeekBar.progress = Color.red(color)
        bgGreenSeekBar.progress = Color.green(color)
        bgBlueSeekBar.progress = Color.blue(color)
        updatePreview()
    }

    private fun setTextColor(color: Int) {
        selectedTextColor = color
        textRedSeekBar.progress = Color.red(color)
        textGreenSeekBar.progress = Color.green(color)
        textBlueSeekBar.progress = Color.blue(color)
        updatePreview()
    }

    private fun updatePreview() {
        val customLabel = customLabelInput.text.toString()
        previewText.text = customLabel.ifEmpty { appName }
        
        selectedBackgroundColor?.let {
            previewCard.setCardBackgroundColor(it)
            bgColorPreview.setCardBackgroundColor(it)
        } ?: run {
            previewCard.setCardBackgroundColor(Color.TRANSPARENT)
            bgColorPreview.setCardBackgroundColor(Color.LTGRAY)
        }
        
        selectedTextColor?.let {
            previewText.setTextColor(it)
            textColorPreview.setCardBackgroundColor(it)
        } ?: run {
            previewText.setTextColor(Color.WHITE)
            textColorPreview.setCardBackgroundColor(Color.WHITE)
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
        Toast.makeText(this, "Customization saved!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun resetCustomization() {
        prefsManager.deleteAppCustomization(packageName)
        Toast.makeText(this, "Customization reset", Toast.LENGTH_SHORT).show()
        finish()
    }
}