package com.aleix.thumbgridtest

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.os.LocaleListCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

object Prefs {
    const val NAME = "thumbgrid_prefs"

    const val KEY_HEIGHT = "height_fraction"
    const val KEY_GAP = "gap_fraction"
    const val KEY_THRESHOLD = "swipe_threshold_dp"
    const val KEY_DOUBLE_MS = "double_action_ms"
    const val KEY_AUTOCAP = "autocap"
    const val KEY_AUTOSPACE = "autospace"
    const val KEY_CURSOR_WORD = "cursor_word"
    const val KEY_SENS_CHARS = "sens_chars"
    const val KEY_SENS_ERASE = "sens_erase"
    const val KEY_FLICK_MS = "flick_ms"
    const val KEY_LANG = "lang_current"
    const val KEY_LANGS = "langs_enabled"
    const val KEY_SHOW_HINTS = "show_hints"
    const val KEY_BIGRAMS = "bigrams_enabled"
    const val KEY_ONBOARDED = "onboarded"
    const val KEY_APP_LANG = "app_lang"
    const val KEY_COLOR_DARK = "color_dark"
    const val KEY_COLOR_LIGHT = "color_light"

    const val KEY_MARGIN_SIDE = "margin_side"
    const val KEY_MARGIN_LEFT = "margin_left"
    const val KEY_MARGIN_RIGHT = "margin_right"
    const val KEY_MARGIN_INDEP = "margin_indep"
    const val KEY_MARGIN_BOTTOM = "margin_bottom"

    const val KEY_ANIM_STRONG = "anim_strong"
    const val KEY_ANIM_DEPTH = "anim_depth"
    const val KEY_ANIM_HELP = "anim_help"
    const val KEY_ANIM_TRACE = "anim_trace"
    const val KEY_ANIM_SCALE = "anim_scale"
    const val KEY_VMAX = "vmax_cps"
    const val KEY_CASE_MS = "case_ms"

    const val DEFAULT_HEIGHT = 0.35f
    const val DEFAULT_GAP = 0.10f
    const val DEFAULT_THRESHOLD_DP = 24f
    const val DEFAULT_DOUBLE_MS = 350f
    const val DEFAULT_SENS_CHARS = 25f
    const val DEFAULT_SENS_ERASE = 6f
    const val DEFAULT_FLICK_MS = 180f
    const val DEFAULT_MARGIN_SIDE = 0f
    const val DEFAULT_MARGIN_BOTTOM = 12f
    const val DEFAULT_ANIM_SCALE = 100f
    const val DEFAULT_VMAX = 20f
    const val DEFAULT_CASE_MS = 260f

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun height(ctx: Context) = prefs(ctx).getFloat(KEY_HEIGHT, DEFAULT_HEIGHT)
    fun gap(ctx: Context) = prefs(ctx).getFloat(KEY_GAP, DEFAULT_GAP)
    fun threshold(ctx: Context) = prefs(ctx).getFloat(KEY_THRESHOLD, DEFAULT_THRESHOLD_DP)
    fun doubleMs(ctx: Context) = prefs(ctx).getFloat(KEY_DOUBLE_MS, DEFAULT_DOUBLE_MS)
    fun autoCap(ctx: Context) = prefs(ctx).getBoolean(KEY_AUTOCAP, true)
    fun autoSpace(ctx: Context) = prefs(ctx).getBoolean(KEY_AUTOSPACE, true)
    fun cursorWord(ctx: Context) = prefs(ctx).getBoolean(KEY_CURSOR_WORD, false)
    fun sensChars(ctx: Context) = prefs(ctx).getFloat(KEY_SENS_CHARS, DEFAULT_SENS_CHARS)
    fun sensErase(ctx: Context) = prefs(ctx).getFloat(KEY_SENS_ERASE, DEFAULT_SENS_ERASE)
    fun flickMs(ctx: Context) = prefs(ctx).getFloat(KEY_FLICK_MS, DEFAULT_FLICK_MS)
    fun showHints(ctx: Context) = prefs(ctx).getBoolean(KEY_SHOW_HINTS, true)
    fun bigrams(ctx: Context) = prefs(ctx).getBoolean(KEY_BIGRAMS, false)
    fun onboarded(ctx: Context) = prefs(ctx).getBoolean(KEY_ONBOARDED, false)
    fun setOnboarded(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_ONBOARDED, v).apply()
    fun appLang(ctx: Context) = prefs(ctx).getString(KEY_APP_LANG, "ca") ?: "ca"
    fun colorDark(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_DARK, 0)
    fun colorLight(ctx: Context) = prefs(ctx).getInt(KEY_COLOR_LIGHT, 0)

    fun marginSide(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_SIDE, DEFAULT_MARGIN_SIDE)
    fun marginIndep(ctx: Context) = prefs(ctx).getBoolean(KEY_MARGIN_INDEP, false)
    fun marginLeft(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_LEFT, marginSide(ctx))
        else marginSide(ctx)
    fun marginRight(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_RIGHT, marginSide(ctx))
        else marginSide(ctx)
    fun marginBottom(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM)

    fun animStrong(ctx: Context) = prefs(ctx).getBoolean(KEY_ANIM_STRONG, true)
    fun animDepth(ctx: Context) = prefs(ctx).getBoolean(KEY_ANIM_DEPTH, true)
    fun animHelp(ctx: Context) = prefs(ctx).getBoolean(KEY_ANIM_HELP, true)
    fun animTrace(ctx: Context) = prefs(ctx).getBoolean(KEY_ANIM_TRACE, false)
    fun animScale(ctx: Context) = prefs(ctx).getFloat(KEY_ANIM_SCALE, DEFAULT_ANIM_SCALE)
    fun vmax(ctx: Context) = prefs(ctx).getFloat(KEY_VMAX, DEFAULT_VMAX)
    fun caseMs(ctx: Context) = prefs(ctx).getFloat(KEY_CASE_MS, DEFAULT_CASE_MS)

    fun langs(ctx: Context): List<String> =
        (prefs(ctx).getString(KEY_LANGS, "ca,es,en") ?: "ca,es,en")
            .split(",").map { it.trim() }.filter { it in Layouts.CODES }

    fun lang(ctx: Context): String {
        val enabled = langs(ctx)
        val cur = prefs(ctx).getString(KEY_LANG, "ca") ?: "ca"
        return if (cur in enabled) cur else (enabled.firstOrNull() ?: "ca")
    }

    fun setLang(ctx: Context, code: String) {
        prefs(ctx).edit().putString(KEY_LANG, code).apply()
    }
}

fun blendColor(a: Int, b: Int, t: Float): Int {
    val ar = (a shr 16) and 0xFF; val ag = (a shr 8) and 0xFF; val ab = a and 0xFF
    val br = (b shr 16) and 0xFF; val bg = (b shr 8) and 0xFF; val bb = b and 0xFF
    val r = (ar + (br - ar) * t).toInt().coerceIn(0, 255)
    val g = (ag + (bg - ag) * t).toInt().coerceIn(0, 255)
    val bl = (ab + (bb - ab) * t).toInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
}

object Themes {
    const val BG = 0; const val KEYCAP = 1; const val SPECIAL = 2
    const val SLIDER = 3; const val KNOB = 4; const val STRIP = 5

    class Theme(
        val principal: Int,
        val pole: Int,
        val text: Int,
        val hint: Int,
        private val mixes: FloatArray,
        val keycapShadow: Boolean
    ) {
        val role: IntArray = IntArray(mixes.size) { blendColor(principal, pole, mixes[it]) }
        val isDark: Boolean get() = pole == Color.BLACK
        val flashPole: Int get() = if (isDark) Color.WHITE else Color.BLACK
        val shadowColor: Int get() = if (isDark) Color.BLACK else 0xFF1C1C1E.toInt()
        fun withPrincipal(c: Int) = Theme(c, pole, text, hint, mixes, keycapShadow)
    }

    val DARK = Theme(
        principal = 0xFF8E8E93.toInt(), pole = Color.BLACK,
        text = Color.WHITE, hint = 0xFF8E8E93.toInt(),
        mixes = floatArrayOf(1.00f, 0.70f, 0.45f, 0.88f, 0.35f, 0.95f),
        keycapShadow = false
    )

    val LIGHT = Theme(
        principal = 0xFF8E8E93.toInt(), pole = Color.WHITE,
        text = 0xFF1C1C1E.toInt(), hint = 0xFF8E8E93.toInt(),
        mixes = floatArrayOf(0.18f, 1.00f, 0.45f, 0.35f, 1.00f, 0.12f),
        keycapShadow = true
    )

    fun current(ctx: Context): Theme {
        val night = (ctx.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val base = if (night) DARK else LIGHT
        val override = if (night) Prefs.colorDark(ctx) else Prefs.colorLight(ctx)
        return if (override == 0) base else base.withPrincipal(override)
    }
}

interface TextTarget {
    fun commit(text: String)
    fun deleteBefore(): String
    fun enter()
    fun peekBefore(n: Int): String
    fun moveCursor(delta: Int, wordMode: Boolean)
}

enum class SwipeDir { TAP, N, NE, E, SE, S, SW, W, NW }

class MainActivity : AppCompatActivity() {

    private lateinit var dualGridView: DualGridView
    private lateinit var editText: SelEditText

    private inner class SelEditText(ctx: Context) : AppCompatEditText(ctx) {
        var onSel: (() -> Unit)? = null
        override fun onSelectionChanged(selStart: Int, selEnd: Int) {
            super.onSelectionChanged(selStart, selEnd)
            onSel?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!Prefs.onboarded(this)) {
            setContentView(buildChooser())
            return
        }

        editText = SelEditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            hint = getString(R.string.test_hint)
            setBackgroundColor(Color.parseColor("#222222"))
            setPadding(32, 24, 32, 24)
            onSel = { if (::dualGridView.isInitialized) dualGridView.invalidate() }
        }

        val enableButton = MaterialButton(this).apply {
            text = getString(R.string.btn_system_settings)
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val settingsButton = MaterialButton(this).apply {
            text = getString(R.string.btn_app_settings)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            }
        }

        val spacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }

        dualGridView = DualGridView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textTarget = object : TextTarget {
                override fun commit(text: String) {
                    val e = editText.text ?: return
                    e.insert(editText.selectionStart.coerceAtLeast(0), text)
                }
                override fun deleteBefore(): String {
                    val e = editText.text ?: return ""
                    val s = editText.selectionStart
                    if (s <= 0) return ""
                    val ch = e.substring(s - 1, s)
                    e.delete(s - 1, s)
                    return ch
                }
                override fun enter() { commit("\n") }
                override fun peekBefore(n: Int): String {
                    val e = editText.text ?: return ""
                    val s = editText.selectionStart.coerceAtLeast(0)
                    return e.substring(maxOf(0, s - n), s)
                }
                override fun moveCursor(delta: Int, wordMode: Boolean) {
                    val e = editText.text ?: return
                    val s = editText.selectionStart.coerceAtLeast(0)
                    val full = e.toString()
                    val target = if (!wordMode) s + delta
                    else if (delta > 0) {
                        var pos = s
                        repeat(delta) {
                            val idx = full.indexOf(' ', pos)
                            pos = if (idx < 0) full.length else idx + 1
                        }
                        pos
                    } else {
                        var pos = s
                        repeat(-delta) {
                            var t = full.substring(0, pos)
                            while (t.endsWith(" ")) t = t.dropLast(1)
                            val idx = t.lastIndexOf(' ')
                            pos = if (idx < 0) 0 else idx + 1
                        }
                        pos
                    }
                    editText.setSelection(target.coerceIn(0, full.length))
                }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111111"))
            addView(editText)
            addView(enableButton)
            addView(settingsButton)
            addView(spacer)
            addView(dualGridView)
        }

        setContentView(root)
    }

    private fun buildChooser(): View {
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111111"))
            setPadding(64, 96, 64, 64)
        }
        lay.addView(TextView(this).apply {
            text = getString(R.string.chooser_title)
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 48)
        })
        for (code in Layouts.CODES) {
            lay.addView(MaterialButton(this).apply {
                text = Layouts.NATIVE[code] ?: code
                setOnClickListener { chooseLang(code) }
            })
        }
        return lay
    }

    private fun chooseLang(code: String) {
        val others = Layouts.CODES.filter { it != code }
        Prefs.setLang(this, code)
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit()
            .putString(Prefs.KEY_LANGS, (listOf(code) + others).joinToString(","))
            .putString(Prefs.KEY_APP_LANG, code)
            .putBoolean(Prefs.KEY_ONBOARDED, true)
            .apply()
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
        recreate()
    }

    override fun onResume() {
        super.onResume()
        if (::dualGridView.isInitialized) dualGridView.reloadPrefs()
    }
}

class SettingsActivity : AppCompatActivity() {

    private val reqUnlock = 1234
    private lateinit var root: LinearLayout
    private lateinit var currentContainer: LinearLayout

    private val swatches = listOf(
        0,
        0xFF4C86C8.toInt(),
        0xFF5BA85A.toInt(),
        0xFF9A6BB8.toInt(),
        0xFFD08A3E.toInt(),
        0xFFC45252.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#111111"))
        }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(root, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        root.addView(TextView(this).apply {
            text = getString(R.string.settings_title)
            textSize = 24f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 24)
        })

        section(R.string.section_languages)
        languageRow()
        keyboardLangs()

        section(R.string.section_appearance)
        appearanceBlock()

        section(R.string.section_geometry)
        mSlider(R.string.slider_height, 25f, 60f, Prefs.height(this) * 100f,
            { getString(R.string.fmt_percent_screen, it.toInt()) })
        { v -> putF(Prefs.KEY_HEIGHT, v / 100f) }
        mSlider(R.string.slider_gap, 0f, 15f, Prefs.gap(this) * 100f,
            { getString(R.string.fmt_percent_width, it.toInt()) })
        { v -> putF(Prefs.KEY_GAP, v / 100f) }
        marginsBlock()
        mSlider(R.string.slider_margin_bottom, 8f, 25f, Prefs.marginBottom(this),
            { getString(R.string.fmt_percent_height, it.toInt()) })
        { v -> putF(Prefs.KEY_MARGIN_BOTTOM, v) }

        section(R.string.section_sensitivity)
        mSlider(R.string.slider_threshold, 12f, 48f, Prefs.threshold(this),
            { getString(R.string.fmt_dp, it.toInt()) })
        { v -> putF(Prefs.KEY_THRESHOLD, v) }
        mSlider(R.string.slider_double, 150f, 700f, Prefs.doubleMs(this),
            { getString(R.string.fmt_ms, it.toInt()) })
        { v -> putF(Prefs.KEY_DOUBLE_MS, v) }
        mSlider(R.string.slider_sens_chars, 5f, 60f, Prefs.sensChars(this),
            { getString(R.string.fmt_chars_swipe, it.toInt()) })
        { v -> putF(Prefs.KEY_SENS_CHARS, v) }
        mSlider(R.string.slider_vmax, 5f, 40f, Prefs.vmax(this),
            { getString(R.string.fmt_cps, it.toInt()) })
        { v -> putF(Prefs.KEY_VMAX, v) }
        mSlider(R.string.slider_sens_erase, 2f, 30f, Prefs.sensErase(this),
            { getString(R.string.fmt_chars_swipe, it.toInt()) })
        { v -> putF(Prefs.KEY_SENS_ERASE, v) }
        mSlider(R.string.slider_flick, 80f, 400f, Prefs.flickMs(this),
            { getString(R.string.fmt_ms, it.toInt()) })
        { v -> putF(Prefs.KEY_FLICK_MS, v) }

        section(R.string.section_comfort)
        mSwitch(R.string.switch_autocap, Prefs.autoCap(this)) { putB(Prefs.KEY_AUTOCAP, it) }
        mSwitch(R.string.switch_autospace, Prefs.autoSpace(this)) { putB(Prefs.KEY_AUTOSPACE, it) }
        mSwitch(R.string.switch_cursor_word, Prefs.cursorWord(this)) { putB(Prefs.KEY_CURSOR_WORD, it) }
        mSwitch(R.string.switch_hints, Prefs.showHints(this)) { putB(Prefs.KEY_SHOW_HINTS, it) }

        section(R.string.section_animations)
        mSlider(R.string.slider_anim_scale, 50f, 200f, Prefs.animScale(this),
            { getString(R.string.fmt_percent, it.toInt()) })
        { v -> putF(Prefs.KEY_ANIM_SCALE, v) }
        mSlider(R.string.slider_case_ms, 0f, 500f, Prefs.caseMs(this),
            { if (it.toInt() < 150) getString(R.string.case_instant)
            else getString(R.string.fmt_ms, it.toInt()) })
        { v -> putF(Prefs.KEY_CASE_MS, v) }
        mSwitch(R.string.switch_anim_strong, Prefs.animStrong(this)) { putB(Prefs.KEY_ANIM_STRONG, it) }
        mSwitch(R.string.switch_anim_depth, Prefs.animDepth(this)) { putB(Prefs.KEY_ANIM_DEPTH, it) }
        mSwitch(R.string.switch_anim_help, Prefs.animHelp(this)) { putB(Prefs.KEY_ANIM_HELP, it) }
        mSwitch(R.string.switch_anim_trace, Prefs.animTrace(this)) { putB(Prefs.KEY_ANIM_TRACE, it) }

        section(R.string.section_advanced)
        bigramsBlock()

        root.addView(TextView(this).apply {
            text = getString(R.string.settings_note)
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, 40, 0, 0)
        })

        setContentView(scroll)
    }

    private fun putF(key: String, v: Float) =
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit().putFloat(key, v).apply()

    private fun putB(key: String, v: Boolean) =
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit().putBoolean(key, v).apply()

    private fun add(v: View) {
        currentContainer.addView(v)
    }

    private fun section(titleRes: Int, expanded: Boolean = true) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = if (expanded) View.VISIBLE else View.GONE
        }
        val header = TextView(this).apply {
            text = (if (expanded) "▼  " else "▶  ") + getString(titleRes)
            textSize = 15f
            setTextColor(Color.parseColor("#8E8E93"))
            setPadding(0, 40, 0, 12)
            setOnClickListener {
                val show = container.visibility == View.GONE
                container.visibility = if (show) View.VISIBLE else View.GONE
                text = (if (show) "▼  " else "▶  ") + getString(titleRes)
            }
        }
        root.addView(header)
        root.addView(container)
        currentContainer = container
    }

    private fun mSwitch(labelRes: Int, current: Boolean, onValue: (Boolean) -> Unit): MaterialSwitch {
        val sw = MaterialSwitch(this).apply {
            text = getString(labelRes)
            textSize = 16f
            setTextColor(Color.WHITE)
            isChecked = current
            setPadding(0, 16, 0, 16)
            setOnCheckedChangeListener { _, c -> onValue(c) }
        }
        add(sw)
        return sw
    }

    private fun mSlider(
        labelRes: Int, from: Float, to: Float, current: Float,
        format: (Float) -> String, onValue: (Float) -> Unit
    ): Slider {
        val tv = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 20, 0, 4)
            text = getString(labelRes) + ": " + format(current)
        }
        val sl = Slider(this).apply {
            valueFrom = from
            valueTo = to
            stepSize = 1f
            value = current.coerceIn(from, to)
            addOnChangeListener { _, value, _ ->
                tv.text = getString(labelRes) + ": " + format(value)
                onValue(value)
            }
        }
        add(tv)
        add(sl)
        return sl
    }

    private fun appearanceBlock() {
        add(TextView(this).apply {
            text = getString(R.string.color_dark_row)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 8)
        })
        add(swatchRow(isDark = true))
        add(TextView(this).apply {
            text = getString(R.string.color_light_row)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 24, 0, 8)
        })
        add(swatchRow(isDark = false))
        add(TextView(this).apply {
            text = getString(R.string.settings_note)
            textSize = 12f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 0)
        })
    }

    private fun swatchRow(isDark: Boolean): LinearLayout {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val current = if (isDark) Prefs.colorDark(this) else Prefs.colorLight(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (c in swatches) {
            row.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(96, 96).apply { marginEnd = 16 }
                setBackgroundColor(if (c == 0) 0xFF8E8E93.toInt() else c)
                alpha = if (c == current) 1.0f else 0.45f
                setOnClickListener {
                    prefs.edit().putInt(
                        if (isDark) Prefs.KEY_COLOR_DARK else Prefs.KEY_COLOR_LIGHT, c
                    ).apply()
                    for (i in 0 until row.childCount) {
                        row.getChildAt(i).alpha = if (swatches[i] == c) 1.0f else 0.45f
                    }
                }
            })
        }
        return row
    }

    private fun languageRow() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val current = Prefs.appLang(this)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val codes = listOf(
            "ca" to R.string.lang_ca,
            "es" to R.string.lang_es,
            "en" to R.string.lang_en
        )
        for ((code, nameRes) in codes) {
            row.addView(MaterialButton(this).apply {
                text = getString(nameRes)
                alpha = if (code == current) 1.0f else 0.55f
                setOnClickListener {
                    prefs.edit().putString(Prefs.KEY_APP_LANG, code).apply()
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(code))
                    recreate()
                }
            })
        }
        add(row)
    }

    private fun keyboardLangs() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        add(TextView(this).apply {
            text = getString(R.string.keyboard_langs_title)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 24, 0, 8)
        })
        val enabledLangs = Prefs.langs(this).toMutableList()
        for (code in Layouts.CODES) {
            val labelRes = when (code) {
                "ca" -> R.string.lang_ca
                "es" -> R.string.lang_es
                else -> R.string.lang_en
            }
            val sw = mSwitch(labelRes, code in enabledLangs) { }
            sw.setOnCheckedChangeListener { _, checked ->
                if (checked) {
                    if (code !in enabledLangs) enabledLangs.add(code)
                } else {
                    if (enabledLangs.size == 1) {
                        sw.isChecked = true
                        return@setOnCheckedChangeListener
                    }
                    enabledLangs.remove(code)
                }
                prefs.edit().putString(
                    Prefs.KEY_LANGS,
                    Layouts.CODES.filter { it in enabledLangs }.joinToString(",")
                ).apply()
            }
        }
    }

    private fun marginsBlock() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val indep = mSwitch(R.string.switch_indep_margins, Prefs.marginIndep(this)) { }
        val sym = mSlider(R.string.slider_margin_side, 0f, 12f, Prefs.marginSide(this),
            { getString(R.string.fmt_percent_width, it.toInt()) })
        { v -> putF(Prefs.KEY_MARGIN_SIDE, v) }
        val left = mSlider(R.string.slider_margin_left, 0f, 12f, Prefs.marginLeft(this),
            { getString(R.string.fmt_percent_width, it.toInt()) })
        { v -> putF(Prefs.KEY_MARGIN_LEFT, v) }
        val right = mSlider(R.string.slider_margin_right, 0f, 12f, Prefs.marginRight(this),
            { getString(R.string.fmt_percent_width, it.toInt()) })
        { v -> putF(Prefs.KEY_MARGIN_RIGHT, v) }
        fun refresh() {
            val i = indep.isChecked
            sym.isEnabled = !i
            left.isEnabled = i
            right.isEnabled = i
        }
        indep.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val s = Prefs.marginSide(this)
                prefs.edit().putFloat(Prefs.KEY_MARGIN_LEFT, s)
                    .putFloat(Prefs.KEY_MARGIN_RIGHT, s)
                    .putBoolean(Prefs.KEY_MARGIN_INDEP, true).apply()
                left.value = s.coerceIn(left.valueFrom, left.valueTo)
                right.value = s.coerceIn(right.valueFrom, right.valueTo)
            } else {
                prefs.edit().putBoolean(Prefs.KEY_MARGIN_INDEP, false).apply()
            }
            refresh()
        }
        refresh()
    }

    private fun bigramsBlock() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val summary = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 8)
        }
        fun refreshSummary() {
            val lines = Layouts.CODES.mapNotNull { code ->
                val f = java.io.File(filesDir, "bigrams_$code.txt")
                val n = if (f.exists()) f.readLines().size else 0
                if (n > 0) "${code.uppercase()}: $n" else null
            }
            summary.text = if (lines.isEmpty()) getString(R.string.bigrams_empty)
            else getString(R.string.bigrams_summary, lines.joinToString(" · "))
        }
        refreshSummary()
        add(summary)

        val sw = MaterialSwitch(this).apply {
            text = getString(R.string.switch_bigrams)
            textSize = 16f
            setTextColor(Color.WHITE)
            isChecked = Prefs.bigrams(this@SettingsActivity)
            setPadding(0, 16, 0, 16)
        }
        sw.setOnCheckedChangeListener { _, checked ->
            if (checked && !prefs.getBoolean(Prefs.KEY_BIGRAMS, false)) {
                sw.isChecked = false
                showBigramsWarning {
                    prefs.edit().putBoolean(Prefs.KEY_BIGRAMS, true).apply()
                    sw.isChecked = true
                }
            } else {
                prefs.edit().putBoolean(Prefs.KEY_BIGRAMS, checked).apply()
            }
        }
        add(sw)

        add(MaterialButton(this).apply {
            text = getString(R.string.btn_delete_bigrams)
            setOnClickListener {
                var n = 0
                for (code in Layouts.CODES) {
                    val f = java.io.File(filesDir, "bigrams_$code.txt")
                    if (f.exists()) { f.delete(); n++ }
                }
                text = if (n > 0) getString(R.string.deleted_n, n) else getString(R.string.no_register)
                refreshSummary()
            }
        })

        add(MaterialButton(this).apply {
            text = getString(R.string.btn_view_bigrams)
            setOnClickListener {
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                val intent = km.createConfirmDeviceCredentialIntent(
                    getString(R.string.unlock_title),
                    getString(R.string.unlock_desc)
                )
                if (intent != null) startActivityForResult(intent, reqUnlock)
                else showRegister()
            }
        })
    }

    private fun showBigramsWarning(onAccept: () -> Unit) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.bigrams_warning_title))
            .setMessage(getString(R.string.bigrams_warning_body))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.bigrams_warning_cancel), null)
            .setPositiveButton(getString(R.string.bigrams_warning_confirm, 5), null)
            .create()
        dialog.show()
        val button = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        button.isEnabled = false
        var remaining = 5
        val handler = Handler(Looper.getMainLooper())
        val tick = object : Runnable {
            override fun run() {
                remaining -= 1
                if (remaining > 0) {
                    button.text = getString(R.string.bigrams_warning_confirm, remaining)
                    handler.postDelayed(this, 1000L)
                } else {
                    button.text = getString(R.string.bigrams_warning_ok)
                    button.isEnabled = true
                    button.setOnClickListener {
                        dialog.dismiss()
                        onAccept()
                    }
                }
            }
        }
        handler.postDelayed(tick, 1000L)
    }

    @Deprecated("API antiga però suficient per al prototip.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == reqUnlock && resultCode == RESULT_OK) showRegister()
    }

    private fun showRegister() {
        val sb = StringBuilder()
        for (code in Layouts.CODES) {
            val f = java.io.File(filesDir, "bigrams_$code.txt")
            if (f.exists()) {
                sb.appendLine("===== ${code.uppercase()} =====")
                sb.append(f.readText())
                sb.appendLine()
            }
        }
        val view = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#222222"))
            addView(TextView(this@SettingsActivity).apply {
                text = if (sb.isEmpty()) getString(R.string.register_empty) else sb.toString()
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(32, 32, 32, 32)
                setTextIsSelectable(true)
            })
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.register_title))
            .setView(view)
            .setPositiveButton(getString(R.string.close), null)
            .show()
    }
}

class ThumbImeService : InputMethodService() {

    private var gridView: DualGridView? = null

    override fun onCreateInputView(): View {
        val view = DualGridView(this)
        view.textTarget = object : TextTarget {
            override fun commit(text: String) {
                currentInputConnection?.commitText(text, 1)
            }
            override fun deleteBefore(): String {
                val ic = currentInputConnection ?: return ""
                val before = ic.getTextBeforeCursor(1, 0)?.toString() ?: ""
                if (before.isNotEmpty()) ic.deleteSurroundingText(1, 0)
                return before
            }
            override fun enter() {
                val ic = currentInputConnection ?: return
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            override fun peekBefore(n: Int): String =
                currentInputConnection?.getTextBeforeCursor(n, 0)?.toString() ?: ""

            override fun moveCursor(delta: Int, wordMode: Boolean) {
                val ic = currentInputConnection ?: return
                val before = ic.getTextBeforeCursor(5000, 0)?.toString()
                val after = ic.getTextAfterCursor(5000, 0)?.toString()
                if (before == null || after == null) return
                val chars = if (!wordMode) delta
                else if (delta > 0) {
                    var pos = 0
                    repeat(delta) {
                        val idx = after.indexOf(' ', pos)
                        pos = if (idx < 0) after.length else idx + 1
                    }
                    pos
                } else {
                    var pos = before.length
                    repeat(-delta) {
                        var t = before.substring(0, pos)
                        while (t.endsWith(" ")) t = t.dropLast(1)
                        val idx = t.lastIndexOf(' ')
                        pos = if (idx < 0) 0 else idx + 1
                    }
                    pos - before.length
                }
                val clamped = chars.coerceIn(-before.length, after.length)
                if (clamped == 0) return
                val code = if (clamped < 0) KeyEvent.KEYCODE_DPAD_LEFT
                else KeyEvent.KEYCODE_DPAD_RIGHT
                repeat(abs(clamped)) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
                }
            }
        }
        gridView = view
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (!Prefs.onboarded(this)) {
            val sys = resources.configuration.locales.get(0)?.language ?: "ca"
            val code = if (sys in Layouts.CODES) sys else "ca"
            Prefs.setLang(this, code)
            Prefs.setOnboarded(this, true)
        }
        gridView?.reloadPrefs()
        gridView?.sensitiveField = isSensitiveField(info)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int,
        newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
            candidatesStart, candidatesEnd)
        gridView?.invalidate()
    }

    private fun isSensitiveField(info: EditorInfo?): Boolean {
        val type = info?.inputType ?: return false
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        val textSensitive = cls == InputType.TYPE_CLASS_TEXT && (
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                        variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                        variation == InputType.TYPE_TEXT_VARIATION_URI
                )
        val numberSensitive = cls == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        return textSensitive || numberSensitive
    }
}

class DualGridView(context: Context) : View(context) {

    var textTarget: TextTarget? = null
    var sensitiveField = false

    private enum class GridSide { LEFT, RIGHT }
    private enum class Zone { LEFT, RIGHT, SPACE, BAR, SLIDER, CAPSULE }
    private enum class ShiftMode { OFF, ONCE, CAPS }
    private enum class Icon { SHIFT, CAPS, ENTER, BACK, REDO }

    private val barPages = listOf(
        listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
        listOf("\"", "(", ")", "@", "#", "€", "%", "&", "_", "-"),
        listOf("+", "=", ";", ":", "/", "\\", "|", "<", ">", "*")
    )

    private val autoSpaceChars = ".?!,;:"

    private data class PointerState(
        val pointerId: Int,
        val zone: Zone,
        val side: GridSide?,
        val originRow: Int,
        val originCol: Int,
        val cellIndex: Int,
        val startX: Float,
        val startY: Float,
        val startTime: Long,
        var currentX: Float,
        var currentY: Float,
        var lastX: Float = startX,
        var lastTime: Long = 0L,
        var cursorAcc: Float = 0f,
        var eraseAcc: Float = 0f,
        var redoAcc: Float = 0f,
        var deleted: Int = 0,
        var redone: Int = 0,
        var edgeRunnable: Runnable? = null,
        var edgeInterval: Long = 120L,
        var knobDrag: Boolean = false,
        var grabX: Float = 0f,
        var knobP: Float = 0f
    )

    private class PressAnim(
        val side: GridSide, val row: Int, val col: Int,
        val start: Long, var release: Long = -1L,
        var sel: SwipeDir = SwipeDir.TAP
    )

    private class FeedbackAnim(
        val side: GridSide?, val row: Int, val col: Int,
        val pill: Boolean, val char: String, val start: Long
    )

    private class GlyphSlide(val icon: Icon, val dir: SwipeDir, val start: Long)

    private val activePointers = mutableMapOf<Int, PointerState>()
    private val undoStack = ArrayDeque<String>()
    private val handler = Handler(Looper.getMainLooper())

    private val presses = mutableMapOf<Int, PressAnim>()
    private val fadingPresses = mutableListOf<PressAnim>()
    private val feedbacks = mutableListOf<FeedbackAnim>()
    private val slides = mutableListOf<GlyphSlide>()

    private var barVisible = false
    private var barPage = 0
    private var barP = 0f
    private var barTarget = 0f
    private var barAnimStart = 0L
    private var barAnimFrom = 0f

    private var shiftMode = ShiftMode.OFF
    private var lastUpSwipeTime = 0L
    private var lastCenterTapTime = 0L
    private var lastLetter: String? = null

    private var lastCapsVisual = false
    private var caseInitialized = false
    private var caseAnimStart = -1L
    private var caseFromUpper = false
    private var attachTime = 0L

    private var capsuleAnimStart = -1L
    private var capsuleOld = ""

    private var knobP = 0f
    private var knobSpringStart = -1L
    private var knobSpringFrom = 0f
    private var tickOffset = 0f

    private var pillWasDown = false
    private var pillReleasedAt = -1L

    private var heightFraction = Prefs.height(context)
    private var gapFraction = Prefs.gap(context)
    private var swipeThreshold = Prefs.threshold(context) * resources.displayMetrics.density
    private var doubleWindowMs = Prefs.doubleMs(context)
    private var autoCap = Prefs.autoCap(context)
    private var autoSpace = Prefs.autoSpace(context)
    private var cursorWord = Prefs.cursorWord(context)
    private var sensChars = Prefs.sensChars(context)
    private var sensErase = Prefs.sensErase(context)
    private var flickWindowMs = Prefs.flickMs(context)
    private var showHints = Prefs.showHints(context)
    private var bigramsEnabled = Prefs.bigrams(context)
    private var marginLeftPct = Prefs.marginLeft(context)
    private var marginRightPct = Prefs.marginRight(context)
    private var marginBottomPct = Prefs.marginBottom(context)
    private var animStrong = Prefs.animStrong(context)
    private var animDepth = Prefs.animDepth(context)
    private var animHelp = Prefs.animHelp(context)
    private var animTrace = Prefs.animTrace(context)
    private var animScale = Prefs.animScale(context) / 100f
    private var vmax = Prefs.vmax(context)
    private var caseMs = Prefs.caseMs(context)
    private var lang = Prefs.lang(context)
    private var layout = Layouts.forLang(lang)
    private var theme = Themes.current(context)

    private var scale = 1f
    private fun px(v: Float) = v * scale * resources.displayMetrics.density
    private fun dur(base: Long) = if (animScale <= 0f) 0L else (base * animScale).toLong()
    private fun blobDelay() = dur(90)
    private fun fbDur() = dur(500)
    private fun slideDur() = dur(350)
    private fun caseDur(): Long = if (caseMs < 150f) 0L else dur(caseMs.toLong())
    private fun glyphColor() = if (theme.isDark) Color.WHITE else Color.BLACK

    private val fontRegular: Typeface by lazy { loadFont("outfit_400.ttf") }
    private val fontMedium: Typeface by lazy { loadFont("outfit_500.ttf") }
    private val fontSemiBold: Typeface by lazy { loadFont("outfit_600.ttf") }

    private fun loadFont(name: String): Typeface = try {
        Typeface.createFromAsset(context.assets, "fonts/$name")
    } catch (e: Exception) {
        Typeface.DEFAULT
    }

    fun reloadPrefs() {
        heightFraction = Prefs.height(context)
        gapFraction = Prefs.gap(context)
        swipeThreshold = Prefs.threshold(context) * resources.displayMetrics.density
        doubleWindowMs = Prefs.doubleMs(context)
        autoCap = Prefs.autoCap(context)
        autoSpace = Prefs.autoSpace(context)
        cursorWord = Prefs.cursorWord(context)
        sensChars = Prefs.sensChars(context)
        sensErase = Prefs.sensErase(context)
        flickWindowMs = Prefs.flickMs(context)
        showHints = Prefs.showHints(context)
        bigramsEnabled = Prefs.bigrams(context)
        marginLeftPct = Prefs.marginLeft(context)
        marginRightPct = Prefs.marginRight(context)
        marginBottomPct = Prefs.marginBottom(context)
        animStrong = Prefs.animStrong(context)
        animDepth = Prefs.animDepth(context)
        animHelp = Prefs.animHelp(context)
        animTrace = Prefs.animTrace(context)
        animScale = Prefs.animScale(context) / 100f
        vmax = Prefs.vmax(context)
        caseMs = Prefs.caseMs(context)
        lang = Prefs.lang(context)
        layout = Layouts.forLang(lang)
        theme = Themes.current(context)
        scale = (resources.displayMetrics.widthPixels /
                resources.displayMetrics.density) / 430f
        requestLayout()
        invalidate()
    }

    init {
        scale = (resources.displayMetrics.widthPixels /
                resources.displayMetrics.density) / 430f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        attachTime = SystemClock.uptimeMillis()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        val desired = (resources.displayMetrics.heightPixels * heightFraction).toInt()
        val h = when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> minOf(desired, size)
            else -> desired
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), h)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        activePointers.keys.toList().forEach { stopEdgeRepeat(it) }
        handler.removeCallbacks(animLoop)
    }

    private fun cubicOut(t: Float): Float {
        val u = 1f - t.coerceIn(0f, 1f)
        return 1f - u * u * u
    }

    private fun t01(start: Long, durMs: Long, now: Long): Float {
        if (durMs <= 0L) return 1f
        return ((now - start).toFloat() / durMs).coerceIn(0f, 1f)
    }

    private val animLoop = object : Runnable {
        override fun run() {
            invalidate()
            if (anyAnimActive(SystemClock.uptimeMillis())) {
                handler.postDelayed(this, 16L)
            }
        }
    }

    private fun kickAnim() {
        handler.removeCallbacks(animLoop)
        handler.post(animLoop)
    }

    private fun anyAnimActive(now: Long): Boolean {
        if (presses.isNotEmpty()) return true
        if (fadingPresses.any { t01(it.release, fbDur(), now) < 1f }) return true
        if (feedbacks.any { t01(it.start, fbDur(), now) < 1f }) return true
        if (slides.any { t01(it.start, slideDur(), now) < 1f }) return true
        if (barAnimStart >= 0L && t01(barAnimStart, dur(200), now) < 1f) return true
        if (caseAnimStart >= 0L && t01(caseAnimStart, caseDur(), now) < 1f) return true
        if (capsuleAnimStart >= 0L && t01(capsuleAnimStart, dur(150), now) < 1f) return true
        if (knobSpringStart >= 0L && t01(knobSpringStart, dur(200), now) < 1f) return true
        if (activePointers.values.any { it.knobDrag }) return true
        if (pillWasDown) return true
        if (pillReleasedAt >= 0L && t01(pillReleasedAt, fbDur(), now) < 1f) return true
        return false
    }

    private data class Geo(
        val stripH: Float, val sliderZone: Float,
        val sliderTop: Float, val sliderH: Float,
        val gridTop: Float,
        val rowH: Float, val colW: Float, val gapU: Float,
        val padH: Float, val padV: Float,
        val gapLeft: Float, val gapRight: Float, val pillW: Float,
        val contentLeft: Float, val contentWidth: Float,
        val capsuleTop: Float, val capsuleH: Float
    )

    private fun geo(): Geo {
        val padH = px(8f); val padV = px(8f); val gapU = px(8f)
        val stripH = px(48f)
        val sliderZone = px(64f)
        val sliderH = px(32f)
        val bottomExtra = height * (marginBottomPct / 100f)
        val zoneTop = height - bottomExtra - sliderZone
        val gridTop = barP * stripH
        val gridH = zoneTop - gridTop
        val contentLeft = width * (marginLeftPct / 100f)
        val contentWidth = width - contentLeft - width * (marginRightPct / 100f)
        val avail = contentWidth - 2 * padH
        val pillW = maxOf(px(24f), contentWidth * gapFraction)
        val colW = (avail - pillW - 6 * gapU) / 6f
        val gridW = 3 * colW + 2 * gapU
        val gapLeft = contentLeft + padH + gridW + gapU
        val gapRight = gapLeft + pillW
        val rowH = (gridH - 2 * padV - 2 * gapU) / 3f
        val sliderTop = zoneTop + (sliderZone - sliderH) / 2f
        val capsuleH = (sliderZone - sliderH) / 2f
        val capsuleTop = zoneTop
        return Geo(stripH, sliderZone, sliderTop, sliderH, gridTop, rowH, colW, gapU,
            padH, padV, gapLeft, gapRight, pillW, contentLeft, contentWidth,
            capsuleTop, capsuleH)
    }

    private fun cellRect(g: Geo, side: GridSide, row: Int, col: Int): RectF {
        val originX = if (side == GridSide.LEFT) g.contentLeft + g.padH
        else g.gapRight + g.gapU
        val x = originX + col * (g.colW + g.gapU)
        val y = g.gridTop + g.padV + row * (g.rowH + g.gapU)
        return RectF(x, y, x + g.colW, y + g.rowH)
    }

    private fun capsuleWidth(): Float =
        langPaint.measureText(lang.uppercase()) + px(16f)

    private fun keyAt(side: GridSide, row: Int, col: Int): KeyDef {
        val list = if (side == GridSide.LEFT) layout.left else layout.right
        return list[row * 3 + col]
    }

    private fun cycleLang() {
        val enabled = Prefs.langs(context)
        if (enabled.isEmpty()) return
        val idx = enabled.indexOf(lang).coerceAtLeast(0)
        capsuleOld = lang
        capsuleAnimStart = SystemClock.uptimeMillis()
        lang = enabled[(idx + 1) % enabled.size]
        layout = Layouts.forLang(lang)
        Prefs.setLang(context, lang)
        kickAnim()
    }

    private fun shouldAutoCap(): Boolean {
        if (!autoCap) return false
        val b = textTarget?.peekBefore(3) ?: return true
        val trimmed = b.trimEnd(' ')
        if (trimmed.isEmpty()) return true
        return trimmed.last() in ".?!\n"
    }

    private fun logBigram(prev: String, cur: String) {
        try {
            java.io.File(context.filesDir, "bigrams_$lang.txt")
                .appendText("$prev$cur\n")
        } catch (_: Exception) {
        }
    }

    private fun commitChar(raw: String): String {
        var out = raw
        if (out.length == 1 && out[0].isLetter()) {
            val upper = shiftMode != ShiftMode.OFF || shouldAutoCap()
            if (upper) out = out.uppercase()
            if (shiftMode == ShiftMode.ONCE) shiftMode = ShiftMode.OFF
        }
        textTarget?.commit(out)
        if (autoSpace && out.length == 1 && out[0] in autoSpaceChars) {
            textTarget?.commit(" ")
        }
        undoStack.clear()
        if (bigramsEnabled && !sensitiveField && out.length == 1 && out[0].isLetter()) {
            val cur = out.lowercase()
            lastLetter?.let { prev -> logBigram(prev, cur) }
            lastLetter = cur
        } else {
            lastLetter = null
        }
        invalidate()
        return out
    }

    private fun spawnFeedback(side: GridSide?, row: Int, col: Int, pill: Boolean, char: String) {
        if (!animHelp) return
        feedbacks.add(FeedbackAnim(side, row, col, pill, char, SystemClock.uptimeMillis()))
        kickAnim()
    }

    private fun cellFeedback(side: GridSide, row: Int, col: Int): FeedbackAnim? =
        feedbacks.firstOrNull { !it.pill && it.side == side && it.row == row && it.col == col }

    private fun startEdgeRepeat(id: Int, leftward: Boolean) {
        val state = activePointers[id] ?: return
        if (state.edgeRunnable != null) return
        val runnable = object : Runnable {
            override fun run() {
                val s = activePointers[id] ?: return
                if (leftward) {
                    val ch = textTarget?.deleteBefore() ?: ""
                    if (ch.isNotEmpty()) { undoStack.addLast(ch); s.deleted++ }
                } else {
                    val ch = undoStack.removeLastOrNull()
                    if (ch != null) { textTarget?.commit(ch); s.redone++ }
                }
                s.edgeInterval = maxOf(30L, (s.edgeInterval * 0.85).toLong())
                handler.postDelayed(this, s.edgeInterval)
            }
        }
        state.edgeRunnable = runnable
        state.edgeInterval = 120L
        handler.postDelayed(runnable, 250L)
    }

    private fun stopEdgeRepeat(id: Int) {
        activePointers[id]?.edgeRunnable?.let { handler.removeCallbacks(it) }
        activePointers[id]?.edgeRunnable = null
    }

    private fun knobCenterX(g: Geo): Float = g.contentLeft + g.contentWidth / 2f
    private fun knobD(g: Geo): Float = px(40f)
    private fun knobTravel(g: Geo): Float {
        val inner = g.contentWidth - 2 * px(16f)
        return inner / 2f - knobD(g) / 2f
    }

    private fun sliderMove(n: Int) {
        if (n == 0) return
        textTarget?.moveCursor(n, cursorWord)
        tickOffset += n * px(12f)
    }

    private val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val iconBasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private fun iconColor(): Int =
        if (theme.isDark) 0xFFFFFFFF.toInt() else 0xFF111111.toInt()

    private fun drawIcon(
        c: Canvas, ic: Icon, cx: Float, cy: Float, s: Float,
        withBase: Boolean = false, alpha: Int = 255
    ) {
        val col = iconColor()
        if (withBase) {
            iconBasePaint.color = blendColor(theme.role[Themes.KEYCAP], col, 0.30f)
            iconBasePaint.alpha = (alpha * 0.85f).toInt()
            c.drawCircle(cx, cy, s * 0.78f, iconBasePaint)
        }
        iconPaint.color = col
        iconPaint.alpha = alpha
        iconPaint.strokeWidth = s * 0.10f
        val h = s / 2f
        val path = Path()
        when (ic) {
            Icon.SHIFT, Icon.CAPS -> {
                val lift = if (ic == Icon.CAPS) -h * 0.35f else 0f
                path.moveTo(cx, cy - h + lift)
                path.lineTo(cx - h * 0.7f, cy - h * 0.1f + lift)
                path.lineTo(cx - h * 0.35f, cy - h * 0.1f + lift)
                path.lineTo(cx - h * 0.35f, cy + h * 0.6f + lift)
                path.lineTo(cx + h * 0.35f, cy + h * 0.6f + lift)
                path.lineTo(cx + h * 0.35f, cy - h * 0.1f + lift)
                path.lineTo(cx + h * 0.7f, cy - h * 0.1f + lift)
                path.close()
                c.drawPath(path, iconPaint)
                if (ic == Icon.CAPS) {
                    c.drawLine(cx - h * 0.7f, cy + h, cx + h * 0.7f, cy + h, iconPaint)
                }
            }
            Icon.ENTER -> {
                path.moveTo(cx + h * 0.6f, cy - h)
                path.lineTo(cx + h * 0.6f, cy + h * 0.2f)
                path.lineTo(cx - h * 0.7f, cy + h * 0.2f)
                c.drawPath(path, iconPaint)
                c.drawLine(cx - h * 0.35f, cy - h * 0.15f, cx - h * 0.7f, cy + h * 0.2f, iconPaint)
                c.drawLine(cx - h * 0.35f, cy + h * 0.55f, cx - h * 0.7f, cy + h * 0.2f, iconPaint)
            }
            Icon.BACK -> {
                path.moveTo(cx - h * 0.75f, cy)
                path.lineTo(cx - h * 0.25f, cy - h * 0.6f)
                path.lineTo(cx + h * 0.75f, cy - h * 0.6f)
                path.lineTo(cx + h * 0.75f, cy + h * 0.6f)
                path.lineTo(cx - h * 0.25f, cy + h * 0.6f)
                path.close()
                c.drawPath(path, iconPaint)
                c.drawLine(cx - h * 0.15f, cy - h * 0.25f, cx + h * 0.4f, cy + h * 0.25f, iconPaint)
                c.drawLine(cx + h * 0.4f, cy - h * 0.25f, cx - h * 0.15f, cy + h * 0.25f, iconPaint)
            }
            Icon.REDO -> {
                path.moveTo(cx - h * 0.5f, cy + h * 0.6f)
                path.lineTo(cx - h * 0.5f, cy - h * 0.1f)
                path.cubicTo(
                    cx - h * 0.5f, cy - h * 0.75f,
                    cx + h * 0.5f, cy - h * 0.75f,
                    cx + h * 0.5f, cy - h * 0.1f
                )
                path.lineTo(cx + h * 0.5f, cy + h * 0.6f)
                c.drawPath(path, iconPaint)
                c.drawLine(cx + h * 0.2f, cy + h * 0.3f, cx + h * 0.5f, cy + h * 0.6f, iconPaint)
                c.drawLine(cx + h * 0.8f, cy + h * 0.3f, cx + h * 0.5f, cy + h * 0.6f, iconPaint)
            }
        }
        iconPaint.alpha = 255
        iconBasePaint.alpha = 255
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val keycapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 6f
        color = Color.parseColor("#CCFFFFFF")
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 2f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val hintKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val langPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }

    private fun drawSoftShadow(canvas: Canvas, r: RectF, radius: Float) {
        val alphas = intArrayOf(0x14, 0x0F, 0x0A)
        for (i in alphas.indices) {
            val dy = px(2f) * (i + 1)
            val grow = px(1f) * i
            shadowPaint.color = theme.shadowColor
            shadowPaint.alpha = alphas[i]
            canvas.drawRoundRect(
                r.left - grow, r.top - grow + dy,
                r.right + grow, r.bottom + grow + dy,
                radius, radius, shadowPaint
            )
        }
        shadowPaint.alpha = 255
    }

    private fun drawHints(
        canvas: Canvas, key: KeyDef, r: RectF, capsVisual: Boolean,
        press: PressAnim?, now: Long, flashed: Boolean = false
    ) {
        hintKeyPaint.color = if (flashed) glyphColor() else theme.hint
        val baseSize = px(13f)
        val yCbase = baseSize * 0.35f
        for ((dir, raw) in key.swipes) {
            val ch = if (capsVisual) raw.uppercase() else raw
            var extra = 0f
            var sizeMul = 1f
            var bold = false
            if (press != null && animDepth) {
                val pr = pressProgress(press, now)
                extra = pr * px(6f)
                sizeMul = 1f + 0.2f * pr
                if (press.sel == dir) { sizeMul = 1f + 0.5f * pr; bold = true }
            }
            hintKeyPaint.textSize = baseSize * sizeMul
            hintKeyPaint.typeface = if (bold) fontSemiBold else fontRegular
            val cx = when (dir) {
                SwipeDir.N, SwipeDir.S -> r.centerX()
                SwipeDir.NE, SwipeDir.E, SwipeDir.SE -> r.right - r.width() * 0.16f
                else -> r.left + r.width() * 0.16f
            } + when (dir) {
                SwipeDir.E, SwipeDir.NE, SwipeDir.SE -> extra
                SwipeDir.W, SwipeDir.NW, SwipeDir.SW -> -extra
                else -> 0f
            }
            val cy = when (dir) {
                SwipeDir.N, SwipeDir.NE, SwipeDir.NW -> r.top + r.height() * 0.14f - extra
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> r.bottom - r.height() * 0.10f + extra
                else -> r.centerY()
            }
            canvas.drawText(ch, cx, cy + yCbase, hintKeyPaint)
        }
    }

    private fun pressProgress(p: PressAnim, now: Long): Float {
        return if (p.release < 0) cubicOut(t01(p.start + blobDelay(), dur(130), now))
        else 1f - cubicOut(t01(p.release, dur(100), now))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val now = SystemClock.uptimeMillis()
        val g = geo()
        val capsVisual = shiftMode != ShiftMode.OFF || shouldAutoCap()

        fadingPresses.removeAll { t01(it.release, fbDur(), now) >= 1f }
        feedbacks.removeAll { t01(it.start, fbDur(), now) >= 1f }
        slides.removeAll { t01(it.start, slideDur(), now) >= 1f }

        if (barAnimStart >= 0L) {
            val t = cubicOut(t01(barAnimStart, dur(200), now))
            barP = barAnimFrom + (barTarget - barAnimFrom) * t
            if (!animStrong) barP = barTarget
        }

        if (knobSpringStart >= 0L) {
            val t = cubicOut(t01(knobSpringStart, dur(200), now))
            knobP = knobSpringFrom * (1f - t)
            if (t >= 1f) { knobSpringStart = -1L; knobP = 0f }
            if (!animDepth) { knobSpringStart = -1L; knobP = 0f }
        }

        if (!caseInitialized) {
            caseInitialized = true
            lastCapsVisual = capsVisual
        } else if (capsVisual != lastCapsVisual) {
            caseFromUpper = lastCapsVisual
            val allow = caseDur() > 0L && animStrong && (now - attachTime > 300L)
            caseAnimStart = if (allow) now else -1L
            lastCapsVisual = capsVisual
            if (caseAnimStart >= 0L) kickAnim()
        }
        val caseT = if (caseAnimStart >= 0L) cubicOut(t01(caseAnimStart, caseDur(), now)) else 1f

        canvas.drawColor(theme.role[Themes.BG])

        if (barP > 0.001f) {
            canvas.save()
            canvas.clipRect(0f, 0f, width.toFloat(), g.stripH)
            canvas.translate(0f, (1f - barP) * g.stripH)
            canvas.drawRect(0f, 0f, width.toFloat(), g.stripH,
                keycapPaint.apply { color = theme.role[Themes.STRIP] })
            val cw = g.contentWidth / 10f
            val page = barPages[barPage]
            textPaint.color = theme.text
            textPaint.textSize = px(20f)
            textPaint.typeface = fontMedium
            val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
            for (i in 0..9) {
                canvas.drawText(page[i], g.contentLeft + i * cw + cw / 2f,
                    g.stripH / 2f + yC, textPaint)
            }
            hintPaint.color = theme.hint
            hintPaint.textSize = px(10f)
            for (p in barPages.indices) {
                val dotX = g.contentLeft + g.contentWidth / 2f + (p - 1) * px(14f)
                hintPaint.alpha = if (p == barPage) 255 else 120
                canvas.drawText("•", dotX, g.stripH - px(6f), hintPaint)
            }
            hintPaint.alpha = 255
            canvas.restore()
        }

        fun dimFor(r: RectF): Float {
            if (!animDepth) return 0f
            var d = 0f
            val all = presses.values.toList() + fadingPresses
            for (p in all) {
                val pr = pressProgress(p, now)
                if (pr <= 0f) continue
                val pc = cellRect(g, p.side, p.row, p.col)
                val dist = hypot(pc.centerX() - r.centerX(), pc.centerY() - r.centerY())
                val fall = (1f - dist / (3.5f * g.colW)).coerceIn(0f, 1f)
                d = maxOf(d, pr * fall)
            }
            return d
        }

        val radius = px(16f)
        for (side in listOf(GridSide.LEFT, GridSide.RIGHT)) {
            for (row in 0..2) {
                for (col in 0..2) {
                    val r = cellRect(g, side, row, col)
                    val press = presses.values.firstOrNull {
                        it.side == side && it.row == row && it.col == col
                    }
                    val fading = fadingPresses.firstOrNull {
                        it.side == side && it.row == row && it.col == col
                    }
                    val lifted = press != null && animDepth && pressProgress(press, now) > 0.05f

                    var capColor = theme.role[Themes.KEYCAP]
                    val dim = dimFor(r)
                    if (dim > 0f) capColor = blendColor(capColor, theme.pole, dim * 0.35f)

                    val fb = cellFeedback(side, row, col)
                    val fbT = if (fb != null) t01(fb.start, fbDur(), now) else 1f
                    val flash = if (!animHelp) 0f else when {
                        press != null -> 0.45f
                        fading != null -> 0.45f * (1f - t01(fading.release, fbDur(), now))
                        else -> 0f
                    }
                    if (flash > 0f) capColor = blendColor(capColor, theme.flashPole, flash)

                    if (theme.keycapShadow && !lifted) drawSoftShadow(canvas, r, radius)
                    keycapPaint.color = capColor
                    canvas.drawRoundRect(r, radius, radius, keycapPaint)

                    val key = keyAt(side, row, col)

                    canvas.save()
                    canvas.clipRect(r)
                    val yC = -(letterPaint.descent() + letterPaint.ascent()) / 2f
                    val midY = r.centerY() + yC
                    letterPaint.textSize = px(28f)
                    letterPaint.color = theme.text
                    val lower = key.tap
                    val upper = key.tap.uppercase()
                    if (fb != null && fbT < 0.75f) {
                        letterPaint.typeface = fontSemiBold
                        canvas.drawText(fb.char, r.centerX(), midY, letterPaint)
                    } else if (fb != null) {
                        val u = (fbT - 0.75f) / 0.25f
                        letterPaint.typeface = fontSemiBold
                        letterPaint.alpha = (255 * (1f - u)).toInt()
                        canvas.drawText(fb.char, r.centerX(), midY, letterPaint)
                        letterPaint.typeface = if (capsVisual) fontSemiBold else fontMedium
                        letterPaint.alpha = (255 * u).toInt()
                        canvas.drawText(if (capsVisual) upper else lower, r.centerX(), midY, letterPaint)
                        letterPaint.alpha = 255
                    } else if (!lifted && animStrong && caseT < 1f) {
                        val H = r.height()
                        val gap = 0.25f * H
                        val a = 0.4286f
                        val b = 0.5714f
                        val t = caseT
                        val yIn: Float
                        val yOut: Float
                        when {
                            t < a -> {
                                val u = t / a
                                yIn = H - u * (H - gap)
                                yOut = 0f
                            }
                            t < b -> {
                                val u = (t - a) / (b - a)
                                yIn = gap - u * gap
                                yOut = -u * gap
                            }
                            else -> {
                                val u = (t - b) / (1f - b)
                                yIn = 0f
                                yOut = -gap - u * (H - gap)
                            }
                        }
                        val inCh = if (capsVisual) upper else lower
                        val outCh = if (caseFromUpper) upper else lower
                        letterPaint.typeface = if (capsVisual) fontSemiBold else fontMedium
                        canvas.drawText(inCh, r.centerX(), midY + yIn, letterPaint)
                        letterPaint.typeface = if (caseFromUpper) fontSemiBold else fontMedium
                        canvas.drawText(outCh, r.centerX(), midY + yOut, letterPaint)
                    } else if (!lifted) {
                        val boldSel = press != null && press.sel == SwipeDir.TAP && animDepth
                        letterPaint.typeface =
                            if (capsVisual || boldSel) fontSemiBold else fontMedium
                        canvas.drawText(if (capsVisual) upper else lower, r.centerX(), midY, letterPaint)
                    }
                    canvas.restore()

                    if (showHints && !lifted && fb == null) {
                        val flashed = animHelp && (press != null || fading != null)
                        drawHints(canvas, key, r, capsVisual, press, now, flashed)
                    }
                }
            }
        }

        if (animDepth) {
            val all = presses.values.toList() + fadingPresses
            for (p in all) {
                val pr = pressProgress(p, now)
                if (pr <= 0f) continue
                val r = cellRect(g, p.side, p.row, p.col)
                val sc = 1f + 0.35f * pr
                val cx = r.centerX(); val cy = r.centerY()
                val lr = RectF(
                    cx - r.width() * sc / 2f, cy - r.height() * sc / 2f,
                    cx + r.width() * sc / 2f, cy + r.height() * sc / 2f
                )
                val liftFlash = if (!animHelp) 0f else when {
                    p.release < 0 -> 0.45f
                    else -> 0.45f * (1f - t01(p.release, fbDur(), now))
                }
                val liftColor =
                    if (liftFlash > 0f) blendColor(theme.role[Themes.KEYCAP], theme.flashPole, liftFlash)
                    else theme.role[Themes.KEYCAP]
                drawSoftShadow(canvas, lr, radius * sc)
                keycapPaint.color = liftColor
                canvas.drawRoundRect(lr, radius * sc, radius * sc, keycapPaint)

                val key = keyAt(p.side, p.row, p.col)
                val fb = cellFeedback(p.side, p.row, p.col)
                val fbT = if (fb != null) t01(fb.start, fbDur(), now) else 1f
                val label = if (fb != null && fbT < 0.75f) fb.char
                else if (capsVisual) key.tap.uppercase() else key.tap
                val yC = -(letterPaint.descent() + letterPaint.ascent()) / 2f
                letterPaint.textSize = px(28f)
                val centerScale = if (p.sel != SwipeDir.TAP) 0.9f else 1f
                letterPaint.typeface =
                    if (fb != null || p.sel == SwipeDir.TAP || capsVisual) fontSemiBold else fontMedium
                letterPaint.color = theme.text
                canvas.save()
                canvas.translate(cx, cy + yC)
                canvas.scale(centerScale, centerScale)
                canvas.drawText(label, 0f, 0f, letterPaint)
                canvas.restore()

                if (fb == null) drawHints(canvas, key, lr, capsVisual, p, now, flashed = animHelp)
            }
        }

        val pillDownNow = activePointers.values.any { it.zone == Zone.SPACE }
        if (!pillDownNow && pillWasDown) pillReleasedAt = now
        pillWasDown = pillDownNow

        val pillRect = RectF(g.gapLeft, g.gridTop + g.padV,
            g.gapRight, g.gridTop + g.padV + 3 * g.rowH + 2 * g.gapU)
        var pillColor = theme.role[Themes.KEYCAP]
        val pillFb = feedbacks.firstOrNull { it.pill }
        val pillFbT = if (pillFb != null) t01(pillFb.start, fbDur(), now) else 1f
        val pillFlash = if (!animHelp) 0f else when {
            pillDownNow -> 0.45f
            pillReleasedAt >= 0 -> 0.45f * (1f - t01(pillReleasedAt, fbDur(), now))
            else -> 0f
        }
        if (pillFlash > 0f) pillColor = blendColor(pillColor, theme.flashPole, pillFlash)
        if (theme.keycapShadow) drawSoftShadow(canvas, pillRect, g.pillW / 2f)
        keycapPaint.color = pillColor
        canvas.drawRoundRect(pillRect, g.pillW / 2f, g.pillW / 2f, keycapPaint)

        if (shiftMode == ShiftMode.CAPS) {
            drawIcon(canvas, Icon.CAPS, pillRect.centerX(), pillRect.centerY(), px(24f))
        } else if (pillFb != null && pillFbT < 0.75f) {
            hintPaint.color = theme.text
            hintPaint.textSize = px(18f)
            hintPaint.typeface = fontSemiBold
            val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
            canvas.drawText(pillFb.char, pillRect.centerX(), pillRect.centerY() + pillYC, hintPaint)
        } else if (pillFb != null) {
            val u = (pillFbT - 0.75f) / 0.25f
            hintPaint.color = theme.text
            hintPaint.textSize = px(18f)
            hintPaint.typeface = fontSemiBold
            hintPaint.alpha = (255 * (1f - u)).toInt()
            val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
            canvas.drawText(pillFb.char, pillRect.centerX(), pillRect.centerY() + pillYC, hintPaint)
            hintPaint.alpha = 255
        } else {
            hintPaint.color = theme.hint
            hintPaint.textSize = px(18f)
            hintPaint.typeface = fontRegular
            val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
            canvas.drawText("␣", pillRect.centerX(), pillRect.centerY() + pillYC, hintPaint)
        }

        for (s in slides.toList()) {
            val tr = t01(s.start, slideDur(), now)
            val t = cubicOut(tr)
            val fade = ((tr - 0.55f) / 0.45f).coerceIn(0f, 1f)
            val alpha = (255 * (1f - fade)).toInt()
            val travel = pillRect.height() * 0.35f
            val dx = when (s.dir) {
                SwipeDir.W -> -travel * t
                SwipeDir.E -> travel * t
                else -> 0f
            }
            val dy = when (s.dir) {
                SwipeDir.N, SwipeDir.NE, SwipeDir.NW -> -travel * t
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> travel * t
                else -> 0f
            }
            val withBase = (s.icon == Icon.BACK || s.icon == Icon.REDO)
            drawIcon(canvas, s.icon, pillRect.centerX() + dx, pillRect.centerY() + dy,
                px(26f), withBase = withBase, alpha = alpha)
        }

        val capW = capsuleWidth()
        val capRect = RectF(g.contentLeft + px(8f), g.capsuleTop,
            g.contentLeft + px(8f) + capW, g.capsuleTop + g.capsuleH)
        keycapPaint.color = theme.role[Themes.SPECIAL]
        canvas.drawRoundRect(capRect, g.capsuleH / 2f, g.capsuleH / 2f, keycapPaint)
        langPaint.color = theme.text
        langPaint.textSize = px(11f)
        langPaint.typeface = fontMedium
        val capYC = -(langPaint.descent() + langPaint.ascent()) / 2f
        val capT = if (capsuleAnimStart >= 0L && animStrong)
            cubicOut(t01(capsuleAnimStart, dur(150), now)) else 1f
        if (capT < 1f) {
            langPaint.alpha = (255 * (1f - capT)).toInt()
            canvas.drawText(capsuleOld.uppercase(), capRect.centerX(),
                capRect.centerY() + capYC - (1f - capT) * px(6f), langPaint)
            langPaint.alpha = (255 * capT).toInt()
            canvas.drawText(lang.uppercase(), capRect.centerX(),
                capRect.centerY() + capYC + (1f - capT) * px(6f), langPaint)
            langPaint.alpha = 255
        } else {
            canvas.drawText(lang.uppercase(), capRect.centerX(), capRect.centerY() + capYC, langPaint)
        }

        val sliderRect = RectF(g.contentLeft + px(16f), g.sliderTop,
            g.contentLeft + g.contentWidth - px(16f), g.sliderTop + g.sliderH)
        keycapPaint.color = theme.role[Themes.SLIDER]
        canvas.drawRoundRect(sliderRect, g.sliderH / 2f, g.sliderH / 2f, keycapPaint)
        tickPaint.color = theme.hint
        tickPaint.alpha = 110
        val tickSpacing = px(12f)
        val mod = ((tickOffset % tickSpacing) + tickSpacing) % tickSpacing
        var tx = sliderRect.left + tickSpacing - mod
        while (tx < sliderRect.right - tickSpacing / 2f) {
            if (tx > sliderRect.left + tickSpacing / 2f) {
                canvas.drawLine(tx, sliderRect.centerY() - px(6f),
                    tx, sliderRect.centerY() + px(6f), tickPaint)
            }
            tx += tickSpacing
        }
        tickPaint.alpha = 255

        val kd = knobD(g)
        val kcx = knobCenterX(g) + knobP * knobTravel(g)
        val knobRect = RectF(kcx - kd / 2f, sliderRect.centerY() - kd / 2f,
            kcx + kd / 2f, sliderRect.centerY() + kd / 2f)
        drawSoftShadow(canvas, knobRect, kd / 2f)
        keycapPaint.color = theme.role[Themes.KNOB]
        canvas.drawRoundRect(knobRect, kd / 2f, kd / 2f, keycapPaint)
        tickPaint.color = theme.hint
        for (i in -1..1) {
            val bx = knobRect.centerX() + i * px(6f)
            canvas.drawLine(bx, knobRect.centerY() - px(8f),
                bx, knobRect.centerY() + px(8f), tickPaint)
        }

        if (animTrace) {
            for (state in activePointers.values) {
                linePaint.strokeCap = Paint.Cap.ROUND
                linePaint.color = theme.text
                linePaint.alpha = 55
                linePaint.strokeWidth = px(10f)
                canvas.drawLine(state.startX, state.startY, state.currentX, state.currentY, linePaint)
                linePaint.alpha = 140
                linePaint.strokeWidth = px(3f)
                canvas.drawLine(state.startX, state.startY, state.currentX, state.currentY, linePaint)
                linePaint.alpha = 255
            }
        }

        // Blindatge anti-fotograma-congelat: si queda alguna animació viva,
        // el propi onDraw re-programa el següent fotograma. Cap marc a mig
        // fer pot quedar-se pintat per sempre (adiós "L levitant" i glifs encallats).
        if (caseAnimStart >= 0L && caseT >= 1f) caseAnimStart = -1L
        if (anyAnimActive(now)) kickAnim()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return true

        val g = geo()
        val edgeZone = 24 * resources.displayMetrics.density
        val now = SystemClock.uptimeMillis()

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = event.getX(index)
                val y = event.getY(index)

                var zone = Zone.LEFT
                var side: GridSide? = null
                var row = 0; var col = 0; var cellIndex = 0
                var knobDrag = false

                val capW = capsuleWidth()
                val capExtra = px(12f)
                val kd = knobD(g)
                val kcx = knobCenterX(g) + knobP * knobTravel(g)
                when {
                    barP > 0.5f && y < g.gridTop -> {
                        zone = Zone.BAR
                        cellIndex = ((x - g.contentLeft) / (g.contentWidth / 10f))
                            .toInt().coerceIn(0, 9)
                    }
                    y >= g.capsuleTop - capExtra && y <= g.capsuleTop + g.capsuleH + capExtra &&
                            x >= g.contentLeft && x <= g.contentLeft + px(8f) + capW + capExtra -> {
                        zone = Zone.CAPSULE
                    }
                    y >= g.sliderTop - capExtra -> {
                        zone = Zone.SLIDER
                        if (abs(x - kcx) < kd) {
                            knobDrag = true
                            knobSpringStart = -1L
                        }
                    }
                    x < g.gapLeft && x >= g.contentLeft -> {
                        side = GridSide.LEFT
                        zone = Zone.LEFT
                        col = ((x - (g.contentLeft + g.padH)) / (g.colW + g.gapU)).toInt().coerceIn(0, 2)
                        row = ((y - (g.gridTop + g.padV)) / (g.rowH + g.gapU)).toInt().coerceIn(0, 2)
                    }
                    x > g.gapRight && x <= g.contentLeft + g.contentWidth -> {
                        side = GridSide.RIGHT
                        zone = Zone.RIGHT
                        col = ((x - (g.gapRight + g.gapU)) / (g.colW + g.gapU)).toInt().coerceIn(0, 2)
                        row = ((y - (g.gridTop + g.padV)) / (g.rowH + g.gapU)).toInt().coerceIn(0, 2)
                    }
                    else -> zone = Zone.SPACE
                }

                val st = PointerState(
                    id, zone, side, row, col, cellIndex, x, y,
                    startTime = event.eventTime,
                    currentX = x, currentY = y,
                    lastX = x, lastTime = event.eventTime,
                    knobDrag = knobDrag, grabX = x
                )
                activePointers[id] = st

                if (side != null) {
                    presses[id] = PressAnim(side, row, col, now)
                    kickAnim()
                }
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val id = event.getPointerId(i)
                    val state = activePointers[id] ?: continue
                    val x = event.getX(i)
                    val y = event.getY(i)
                    val t = event.eventTime
                    val dt = (t - state.lastTime).coerceAtLeast(1L)
                    val dxEvent = x - state.lastX
                    state.currentX = x
                    state.currentY = y

                    if (state.zone == Zone.SLIDER) {
                        if (state.knobDrag) {
                            val travel = knobTravel(g)
                            val p = ((x - state.grabX) / travel).coerceIn(-1f, 1f)
                            state.knobP = p
                            knobP = p
                            val v = vmax * p
                            state.cursorAcc += v * dt / 1000f
                            val n = state.cursorAcc.toInt()
                            if (n != 0) { sliderMove(n); state.cursorAcc -= n }
                            kickAnim()
                        } else {
                            val speed = abs(dxEvent) / dt.toFloat()
                            val mult = 1f + minOf(3f, maxOf(0f, speed - 0.4f) / 0.6f)
                            val pxPerCursorUnit =
                                if (cursorWord) width * 5f / sensChars else width / sensChars
                            state.cursorAcc += dxEvent * mult / pxPerCursorUnit
                            val n = state.cursorAcc.toInt()
                            if (n != 0) { sliderMove(n); state.cursorAcc -= n }
                        }
                    } else if (state.zone == Zone.SPACE) {
                        val pxPerEraseUnit = width / sensErase
                        val dx = x - state.startX
                        if (x <= edgeZone && dx < 0) startEdgeRepeat(id, true)
                        else if (x >= width - edgeZone && dx > 0) startEdgeRepeat(id, false)
                        else stopEdgeRepeat(id)

                        val duration = t - state.startTime
                        if (duration >= flickWindowMs) {
                            if (dxEvent < 0f) {
                                state.eraseAcc += -dxEvent / pxPerEraseUnit
                                val n = state.eraseAcc.toInt()
                                if (n > 0) {
                                    state.eraseAcc -= n
                                    repeat(n) {
                                        val ch = textTarget?.deleteBefore() ?: ""
                                        if (ch.isNotEmpty()) { undoStack.addLast(ch); state.deleted++ }
                                    }
                                }
                            } else if (dxEvent > 0f) {
                                state.redoAcc += dxEvent / pxPerEraseUnit
                                val n = state.redoAcc.toInt()
                                if (n > 0) {
                                    state.redoAcc -= n
                                    repeat(n) {
                                        val ch = undoStack.removeLastOrNull() ?: return@repeat
                                        textTarget?.commit(ch)
                                        state.redone++
                                    }
                                }
                            }
                        }
                    } else if (state.side != null) {
                        val press = presses[id]
                        if (press != null) {
                            val dx = x - state.startX
                            val dy = y - state.startY
                            val dist = hypot(dx, dy)
                            press.sel = if (dist < swipeThreshold) SwipeDir.TAP
                            else directionFrom(dx, dy)
                        }
                    }

                    state.lastX = x
                    state.lastTime = t
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                stopEdgeRepeat(id)
                val state = activePointers.remove(id) ?: return true
                presses.remove(id)?.let { p ->
                    p.release = now
                    fadingPresses.add(p)
                    kickAnim()
                }
                if (state.knobDrag) {
                    knobSpringFrom = state.knobP
                    knobSpringStart = now
                    kickAnim()
                }
                resolvePointer(state)
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointers.keys.toList().forEach { stopEdgeRepeat(it) }
                activePointers.keys.toList().forEach { id ->
                    presses.remove(id)?.let { p -> p.release = now; fadingPresses.add(p) }
                }
                activePointers.clear()
                knobSpringFrom = knobP
                knobSpringStart = now
                Log.d("GridTouch", "CANCEL")
            }
        }

        invalidate()
        return true
    }

    private fun toggleBar() {
        val now = SystemClock.uptimeMillis()
        if (now - lastCenterTapTime < doubleWindowMs.toLong()) {
            barVisible = true
            barPage = 1
            lastCenterTapTime = 0L
        } else {
            barVisible = !barVisible
            barPage = 0
            lastCenterTapTime = now
        }
        barTarget = if (barVisible) 1f else 0f
        barAnimFrom = barP
        barAnimStart = now
        if (!animStrong) barP = barTarget
        kickAnim()
    }

    private fun resolvePointer(state: PointerState) {
        val dx = state.currentX - state.startX
        val dy = state.currentY - state.startY
        val distance = hypot(dx, dy)
        val dir = if (distance < swipeThreshold) SwipeDir.TAP else directionFrom(dx, dy)

        when (state.zone) {
            Zone.BAR -> {
                if (dir == SwipeDir.TAP) {
                    commitChar(barPages[barPage][state.cellIndex])
                } else if (dir == SwipeDir.W) {
                    barPage = (barPage + 1).coerceAtMost(barPages.size - 1)
                    invalidate()
                } else if (dir == SwipeDir.E) {
                    barPage = (barPage - 1).coerceAtLeast(0)
                    invalidate()
                }
            }

            Zone.CAPSULE -> {
                if (dir == SwipeDir.TAP) cycleLang()
            }

            Zone.SLIDER -> {
                if (dir == SwipeDir.TAP) {
                    val g = geo()
                    val inCenter = state.startX > g.contentLeft + g.contentWidth / 3f &&
                            state.startX < g.contentLeft + 2f * g.contentWidth / 3f
                    val kd = knobD(g)
                    val kcx = knobCenterX(g)
                    val onKnob = abs(state.startX - kcx) < kd
                    if (inCenter || onKnob) toggleBar()
                }
            }

            Zone.SPACE -> when (dir) {
                SwipeDir.TAP -> {
                    val b = textTarget?.peekBefore(2) ?: ""
                    val blocked = autoSpace && b.length == 2 && b[1] == ' ' && b[0] in autoSpaceChars
                    if (!blocked) {
                        textTarget?.commit(" ")
                        undoStack.clear()
                        spawnFeedback(null, 0, 0, true, "␣")
                    }
                }
                SwipeDir.W -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        val ch = textTarget?.deleteBefore() ?: ""
                        if (ch.isNotEmpty()) undoStack.addLast(ch)
                    }
                    slides.add(GlyphSlide(Icon.BACK, dir, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                SwipeDir.E -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        undoStack.removeLastOrNull()?.let { textTarget?.commit(it) }
                    }
                    slides.add(GlyphSlide(Icon.REDO, dir, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> {
                    textTarget?.enter()
                    undoStack.clear()
                    slides.add(GlyphSlide(Icon.ENTER, SwipeDir.S, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                else -> {
                    val now = SystemClock.uptimeMillis()
                    val prevMode = shiftMode
                    shiftMode = when {
                        shiftMode == ShiftMode.CAPS -> ShiftMode.OFF
                        now - lastUpSwipeTime < doubleWindowMs.toLong() -> {
                            lastUpSwipeTime = 0L
                            ShiftMode.CAPS
                        }
                        else -> {
                            lastUpSwipeTime = now
                            ShiftMode.ONCE
                        }
                    }
                    val icon = if (shiftMode == ShiftMode.CAPS && prevMode != ShiftMode.CAPS) {
                        Icon.CAPS
                    } else {
                        Icon.SHIFT
                    }
                    slides.add(GlyphSlide(icon, SwipeDir.N, now))
                    kickAnim()
                }
            }

            Zone.LEFT, Zone.RIGHT -> {
                val side = state.side ?: return
                val key = keyAt(side, state.originRow, state.originCol)
                val raw = if (dir == SwipeDir.TAP) key.tap else key.swipes[dir]
                if (raw != null) {
                    val out = commitChar(raw)
                    spawnFeedback(side, state.originRow, state.originCol, pill = false, char = out)
                }
            }
        }
    }

    private fun directionFrom(dx: Float, dy: Float): SwipeDir {
        val deg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble()))
        return when {
            deg >= -22.5 && deg < 22.5 -> SwipeDir.E
            deg >= 22.5 && deg < 67.5 -> SwipeDir.SE
            deg >= 67.5 && deg < 112.5 -> SwipeDir.S
            deg >= 112.5 && deg < 157.5 -> SwipeDir.SW
            deg >= 157.5 || deg < -157.5 -> SwipeDir.W
            deg >= -157.5 && deg < -112.5 -> SwipeDir.NW
            deg >= -112.5 && deg < -67.5 -> SwipeDir.N
            else -> SwipeDir.NE
        }
    }
}