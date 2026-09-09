package com.aleix.thumbgridtest

import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import androidx.appcompat.widget.AppCompatEditText

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

    const val KEY_MARGIN_SIDE = "margin_side"
    const val KEY_MARGIN_LEFT = "margin_left"
    const val KEY_MARGIN_RIGHT = "margin_right"
    const val KEY_MARGIN_INDEP = "margin_indep"
    const val KEY_MARGIN_BOTTOM = "margin_bottom"

    const val DEFAULT_HEIGHT = 0.35f
    const val DEFAULT_GAP = 0.10f
    const val DEFAULT_THRESHOLD_DP = 24f
    const val DEFAULT_DOUBLE_MS = 350f
    const val DEFAULT_SENS_CHARS = 25f
    const val DEFAULT_SENS_ERASE = 6f
    const val DEFAULT_FLICK_MS = 180f
    const val DEFAULT_MARGIN_SIDE = 0f
    const val DEFAULT_MARGIN_BOTTOM = 12f

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
    fun bigrams(ctx: Context) = prefs(ctx).getBoolean(KEY_BIGRAMS, true)
    fun onboarded(ctx: Context) = prefs(ctx).getBoolean(KEY_ONBOARDED, false)
    fun setOnboarded(ctx: Context, v: Boolean) = prefs(ctx).edit().putBoolean(KEY_ONBOARDED, v).apply()

    fun marginSide(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_SIDE, DEFAULT_MARGIN_SIDE)
    fun marginIndep(ctx: Context) = prefs(ctx).getBoolean(KEY_MARGIN_INDEP, false)
    fun marginLeft(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_LEFT, marginSide(ctx))
        else marginSide(ctx)
    fun marginRight(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_RIGHT, marginSide(ctx))
        else marginSide(ctx)
    fun marginBottom(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM)

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

/** Barreja lineal de dos colors ARGB. */
fun blendColor(a: Int, b: Int, t: Float): Int {
    val ar = (a shr 16) and 0xFF; val ag = (a shr 8) and 0xFF; val ab = a and 0xFF
    val br = (b shr 16) and 0xFF; val bg = (b shr 8) and 0xFF; val bb = b and 0xFF
    val r = (ar + (br - ar) * t).toInt().coerceIn(0, 255)
    val g = (ag + (bg - ag) * t).toInt().coerceIn(0, 255)
    val bl = (ab + (bb - ab) * t).toInt().coerceIn(0, 255)
    return (0xFF shl 24) or (r shl 16) or (g shl 8) or bl
}

/**
 * Motor de temes: rampa de 4 passos des del color principal cap al pol del mode
 * (negre en fosc, blanc en clar), amb rols assignats per fracció de barreja.
 */
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
        return if (night) DARK else LIGHT
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

    /** EditText que avisa quan canvia la selecció, per refrescar la preview de majúscules. */
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
            hint = "Prova el teclat aquí"
            setBackgroundColor(Color.parseColor("#222222"))
            setPadding(32, 24, 32, 24)
            onSel = { if (::dualGridView.isInitialized) dualGridView.invalidate() }
        }

        val enableButton = Button(this).apply {
            text = "Obre els ajustos de teclats del sistema"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val settingsButton = Button(this).apply {
            text = "Ajustos del teclat"
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

    /** Onboarding: tria d'idioma inicial (teclat + locale de la app). */
    private fun buildChooser(): View {
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#111111"))
            setPadding(64, 96, 64, 64)
        }
        lay.addView(TextView(this).apply {
            text = "Tria el teu idioma / Elige tu idioma / Choose your language"
            textSize = 20f
            setTextColor(Color.WHITE)
            setPadding(0, 0, 0, 48)
        })
        for (code in Layouts.CODES) {
            lay.addView(Button(this).apply {
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
            .putBoolean(Prefs.KEY_ONBOARDED, true)
            .apply()
        recreate()
    }

    override fun onResume() {
        super.onResume()
        if (::dualGridView.isInitialized) dualGridView.reloadPrefs()
    }
}

class SettingsActivity : AppCompatActivity() {

    private val reqUnlock = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#111111"))
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(root, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        root.addView(TextView(this).apply {
            text = "Ajustos del teclat"
            textSize = 22f
            setTextColor(Color.WHITE)
        })

        fun slider(
            label: String, min: Int, max: Int, current: Int,
            format: (Int) -> String, onValue: (Int) -> Unit
        ): SeekBar {
            val text = TextView(this).apply {
                textSize = 17f
                setTextColor(Color.WHITE)
                setPadding(0, 32, 0, 8)
            }
            val bar = SeekBar(this).apply {
                this.max = max - min
                progress = (current - min).coerceIn(0, max - min)
                text.text = "$label: ${format(min + progress)}"
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(b: SeekBar?, p: Int, user: Boolean) {
                        if (!user) return
                        val v = min + p
                        text.text = "$label: ${format(v)}"
                        onValue(v)
                    }
                    override fun onStartTrackingTouch(b: SeekBar?) {}
                    override fun onStopTrackingTouch(b: SeekBar?) {}
                })
            }
            root.addView(text)
            root.addView(bar)
            return bar
        }

        slider("Alçada del teclat", 25, 60, (Prefs.height(this) * 100).toInt(),
            { "$it% de la pantalla" })
        { v -> prefs.edit().putFloat(Prefs.KEY_HEIGHT, v / 100f).apply() }
        slider("Separació entre graelles", 0, 15, (Prefs.gap(this) * 100).toInt(),
            { "$it% de l'amplada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_GAP, v / 100f).apply() }

        val indepBox = CheckBox(this).apply {
            text = "Marges laterals independents"
            setTextColor(Color.WHITE)
            isChecked = Prefs.marginIndep(this@SettingsActivity)
        }
        root.addView(indepBox)

        val symBar = slider("Marge lateral (simètric)", 0, 12, Prefs.marginSide(this).toInt(),
            { "$it% de l'amplada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_SIDE, v.toFloat()).apply() }
        val leftBar = slider("Marge esquerre", 0, 12, Prefs.marginLeft(this).toInt(),
            { "$it% de l'amplada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_LEFT, v.toFloat()).apply() }
        val rightBar = slider("Marge dret", 0, 12, Prefs.marginRight(this).toInt(),
            { "$it% de l'amplada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_RIGHT, v.toFloat()).apply() }

        fun refreshMarginBars() {
            val indep = indepBox.isChecked
            symBar.isEnabled = !indep
            leftBar.isEnabled = indep
            rightBar.isEnabled = indep
        }
        indepBox.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                val sym = Prefs.marginSide(this)
                prefs.edit()
                    .putFloat(Prefs.KEY_MARGIN_LEFT, sym)
                    .putFloat(Prefs.KEY_MARGIN_RIGHT, sym)
                    .putBoolean(Prefs.KEY_MARGIN_INDEP, true)
                    .apply()
                leftBar.progress = sym.toInt()
                rightBar.progress = sym.toInt()
            } else {
                prefs.edit().putBoolean(Prefs.KEY_MARGIN_INDEP, false).apply()
            }
            refreshMarginBars()
        }
        refreshMarginBars()

        slider("Marge inferior (barra)", 8, 25, Prefs.marginBottom(this).toInt(),
            { "$it% de l'alçada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_BOTTOM, v.toFloat()).apply() }
        slider("Llindar de gest", 12, 48, Prefs.threshold(this).toInt(),
            { "$it dp" })
        { v -> prefs.edit().putFloat(Prefs.KEY_THRESHOLD, v.toFloat()).apply() }
        slider("Temps de doble acció", 150, 700, Prefs.doubleMs(this).toInt(),
            { "$it ms" })
        { v -> prefs.edit().putFloat(Prefs.KEY_DOUBLE_MS, v.toFloat()).apply() }
        slider("Sensibilitat del cursor", 5, 60, Prefs.sensChars(this).toInt(),
            { "$it caràcters per lliscada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_SENS_CHARS, v.toFloat()).apply() }
        slider("Sensibilitat d'esborrat", 2, 30, Prefs.sensErase(this).toInt(),
            { "$it caràcters per lliscada" })
        { v -> prefs.edit().putFloat(Prefs.KEY_SENS_ERASE, v.toFloat()).apply() }
        slider("Finestra de flick", 80, 400, Prefs.flickMs(this).toInt(),
            { "$it ms" })
        { v -> prefs.edit().putFloat(Prefs.KEY_FLICK_MS, v.toFloat()).apply() }

        root.addView(CheckBox(this).apply {
            text = "Majúscula automàtica"
            setTextColor(Color.WHITE)
            isChecked = Prefs.autoCap(this@SettingsActivity)
            setOnCheckedChangeListener { _, c ->
                prefs.edit().putBoolean(Prefs.KEY_AUTOCAP, c).apply()
            }
        })
        root.addView(CheckBox(this).apply {
            text = "Espai automàtic després de signe"
            setTextColor(Color.WHITE)
            isChecked = Prefs.autoSpace(this@SettingsActivity)
            setOnCheckedChangeListener { _, c ->
                prefs.edit().putBoolean(Prefs.KEY_AUTOSPACE, c).apply()
            }
        })
        root.addView(CheckBox(this).apply {
            text = "Moure el cursor de paraula en paraula"
            setTextColor(Color.WHITE)
            isChecked = Prefs.cursorWord(this@SettingsActivity)
            setOnCheckedChangeListener { _, c ->
                prefs.edit().putBoolean(Prefs.KEY_CURSOR_WORD, c).apply()
            }
        })
        root.addView(CheckBox(this).apply {
            text = "Mostra pistes de gestos a les tecles"
            setTextColor(Color.WHITE)
            isChecked = Prefs.showHints(this@SettingsActivity)
            setOnCheckedChangeListener { _, c ->
                prefs.edit().putBoolean(Prefs.KEY_SHOW_HINTS, c).apply()
            }
        })
        root.addView(CheckBox(this).apply {
            text = "Registra bigrams per optimitzar el layout"
            setTextColor(Color.WHITE)
            isChecked = Prefs.bigrams(this@SettingsActivity)
            setOnCheckedChangeListener { _, c ->
                prefs.edit().putBoolean(Prefs.KEY_BIGRAMS, c).apply()
            }
        })

        root.addView(TextView(this).apply {
            val lines = Layouts.CODES.mapNotNull { code ->
                val f = java.io.File(filesDir, "bigrams_$code.txt")
                val n = if (f.exists()) f.readLines().size else 0
                if (n > 0) "${code.uppercase()}: $n parells" else null
            }
            text = if (lines.isEmpty()) "Registre de bigrams: buit"
            else "Registre de bigrams: " + lines.joinToString(" · ")
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 24, 0, 0)
        })

        root.addView(Button(this).apply {
            text = "Esborra el registre de bigrams"
            setOnClickListener {
                var n = 0
                for (code in Layouts.CODES) {
                    val f = java.io.File(filesDir, "bigrams_$code.txt")
                    if (f.exists()) { f.delete(); n++ }
                }
                text = if (n > 0) "Registre esborrat ($n fitxers)" else "No hi havia registre"
            }
        })

        root.addView(Button(this).apply {
            text = "Veure el registre (requereix desbloqueig)"
            setOnClickListener {
                val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                val intent = km.createConfirmDeviceCredentialIntent(
                    "Desbloqueig necessari",
                    "Introdueix el patró, PIN o contrasenya per veure el registre de bigrams"
                )
                if (intent != null) startActivityForResult(intent, reqUnlock)
                else showRegister()
            }
        })

        root.addView(TextView(this).apply {
            text = "Idiomes del cicle (tap a la càpsula del teclat)"
            textSize = 17f
            setTextColor(Color.WHITE)
            setPadding(0, 32, 0, 8)
        })
        val enabledLangs = Prefs.langs(this).toMutableList()
        for (code in Layouts.CODES) {
            root.addView(CheckBox(this).apply {
                text = Layouts.NAMES[code] ?: code
                setTextColor(Color.WHITE)
                isChecked = code in enabledLangs
                setOnCheckedChangeListener { _, checked ->
                    if (checked) { if (code !in enabledLangs) enabledLangs.add(code) }
                    else { enabledLangs.remove(code) }
                    if (enabledLangs.isEmpty()) { isChecked = true; enabledLangs.add(code) }
                    prefs.edit().putString(
                        Prefs.KEY_LANGS,
                        Layouts.CODES.filter { it in enabledLangs }.joinToString(",")
                    ).apply()
                }
            })
        }

        root.addView(TextView(this).apply {
            text = "Els canvis s'apliquen la propera vegada que es mostra el teclat."
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 32, 0, 0)
        })

        setContentView(scroll)
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
                text = if (sb.isEmpty()) "Registre buit" else sb.toString()
                textSize = 12f
                setTextColor(Color.WHITE)
                setPadding(32, 32, 32, 32)
                setTextIsSelectable(true)
            })
        }
        AlertDialog.Builder(this)
            .setTitle("Registre de bigrams")
            .setView(view)
            .setPositiveButton("Tanca", null)
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

        // Fallback d'onboarding: IME activat sense haver obert mai l'app.
        if (!Prefs.onboarded(this)) {
            val sys = resources.configuration.locales.get(0)?.language ?: "ca"
            val code = if (sys in Layouts.CODES) sys else "ca"
            Prefs.setLang(this, code)
            Prefs.setOnboarded(this, true)
        }

        gridView?.reloadPrefs()
        gridView?.sensitiveField = isSensitiveField(info)
    }

    /** Refresc de la preview de majúscula quan el cursor canvia de lloc. */
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
        var edgeInterval: Long = 120L
    )

    private val activePointers = mutableMapOf<Int, PointerState>()
    private val undoStack = ArrayDeque<String>()
    private val handler = Handler(Looper.getMainLooper())

    private var barVisible = false
    private var barPage = 0

    private var shiftMode = ShiftMode.OFF
    private var lastUpSwipeTime = 0L
    private var lastCenterTapTime = 0L
    private var lastLetter: String? = null

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
    private var lang = Prefs.lang(context)
    private var layout = Layouts.forLang(lang)
    private var theme = Themes.current(context)

    // Escala de disseny: el llenç de referència fa 430 unitats d'amplada.
    private var scale = 1f
    private fun px(v: Float) = v * scale * resources.displayMetrics.density

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
    }

    private data class Geo(
        val barHeight: Float, val sliderZone: Float,
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
        val barHeight = if (barVisible) px(48f) else 0f
        val sliderZone = px(64f)
        val sliderH = px(32f)
        val gridTop = barHeight
        val gridH = height - barHeight - sliderZone
        val contentLeft = width * (marginLeftPct / 100f)
        val contentWidth = width - contentLeft - width * (marginRightPct / 100f)
        val avail = contentWidth - 2 * padH
        val pillW = maxOf(px(24f), contentWidth * gapFraction)
        val colW = (avail - pillW - 6 * gapU) / 6f
        val gridW = 3 * colW + 2 * gapU
        val gapLeft = contentLeft + padH + gridW + gapU
        val gapRight = gapLeft + pillW
        val rowH = (gridH - 2 * padV - 2 * gapU) / 3f
        val sliderTop = height - sliderZone + (sliderZone - sliderH) / 2f
        val capsuleH = px(20f)
        val capsuleTop = height - sliderZone + ((sliderZone - sliderH) / 2f - capsuleH) / 2f
        return Geo(barHeight, sliderZone, sliderTop, sliderH, gridTop, rowH, colW, gapU,
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
        lang = enabled[(idx + 1) % enabled.size]
        layout = Layouts.forLang(lang)
        Prefs.setLang(context, lang)
        invalidate()
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

    private fun commitChar(raw: String) {
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
        // Escriure text nou invalida l'historial de refer (com qualsevol editor)
        undoStack.clear()
        if (bigramsEnabled && !sensitiveField && out.length == 1 && out[0].isLetter()) {
            val cur = out.lowercase()
            lastLetter?.let { prev -> logBigram(prev, cur) }
            lastLetter = cur
        } else {
            lastLetter = null
        }
        invalidate()
    }

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

    /** Ombra suau falsa (3 capes) compatible amb acceleració per hardware. */
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

    private fun drawHints(
        canvas: Canvas, key: KeyDef, r: RectF, capsVisual: Boolean
    ) {
        hintKeyPaint.color = theme.hint
        hintKeyPaint.textSize = px(13f)
        hintKeyPaint.typeface = fontRegular
        val yC = -(hintKeyPaint.descent() + hintKeyPaint.ascent()) / 2f
        for ((dir, raw) in key.swipes) {
            val ch = if (capsVisual) raw.uppercase() else raw
            val cx = when (dir) {
                SwipeDir.N, SwipeDir.S -> r.centerX()
                SwipeDir.NE, SwipeDir.E, SwipeDir.SE -> r.right - r.width() * 0.16f
                else -> r.left + r.width() * 0.16f
            }
            val cy = when (dir) {
                SwipeDir.N, SwipeDir.NE, SwipeDir.NW -> r.top + r.height() * 0.14f
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> r.bottom - r.height() * 0.10f
                else -> r.centerY()
            }
            canvas.drawText(ch, cx, cy + yC, hintKeyPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val g = geo()
        val capsVisual = shiftMode != ShiftMode.OFF || shouldAutoCap()

        // Fons del teclat
        canvas.drawColor(theme.role[Themes.BG])

        // Franja superior: barra de capes (substitueix els suggeriments)
        if (g.barHeight > 0f) {
            canvas.drawRect(0f, 0f, width.toFloat(), g.barHeight,
                keycapPaint.apply { color = theme.role[Themes.STRIP] })
            val cw = g.contentWidth / 10f
            val page = barPages[barPage]
            textPaint.color = theme.text
            textPaint.textSize = px(20f)
            textPaint.typeface = fontMedium
            val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
            for (i in 0..9) {
                canvas.drawText(page[i], g.contentLeft + i * cw + cw / 2f,
                    g.barHeight / 2f + yC, textPaint)
            }
            hintPaint.color = theme.hint
            hintPaint.textSize = px(10f)
            for (p in barPages.indices) {
                val dotX = g.contentLeft + g.contentWidth / 2f + (p - 1) * px(14f)
                hintPaint.alpha = if (p == barPage) 255 else 120
                canvas.drawText("•", dotX, g.barHeight - px(6f), hintPaint)
            }
            hintPaint.alpha = 255
        }

        // Keycaps de les dues graelles
        val radius = px(16f)
        for (side in listOf(GridSide.LEFT, GridSide.RIGHT)) {
            for (row in 0..2) {
                for (col in 0..2) {
                    val r = cellRect(g, side, row, col)
                    if (theme.keycapShadow) drawSoftShadow(canvas, r, radius)
                    keycapPaint.color = theme.role[Themes.KEYCAP]
                    canvas.drawRoundRect(r, radius, radius, keycapPaint)

                    val key = keyAt(side, row, col)
                    letterPaint.color = theme.text
                    letterPaint.textSize = px(28f)
                    letterPaint.typeface = if (capsVisual) fontSemiBold else fontMedium
                    val yC = -(letterPaint.descent() + letterPaint.ascent()) / 2f
                    val label = if (capsVisual) key.tap.uppercase() else key.tap
                    canvas.drawText(label, r.centerX(), r.centerY() + yC, letterPaint)

                    if (showHints) drawHints(canvas, key, r, capsVisual)
                }
            }
        }

        // Pill del gap (espai)
        val pillRect = RectF(g.gapLeft, g.gridTop + g.padV,
            g.gapRight, g.gridTop + g.padV + 3 * g.rowH + 2 * g.gapU)
        if (theme.keycapShadow) drawSoftShadow(canvas, pillRect, g.pillW / 2f)
        keycapPaint.color = theme.role[Themes.KEYCAP]
        canvas.drawRoundRect(pillRect, g.pillW / 2f, g.pillW / 2f, keycapPaint)
        hintPaint.color = theme.hint
        hintPaint.textSize = px(18f)
        hintPaint.typeface = fontRegular
        val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
        canvas.drawText("␣", pillRect.centerX(), pillRect.centerY() + pillYC, hintPaint)

        // Càpsula d'idioma entre la graella i el lliscador
        val capW = capsuleWidth()
        val capRect = RectF(g.contentLeft + px(8f), g.capsuleTop,
            g.contentLeft + px(8f) + capW, g.capsuleTop + g.capsuleH)
        keycapPaint.color = theme.role[Themes.SPECIAL]
        canvas.drawRoundRect(capRect, g.capsuleH / 2f, g.capsuleH / 2f, keycapPaint)
        langPaint.color = theme.text
        langPaint.textSize = px(12f)
        langPaint.typeface = fontMedium
        val capYC = -(langPaint.descent() + langPaint.ascent()) / 2f
        canvas.drawText(lang.uppercase(), capRect.centerX(), capRect.centerY() + capYC, langPaint)

        // Lliscador inferior amb franges i knob central
        val sliderRect = RectF(g.contentLeft + px(16f), g.sliderTop,
            g.contentLeft + g.contentWidth - px(16f), g.sliderTop + g.sliderH)
        keycapPaint.color = theme.role[Themes.SLIDER]
        canvas.drawRoundRect(sliderRect, g.sliderH / 2f, g.sliderH / 2f, keycapPaint)
        tickPaint.color = theme.hint
        tickPaint.alpha = 110
        val tickSpacing = px(12f)
        var tx = sliderRect.left + tickSpacing
        while (tx < sliderRect.right - tickSpacing) {
            canvas.drawLine(tx, sliderRect.centerY() - px(6f),
                tx, sliderRect.centerY() + px(6f), tickPaint)
            tx += tickSpacing
        }
        tickPaint.alpha = 255
        val knobD = px(40f)
        val knobRect = RectF(sliderRect.centerX() - knobD / 2f, sliderRect.centerY() - knobD / 2f,
            sliderRect.centerX() + knobD / 2f, sliderRect.centerY() + knobD / 2f)
        drawSoftShadow(canvas, knobRect, knobD / 2f)
        keycapPaint.color = theme.role[Themes.KNOB]
        canvas.drawRoundRect(knobRect, knobD / 2f, knobD / 2f, keycapPaint)
        tickPaint.color = theme.hint
        for (i in -1..1) {
            val bx = knobRect.centerX() + i * px(6f)
            canvas.drawLine(bx, knobRect.centerY() - px(8f),
                bx, knobRect.centerY() + px(8f), tickPaint)
        }

        // Vectors de gest actius
        for (state in activePointers.values) {
            canvas.drawLine(state.startX, state.startY, state.currentX, state.currentY, linePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return true

        val g = geo()
        val edgeZone = 24 * resources.displayMetrics.density

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.actionIndex
                val id = event.getPointerId(index)
                val x = event.getX(index)
                val y = event.getY(index)

                var zone = Zone.LEFT
                var side: GridSide? = null
                var row = 0; var col = 0; var cellIndex = 0

                val capW = capsuleWidth()
                val capExtra = px(12f)
                when {
                    g.barHeight > 0f && y < g.barHeight -> {
                        zone = Zone.BAR
                        cellIndex = ((x - g.contentLeft) / (g.contentWidth / 10f))
                            .toInt().coerceIn(0, 9)
                    }
                    y > height - g.sliderZone && y < g.sliderTop -> {
                        zone = Zone.CAPSULE
                    }
                    y >= g.sliderTop - capExtra && y <= g.sliderTop + g.sliderH + capExtra &&
                            x >= g.contentLeft && x <= g.contentLeft + px(8f) + capW + capExtra -> {
                        zone = Zone.CAPSULE
                    }
                    y >= g.sliderTop - capExtra -> {
                        zone = Zone.SLIDER
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

                activePointers[id] = PointerState(
                    id, zone, side, row, col, cellIndex, x, y,
                    startTime = event.eventTime,
                    currentX = x, currentY = y,
                    lastX = x, lastTime = event.eventTime
                )
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
                    val speed = abs(dxEvent) / dt.toFloat()
                    val mult = 1f + minOf(3f, maxOf(0f, speed - 0.4f) / 0.6f)
                    state.currentX = x
                    state.currentY = y

                    val pxPerCursorUnit =
                        if (cursorWord) width * 5f / sensChars else width / sensChars
                    val pxPerEraseUnit = width / sensErase

                    if (state.zone == Zone.SLIDER) {
                        state.cursorAcc += dxEvent * mult / pxPerCursorUnit
                        val n = state.cursorAcc.toInt()
                        if (n != 0) {
                            textTarget?.moveCursor(n, cursorWord)
                            state.cursorAcc -= n
                        }
                    } else if (state.zone == Zone.SPACE) {
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
                resolvePointer(state)
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointers.keys.toList().forEach { stopEdgeRepeat(it) }
                activePointers.clear()
                Log.d("GridTouch", "CANCEL")
            }
        }

        invalidate()
        return true
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
                    if (inCenter) {
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
                        invalidate()
                    }
                }
            }

            Zone.SPACE -> when (dir) {
                SwipeDir.TAP -> {
                    val b = textTarget?.peekBefore(2) ?: ""
                    val blocked = autoSpace && b.length == 2 && b[1] == ' ' && b[0] in autoSpaceChars
                    if (!blocked) textTarget?.commit(" ")
                }
                SwipeDir.W -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        val ch = textTarget?.deleteBefore() ?: ""
                        if (ch.isNotEmpty()) undoStack.addLast(ch)
                    }
                }
                SwipeDir.E -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        undoStack.removeLastOrNull()?.let { textTarget?.commit(it) }
                    }
                }
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> textTarget?.enter()
                else -> {
                    val now = SystemClock.uptimeMillis()
                    shiftMode = when {
                        shiftMode == ShiftMode.CAPS -> ShiftMode.OFF
                        now - lastUpSwipeTime < doubleWindowMs.toLong() -> {
                            lastUpSwipeTime = 0L; ShiftMode.CAPS
                        }
                        else -> { lastUpSwipeTime = now; ShiftMode.ONCE }
                    }
                    invalidate()
                }
            }

            Zone.LEFT, Zone.RIGHT -> {
                val side = state.side ?: return
                val key = keyAt(side, state.originRow, state.originCol)
                val raw = if (dir == SwipeDir.TAP) key.tap else key.swipes[dir]
                if (raw != null) commitChar(raw)
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