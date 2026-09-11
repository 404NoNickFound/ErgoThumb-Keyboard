package com.aleix.thumbgridtest

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider

class TutorialActivity : AppCompatActivity() {

    companion object {
        const val GITHUB_URL = ""
    }

    private lateinit var grid: DualGridView
    private lateinit var overlay: DemoOverlay
    private lateinit var editor: AppCompatEditText
    private lateinit var stepTv: TextView
    private lateinit var titleTv: TextView
    private lateinit var descTv: TextView
    private lateinit var panel: LinearLayout
    private lateinit var cardScroll: ScrollView
    private lateinit var actionBtn: MaterialButton
    private lateinit var skipBtn: MaterialButton

    private var midH = 0

    private var titles: Array<String> = emptyArray()
    private var descs: Array<String> = emptyArray()
    private var sampleText = ""
    private var gateActive = false
    private var step = 0

    private var anyCommits = 0
    private var deletes = 0
    private var deletesAtStep = 0
    private var redos = 0
    private var cursorMoved = false
    private var enterDone = false
    private var pageChanged = false
    private var langCycles = 0

    private val writingSteps = setOf(1, 2, 3, 7, 9, 10, 11)
    private val manipSteps = setOf(4, 5, 6, 8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val th = Themes.current(this)
        val bgColor = th.appBg()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        titles = resources.getStringArray(R.array.tutorial_titles)
        descs = resources.getStringArray(R.array.tutorial_descs)
        sampleText = resources.getStringArray(R.array.tutorial_text).joinToString("\n\n")

        // Alçades en píxels, sense pesos: impossibles de col·lapsar.
        val screenH = resources.displayMetrics.heightPixels
        val density = resources.displayMetrics.density
        val topH = (56 * density).toInt()
        val kbH = (screenH * Prefs.height(this)).toInt()
        midH = (screenH - topH - kbH).coerceAtLeast((120 * density).toInt())

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
        }

        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(32, 8, 32, 8)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, topH
            )
        }
        stepTv = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.GRAY)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        skipBtn = MaterialButton(this).apply {
            text = getString(R.string.tutorial_skip)
            setOnClickListener { finishTutorial() }
        }
        topBar.addView(stepTv)
        topBar.addView(skipBtn)

        cardScroll = ScrollView(this).apply {
            // Failsafe: per defecte ocupa TOT el mig; si el repartiment es perdés,
            // la targeta continua visible i el teclat continua a sota.
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, midH
            )
        }
        panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 8, 32, 16)
        }
        titleTv = TextView(this).apply {
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 8)
        }
        descTv = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#CCCCCC"))
            setPadding(0, 0, 0, 12)
        }
        actionBtn = MaterialButton(this)
        panel.addView(titleTv)
        panel.addView(descTv)
        panel.addView(actionBtn)
        cardScroll.addView(panel)

        editor = AppCompatEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0
            )
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#222222"))
            setPadding(24, 16, 24, 16)
            gravity = android.view.Gravity.TOP
            showSoftInputOnFocus = false
            visibility = android.view.View.GONE
        }

        val gridWrap = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        grid = DualGridView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            tutorialMode = true
            textTarget = object : TextTarget {
                override fun commit(text: String) {
                    val e = editor.text ?: return
                    e.insert(editor.selectionStart.coerceAtLeast(0), text)
                }
                override fun deleteBefore(): String {
                    val e = editor.text ?: return ""
                    val s = editor.selectionStart
                    if (s <= 0) return ""
                    val ch = e.substring(s - 1, s)
                    e.delete(s - 1, s)
                    return ch
                }
                override fun enter() { commit("\n") }
                override fun peekBefore(n: Int): String {
                    val e = editor.text ?: return ""
                    val s = editor.selectionStart.coerceAtLeast(0)
                    return e.substring(maxOf(0, s - n), s)
                }
                override fun moveCursor(delta: Int, wordMode: Boolean) {
                    val e = editor.text ?: return
                    val s = editor.selectionStart.coerceAtLeast(0)
                    val full = e.toString()
                    editor.setSelection((s + delta).coerceIn(0, full.length))
                }
            }
            tutEvent = { ev -> onGridEvent(ev) }
        }
        overlay = DemoOverlay(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        overlay.grid = grid
        gridWrap.addView(grid)
        gridWrap.addView(overlay)

        root.addView(topBar)
        root.addView(cardScroll)
        root.addView(editor)
        root.addView(gridWrap)
        setContentView(root)

        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(editor.windowToken, 0)

        if (imeEnabled()) showStep() else showGate()
    }

    override fun onResume() {
        super.onResume()
        if (gateActive && imeEnabled()) {
            gateActive = false
            showStep()
        }
    }

    private fun setH(v: android.view.View, h: Int) {
        val lp = v.layoutParams
        lp.height = h
        v.layoutParams = lp
    }

    private fun imeEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        return imm.enabledInputMethodList.any { it.packageName == packageName }
    }

    private fun showGate() {
        gateActive = true
        skipBtn.visibility = android.view.View.GONE
        stepTv.text = ""
        titleTv.text = getString(R.string.gate_title)
        descTv.text = getString(R.string.gate_desc)
        clearExtraPanels()
        editor.visibility = android.view.View.GONE
        setH(cardScroll, midH)
        overlay.setStep(-1)
        actionBtn.visibility = android.view.View.VISIBLE
        actionBtn.text = getString(R.string.btn_system_settings)
        actionBtn.setOnClickListener {
            startActivity(Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
    }

    private fun onGridEvent(ev: String) {
        when (ev) {
            "delete" -> deletes++
            "redo" -> redos++
            "enter" -> enterDone = true
            "cursor" -> cursorMoved = true
            "barpage" -> pageChanged = true
            "lang" -> langCycles++
        }
        if (ev.startsWith("commit")) anyCommits++
        checkStep()
    }

    private fun norm() = (editor.text?.toString() ?: "").lowercase().trim()

    private fun checkStep() {
        if (gateActive) return
        val raw = editor.text?.toString() ?: ""
        val ok = when (step) {
            1 -> norm().contains(getString(R.string.tutorial_target_word).lowercase())
            2 -> norm().contains(getString(R.string.tutorial_target_phrase).lowercase())
            3 -> anyCommits >= 8
            4 -> deletes >= 8
            5 -> redos >= 8
            6 -> (deletes - deletesAtStep) >= 30
            7 -> raw.count { it.isUpperCase() } >= 3
            8 -> cursorMoved && enterDone
            9 -> pageChanged && raw.any { it.isDigit() } && raw.contains("@")
            10 -> raw.any { !it.isLetterOrDigit() && !it.isWhitespace() && it != '@' }
            11 -> langCycles >= 2
            else -> false
        }
        if (ok) next()
    }

    private fun next() {
        step++
        resetCounters()
        when {
            step == 4 || step == 8 -> {
                editor.setText(sampleText)
                editor.setSelection(editor.text?.length ?: 0)
            }
            step in writingSteps -> editor.setText("")
        }
        showStep()
    }

    private fun resetCounters() {
        anyCommits = 0
        deletes = 0; deletesAtStep = 0; redos = 0
        cursorMoved = false; enterDone = false
        pageChanged = false; langCycles = 0
    }

    private fun showStep() {
        skipBtn.visibility = android.view.View.VISIBLE
        stepTv.text = getString(R.string.tutorial_step_of, step + 1, titles.size)
        titleTv.text = titles.getOrElse(step) { "" }
        descTv.text = descs.getOrElse(step) { "" }
        clearExtraPanels()

        val showEditor = step in writingSteps || step in manipSteps
        editor.visibility = if (showEditor) android.view.View.VISIBLE else android.view.View.GONE
        if (showEditor) {
            val c = (midH * 0.60f).toInt()
            setH(cardScroll, c)
            setH(editor, midH - c)
            editor.requestFocus()
        } else {
            setH(cardScroll, midH)
        }

        overlay.setStep(step)

        when (step) {
            0 -> {
                buildVisualPanel()
                actionBtn.visibility = android.view.View.VISIBLE
                actionBtn.text = getString(R.string.tutorial_continue)
                actionBtn.setOnClickListener { next() }
            }
            titles.size - 1 -> {
                buildTourPanel()
                actionBtn.visibility = android.view.View.VISIBLE
                actionBtn.text = getString(R.string.tutorial_finish)
                actionBtn.setOnClickListener { finishTutorial() }
            }
            else -> actionBtn.visibility = android.view.View.GONE
        }
    }

    private var extraViews = mutableListOf<android.view.View>()

    private fun clearExtraPanels() {
        extraViews.forEach { panel.removeView(it) }
        extraViews.clear()
    }

    private fun addExtra(v: android.view.View) {
        extraViews.add(v)
        panel.addView(v, panel.indexOfChild(actionBtn))
    }

    private fun buildVisualPanel() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        addExtra(labelView(getString(R.string.color_dark_row)))
        addExtra(swatchRow(isDark = true))
        addExtra(labelView(getString(R.string.color_light_row)))
        addExtra(swatchRow(isDark = false))

        addExtra(miniSlider(R.string.slider_height, 25f, 60f, Prefs.height(this) * 100f) { v ->
            prefs.edit().putFloat(Prefs.KEY_HEIGHT, v / 100f).apply(); grid.reloadPrefs()
        })
        addExtra(miniSlider(R.string.slider_gap, 0f, 15f, Prefs.gap(this) * 100f) { v ->
            prefs.edit().putFloat(Prefs.KEY_GAP, v / 100f).apply(); grid.reloadPrefs()
        })
        addExtra(miniSlider(R.string.slider_margin_bottom, 0f, 25f, Prefs.marginBottom(this)) { v ->
            prefs.edit().putFloat(Prefs.KEY_MARGIN_BOTTOM, v).apply(); grid.reloadPrefs()
        })

        addExtra(labelView(getString(R.string.keyboard_langs_title)))
        val enabled = Prefs.langs(this).toMutableList()
        for (code in Layouts.CODES) {
            val nameRes = when (code) {
                "ca" -> R.string.lang_ca; "es" -> R.string.lang_es; else -> R.string.lang_en
            }
            val sw = MaterialSwitch(this).apply {
                text = getString(nameRes)
                setTextColor(Color.WHITE)
                isChecked = code in enabled
                setOnCheckedChangeListener { _, c ->
                    if (c) { if (code !in enabled) enabled.add(code) }
                    else {
                        if (enabled.size > 1) enabled.remove(code)
                        else { isChecked = true; return@setOnCheckedChangeListener }
                    }
                    prefs.edit().putString(
                        Prefs.KEY_LANGS,
                        Layouts.CODES.filter { it in enabled }.joinToString(",")
                    ).apply()
                    grid.reloadPrefs()
                }
            }
            addExtra(sw)
        }

        addExtra(labelView(getString(R.string.sensitivity_note)))
    }

    private fun buildTourPanel() {
        val sections = SettingsActivity.SECTION_TITLES
        for ((i, s) in sections.withIndex()) {
            val key = SettingsActivity.SECTION_KEYS.getOrElse(i) { "" }
            addExtra(cardView(getString(s)).apply {
                setOnClickListener {
                    startActivity(
                        Intent(this@TutorialActivity, SettingsActivity::class.java)
                            .putExtra(SettingsActivity.EXTRA_FOCUS, key)
                    )
                }
            })
        }
        addExtra(MaterialButton(this).apply {
            text = getString(R.string.help_github)
            setOnClickListener { openGithub() }
        })
    }

    private fun openGithub() {
        if (GITHUB_URL.isEmpty()) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.github_not_published))
                .setPositiveButton(getString(R.string.close), null)
                .show()
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(GITHUB_URL)))
        }
    }

    private fun labelView(t: String) = TextView(this).apply {
        text = t
        textSize = 13f
        setTextColor(Color.GRAY)
        setPadding(0, 12, 0, 4)
    }

    private fun cardView(t: String) = TextView(this).apply {
        text = t
        textSize = 15f
        setTextColor(Color.WHITE)
        setBackgroundColor(Color.parseColor("#333333"))
        setPadding(24, 20, 24, 20)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 12
        layoutParams = params
    }

    private val swatchColors = listOf(
        0, 0xFF4C86C8.toInt(), 0xFF5BA85A.toInt(),
        0xFF9A6BB8.toInt(), 0xFFD08A3E.toInt(), 0xFFC45252.toInt()
    )

    private fun swatchRow(isDark: Boolean): LinearLayout {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val current = if (isDark) Prefs.colorDark(this) else Prefs.colorLight(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (c in swatchColors) {
            row.addView(android.view.View(this).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80).apply { marginEnd = 12 }
                setBackgroundColor(if (c == 0) 0xFF8E8E93.toInt() else c)
                alpha = if (c == current) 1.0f else 0.45f
                setOnClickListener {
                    prefs.edit().putInt(
                        if (isDark) Prefs.KEY_COLOR_DARK else Prefs.KEY_COLOR_LIGHT, c
                    ).apply()
                    for (i in 0 until row.childCount) {
                        row.getChildAt(i).alpha = if (swatchColors[i] == c) 1.0f else 0.45f
                    }
                    grid.reloadPrefs()
                }
            })
        }
        return row
    }

    private fun miniSlider(labelRes: Int, from: Float, to: Float, current: Float, onValue: (Float) -> Unit): Slider {
        return Slider(this).apply {
            valueFrom = from
            valueTo = to
            stepSize = 1f
            value = current.coerceIn(from, to)
            contentDescription = getString(labelRes)
            addOnChangeListener { _, value, _ -> onValue(value) }
        }
    }

    private fun finishTutorial() {
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit()
            .putBoolean(Prefs.KEY_TUTORIAL_DONE, true).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}