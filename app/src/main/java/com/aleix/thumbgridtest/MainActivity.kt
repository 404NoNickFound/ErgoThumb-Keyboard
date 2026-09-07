package com.aleix.thumbgridtest

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.inputmethodservice.InputMethodService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
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
    const val KEY_MARGIN_SIDE = "margin_side"
    const val KEY_MARGIN_BOTTOM = "margin_bottom"
    const val KEY_MARGIN_LEFT = "margin_left"
    const val KEY_MARGIN_RIGHT = "margin_right"
    const val KEY_MARGIN_INDEP = "margin_indep"

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
    fun marginSide(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_SIDE, DEFAULT_MARGIN_SIDE)
    fun marginBottom(ctx: Context) = prefs(ctx).getFloat(KEY_MARGIN_BOTTOM, DEFAULT_MARGIN_BOTTOM)
    fun marginIndep(ctx: Context) = prefs(ctx).getBoolean(KEY_MARGIN_INDEP, false)
    fun marginLeft(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_LEFT, marginSide(ctx))
        else marginSide(ctx)
    fun marginRight(ctx: Context) =
        if (marginIndep(ctx)) prefs(ctx).getFloat(KEY_MARGIN_RIGHT, marginSide(ctx))
        else marginSide(ctx)

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

/** Superfície on escriu el teclat: l'EditText de proves o el camp real via IME. */
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
    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        editText = EditText(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
            hint = "Prova el teclat aquí"
            setBackgroundColor(Color.parseColor("#222222"))
            setPadding(32, 24, 32, 24)
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

    override fun onResume() {
        super.onResume()
        if (::dualGridView.isInitialized) dualGridView.reloadPrefs()
    }
}

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)

        // ScrollView: sense això, un LinearLayout no es pot desplaçar
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
                progress = current - min
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
            format = { "$it% de la pantalla" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_HEIGHT, v / 100f).apply() })
        slider("Separació entre graelles", 0, 15, (Prefs.gap(this) * 100).toInt(),
            format = { "$it% de l'amplada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_GAP, v / 100f).apply() })

        // --- Marges laterals: simètric per defecte, independents opcional ---
        val indepBox = CheckBox(this).apply {
            text = "Marges laterals independents"
            setTextColor(Color.WHITE)
            isChecked = Prefs.marginIndep(this@SettingsActivity)
        }
        root.addView(indepBox)

        val symBar = slider("Marge lateral (simètric)", 0, 12, Prefs.marginSide(this).toInt(),
            format = { "$it% de l'amplada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_SIDE, v.toFloat()).apply() })
        val leftBar = slider("Marge esquerre", 0, 12, Prefs.marginLeft(this).toInt(),
            format = { "$it% de l'amplada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_LEFT, v.toFloat()).apply() })
        val rightBar = slider("Marge dret", 0, 12, Prefs.marginRight(this).toInt(),
            format = { "$it% de l'amplada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_RIGHT, v.toFloat()).apply() })

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
            format = { "$it% de l'alçada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_MARGIN_BOTTOM, v.toFloat()).apply() })
        slider("Llindar de gest", 12, 48, Prefs.threshold(this).toInt(),
            format = { "$it dp" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_THRESHOLD, v.toFloat()).apply() })
        slider("Temps de doble acció", 150, 700, Prefs.doubleMs(this).toInt(),
            format = { "$it ms" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_DOUBLE_MS, v.toFloat()).apply() })
        slider("Sensibilitat del cursor", 5, 60, Prefs.sensChars(this).toInt(),
            format = { "$it caràcters per lliscada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_SENS_CHARS, v.toFloat()).apply() })
        slider("Sensibilitat d'esborrat", 2, 30, Prefs.sensErase(this).toInt(),
            format = { "$it caràcters per lliscada" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_SENS_ERASE, v.toFloat()).apply() })
        slider("Finestra de flick", 80, 400, Prefs.flickMs(this).toInt(),
            format = { "$it ms" },
            onValue = { v -> prefs.edit().putFloat(Prefs.KEY_FLICK_MS, v.toFloat()).apply() })

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

        root.addView(TextView(this).apply {
            text = "Idiomes del cicle (tap al codi del lliscador)"
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
                    if (enabledLangs.isEmpty()) {
                        isChecked = true
                        enabledLangs.add(code)
                    }
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
        gridView?.reloadPrefs()
    }
}

class DualGridView(context: Context) : View(context) {

    var textTarget: TextTarget? = null

    private enum class GridSide { LEFT, RIGHT }
    private enum class Zone { LEFT, RIGHT, SPACE, BAR, SLIDER, LANG }
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
        requestLayout()
        invalidate()
    }

    private val highlightPaints = listOf(
        Color.parseColor("#6633B5E5"),
        Color.parseColor("#66FF8800"),
        Color.parseColor("#66AA00FF"),
        Color.parseColor("#6600CC44")
    ).map { color ->
        Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }
    }

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#CCFFFFFF"); style = Paint.Style.STROKE; strokeWidth = 6f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#555555"); style = Paint.Style.STROKE; strokeWidth = 4f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 56f; textAlign = Paint.Align.CENTER
    }
    private val letterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE; textSize = 56f; textAlign = Paint.Align.CENTER
    }
    private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#999999"); textSize = 26f; textAlign = Paint.Align.CENTER
    }
    private val hintKeyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#8A8A8A"); textSize = 26f; textAlign = Paint.Align.CENTER
    }
    private val langPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFB74D"); textSize = 30f
        textAlign = Paint.Align.LEFT; strokeWidth = 5f
    }

    init { setBackgroundColor(Color.parseColor("#111111")) }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        val desired = (resources.displayMetrics.heightPixels * heightFraction).toInt()
        val height = when (mode) {
            MeasureSpec.EXACTLY -> size
            MeasureSpec.AT_MOST -> minOf(desired, size)
            else -> desired
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), height)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        activePointers.keys.toList().forEach { stopEdgeRepeat(it) }
    }

    private val gapActive: Boolean get() = gapFraction >= 0.08f

    private data class Geo(
        val barHeight: Float,
        val stripHeight: Float,
        val sliderHeight: Float,
        val gridTop: Float,
        val gridHeight: Float,
        val gridWidth: Float,
        val gapLeft: Float,
        val gapRight: Float,
        val contentLeft: Float,
        val contentWidth: Float
    )

    private fun geo(): Geo {
        val barHeight = if (barVisible) height * 0.16f else 0f
        // El marge inferior ÉS la zona de la barra: si creix, la barra creix
        val sliderHeight = height * (marginBottomPct / 100f)
        val stripHeight = if (gapActive) 0f else (height - barHeight - sliderHeight) * 0.20f
        val gridTop = barHeight
        val gridHeight = height - barHeight - stripHeight - sliderHeight
        val contentLeft = width * (marginLeftPct / 100f)
        val contentWidth = width - contentLeft - width * (marginRightPct / 100f)
        val gap = contentWidth * gapFraction
        val gridWidth = (contentWidth - gap) / 2f
        val gapLeft = contentLeft + gridWidth
        return Geo(barHeight, stripHeight, sliderHeight, gridTop, gridHeight,
            gridWidth, gapLeft, gapLeft + gap, contentLeft, contentWidth)
    }

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
        if (bigramsEnabled && out.length == 1 && out[0].isLetter()) {
            val cur = out.lowercase()
            lastLetter?.let { prev -> logBigram(prev, cur) }
            lastLetter = cur
        } else {
            lastLetter = null
        }
        invalidate()
    }

    // ----- Auto-repetit accelerat quan mantens el dit a la vora -----

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

    /** Dibuixa, en petit, el caràcter de cada gest a la vora corresponent. */
    private fun drawHints(
        canvas: Canvas, key: KeyDef,
        left: Float, top: Float, cellW: Float, cellH: Float
    ) {
        val yC = -(hintKeyPaint.descent() + hintKeyPaint.ascent()) / 2f
        for ((dir, ch) in key.swipes) {
            val cx = when (dir) {
                SwipeDir.N, SwipeDir.S -> left + cellW / 2f
                SwipeDir.NE, SwipeDir.E, SwipeDir.SE -> left + cellW * 0.84f
                else -> left + cellW * 0.16f
            }
            val cy = when (dir) {
                SwipeDir.N, SwipeDir.NE, SwipeDir.NW -> top + cellH * 0.14f
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> top + cellH * 0.90f
                else -> top + cellH / 2f
            }
            canvas.drawText(ch, cx, cy + yC, hintKeyPaint)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val g = geo()
        val cellW = g.gridWidth / 3f
        val cellH = g.gridHeight / 3f
        val yCenter = -(textPaint.descent() + textPaint.ascent()) / 2f
        val centerX = g.contentLeft + g.contentWidth / 2f

        // Barra superior de capes
        if (g.barHeight > 0f) {
            val cw = g.contentWidth / 10f
            val page = barPages[barPage]
            for (i in 0..9) {
                canvas.drawText(page[i], g.contentLeft + i * cw + cw / 2f,
                    g.barHeight / 2f + yCenter, textPaint)
            }
            canvas.drawLine(g.contentLeft, g.barHeight,
                g.contentLeft + g.contentWidth, g.barHeight, gridPaint)
            for (p in barPages.indices) {
                val dotX = centerX + (p - 1) * 40f
                val dotPaint = if (p == barPage) textPaint else hintPaint
                canvas.drawText("•", dotX, g.barHeight - 14f, dotPaint)
            }
        }

        // Franja d'espai (només si el gap és estret)
        if (g.stripHeight > 0f) {
            val top = height - g.sliderHeight - g.stripHeight
            canvas.drawLine(g.contentLeft, top, g.contentLeft + g.contentWidth, top, gridPaint)
            canvas.drawText("␣ · ← esborra · → refa · ↓ enter · ↑ majúscula",
                centerX, top + g.stripHeight / 2f + yCenter, hintPaint)
        }

        // Gap com a espai
        if (gapActive) {
            canvas.drawText("␣", (g.gapLeft + g.gapRight) / 2f,
                g.gridTop + g.gridHeight / 2f + yCenter, textPaint)
        }

        // Lliscador de cursor a sota de tot
        val sliderTop = height - g.sliderHeight
        canvas.drawLine(g.contentLeft, sliderTop, g.contentLeft + g.contentWidth, sliderTop, gridPaint)
        canvas.drawText(
            "⇔ cursor · tap centre: 123 · doble: símbols · tap ${lang.uppercase()}: idioma",
            centerX, sliderTop + g.sliderHeight / 2f + yCenter, hintPaint)

        // Botó d'idioma a l'extrem esquerre del lliscador
        val langBtn = g.contentWidth * 0.16f
        canvas.drawText(lang.uppercase(), g.contentLeft + 24f,
            sliderTop + g.sliderHeight / 2f + yCenter, langPaint)
        canvas.drawLine(g.contentLeft + 16f, height - 12f,
            g.contentLeft + langBtn - 16f, height - 12f, langPaint)

        // Ressalts i vectors dels dits actius
        for (state in activePointers.values) {
            if (state.side != null) {
                val paint = highlightPaints[state.pointerId % highlightPaints.size]
                val offset = if (state.side == GridSide.LEFT) g.contentLeft else g.gapRight
                val left = offset + state.originCol * cellW
                val top = g.gridTop + state.originRow * cellH
                canvas.drawRect(left, top, left + cellW, top + cellH, paint)
            }
            canvas.drawLine(state.startX, state.startY, state.currentX, state.currentY, linePaint)
        }

        // Línies de les dues graelles
        for (grid in 0..1) {
            val offset = if (grid == 0) g.contentLeft else g.gapRight
            for (i in 0..3) {
                val x = offset + i * cellW
                canvas.drawLine(x, g.gridTop, x, g.gridTop + g.gridHeight, gridPaint)
                val y = g.gridTop + i * cellH
                canvas.drawLine(offset, y, offset + g.gridWidth, y, gridPaint)
            }
        }

        // Lletres + pistes de gestos
        val capsVisual = shiftMode != ShiftMode.OFF || shouldAutoCap()
        letterPaint.color =
            if (shiftMode == ShiftMode.CAPS) Color.parseColor("#FFB74D") else Color.WHITE

        for (row in 0..2) {
            for (col in 0..2) {
                val cy = g.gridTop + row * cellH + cellH / 2f + yCenter
                val cellTop = g.gridTop + row * cellH

                val lk = layout.left[row * 3 + col]
                canvas.drawText(
                    if (capsVisual) lk.tap.uppercase() else lk.tap,
                    g.contentLeft + col * cellW + cellW / 2f, cy, letterPaint)
                if (showHints) drawHints(canvas, lk, g.contentLeft + col * cellW, cellTop, cellW, cellH)

                val rk = layout.right[row * 3 + col]
                canvas.drawText(
                    if (capsVisual) rk.tap.uppercase() else rk.tap,
                    g.gapRight + col * cellW + cellW / 2f, cy, letterPaint)
                if (showHints) drawHints(canvas, rk, g.gapRight + col * cellW, cellTop, cellW, cellH)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return true

        val g = geo()
        val cellW = g.gridWidth / 3f
        val cellH = g.gridHeight / 3f
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

                when {
                    g.barHeight > 0f && y < g.barHeight -> {
                        zone = Zone.BAR
                        cellIndex = ((x - g.contentLeft) / (g.contentWidth / 10f))
                            .toInt().coerceIn(0, 9)
                    }
                    y > height - g.sliderHeight -> {
                        zone = if (x < g.contentLeft + g.contentWidth * 0.16f) Zone.LANG
                        else Zone.SLIDER
                    }
                    g.stripHeight > 0f && y > height - g.sliderHeight - g.stripHeight ->
                        zone = Zone.SPACE
                    gapActive && x >= g.gapLeft && x <= g.gapRight -> zone = Zone.SPACE
                    else -> {
                        side = if (x < width / 2f) GridSide.LEFT else GridSide.RIGHT
                        zone = if (side == GridSide.LEFT) Zone.LEFT else Zone.RIGHT
                        val localX = if (side == GridSide.LEFT) x - g.contentLeft
                        else x - g.gapRight
                        col = (localX / cellW).toInt().coerceIn(0, 2)
                        row = ((y - g.gridTop) / cellH).toInt().coerceIn(0, 2)
                    }
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

                    val pxPerCursorUnit = if (cursorWord) width * 5f / sensChars
                    else width / sensChars
                    val pxPerEraseUnit = width / sensErase

                    if (state.zone == Zone.SLIDER || state.zone == Zone.LANG) {
                        state.cursorAcc += dxEvent * mult / pxPerCursorUnit
                        val n = state.cursorAcc.toInt()
                        if (n != 0) {
                            textTarget?.moveCursor(n, cursorWord)
                            state.cursorAcc -= n
                        }
                    } else if (state.zone == Zone.SPACE) {
                        val dx = x - state.startX

                        // Mantingut a la vora => auto-repetit accelerat
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
                                        if (ch.isNotEmpty()) {
                                            undoStack.addLast(ch)
                                            state.deleted++
                                        }
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

            Zone.LANG -> {
                if (dir == SwipeDir.TAP) cycleLang()
            }

            Zone.SLIDER -> {
                if (dir == SwipeDir.TAP) {
                    val contentLeft = width * (marginLeftPct / 100f)
                    val contentWidth = width - contentLeft - width * (marginRightPct / 100f)
                    val inCenter = state.startX > contentLeft + contentWidth / 3f &&
                            state.startX < contentLeft + 2f * contentWidth / 3f
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
                    val blocked = autoSpace && b.length == 2 &&
                            b[1] == ' ' && b[0] in autoSpaceChars
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