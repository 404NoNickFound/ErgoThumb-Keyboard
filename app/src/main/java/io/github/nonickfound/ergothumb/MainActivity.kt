package io.github.nonickfound.ergothumb

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.content.res.ColorStateList
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
import android.view.inputmethod.InputMethodManager
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
    const val KEY_ACCENT_ON = "accent_on"
    const val KEY_ACCENT_COLOR = "accent_color"
    const val KEY_SPACEBAR_H = "spacebar_h"
    const val KEY_TUTORIAL_DONE = "tutorial_done"
    const val KEY_ERASE_PRECISION = "erase_precision"

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

    // - Spelling corrector (C1) -
    const val KEY_CORRECTOR = "corrector_enabled"
    const val KEY_CORRECTOR_WARNED = "corrector_warned"
    const val KEY_AUTOCORRECT_SPACE = "autocorrect_space"
    const val KEY_CONFIDENCE = "confidence"
    const val KEY_UNDO_WHOLE = "undo_whole_correction"
    const val KEY_BLACKLIST = "autocorrect_blacklist"
    const val KEY_LEARNED = "learned_corrections"
    const val DEFAULT_CONFIDENCE = 80f

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
    const val DEFAULT_SPACEBAR_H = 56f


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
    fun accentOn(ctx: Context) = prefs(ctx).getBoolean(KEY_ACCENT_ON, false)
    fun accentColor(ctx: Context) = prefs(ctx).getInt(KEY_ACCENT_COLOR, 0)
    const val KEY_ONBOARD_SHOWN = "onboard_shown"
    fun onboardShown(ctx: Context) = prefs(ctx).getBoolean(KEY_ONBOARD_SHOWN, false)
    fun setOnboardShown(ctx: Context, v: Boolean) =
        prefs(ctx).edit().putBoolean(KEY_ONBOARD_SHOWN, v).apply()
    fun spaceBarH(ctx: Context) = prefs(ctx).getFloat(KEY_SPACEBAR_H, DEFAULT_SPACEBAR_H)
    fun tutorialDone(ctx: Context) = prefs(ctx).getBoolean(KEY_TUTORIAL_DONE, false)
    fun erasePrecision(ctx: Context) = prefs(ctx).getBoolean(KEY_ERASE_PRECISION, false)

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

    // - Spelling corrector (C1) -
    fun corrector(ctx: Context) = prefs(ctx).getBoolean(KEY_CORRECTOR, false)
    fun correctorWarned(ctx: Context) = prefs(ctx).getBoolean(KEY_CORRECTOR_WARNED, false)
    fun autocorrectSpace(ctx: Context) = prefs(ctx).getBoolean(KEY_AUTOCORRECT_SPACE, true)
    fun confidence(ctx: Context) = prefs(ctx).getFloat(KEY_CONFIDENCE, DEFAULT_CONFIDENCE)
    fun undoWhole(ctx: Context) = prefs(ctx).getBoolean(KEY_UNDO_WHOLE, true)
    fun blacklist(ctx: Context): Set<String> = nsSet(ctx, KEY_BLACKLIST)
    fun learned(ctx: Context): Map<String, String> {
        ensureNs(ctx)
        val cur = lang(ctx)
        val raw = prefs(ctx).getString(KEY_LEARNED, "") ?: ""
        if (raw.isEmpty()) return emptyMap()
        val m = HashMap<String, String>()
        for (e in raw.split(",")) {
            val first = e.indexOf('>')
            if (first <= 0) continue
            val second = e.indexOf('>', first + 1)
            if (second <= 0) continue
            if (e.substring(0, first) != cur) continue
            m[e.substring(first + 1, second)] = e.substring(second + 1)
        }
        return m
    }

    private const val KEY_DEMOTED = "demoted_corrections"
    fun demoted(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(KEY_DEMOTED, emptySet()) ?: emptySet()
    fun addDemoted(ctx: Context, pair: String) {
        val set = demoted(ctx).toMutableSet(); set.add(pair)
        prefs(ctx).edit().putStringSet(KEY_DEMOTED, set).apply()
    }
    fun putLearned(ctx: Context, key: String, value: String) {
        val cur = lang(ctx)
        val m = learned(ctx).toMutableMap()
        m[key] = value
        val raw = prefs(ctx).getString(KEY_LEARNED, "") ?: ""
        val others = raw.split(",").filter { e ->
            val first = e.indexOf('>')
            first > 0 && e.substring(0, first) != cur && e.count { it == '>' } >= 2
        }
        val mine = m.entries.map { "$cur>${it.key}>${it.value}" }
        prefs(ctx).edit().putString(KEY_LEARNED, (others + mine).joinToString(",")).apply()
    }
    const val KEY_NEXTWORD = "nextword_enabled"
    const val KEY_NEXTWORD_WARNED = "nextword_warned"
    const val KEY_WORDPAIRS = "wordpairs"

    fun nextWord(ctx: Context) = prefs(ctx).getBoolean(KEY_NEXTWORD, false)
    fun nextWordWarned(ctx: Context) = prefs(ctx).getBoolean(KEY_NEXTWORD_WARNED, false)

    fun logWordPair(ctx: Context, lang: String, prev: String, next: String) {
        val raw = prefs(ctx).getString(KEY_WORDPAIRS, "") ?: ""
        val m = LinkedHashMap<String, Int>()
        for (e in raw.split(",").filter { it.isNotEmpty() }) {
            val i = e.lastIndexOf('=')
            if (i > 0) m[e.substring(0, i)] = e.substring(i + 1).toIntOrNull() ?: 1
        }
        val key = "$lang>$prev>$next"
        m[key] = (m[key] ?: 0) + 1
        prefs(ctx).edit().putString(KEY_WORDPAIRS,
            m.entries.joinToString(",") { "${it.key}=${it.value}" }).apply()
    }

    fun wordPairFollowers(ctx: Context, lang: String, prev: String): List<Pair<String, Int>> {
        val raw = prefs(ctx).getString(KEY_WORDPAIRS, "") ?: ""
        val pre = "$lang>$prev>"
        return raw.split(",").filter { it.startsWith(pre) }.mapNotNull { e ->
            val i = e.lastIndexOf('=')
            if (i <= 0) null else e.substring(pre.length, i) to (e.substring(i + 1).toIntOrNull() ?: 1)
        }.sortedByDescending { it.second }
    }
    const val KEY_PERSONAL = "personal_dict"
    private const val KEY_NS_DONE = "namespaces_migrated"

    private fun ensureNs(ctx: Context) {
        if (prefs(ctx).getBoolean(KEY_NS_DONE, false)) return
        val cur = lang(ctx)
        val rawL = prefs(ctx).getString(KEY_LEARNED, "") ?: ""
        if (rawL.isNotEmpty()) {
            prefs(ctx).edit().putString(KEY_LEARNED, rawL.split(",").map { e ->
                if (e.count { it == '>' } == 1) "$cur>$e" else e
            }.joinToString(",")).apply()
        }
        for (k in listOf(KEY_PERSONAL, KEY_BLACKLIST)) {
            val raw = prefs(ctx).getString(k, "") ?: ""
            if (raw.isNotEmpty()) {
                prefs(ctx).edit().putString(k, raw.split(",").map { e ->
                    if (!e.contains('>')) "$cur>$e" else e
                }.joinToString(",")).apply()
            }
        }
        prefs(ctx).edit().putBoolean(KEY_NS_DONE, true).apply()
    }

    private fun nsSet(ctx: Context, key: String): Set<String> {
        ensureNs(ctx)
        val cur = lang(ctx)
        return (prefs(ctx).getString(key, "") ?: "").split(",").mapNotNull { e ->
            val i = e.indexOf('>')
            if (i <= 0) null else if (e.substring(0, i) == cur) e.substring(i + 1) else null
        }.toSet()
    }

    private fun rewriteNsSet(ctx: Context, key: String, s: Set<String>) {
        val cur = lang(ctx)
        val raw = prefs(ctx).getString(key, "") ?: ""
        val others = raw.split(",").filter { e ->
            val i = e.indexOf('>')
            i > 0 && e.substring(0, i) != cur
        }
        val mine = s.map { "$cur>$it" }
        prefs(ctx).edit().putString(key, (others + mine).joinToString(",")).apply()
    }

    fun personal(ctx: Context): Set<String> = nsSet(ctx, KEY_PERSONAL)

    fun addPersonal(ctx: Context, w: String) {
        val s = personal(ctx).toMutableSet(); s.add(w)
        rewriteNsSet(ctx, KEY_PERSONAL, s)
    }

    fun removePersonal(ctx: Context, w: String) {
        val s = personal(ctx).toMutableSet(); s.remove(w)
        rewriteNsSet(ctx, KEY_PERSONAL, s)
    }

    fun addBlacklist(ctx: Context, w: String) {
        val s = blacklist(ctx).toMutableSet(); s.add(w)
        rewriteNsSet(ctx, KEY_BLACKLIST, s)
    }

    fun removeBlacklist(ctx: Context, w: String) {
        val s = blacklist(ctx).toMutableSet(); s.remove(w)
        rewriteNsSet(ctx, KEY_BLACKLIST, s)
    }

    fun purgeLearnedValue(ctx: Context, value: String) {
        val raw = prefs(ctx).getString(KEY_LEARNED, "") ?: ""
        val kept = raw.split(",").filter { e ->
            val first = e.indexOf('>')
            val second = if (first >= 0) e.indexOf('>', first + 1) else -1
            !(second > 0 && e.substring(second + 1) == value)
        }
        prefs(ctx).edit().putString(KEY_LEARNED, kept.joinToString(",")).apply()
    }

    const val KEY_CLIPHIST = "clip_history"
    const val KEY_CLIPHIST_ON = "clip_history_enabled"
    private const val CLIP_SEP = ""
    private const val ENTRY_SEP = ""
    private const val CLIP_TTL_MS = 3_600_000L
    private const val KEY_CLIPSUPPRESSED = "clip_suppressed"

    fun clipHistOn(ctx: Context) = prefs(ctx).getBoolean(KEY_CLIPHIST_ON, true)

    fun clipHistory(ctx: Context): List<Pair<Long, String>> {
        val raw = prefs(ctx).getString(KEY_CLIPHIST, "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split(ENTRY_SEP).mapNotNull { e ->
            val i = e.indexOf(CLIP_SEP)
            if (i <= 0) null else (e.substring(0, i).toLongOrNull() ?: 0L) to e.substring(i + 1)
        }
    }

    private fun saveClipHistory(ctx: Context, list: List<Pair<Long, String>>) {
        prefs(ctx).edit().putString(KEY_CLIPHIST,
            list.joinToString(ENTRY_SEP) { "${it.first}$CLIP_SEP${it.second}" }).apply()
    }

    fun pruneClipHistory(ctx: Context) {
        val now = System.currentTimeMillis()
        val all = clipHistory(ctx)
        val kept = all.filter { now - it.first <= CLIP_TTL_MS }
        if (kept.size != all.size) saveClipHistory(ctx, kept)
    }

    fun addClipEntry(ctx: Context, text: String) {
        val t = text.replace("\n", " ").trim()
        if (t.isEmpty()) return
        unsuppressClip(ctx, t)
        val list = clipHistory(ctx).filter { it.second != t }.toMutableList()
        list.add(0, System.currentTimeMillis() to t)
        saveClipHistory(ctx, list.take(20))
    }

    fun clipSuppressed(ctx: Context): List<String> =
        (prefs(ctx).getString(KEY_CLIPSUPPRESSED, "") ?: "")
            .split(ENTRY_SEP).filter { it.isNotEmpty() }

    fun suppressClip(ctx: Context, text: String) {
        val cur = clipSuppressed(ctx).filter { it != text }.toMutableList()
        cur.add(text)
        prefs(ctx).edit().putString(KEY_CLIPSUPPRESSED,
            cur.takeLast(20).joinToString(ENTRY_SEP)).apply()
    }

    private fun unsuppressClip(ctx: Context, text: String) {
        val before = clipSuppressed(ctx)
        val cur = before.filter { it != text }
        if (cur.size != before.size)
            prefs(ctx).edit().putString(KEY_CLIPSUPPRESSED,
                cur.joinToString(ENTRY_SEP)).apply()
    }

    fun touchClipEntry(ctx: Context, text: String) {
        val list = clipHistory(ctx).toMutableList()
        val i = list.indexOfFirst { it.second == text }
        if (i >= 0) { list[i] = System.currentTimeMillis() to text; saveClipHistory(ctx, list) }
    }

    fun removeClipEntry(ctx: Context, text: String) {
        saveClipHistory(ctx, clipHistory(ctx).filter { it.second != text })
        suppressClip(ctx, text)
    }

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
        fun appBg(): Int = blendColor(principal, pole, 0.92f)
        fun buttonBg(): Int = blendColor(principal, pole, 0.35f)
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
        val raw = if (override == 0) base.principal else override
        val dist = colorDist(raw, base.pole)
        val eff = if (dist < 0.30f)
            blendColor(raw, if (night) Color.WHITE else Color.BLACK, 0.55f) else raw
        val acc = if (Prefs.accentOn(ctx)) Prefs.accentColor(ctx) else 0
        val finalPrincipal = if (acc != 0) acc else eff
        return if (finalPrincipal == base.principal) base else base.withPrincipal(finalPrincipal)
    }

    private fun colorDist(a: Int, b: Int): Float {
        val ar = ((a shr 16) and 0xFF) / 255f; val ag = ((a shr 8) and 0xFF) / 255f; val ab = (a and 0xFF) / 255f
        val br = ((b shr 16) and 0xFF) / 255f; val bg = ((b shr 8) and 0xFF) / 255f; val bb = (b and 0xFF) / 255f
        return (abs(ar - br) + abs(ag - bg) + abs(ab - bb)) / 3f
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
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(Prefs.appLang(this)))

        if (!Prefs.onboarded(this)) {
            setContentView(buildChooser())
            return
        }
        if (!Prefs.onboardShown(this)) {
            setContentView(buildOnboarding())
            return
        }
        if (!Prefs.tutorialDone(this)) {
            startActivity(Intent(this, TutorialActivity::class.java))
            finish()
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
            maxLines = 8
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
        val genButton = MaterialButton(this).apply {
            text = getString(R.string.gen_text)
            setOnClickListener {
                editText.append(genParagraphs.random() + "\n\n")
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

        val th = Themes.current(this)
        val bgColor = th.appBg()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bgColor)
            addView(editText)
            addView(enableButton)
            addView(settingsButton)
            addView(genButton)
            addView(spacer)
            addView(dualGridView)
        }

        for (b in listOf(enableButton, settingsButton, genButton)) {
            b.backgroundTintList = ColorStateList.valueOf(th.buttonBg())
            b.setTextColor(th.text)
            b.cornerRadius = 999
        }
        val edBg = android.graphics.drawable.GradientDrawable()
        edBg.cornerRadius = 16 * resources.displayMetrics.density
        edBg.setColor(blendColor(bgColor, th.text, 0.10f))
        editText.background = edBg
        setContentView(root)
    }
    private val genParagraphs = listOf(
        "La llum del matí entrava per la finestra mentre el teclat repicava sense pressa, i cada paraula trobava el seu lloc com si sempre l'hagués esperat.",
        "De vegades escriure és només deixar que els dits recordin el camí: cap amunt per les majúscules, cap a l'esquerra per esborrar, cap al centre per continuar.",
        "El gat dormia sobre la taula, aliè als missatges que anaven i venien, i de tant en tant obria un ull per comprovar que seguíem escrivint.",
        "Quan el vespre arribava, les frases curtes deixaven pas a paràgrafs sencers, i el teclat aguantava el ritme sense queixar-se ni una sola vegada."
    )

    private fun buildOnboarding(): View {
        val th = Themes.current(this)
        val bgColor = th.appBg()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        val scroll = ScrollView(this).apply { setBackgroundColor(bgColor) }
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 64, 48, 48)
        }
        val cards = listOf(
            Triple(R.string.ob1_title, R.string.ob1_body, R.string.ob1_more),
            Triple(R.string.ob2_title, R.string.ob2_body, -1),
            Triple(R.string.ob3_title, R.string.ob3_body, -1),
            Triple(R.string.ob4_title, R.string.ob4_body, -1)
        )
        for ((tRes, bRes, mRes) in cards) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 28, 32, 28)
                val gd = android.graphics.drawable.GradientDrawable()
                gd.cornerRadius = 16 * resources.displayMetrics.density
                gd.setColor(blendColor(bgColor, th.text, 0.08f))
                background = gd
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = 24
                layoutParams = lp
            }
            card.addView(TextView(this).apply {
                text = getString(tRes)
                textSize = 18f
                setTextColor(th.text)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            })
            card.addView(TextView(this).apply {
                text = getString(bRes)
                textSize = 14f
                setTextColor(th.hint)
                setPadding(0, 12, 0, 0)
            })
            if (mRes != -1) {
                val more = TextView(this).apply {
                    text = getString(mRes)
                    textSize = 13f
                    setTextColor(th.hint)
                    setPadding(0, 12, 0, 0)
                    visibility = View.GONE
                }
                val tog = TextView(this).apply {
                    text = "▸  " + getString(R.string.ob_more)
                    textSize = 13f
                    setTextColor(th.principal)
                    setPadding(0, 12, 0, 0)
                    setOnClickListener {
                        val show = more.visibility == View.GONE
                        more.visibility = if (show) View.VISIBLE else View.GONE
                        text = (if (show) "▾  " else "▸  ") + getString(R.string.ob_more)
                    }
                }
                card.addView(more)
                card.addView(tog)
            }
            col.addView(card)
        }
        col.addView(MaterialButton(this).apply {
            text = getString(R.string.tutorial_continue)
            cornerRadius = 999
            backgroundTintList = ColorStateList.valueOf(th.buttonBg())
            setTextColor(th.text)
            setOnClickListener {
                Prefs.setOnboardShown(this@MainActivity, true)
                recreate()
            }
        })
        scroll.addView(col)
        return scroll
    }

    private fun buildChooser(): View {
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Themes.current(this@MainActivity).appBg())
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

    companion object {
        const val EXTRA_FOCUS = "focus_section"
        val SECTION_KEYS = listOf(
            "languages", "appearance", "geometry", "sensitivity",
            "comfort", "animations", "advanced", "help"
        )
        val SECTION_TITLES = listOf(
            R.string.section_languages, R.string.section_appearance,
            R.string.section_geometry, R.string.section_sensitivity,
            R.string.section_comfort, R.string.section_animations,
            R.string.section_advanced, R.string.section_info
        )
    }

    private val reqUnlock = 1234
    private lateinit var root: LinearLayout
    private lateinit var currentContainer: LinearLayout
    private lateinit var scroll: ScrollView
    private var accent = 0
    private var poleCol = Color.BLACK
    private val sectionHeaders = mutableListOf<TextView>()
    private val sectionContainers = mutableListOf<LinearLayout>()
    private val sectionTitleRes = mutableListOf<Int>()
    private var bgCol = Color.BLACK
    private var textCol = Color.WHITE
    private lateinit var corrSw: MaterialSwitch
    private lateinit var nwSw: MaterialSwitch

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
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(Prefs.appLang(this)))

        scroll = ScrollView(this).apply { }
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        scroll.addView(root, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        val th = Themes.current(this)
        accent = th.principal
        poleCol = th.pole
        val bgColor = th.appBg()
        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(bgColor))
        window.statusBarColor = bgColor
        window.navigationBarColor = bgColor
        scroll.setBackgroundColor(bgColor)
        bgCol = bgColor
        textCol = th.text
        root.setBackgroundColor(bgColor)

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
        mSlider(R.string.slider_spacebar_h, 24f, 120f, Prefs.spaceBarH(this),
            { getString(R.string.fmt_u, it.toInt()) })
        { v -> putF(Prefs.KEY_SPACEBAR_H, v) }
        marginsBlock()
        mSlider(R.string.slider_margin_bottom, 0f, 25f, Prefs.marginBottom(this),
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
        mSwitch(R.string.switch_erase_precision, Prefs.erasePrecision(this))
        { putB(Prefs.KEY_ERASE_PRECISION, it) }
        mSlider(R.string.slider_flick, 80f, 400f, Prefs.flickMs(this),
            { getString(R.string.fmt_ms, it.toInt()) })
        { v -> putF(Prefs.KEY_FLICK_MS, v) }

        section(R.string.section_comfort)
        mSwitch(R.string.switch_autocap, Prefs.autoCap(this)) { putB(Prefs.KEY_AUTOCAP, it) }
        mSwitch(R.string.switch_autospace, Prefs.autoSpace(this)) { putB(Prefs.KEY_AUTOSPACE, it) }
        mSwitch(R.string.switch_cursor_word, Prefs.cursorWord(this)) { putB(Prefs.KEY_CURSOR_WORD, it) }
        mSwitch(R.string.switch_hints, Prefs.showHints(this)) { putB(Prefs.KEY_SHOW_HINTS, it) }

        section(R.string.section_animations)
        mSlider(R.string.slider_anim_scale, 0f, 200f, Prefs.animScale(this),
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

        section(R.string.section_info)
        add(MaterialButton(this).apply {
            text = getString(R.string.help_replay)
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, TutorialActivity::class.java))
            }
        })
        add(MaterialButton(this).apply {
            text = getString(R.string.help_github)
            setOnClickListener { openGithub() }
        })
        add(TextView(this).apply {
            val vn = try {
                packageManager.getPackageInfo(packageName, 0).versionName ?: "?"
            } catch (e: Exception) {
                "?"
            }
            text = getString(R.string.info_version, vn)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 16, 0, 0)
        })
        add(TextView(this).apply {
            text = getString(R.string.info_developer)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 0)
        })
        add(TextView(this).apply {
            text = getString(R.string.info_license)
            textSize = 14f
            setTextColor(Color.GRAY)
            setPadding(0, 8, 0, 0)
        })

        // - Spelling corrector (C1): placed last so it doesn't re-index the tour/focus -
        section(R.string.section_corrector)
        corrSw = mSwitch(R.string.switch_corrector, Prefs.corrector(this)) { v ->
            if (v) {
                if (!Prefs.correctorWarned(this)) {
                    corrSw.isChecked = false
                    showCorrectorWarning {
                        putB(Prefs.KEY_CORRECTOR_WARNED, true)
                        putB(Prefs.KEY_CORRECTOR, true)
                        corrSw.isChecked = true
                    }
                } else putB(Prefs.KEY_CORRECTOR, true)
            } else putB(Prefs.KEY_CORRECTOR, false)
        }
        mSwitch(R.string.switch_autocorrect_space, Prefs.autocorrectSpace(this))
        { putB(Prefs.KEY_AUTOCORRECT_SPACE, it) }
        mSlider(R.string.slider_confidence, 0f, 100f, Prefs.confidence(this),
            { getString(R.string.fmt_percent, it.toInt()) })
        { v -> putF(Prefs.KEY_CONFIDENCE, v) }
        mSwitch(R.string.switch_undo_correction, Prefs.undoWhole(this))
        { putB(Prefs.KEY_UNDO_WHOLE, it) }
        add(MaterialButton(this).apply {
            text = getString(R.string.btn_blacklist)
            setOnClickListener { showBlacklist() }
        })
        mSwitch(R.string.switch_cliphist, Prefs.clipHistOn(this))
        { putB(Prefs.KEY_CLIPHIST_ON, it) }
        nwSw = mSwitch(R.string.switch_nextword, Prefs.nextWord(this)) { v ->
            if (v) {
                if (!Prefs.nextWordWarned(this)) {
                    nwSw.isChecked = false
                    showNextWordWarning {
                        putB(Prefs.KEY_NEXTWORD_WARNED, true)
                        putB(Prefs.KEY_NEXTWORD, true)
                        nwSw.isChecked = true
                    }
                } else putB(Prefs.KEY_NEXTWORD, true)
            } else putB(Prefs.KEY_NEXTWORD, false)
        }
        root.addView(TextView(this).apply {
            text = getString(R.string.settings_note)
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, 40, 0, 0)
        })

        fun tintAll(v: View) {
            if (v is MaterialButton) {
                v.backgroundTintList = ColorStateList.valueOf(th.buttonBg())
                v.setTextColor(th.text)
            }
            if (v is LinearLayout) for (i in 0 until v.childCount) tintAll(v.getChildAt(i))
        }
        tintAll(root)

        val focus = intent.getStringExtra(EXTRA_FOCUS)
        if (focus != null) {
            val idx = SECTION_KEYS.indexOf(focus)
            if (idx >= 0 && idx < sectionContainers.size) {
                sectionContainers[idx].visibility = View.VISIBLE
                sectionHeaders[idx].text = "▼  " + getString(SECTION_TITLES[idx])
                scroll.post {
                    scroll.scrollTo(0, sectionHeaders[idx].top)
                    AlertDialog.Builder(this)
                        .setTitle(getString(SECTION_TITLES[idx]))
                        .setMessage(resources.getStringArray(R.array.section_explains)
                            .getOrElse(idx) { "" })
                        .setPositiveButton(getString(R.string.close), null)
                        .show()
                }
            }
        }

        setContentView(scroll)
    }

    private fun openGithub() {
        if (TutorialActivity.GITHUB_URL.isEmpty()) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.github_not_published))
                .setPositiveButton(getString(R.string.close), null)
                .show()
        } else {
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(TutorialActivity.GITHUB_URL)))
        }
    }

    private fun putF(key: String, v: Float) =
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit().putFloat(key, v).apply()

    private fun putB(key: String, v: Boolean) =
        getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit().putBoolean(key, v).apply()

    private fun add(v: View) {
        currentContainer.addView(v)
    }

    private fun section(titleRes: Int, expanded: Boolean = false) {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        val header = TextView(this).apply {
            text = "▶  " + getString(titleRes)
            textSize = 15f
            setTextColor(textCol)
            val gd = android.graphics.drawable.GradientDrawable()
            gd.cornerRadius = 999f
            gd.setColor(blendColor(bgCol, textCol, 0.10f))
            background = gd
            setPadding(28, 16, 28, 16)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.topMargin = 20
            lp.bottomMargin = 10
            layoutParams = lp
        }
        root.addView(header)
        root.addView(container)
        currentContainer = container
        sectionHeaders.add(header)
        sectionContainers.add(container)
        sectionTitleRes.add(titleRes)
        val idx = sectionContainers.size - 1
        header.setOnClickListener {
            toggleSection(idx, container.visibility != View.VISIBLE)
        }
        if (expanded) setSectionOpen(idx, true)
    }
    private fun animOn() = Prefs.animScale(this) > 0f

    private fun toggleSection(idx: Int, open: Boolean) {
        if (open) {
            for (i in sectionContainers.indices) {
                if (i != idx && sectionContainers[i].visibility == View.VISIBLE) setSectionOpen(i, false)
            }
        }
        setSectionOpen(idx, open)
    }

    private fun setSectionOpen(idx: Int, open: Boolean) {
        val container = sectionContainers[idx]
        val header = sectionHeaders[idx]
        header.text = (if (open) "▼  " else "▶  ") + getString(sectionTitleRes[idx])
        val lp = container.layoutParams
        if (!animOn()) {
            container.visibility = if (open) View.VISIBLE else View.GONE
            lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            container.layoutParams = lp
            return
        }
        if (open) {
            container.visibility = View.VISIBLE
            val wSpec = View.MeasureSpec.makeMeasureSpec(
                root.width - root.paddingLeft - root.paddingRight, View.MeasureSpec.EXACTLY)
            val hSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            container.measure(wSpec, hSpec)
            val va = android.animation.ValueAnimator.ofInt(0, container.measuredHeight)
            va.duration = 180
            va.addUpdateListener { lp.height = it.animatedValue as Int; container.layoutParams = lp }
            va.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: android.animation.Animator) {
                    lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    container.layoutParams = lp
                }
            })
            va.start()
        } else {
            val va = android.animation.ValueAnimator.ofInt(container.height, 0)
            va.duration = 150
            va.addUpdateListener { lp.height = it.animatedValue as Int; container.layoutParams = lp }
            va.addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(a: android.animation.Animator) {
                    container.visibility = View.GONE
                    lp.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                    container.layoutParams = lp
                }
            })
            va.start()
        }
    }

    private fun mSwitch(labelRes: Int, current: Boolean, onValue: (Boolean) -> Unit): MaterialSwitch {
        val sw = MaterialSwitch(this).apply {
            text = getString(labelRes)
            textSize = 16f
            setTextColor(Color.WHITE)
            isChecked = current
            setPadding(0, 16, 0, 16)
            thumbTintList = ColorStateList.valueOf(accent)
            trackTintList = ColorStateList.valueOf(blendColor(accent, poleCol, 0.55f))
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
            setTrackActiveTintList(ColorStateList.valueOf(accent))
            setTrackInactiveTintList(ColorStateList.valueOf(blendColor(accent, poleCol, 0.6f)))
            thumbTintList = ColorStateList.valueOf(accent)
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
        add(label(R.string.color_dark_row))
        add(swatchRow(isDark = true))
        add(customColorButton(isDark = true))
        add(label(R.string.color_light_row))
        add(swatchRow(isDark = false))
        add(customColorButton(isDark = false))
        val accentSw = mSwitch(R.string.switch_accent_custom, Prefs.accentOn(this)) { v ->
            getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit()
                .putBoolean(Prefs.KEY_ACCENT_ON, v).apply()
            recreate()
        }
        val accentBtn = MaterialButton(this).apply {
            text = getString(R.string.accent_pick)
            setOnClickListener {
                ColorPickerDialog(this@SettingsActivity,
                    Themes.current(this@SettingsActivity).isDark,
                    Prefs.accentColor(this@SettingsActivity)) { color ->
                    getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit()
                        .putInt(Prefs.KEY_ACCENT_COLOR, color).apply()
                    recreate()
                }.show()
            }
        }
        add(accentBtn)
        accentBtn.isEnabled = accentSw.isChecked
        accentBtn.alpha = if (accentSw.isChecked) 1f else 0.35f
    }

    private fun label(res: Int): TextView = TextView(this).apply {
        text = getString(res)
        textSize = 14f
        setTextColor(Color.GRAY)
        setPadding(0, 16, 0, 8)
    }

    private fun customColorButton(isDark: Boolean): MaterialButton {
        return MaterialButton(this).apply {
            text = getString(R.string.color_custom)
            setOnClickListener {
                val current = if (isDark) Prefs.colorDark(this@SettingsActivity)
                else Prefs.colorLight(this@SettingsActivity)
                ColorPickerDialog(this@SettingsActivity, isDark, current) { color ->
                    getSharedPreferences(Prefs.NAME, MODE_PRIVATE).edit()
                        .putInt(if (isDark) Prefs.KEY_COLOR_DARK else Prefs.KEY_COLOR_LIGHT, color)
                        .apply()
                    recreate()
                }.show()
            }
        }
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
            sym.alpha = if (i) 0.35f else 1f
            left.isEnabled = i
            left.alpha = if (i) 1f else 0.35f
            right.isEnabled = i
            right.alpha = if (i) 1f else 0.35f
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

    private fun showCorrectorWarning(onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.corrector_warning_title))
            .setMessage(getString(R.string.corrector_warning_body))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.bigrams_warning_cancel), null)
            .setPositiveButton(getString(R.string.corrector_warning_ok)) { _, _ -> onAccept() }
            .show()
    }
    private fun showNextWordWarning(onAccept: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.nextword_warning_title))
            .setMessage(getString(R.string.nextword_warning_body))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.bigrams_warning_cancel), null)
            .setPositiveButton(getString(R.string.nextword_warning_ok)) { _, _ -> onAccept() }
            .show()
    }

    private fun showBlacklist() {
        val prefs = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        val input = android.widget.EditText(this).apply {
            setText(Prefs.blacklist(this@SettingsActivity).joinToString(", "))
            setTextColor(Color.WHITE)
        }
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.blacklist_title))
            .setMessage(getString(R.string.blacklist_hint))
            .setView(input)
            .setNegativeButton(getString(R.string.close), null)
            .setPositiveButton(getString(R.string.color_save)) { _, _ ->
                prefs.edit().putString(
                    Prefs.KEY_BLACKLIST,
                    input.text.toString().split(",").map { it.trim() }
                        .filter { it.isNotEmpty() }.joinToString(",")
                ).apply()
            }
            .show()
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

    // Note: on MIUI, Settings.Secure.selected_input_method_subtype cannot be changed
    // from a third-party IME (its service applies criteria based on the system locale).
    // This call is correct and works on AOSP and other OEM layers; on MIUI it only
    // affects internal behavior, not the selector label.
    @SuppressLint("MissingPermission")
    private fun publishSubtype() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            val info = imm.enabledInputMethodList.firstOrNull { it.packageName == packageName }
            val lang = Prefs.lang(this)
            Log.d("ThumbGrid", "publishSubtype lang=$lang info=${info != null}")
            if (info == null) return
            val target = (0 until info.subtypeCount)
                .map { info.getSubtypeAt(it) }
                .firstOrNull { it.locale == lang }
            Log.d("ThumbGrid", "publishSubtype target=${target?.locale}")
            if (target != null) imm.setCurrentInputMethodSubtype(target)
        } catch (e: Exception) {
            Log.e("ThumbGrid", "publishSubtype error", e)
        }
    }

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
        view.onLangChanged = { publishSubtype() }
        gridView = view
        publishSubtype()
        return view
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        gridView?.flushPendingBigrams()
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        gridView?.flushPendingBigrams()
        super.onDestroy()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        gridView?.resetOverlays()
        if (!Prefs.onboarded(this)) {
            val sys = resources.configuration.locales.get(0)?.language ?: "ca"
            val code = if (sys in Layouts.CODES) sys else "ca"
            Prefs.setLang(this, code)
            Prefs.setOnboarded(this, true)
        }
        gridView?.reloadPrefs()
        gridView?.sensitiveField = isPasswordField(info)
        gridView?.noLearnField = isNoLearnField(info)
        publishSubtype()
        Handler(Looper.getMainLooper()).postDelayed({ publishSubtype() }, 600)
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

    private fun isPasswordField(info: EditorInfo?): Boolean {
        val type = info?.inputType ?: return false
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        val textPw = cls == InputType.TYPE_CLASS_TEXT && (
                variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD
                )
        val numPw = cls == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD
        return textPw || numPw
    }

    private fun isNoLearnField(info: EditorInfo?): Boolean {
        if (isPasswordField(info)) return true
        val type = info?.inputType ?: return false
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        val textNoLearn = cls == InputType.TYPE_CLASS_TEXT && (
                variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS ||
                        variation == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS ||
                        variation == InputType.TYPE_TEXT_VARIATION_URI
                )
        val flagNoLearn = (info.imeOptions and
                EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0
        return textNoLearn || flagNoLearn
    }
}

class DualGridView(context: Context) : View(context) {

    private val drawCellRects = Array(18) { RectF() }
    private val drawRectA = RectF()
    private val drawRectB = RectF()
    private val drawRectC = RectF()
    private val drawRectD = RectF()
    private val drawClipMenuRect = RectF()
    private val geoPillRect = RectF()
    private val geoSuggestRect = RectF()
    private val drawPath = Path()
    private val drawPresses = ArrayList<PressAnim>()
    private val drawAlphas = intArrayOf(0x14, 0x0F, 0x0A)
    private val drawSides = arrayOf(GridSide.LEFT, GridSide.RIGHT)

    var textTarget: TextTarget? = null
    var sensitiveField = false
    var noLearnField = false
    var onLangChanged: (() -> Unit)? = null
    var tutorialMode = false
    var tutEvent: ((String) -> Unit)? = null

    private enum class GridSide { LEFT, RIGHT }
    private enum class Zone { LEFT, RIGHT, SPACE, BAR, SLIDER, CAPSULE, SYMBOL, SUGGEST }
    private enum class ShiftMode { OFF, ONCE, CAPS }
    private enum class Icon { SHIFT, CAPS, ENTER, BACK, REDO }

    private val barPages = listOf(
        listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"),
        listOf("\"", "(", ")", "@", "#", "€", "%", "&", "_", "-"),
        listOf("+", "=", ";", ":", "/", "\\", "|", "<", ">", "*")
    )

    private val autoSpaceChars = ".?!,;:"

    private var symbolLayer = false

    private val symbolRows = listOf(
        listOf("$", "€", "¥", "¢", "©", "®", "™", "~", "¿"),
        listOf("[", "]", "{", "}", "<", ">", "^", "¡", "°"),
        listOf("123", "`", ";", "÷", "\\", "|", "¦", "¬", "BACK"),
        listOf("ABC", "SPACE", "SPACE", "×", "§", "¶", "+", "=", "ENTER")
    )

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
    private val bigramBuffers = LinkedHashMap<String, StringBuilder>()
    private var bigramFlushScheduled = false
    private val bigramFlushRunnable = Runnable { flushPendingBigrams() }

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
    private var autoCapOff = false
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

    private var erasePauseUntil = 0L

    // - Spelling corrector (C1) -
    private var suggestions: List<String> = emptyList()
    private var pendingUndoLen = 0
    private var lastAutocorrect: Pair<String, String>? = null
    private var undoOffer: String? = null
    private var nextWordOn = Prefs.nextWord(context)
    private var nextWordCands: List<String> = emptyList()
    private var lastCompletedWord = ""
    private val rejectedCorrections = mutableSetOf<String>()
    private var correctorOn = Prefs.corrector(context)
    private var autocorrectSpace = Prefs.autocorrectSpace(context)
    private val demoted = Prefs.demoted(context).toMutableSet()
    private var confidenceMin = Prefs.confidence(context)
    private var undoWhole = Prefs.undoWhole(context)
    private var blacklist = Prefs.blacklist(context)
    private var learned = Prefs.learned(context)
    private val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE)
            as android.content.ClipboardManager
    private var clipFull = ""
    private var clipHistOn = Prefs.clipHistOn(context)
    private var clipMenuOpen = false
    private var clipMenuJustOpened = false
    private var cachedClipMenuRows: List<String>? = null
    private var sugPressPointer = -1
    private var sugOpenRunnable: Runnable? = null
    private val LONG_PRESS_MS = 1000L
    private var personal = Prefs.personal(context)
    private var tutorialBanner = !Prefs.tutorialDone(context)
    private var sugManageIndex = -1
    private var sugManageDx = 0f
    private var sugManagePointer = -1
    private var sugManageRunnable: Runnable? = null
    private val TUTORIAL_SENTINEL = ""
    private data class Shred(val x: Float, val y: Float, val vx: Float, val vy: Float, val ch: Char, val t0: Long)
    private data class Pop(val x: Float, val y: Float, val t0: Long)
    private val shreds = mutableListOf<Shred>()
    private val pops = mutableListOf<Pop>()
    private val clipListener = android.content.ClipboardManager.OnPrimaryClipChangedListener {
        if (clipHistOn && context is InputMethodService && !noLearnField) {
            val txt = clipMgr.primaryClip?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)?.coerceToText(context)?.toString().orEmpty()
            if (txt.isNotBlank()) {
                Prefs.pruneClipHistory(context)
                Prefs.addClipEntry(context, txt)
                cachedClipMenuRows = null
                refreshSuggestions()
                invalidate()
            }
        }
    }
    private var locContext: Context = context

    private fun refreshLocContext() {
        val conf = android.content.res.Configuration(resources.configuration)
        conf.setLocale(java.util.Locale.forLanguageTag(lang))
        locContext = context.createConfigurationContext(conf)
    }

    private fun locStr(res: Int): String = locContext.getString(res)

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
    private var erasePrecision = Prefs.erasePrecision(context)
    private var marginLeftPct = Prefs.marginLeft(context)
    private var marginRightPct = Prefs.marginRight(context)
    private var marginBottomPct = Prefs.marginBottom(context)
    private var spaceBarHU = Prefs.spaceBarH(context)
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
        erasePrecision = Prefs.erasePrecision(context)
        marginLeftPct = Prefs.marginLeft(context)
        marginRightPct = Prefs.marginRight(context)
        marginBottomPct = Prefs.marginBottom(context)
        spaceBarHU = Prefs.spaceBarH(context)
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
        // - Spelling corrector (C1) -
        correctorOn = Prefs.corrector(context)
        autocorrectSpace = Prefs.autocorrectSpace(context)
        confidenceMin = Prefs.confidence(context)
        undoWhole = Prefs.undoWhole(context)
        clipHistOn = Prefs.clipHistOn(context)
        nextWordOn = Prefs.nextWord(context)
        personal = Prefs.personal(context)
        SpellEngine.setPersonal(personal)
        tutorialBanner = !Prefs.tutorialDone(context)
        blacklist = Prefs.blacklist(context)
        learned = Prefs.learned(context)
        SpellEngine.loadAsync(context, lang) {
            refreshSuggestions()
            invalidate()
        }
        refreshSuggestions()
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
        presses.clear()
        fadingPresses.clear()
        feedbacks.clear()
        slides.clear()
        caseAnimStart = -1L
        caseInitialized = false
        barP = barTarget
        resetOverlays()
        refreshLocContext()
        if (context is InputMethodService) {
            clipMgr.addPrimaryClipChangedListener(clipListener)
            syncClipFromSystem()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mode = MeasureSpec.getMode(heightMeasureSpec)
        val size = MeasureSpec.getSize(heightMeasureSpec)
        val dm = resources.displayMetrics
        val land = dm.widthPixels > dm.heightPixels
        val frac = if (land) maxOf(heightFraction, 0.55f) else heightFraction
        val desired = (dm.heightPixels * frac).toInt()

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
        if (context is InputMethodService) clipMgr.removePrimaryClipChangedListener(clipListener)
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

    private fun noteBoundary(ch: String) {
        val now = SystemClock.uptimeMillis()
        if (ch == "\n") erasePauseUntil = now + 100L
        else if (erasePrecision && ch == " ") erasePauseUntil = now + 100L
    }

    private fun erasePaused() = SystemClock.uptimeMillis() < erasePauseUntil

    // - Spelling corrector (C1): current token, suggestions, autocorrect and per-word deletion -
    private fun currentToken(): String {
        val before = textTarget?.peekBefore(64) ?: return ""
        val delims = " \n\t.,;:!?()\"«»"
        var idx = -1
        for (i in before.indices) if (before[i] in delims) idx = i
        return if (idx < 0) before else before.substring(idx + 1)
    }

    private fun refreshSuggestions() {
        if (!correctorOn) { suggestions = emptyList(); return }
        if (sensitiveField) { suggestions = emptyList(); clipFull = ""; return }
        val tok = currentToken()
        val low = tok.lowercase()
        if (undoOffer != null && low != undoOffer!!.lowercase()) undoOffer = null
        val base = ArrayList<String>(3)
        if (tok.length >= 2) {
            val applied = if (autocorrectSpace) pendingTarget(tok, low) else null
            val cands = SpellEngine.candidates(tok, 3).toMutableList()
            cands.removeAll { blacklist.contains(it) }
            if (applied != null) {
                cands.remove(applied)
                cands.add(0, applied)
            }
            base.addAll(cands.take(3))
            if (base.isEmpty()) base.add(low)
            undoOffer?.let { u -> if (low == u.lowercase()) { base.remove(u); base.add(0, u) } }
        } else if (tok.isEmpty()) {
            base.addAll(nextWordCands)
        }
        val clip = if (clipHistOn)
            (Prefs.clipHistory(context).firstOrNull()?.second).orEmpty()
        else ""
        clipFull = clip
        val third = if (tok.isEmpty() && clip.isNotEmpty())
            (if (clip.length > 14) clip.take(14) + "…" else clip)
        else base.getOrElse(2) { "" }
        suggestions = when {
            tok.isEmpty() && base.all { it.isEmpty() } && third.isEmpty() &&
                    tutorialBanner && !tutorialMode ->
                listOf("", TUTORIAL_SENTINEL, "")
            base.all { it.isEmpty() } && third.isEmpty() -> emptyList()
            else -> listOf(base.getOrElse(0) { "" }, base.getOrElse(1) { "" }, third)
        }
        Log.d("ThumbGrid", "sugg tok='$tok' n=${suggestions.count { it.isNotEmpty() }} dict=${SpellEngine.size()}")
    }
    private fun handleBoundary(finished: String) {
        autoCapOff = false
        undoOffer = null
        if (sensitiveField) return
        val f = finished.lowercase()
        if (f.isNotEmpty()) {
            if (nextWordOn && !noLearnField && lastCompletedWord.isNotEmpty())
                Prefs.logWordPair(context, lang, lastCompletedWord, f)
            lastCompletedWord = f
            nextWordCands = if (nextWordOn && !noLearnField)
                Prefs.wordPairFollowers(context, lang, f)
                    .filter { SpellEngine.isWord(it.first) && !blacklist.contains(it.first) }
                    .take(3).map { it.first }
            else emptyList()
        } else {
            nextWordCands = emptyList()
        }
        refreshSuggestions()
    }
    fun resetOverlays() {
        clipMenuOpen = false
        clipMenuJustOpened = false
        sugManageIndex = -1
        sugManageDx = 0f
        sugOpenRunnable?.let { handler.removeCallbacks(it) }
        sugOpenRunnable = null
        sugManageRunnable?.let { handler.removeCallbacks(it) }
        sugManageRunnable = null
        sugPressPointer = -1
        sugManagePointer = -1
        invalidate()
    }
    private fun syncClipFromSystem() {
        if (!clipHistOn || context !is InputMethodService || noLearnField) return
        val txt = (clipMgr.primaryClip?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)?.coerceToText(context)?.toString() ?: "")
            .replace("\n", " ").trim()
        if (txt.isEmpty()) return
        if (Prefs.clipSuppressed(context).contains(txt)) return
        if (Prefs.clipHistory(context).firstOrNull()?.second == txt) return
        Prefs.pruneClipHistory(context)
        Prefs.addClipEntry(context, txt)
        cachedClipMenuRows = null
        refreshSuggestions()
        invalidate()
    }

    private fun tryOpenClipMenu(justOpened: Boolean) {
        if (sensitiveField) return
        cachedClipMenuRows = null
        if (clipMenuRows().isEmpty()) return
        clipMenuOpen = true
        if (justOpened) clipMenuJustOpened = true
        tutEvent?.invoke("clipMenu")
        invalidate()
    }

    private fun clipMenuRect(): RectF {
        val g = geo()
        drawClipMenuRect.set(g.contentLeft + px(8f), g.sugH + px(8f),
            g.contentLeft + g.contentWidth - px(8f), g.capsuleTop - px(8f))
        return drawClipMenuRect
    }

    private fun clipMenuRows(): List<String> {
        cachedClipMenuRows?.let { return it }
        Prefs.pruneClipHistory(context)
        return Prefs.clipHistory(context).map { it.second }.take(6)
            .also { cachedClipMenuRows = it }
    }

    private fun applySuggestion(word: String) {
        if (sensitiveField) return
        val token = currentToken()
        if (token.isEmpty()) return
        lastAutocorrect = null
        if (word == token.lowercase()) return
        if (!noLearnField) {
            Prefs.putLearned(context, token.lowercase(), word.lowercase())
            learned = Prefs.learned(context)
        }
        repeat(token.length) { textTarget?.deleteBefore() }
        val formatted = if (token[0].isUpperCase())
            word.replaceFirstChar { it.uppercase() } else word
        textTarget?.commit(formatted)
        textTarget?.commit(" ")
        undoStack.clear()
        pendingUndoLen = 0
        refreshSuggestions()
        invalidate()
    }

    private fun maybeAutocorrect(): Boolean {
        if (sensitiveField) return false
        val tok = currentToken()
        val low = tok.lowercase()
        val target = pendingTarget(tok, low) ?: return false
        repeat(tok.length) { textTarget?.deleteBefore() }
        val formatted = if (tok[0].isUpperCase())
            target.replaceFirstChar { it.uppercase() } else target
        textTarget?.commit(formatted)
        textTarget?.commit(" ")
        tutEvent?.invoke("commit: ")
        undoStack.clear()
        lastAutocorrect = tok to formatted
        pendingUndoLen = 0
        handleBoundary(formatted.lowercase())
        return true
    }

    private fun pendingTarget(tok: String, low: String): String? {
        if (!correctorOn || !autocorrectSpace) return null
        if (tok.length < 2) return null
        if (blacklist.contains(low) || rejectedCorrections.contains(low) ||
            personal.contains(low)) return null
        learned[low]?.let { if (it != low && !blacklist.contains(it)) return it }
        val target = SpellEngine.autocorrectTarget(tok, confidenceMin) ?: return null
        if (demoted.contains(low + "\t" + target)) return null
        return target
    }

    private fun replaceToken(tok: String, target: String) {
        repeat(tok.length) { textTarget?.deleteBefore() }
        val formatted = if (tok[0].isUpperCase())
            target.replaceFirstChar { it.uppercase() } else target
        textTarget?.commit(formatted)
        undoStack.clear()
        pendingUndoLen = formatted.length + 1
    }

    private fun deleteOneUnit(): String {
        if (undoWhole) {
            lastAutocorrect?.let { (orig, formatted) ->
                lastAutocorrect = null
                rejectedCorrections.add(orig.lowercase())
                undoOffer = orig
                val pair = orig.lowercase() + "\t" + formatted.lowercase()
                demoted.add(pair); Prefs.addDemoted(context, pair)
                repeat(formatted.length + 1) { textTarget?.deleteBefore() }
                textTarget?.commit(orig)
                refreshSuggestions()
                return orig
            }
        }
        val ch = textTarget?.deleteBefore() ?: ""
        if (ch.isNotEmpty()) undoStack.addLast(ch)
        return ch
    }

    private data class Geo(
        val stripH: Float, val sliderZone: Float,
        val sliderTop: Float, val sliderH: Float,
        val gridTop: Float,
        val rowH: Float, val colW: Float, val gapU: Float,
        val padH: Float, val padV: Float,
        val leftOrigin: Float, val rightOrigin: Float,
        val contentLeft: Float, val contentWidth: Float,
        val capsuleTop: Float, val capsuleH: Float,
        val pillRect: RectF, val conventional: Boolean,
        val sugH: Float, val suggestRect: RectF,
    val land: Boolean,
    val spaceW: Float
    )

    private fun geo(): Geo {
        val land = resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val compact = height < px(200f) || land
        val padH = if (compact) px(5f) else px(8f)
        val padV = if (compact) px(5f) else px(8f)
        val gapU = if (compact) px(5f) else px(8f)
        val stripH = if (compact) height * 0.12f else px(48f)
        val sliderZone = if (compact) height * 0.14f else px(64f)
        val sliderH = if (compact) sliderZone * 0.5f else px(32f)
        val sugH = if (compact) stripH * 0.78f else px(36f)
        val bottomExtra = height * (marginBottomPct / 100f) + if (land) px(28f) else 0f
        val zoneTop = height - bottomExtra - sliderZone
        val topH = sugH + (stripH - sugH) * barP
        val gridTop = topH
        val dm = resources.displayMetrics
        val shortSide = minOf(dm.widthPixels, dm.heightPixels)
        val leftMarginPx = if (land) shortSide * (marginLeftPct / 100f)
            else width * (marginLeftPct / 100f)
        val rightMarginPx = if (land) shortSide * (marginRightPct / 100f)
            else width * (marginRightPct / 100f)
        val contentLeft = leftMarginPx
        val contentWidth = width - leftMarginPx - rightMarginPx
        val avail = contentWidth - 2 * padH
        val conventional = gapFraction <= 0.001f && !land
        val portraitContent = shortSide - shortSide * (marginLeftPct / 100f) -
            shortSide * (marginRightPct / 100f)
        val portraitAvail = portraitContent - 2 * padH
        val portraitPillW = maxOf(px(24f), portraitContent * gapFraction)
        val portraitColW = (portraitAvail - portraitPillW - 6 * gapU) / 6f
        val pillW = when {
            conventional -> 0f
            land -> maxOf(px(24f), avail - 6 * portraitColW - 6 * gapU)
            else -> maxOf(px(24f), contentWidth * gapFraction)
        }
        val colW = when {
            conventional -> (avail - 5 * gapU) / 6f
            land -> portraitColW
            else -> (avail - pillW - 6 * gapU) / 6f
        }

        val leftOrigin = contentLeft + padH
        val rightOrigin = if (conventional) leftOrigin + 3 * colW + 3 * gapU
        else leftOrigin + 3 * colW + pillW + 4 * gapU
        val spaceBarH = if (land) (zoneTop - gridTop) * 0.20f else px(spaceBarHU)
        val rowH = if (conventional) (zoneTop - gridTop - 2 * padV - 3 * gapU - spaceBarH) / 3f
        else (zoneTop - gridTop - 2 * padV - 2 * gapU) / 3f
        if (conventional) {
            val top = gridTop + padV + 3 * rowH + 3 * gapU
            geoPillRect.set(leftOrigin, top, leftOrigin + 6 * colW + 5 * gapU, top + spaceBarH)
        } else {
            val gl = leftOrigin + 3 * colW + 3 * gapU
            geoPillRect.set(gl, gridTop + padV, gl + pillW, gridTop + padV + 3 * rowH + 2 * gapU)
        }
        val pillRect = geoPillRect
        val sliderTop = zoneTop + (sliderZone - sliderH) / 2f
        val capsuleH = (sliderZone - sliderH) / 2f
        val capsuleTop = zoneTop
        geoSuggestRect.set(contentLeft, 0f, contentLeft + contentWidth, topH)
        val suggestRect = geoSuggestRect
        val spaceW = if (land) portraitPillW else pillW
        return Geo(stripH, sliderZone, sliderTop, sliderH, gridTop, rowH, colW, gapU,
            padH, padV, leftOrigin, rightOrigin, contentLeft, contentWidth,
            capsuleTop, capsuleH, pillRect, conventional, sugH, suggestRect, land, spaceW)
    }

    private fun cellRect(g: Geo, side: GridSide, row: Int, col: Int): RectF {
        val originX = if (side == GridSide.LEFT) g.leftOrigin else g.rightOrigin
        val x = originX + col * (g.colW + g.gapU)
        val y = g.gridTop + g.padV + row * (g.rowH + g.gapU)
        return RectF(x, y, x + g.colW, y + g.rowH)
    }

    private fun cellRectForDraw(g: Geo, side: GridSide, row: Int, col: Int): RectF {
        val index = (if (side == GridSide.LEFT) 0 else 9) + row * 3 + col
        val rect = drawCellRects[index]
        val originX = if (side == GridSide.LEFT) g.leftOrigin else g.rightOrigin
        val x = originX + col * (g.colW + g.gapU)
        val y = g.gridTop + g.padV + row * (g.rowH + g.gapU)
        rect.set(x, y, x + g.colW, y + g.rowH)
        return rect
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
        refreshLocContext()
        suggestions = emptyList()
        blacklist = Prefs.blacklist(context)
        learned = Prefs.learned(context)
        personal = Prefs.personal(context)
        SpellEngine.setPersonal(personal)
        SpellEngine.loadAsync(context, lang) {
            refreshSuggestions()
            invalidate()
        }
        onLangChanged?.invoke()
        tutEvent?.invoke("lang")
        kickAnim()
    }

    private fun shouldAutoCap(): Boolean {
        if (!autoCap || autoCapOff) return false
        val b = textTarget?.peekBefore(3) ?: return true
        val trimmed = b.trimEnd(' ')
        if (trimmed.isEmpty()) return true
        return trimmed.last() in ".?!\n"
    }

    private fun logBigram(prev: String, cur: String) {
        var buffer = bigramBuffers[lang]
        if (buffer == null) {
            buffer = StringBuilder()
            bigramBuffers[lang] = buffer
        }
        buffer.append(prev).append(cur).append('\n')
        if (!bigramFlushScheduled) {
            bigramFlushScheduled = true
            handler.postDelayed(bigramFlushRunnable, 5_000L)
        }
    }

    fun flushPendingBigrams() {
        handler.removeCallbacks(bigramFlushRunnable)
        bigramFlushScheduled = false
        val iterator = bigramBuffers.entries.iterator()
        while (iterator.hasNext()) {
            val (language, buffer) = iterator.next()
            try {
                java.io.File(context.filesDir, "bigrams_$language.txt")
                    .appendText(buffer.toString())
                iterator.remove()
            } catch (_: Exception) {
            }
        }
        if (bigramBuffers.isNotEmpty() && !bigramFlushScheduled) {
            bigramFlushScheduled = true
            handler.postDelayed(bigramFlushRunnable, 5_000L)
        }
    }

    private fun commitChar(raw: String): String {
        var out = raw
        val boundaryWord = if (out.length == 1 && out[0] in autoSpaceChars) currentToken() else ""
        if (out.length == 1 && out[0].isLetter()) {
            val upper = shiftMode != ShiftMode.OFF || shouldAutoCap()
            if (upper) out = out.uppercase()
            if (shiftMode == ShiftMode.ONCE) shiftMode = ShiftMode.OFF
        }
        if (out.length == 1 && out[0] in autoSpaceChars) {
            val b2 = textTarget?.peekBefore(2) ?: ""
            if (b2.length == 2 && b2[1] == ' ' && b2[0] in autoSpaceChars) {
                textTarget?.deleteBefore()
            }
        }
        textTarget?.commit(out)
        if (autoSpace && out.length == 1 && out[0] in autoSpaceChars) {
            textTarget?.commit(" ")
        }
        lastAutocorrect = null
        undoStack.clear()
        if (bigramsEnabled && !noLearnField && !tutorialMode &&
            out.length == 1 && out[0].isLetter()) {
            val cur = out.lowercase()
            lastLetter?.let { prev -> logBigram(prev, cur) }
            lastLetter = cur
        } else {
            lastLetter = null
        }
        tutEvent?.invoke("commit:$out")
        if (boundaryWord.isNotEmpty()) handleBoundary(boundaryWord) else refreshSuggestions()
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
                val now = SystemClock.uptimeMillis()
                if (now < erasePauseUntil) {
                    handler.postDelayed(this, 30L)
                    return
                }
                if (leftward) {
                    val ch = deleteOneUnit()
                    if (ch.isNotEmpty()) {
                        s.deleted++
                        tutEvent?.invoke("delete"); noteBoundary(ch.last().toString())
                    }
                } else {
                    val ch = undoStack.removeLastOrNull()
                    if (ch != null) {
                        textTarget?.commit(ch); s.redone++
                        tutEvent?.invoke("redo"); noteBoundary(ch)
                    }
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
    private fun knobD(g: Geo): Float = g.sliderH * 1.25f
    private fun knobTravel(g: Geo): Float {
        val inner = g.contentWidth - 2 * px(16f)
        return inner / 2f - knobD(g) / 2f
    }

    private fun sliderMove(n: Int) {
        if (n == 0) return
        textTarget?.moveCursor(n, cursorWord)
        tickOffset += n * px(12f)
        tutEvent?.invoke("cursor")
        refreshSuggestions()
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
        val path = drawPath
        path.rewind()
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
        for (i in drawAlphas.indices) {
            val dy = px(2f) * (i + 1)
            val grow = px(1f) * i
            shadowPaint.color = theme.shadowColor
            shadowPaint.alpha = drawAlphas[i]
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
        val landNow = resources.configuration.orientation ==
                android.content.res.Configuration.ORIENTATION_LANDSCAPE
        val baseSize = if (landNow) r.height() * 0.19f else px(13f)
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

    // - Normalized anchors (0..1) for the tutorial DemoOverlay -
    fun demoCell(side: Int, row: Int, col: Int): RectF? {
        if (width == 0 || height == 0) return null
        val g = geo()
        val r = cellRect(g, if (side == 0) GridSide.LEFT else GridSide.RIGHT, row, col)
        return RectF(r.left / width, r.top / height, r.right / width, r.bottom / height)
    }

    fun demoCellForChar(ch: Char): RectF? {
        if (width == 0 || height == 0) return null
        val g = geo()
        for (side in listOf(GridSide.LEFT, GridSide.RIGHT)) {
            for (row in 0..2) for (col in 0..2) {
                if (keyAt(side, row, col).tap.equals(ch.toString(), ignoreCase = true)) {
                    val r = cellRect(g, side, row, col)
                    return RectF(r.left / width, r.top / height, r.right / width, r.bottom / height)
                }
            }
        }
        return null
    }

    fun demoPill(): RectF? {
        if (width == 0 || height == 0) return null
        val g = geo()
        val r = g.pillRect
        return RectF(r.left / width, r.top / height, r.right / width, r.bottom / height)
    }

    fun demoKnob(): FloatArray? {
        if (width == 0 || height == 0) return null
        val g = geo()
        return floatArrayOf(knobCenterX(g) / width, (g.sliderTop + g.sliderH / 2f) / height)
    }

    fun demoCapsule(): FloatArray? {
        if (width == 0 || height == 0) return null
        val g = geo()
        return floatArrayOf(
            (g.contentLeft + px(8f) + capsuleWidth() / 2f) / width,
            (g.capsuleTop + g.capsuleH / 2f) / height
        )
    }

    fun demoStripY(): Float {
        val g = geo()
        return (g.stripH / 2f) / height.coerceAtLeast(1)
    }
    private fun spawnShreds() {
        if (!animHelp) return
        val now = SystemClock.uptimeMillis()
        val cx = width / 2f
        for (i in 0 until 6) {
            shreds.add(Shred(cx + px(6f) * (i - 2.5f), height - px(150f),
                px(2f) * (i - 2.5f), px(140f), "aeioust"[i], now))
        }
        kickAnim()
    }

    private fun spawnPop() {
        if (!animHelp) return
        pops.add(Pop(width / 2f, height - px(120f), SystemClock.uptimeMillis()))
        kickAnim()
    }

    private fun drawParticles(canvas: Canvas, now: Long) {
        val life = 500L
        shreds.removeAll { now - it.t0 > life }
        pops.removeAll { now - it.t0 > life }
        if (shreds.isEmpty() && pops.isEmpty()) return
        letterPaint.textSize = px(14f)
        letterPaint.typeface = fontRegular
        letterPaint.color = theme.hint
        for (s in shreds) {
            val t = (now - s.t0) / life.toFloat()
            letterPaint.alpha = (255 * (1f - t)).toInt()
            canvas.drawText(s.ch.toString(), s.x + s.vx * t, s.y + s.vy * t * t, letterPaint)
        }
        letterPaint.alpha = 255
        val oldTick = tickPaint.style
        tickPaint.style = Paint.Style.STROKE
        for (p in pops) {
            val t = (now - p.t0) / life.toFloat()
            val r = px(10f) + px(30f) * t
            tickPaint.color = theme.hint
            tickPaint.alpha = (255 * (1f - t)).toInt()
            tickPaint.strokeWidth = px(2f)
            for (i in 0 until 8) {
                val a = i * (Math.PI / 4.0).toFloat()
                val ca = Math.cos(a.toDouble()).toFloat()
                val sa = Math.sin(a.toDouble()).toFloat()
                canvas.drawLine(p.x + ca * r, p.y + sa * r,
                    p.x + ca * (r + px(6f)), p.y + sa * (r + px(6f)), tickPaint)
            }
        }
        tickPaint.alpha = 255
        tickPaint.style = oldTick
        if (shreds.isNotEmpty() || pops.isNotEmpty()) kickAnim()
    }

    private fun drawEraseRedoOverlay(canvas: Canvas, g: Geo, eraseT: Float, redoT: Float) {
        val t = maxOf(eraseT, redoT)
        keycapPaint.color = blendColor(theme.role[Themes.BG], 0xFF000000.toInt(), 0.5f * t)
        canvas.drawRect(0f, g.suggestRect.bottom, width.toFloat(), height.toFloat(), keycapPaint)
        val cx = width / 2f
        val cy = height - px(120f)
        val s = px(30f)
        val oldStyle = iconPaint.style
        val oldCap = iconPaint.strokeCap
        iconPaint.style = Paint.Style.STROKE
        iconPaint.strokeCap = Paint.Cap.ROUND
        iconPaint.color = theme.text
        if (eraseT >= redoT) {
            iconPaint.strokeWidth = px(3.5f)
            val topW = s * 1.15f
            val botW = s * 0.8f
            val bt = cy - s * 0.45f
            val bb = cy + s * 0.85f
            val rr = px(4f)
            val body = Path().apply {
                moveTo(cx - topW / 2f, bt)
                lineTo(cx + topW / 2f, bt)
                lineTo(cx + botW / 2f, bb - rr)
                quadTo(cx + botW / 2f, bb, cx + botW / 2f - rr, bb)
                lineTo(cx - botW / 2f + rr, bb)
                quadTo(cx - botW / 2f, bb, cx - botW / 2f, bb - rr)
                close()
            }
            canvas.drawPath(body, iconPaint)
            val inset = s * 0.18f
            for (fx in floatArrayOf(-0.32f, 0f, 0.32f)) {
                val x = cx + topW * fx * 0.5f
                canvas.drawLine(x, bt + inset, x, bb - inset, iconPaint)
            }
            val lidY = bt - px(5f)
            val lidL = cx - topW / 2f - px(3f)
            val lidR = cx + topW / 2f + px(3f)
            canvas.save()
            canvas.rotate(-55f * eraseT, lidL, lidY)
            canvas.drawLine(lidL, lidY, lidR, lidY, iconPaint)
            val nh = px(7f)
            canvas.drawLine(cx - nh, lidY, cx - nh, lidY - px(5f), iconPaint)
            canvas.drawLine(cx - nh, lidY - px(5f), cx + nh, lidY - px(5f), iconPaint)
            canvas.drawLine(cx + nh, lidY - px(5f), cx + nh, lidY, iconPaint)
            canvas.restore()
        } else {
            val ps = s * (0.45f + 0.85f * redoT)
            iconPaint.strokeWidth = px(5f)
            canvas.drawLine(cx - ps, cy, cx + ps, cy, iconPaint)
            canvas.drawLine(cx, cy - ps, cx, cy + ps, iconPaint)
        }
        iconPaint.strokeCap = oldCap
        iconPaint.style = oldStyle
    }
    private fun drawIncognito(canvas: Canvas, r: RectF) {
        val cy = r.centerY()
        val s = px(10f)
        val msg = locStr(R.string.sensitive_hint)
        hintPaint.color = theme.hint
        hintPaint.textSize = px(12f)
        hintPaint.typeface = fontRegular
        val tw = hintPaint.measureText(msg)
        val gap = px(10f)
        val total = s * 2f + gap + tw
        val startX = r.centerX() - total / 2f
        val ix = startX + s
        iconPaint.color = theme.hint
        iconPaint.strokeWidth = px(2f)
        canvas.drawCircle(ix, cy, s, iconPaint)
        canvas.drawLine(ix - s * 0.7f, cy + s * 0.7f, ix + s * 0.7f, cy - s * 0.7f, iconPaint)
        val yC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
        val oldAlign = hintPaint.textAlign
        hintPaint.textAlign = Paint.Align.LEFT
        canvas.drawText(msg, ix + s + gap, cy + yC, hintPaint)
        hintPaint.textAlign = oldAlign
    }


    private fun drawSymbolLayer(canvas: Canvas, g: Geo) {
        val area = drawRectA.apply { set(g.contentLeft, g.gridTop, g.contentLeft + g.contentWidth, g.capsuleTop) }
        keycapPaint.color = theme.role[Themes.BG]
        canvas.drawRect(area, keycapPaint)
        val gapU = g.gapU
        val colW = (area.width() - 10 * gapU) / 9f
        val rowH = (area.height() - 5 * gapU) / 4f
        val radius = px(12f)
        val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
        for (r in 0..3) {
            for (c in 0..8) {
                val sym = symbolRows[r][c]
                if (sym == "SPACE" && c == 2) continue
                val span = if (sym == "SPACE") 2 else 1
                val x = area.left + gapU + c * (colW + gapU)
                val y = area.top + gapU + r * (rowH + gapU)
                val w = colW * span + gapU * (span - 1)
                val rect = drawRectB.apply { set(x, y, x + w, y + rowH) }
                keycapPaint.color = theme.role[Themes.KEYCAP]
                canvas.drawRoundRect(rect, radius, radius, keycapPaint)
                when (sym) {
                    "BACK" -> drawIcon(canvas, Icon.BACK, rect.centerX(), rect.centerY(), px(22f))
                    "ENTER" -> drawIcon(canvas, Icon.ENTER, rect.centerX(), rect.centerY(), px(22f))
                    "SPACE" -> {
                        hintPaint.color = theme.hint
                        hintPaint.textSize = px(16f)
                        hintPaint.typeface = fontRegular
                        canvas.drawText("␣", rect.centerX(), rect.centerY() + yC, hintPaint)
                    }
                    "123", "ABC" -> {
                        textPaint.color = theme.text
                        textPaint.textSize = px(16f)
                        textPaint.typeface = fontMedium
                        canvas.drawText(sym.lowercase(), rect.centerX(), rect.centerY() + yC, textPaint)
                    }
                    else -> {
                        textPaint.color = theme.text
                        textPaint.textSize = px(20f)
                        textPaint.typeface = fontMedium
                        canvas.drawText(sym, rect.centerX(), rect.centerY() + yC, textPaint)
                    }
                }
            }
        }
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

        val shiftCaps = shiftMode != ShiftMode.OFF
        if (!caseInitialized) {
            caseInitialized = true
            lastCapsVisual = shiftCaps
        } else if (shiftCaps != lastCapsVisual) {
            caseFromUpper = lastCapsVisual
            val allow = caseDur() > 0L && animStrong && (now - attachTime > 300L)
            caseAnimStart = if (allow) now else -1L
            lastCapsVisual = shiftCaps
            if (caseAnimStart >= 0L) kickAnim()
        }
        val caseT = if (caseAnimStart >= 0L) cubicOut(t01(caseAnimStart, caseDur(), now)) else 1f

        canvas.drawColor(theme.role[Themes.BG])

        val topRect = g.suggestRect
        keycapPaint.color = theme.role[Themes.STRIP]
        canvas.drawRect(topRect, keycapPaint)
        if (barP > 0.5f) {
            val cw = g.contentWidth / 10f
            val page = barPages[barPage]
            textPaint.color = theme.text
            textPaint.textSize = g.stripH * 0.40f
            textPaint.typeface = fontMedium
            val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
            for (i in 0..9) {
                canvas.drawText(page[i], g.contentLeft + i * cw + cw / 2f,
                    topRect.centerY() + yC, textPaint)
            }
            hintPaint.color = theme.hint
            hintPaint.textSize = px(10f)
            for (p in barPages.indices) {
                val dotX = g.contentLeft + g.contentWidth / 2f + (p - 1) * px(14f)
                hintPaint.alpha = if (p == barPage) 255 else 120
                canvas.drawText("•", dotX, topRect.bottom - px(6f), hintPaint)
            }
            hintPaint.alpha = 255
        } else if (sensitiveField) {
            drawIncognito(canvas, topRect)
        } else if (suggestions.isEmpty()) {
            hintPaint.color = theme.hint
            hintPaint.textSize = g.stripH * 0.25f
            hintPaint.typeface = fontRegular
            val syC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
            canvas.drawText(locStr(R.string.no_suggestions),
                topRect.centerX(), topRect.centerY() + syC, hintPaint)
        } else {
            val third = topRect.width() / 3f
            for (i in 0..2) {
                val candIdx = when (i) { 0 -> 1; 1 -> 0; else -> 2 }
                val rawWord = suggestions.getOrElse(candIdx) { "" }
                val word = if (rawWord == TUTORIAL_SENTINEL)
                    locStr(R.string.banner_tutorial) else rawWord
                if (word.isEmpty()) continue
                val cx = topRect.left + third * i + third / 2f
                if (i == 1) {
                    keycapPaint.color = blendColor(theme.role[Themes.STRIP], theme.flashPole, 0.15f)
                    drawRectA.set(topRect.left + third + px(4f), topRect.top + px(4f),
                        topRect.left + 2 * third - px(4f), topRect.bottom - px(4f))
                    canvas.drawRoundRect(drawRectA, px(10f), px(10f), keycapPaint)
                    textPaint.textSize = g.stripH * 0.33f
                    textPaint.typeface = fontSemiBold
                    textPaint.color = theme.text
                } else {
                    textPaint.textSize = g.stripH * 0.27f
                    textPaint.typeface = fontRegular
                    textPaint.color = theme.hint
                }
                val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
                canvas.drawText(word, cx, topRect.centerY() + yC, textPaint)
            }
        }
        if (sugManageIndex in 0..2) {
            val cy = topRect.centerY()
            val trackH = topRect.height() * 0.5f
            val trackL = topRect.left + px(24f)
            val trackR = topRect.right - px(24f)
            val trackC = (trackL + trackR) / 2f
            val half = (trackR - trackL) / 2f - trackH / 2f
            val knobX = (trackC + sugManageDx).coerceIn(trackC - half, trackC + half)
            keycapPaint.color = blendColor(theme.role[Themes.STRIP], theme.flashPole, 0.25f)
            canvas.drawRoundRect(topRect, px(12f), px(12f), keycapPaint)
            val trackRect = drawRectA.apply { set(trackL, cy - trackH / 2f, trackR, cy + trackH / 2f) }
            keycapPaint.color = theme.role[Themes.SLIDER]
            canvas.drawRoundRect(trackRect, trackH / 2f, trackH / 2f, keycapPaint)
            tickPaint.color = theme.hint
            tickPaint.alpha = 110
            val sp = px(12f)
            var tx2 = trackL + sp
            while (tx2 < trackR - sp / 2f) {
                canvas.drawLine(tx2, cy - px(5f), tx2, cy + px(5f), tickPaint)
                tx2 += sp
            }
            tickPaint.alpha = 255
            drawIcon(canvas, Icon.BACK, trackL - px(14f), cy, px(12f))
            tickPaint.color = theme.hint
            tickPaint.strokeWidth = px(3f)
            val pr = trackR + px(14f)
            canvas.drawLine(pr - px(6f), cy, pr + px(6f), cy, tickPaint)
            canvas.drawLine(pr, cy - px(6f), pr, cy + px(6f), tickPaint)
            tickPaint.strokeWidth = px(2f)
            val th = px(60f)
            val t = (abs(sugManageDx) / th).coerceIn(0f, 1f)
            val targetCol = if (sugManageDx >= 0) 0xFF4CAF50.toInt() else 0xFFE53935.toInt()
            val knobCol = if (t <= 0.02f) theme.role[Themes.KNOB]
            else blendColor(theme.role[Themes.KNOB], targetCol, t)
            val kd2 = trackH * 1.3f
            val knobRect = drawRectB.apply { set(knobX - kd2 / 2f, cy - kd2 / 2f, knobX + kd2 / 2f, cy + kd2 / 2f) }
            drawSoftShadow(canvas, knobRect, kd2 / 2f)
            keycapPaint.color = knobCol
            canvas.drawRoundRect(knobRect, kd2 / 2f, kd2 / 2f, keycapPaint)
            tickPaint.color = theme.text
            for (i in -1..1) {
                val bx = knobX + i * px(5f)
                canvas.drawLine(bx, cy - px(7f), bx, cy + px(7f), tickPaint)
            }
        }
        fun dimFor(r: RectF): Float {
            if (!animDepth) return 0f
            var d = 0f
            for (p in drawPresses) {
                val pr = pressProgress(p, now)
                if (pr <= 0f) continue
                val pc = drawRectA.apply { set(cellRectForDraw(g, p.side, p.row, p.col)) }
                val dist = hypot(pc.centerX() - r.centerX(), pc.centerY() - r.centerY())
                val fall = (1f - dist / (3.5f * g.colW)).coerceIn(0f, 1f)
                d = maxOf(d, pr * fall)
            }
            return d
        }

        val radius = px(16f)
        drawPresses.clear()
        if (animDepth) {
            drawPresses.addAll(presses.values)
            drawPresses.addAll(fadingPresses)
        }
        for (side in drawSides) {
            for (row in 0..2) {
                for (col in 0..2) {
                    val r = cellRectForDraw(g, side, row, col)
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
                    letterPaint.textSize = if (g.land) g.rowH * 0.31f else px(28f)
                    letterPaint.color = theme.text
                    val yC = -(letterPaint.descent() + letterPaint.ascent()) / 2f
                    val midY = r.centerY() + yC
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
                        val inCh = if (shiftCaps) upper else lower
                        val outCh = if (caseFromUpper) upper else lower
                        letterPaint.typeface = if (shiftCaps) fontSemiBold else fontMedium
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
            drawPresses.clear()
            drawPresses.addAll(presses.values)
            drawPresses.addAll(fadingPresses)
            for (p in drawPresses) {
                val pr = pressProgress(p, now)
                if (pr <= 0f) continue
                val r = cellRectForDraw(g, p.side, p.row, p.col)
                val sc = 1f + 0.35f * pr
                val cx = r.centerX(); val cy = r.centerY()
                val lr = drawRectD.apply { set(
                    cx - r.width() * sc / 2f, cy - r.height() * sc / 2f,
                    cx + r.width() * sc / 2f, cy + r.height() * sc / 2f
                ) }
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
                letterPaint.textSize = if (g.land) g.rowH * 0.31f else px(28f)
                val yC = -(letterPaint.descent() + letterPaint.ascent()) / 2f
                val centerScale = if (p.sel != SwipeDir.TAP) 0.9f else 1f
                letterPaint.typeface =
                    if (fb != null || p.sel == SwipeDir.TAP || capsVisual) fontSemiBold else fontMedium
                letterPaint.color = theme.text
                canvas.save()
                canvas.clipRect(lr)
                canvas.translate(cx, cy + yC)
                canvas.scale(centerScale, centerScale)
                canvas.drawText(label, 0f, 0f, letterPaint)
                canvas.restore()

                if (fb == null) {
                    canvas.save()
                    canvas.clipRect(lr)
                    drawHints(canvas, key, lr, capsVisual, p, now, flashed = animHelp)
                    canvas.restore()
                }
            }
        }

        val pillDownNow = activePointers.values.any { it.zone == Zone.SPACE }
        if (!pillDownNow && pillWasDown) pillReleasedAt = now
        pillWasDown = pillDownNow

        val pillRect = g.pillRect
        var pillColor = theme.role[Themes.KEYCAP]
        val pillFb = feedbacks.firstOrNull { it.pill }
        val pillFbT = if (pillFb != null) t01(pillFb.start, fbDur(), now) else 1f
        val pillFlash = if (!animHelp) 0f else when {
            pillDownNow -> 0.45f
            pillReleasedAt >= 0 -> 0.45f * (1f - t01(pillReleasedAt, fbDur(), now))
            else -> 0f
        }
        if (pillFlash > 0f) pillColor = blendColor(pillColor, theme.flashPole, pillFlash)
        val splitPill = g.land && !g.conventional &&
            pillRect.width() > 2 * g.spaceW + px(24f)
        val holeW = if (splitPill) pillRect.width() - 2 * g.spaceW else 0f
        val pieceCount = if (splitPill) 2 else 1
        for (pieceIndex in 0 until pieceCount) {
            val pc = when {
                !splitPill -> pillRect
                pieceIndex == 0 -> drawRectA.apply { set(pillRect.left, pillRect.top,
                    pillRect.centerX() - holeW / 2f, pillRect.bottom) }
                else -> drawRectB.apply { set(pillRect.centerX() + holeW / 2f,
                    pillRect.top, pillRect.right, pillRect.bottom) }
            }
            if (theme.keycapShadow) drawSoftShadow(canvas, pc, pc.height() / 2f)
            keycapPaint.color = pillColor
            canvas.drawRoundRect(pc, pc.height() / 2f, pc.height() / 2f, keycapPaint)
            if (shiftMode == ShiftMode.CAPS) {
                drawIcon(canvas, Icon.CAPS, pc.centerX(), pc.centerY(), px(24f))
            } else if (pillFb != null && pillFbT < 0.75f) {
                hintPaint.color = theme.text
                hintPaint.textSize = px(18f)
                hintPaint.typeface = fontSemiBold
                val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
                canvas.drawText(pillFb.char, pc.centerX(), pc.centerY() + pillYC, hintPaint)
            } else if (pillFb != null) {
                val u = (pillFbT - 0.75f) / 0.25f
                hintPaint.color = theme.text
                hintPaint.textSize = px(18f)
                hintPaint.typeface = fontSemiBold
                hintPaint.alpha = (255 * (1f - u)).toInt()
                val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
                canvas.drawText(pillFb.char, pc.centerX(), pc.centerY() + pillYC, hintPaint)
                hintPaint.alpha = 255
            } else {
                hintPaint.color = theme.hint
                hintPaint.textSize = px(18f)
                hintPaint.typeface = fontRegular
                val pillYC = -(hintPaint.descent() + hintPaint.ascent()) / 2f
                canvas.drawText("␣", pc.centerX(), pc.centerY() + pillYC, hintPaint)
            }
        }

        for (s in slides) {
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
        val capRect = drawRectA.apply { set(g.contentLeft + px(8f), g.capsuleTop,
            g.contentLeft + px(8f) + capW, g.capsuleTop + g.capsuleH) }
        keycapPaint.color = theme.role[Themes.SPECIAL]
        canvas.drawRoundRect(capRect, g.capsuleH / 2f, g.capsuleH / 2f, keycapPaint)
        langPaint.color = theme.text
        langPaint.textSize = g.capsuleH * 0.68f
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

        val sliderRect = drawRectB.apply { set(g.contentLeft + px(16f), g.sliderTop,
            g.contentLeft + g.contentWidth - px(16f), g.sliderTop + g.sliderH) }
        keycapPaint.color = theme.role[Themes.SLIDER]
        canvas.drawRoundRect(sliderRect, g.sliderH / 2f, g.sliderH / 2f, keycapPaint)
        tickPaint.color = theme.hint
        tickPaint.alpha = 110
        val tickSpacing = px(12f)
        val mod = ((tickOffset % tickSpacing) + tickSpacing) % tickSpacing
        var tx = sliderRect.left + tickSpacing - mod
        while (tx < sliderRect.right - tickSpacing / 2f) {
            if (tx > sliderRect.left + tickSpacing / 2f) {
                canvas.drawLine(tx, sliderRect.centerY() - g.sliderH * 0.19f,
                    tx, sliderRect.centerY() + g.sliderH * 0.19f, tickPaint)
            }
            tx += tickSpacing
        }
        tickPaint.alpha = 255

        val kd = knobD(g)
        val kcx = knobCenterX(g) + knobP * knobTravel(g)
        val knobRect = drawRectC.apply { set(kcx - kd / 2f, sliderRect.centerY() - kd / 2f,
            kcx + kd / 2f, sliderRect.centerY() + kd / 2f) }
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

        if (symbolLayer) drawSymbolLayer(canvas, g)
        if (clipMenuOpen) {
            val r = clipMenuRect()
            val rows = clipMenuRows()
            keycapPaint.color = blendColor(theme.role[Themes.BG], theme.pole, 0.35f)
            canvas.drawRoundRect(r, px(16f), px(16f), keycapPaint)
            val rowH = r.height() / 6f
            textPaint.textAlign = Paint.Align.LEFT
            for (i in 0 until 6) {
                val y = r.top + rowH * i
                if (i < rows.size) {
                    textPaint.color = theme.text
                    textPaint.textSize = px(14f)
                    textPaint.typeface = fontRegular
                    val yC = -(textPaint.descent() + textPaint.ascent()) / 2f
                    canvas.drawText(rows[i].take(40), r.left + px(12f), y + rowH / 2f + yC, textPaint)
                    drawIcon(canvas, Icon.BACK, r.right - px(24f), y + rowH / 2f, px(14f))
                }
                if (i < 5) {
                    tickPaint.color = theme.hint
                    tickPaint.alpha = 60
                    canvas.drawLine(r.left + px(8f), y + rowH, r.right - px(8f), y + rowH, tickPaint)
                    tickPaint.alpha = 255
                }
            }
            textPaint.textAlign = Paint.Align.CENTER
        }
        val manageTh = px(60f)
        val eraseT = if (sugManageIndex in 0..2 && sugManageDx < 0f) (-sugManageDx / manageTh).coerceIn(0f, 1f) else 0f
        val redoT = if (sugManageIndex in 0..2 && sugManageDx > 0f) (sugManageDx / manageTh).coerceIn(0f, 1f) else 0f
        if (maxOf(eraseT, redoT) > 0.05f) drawEraseRedoOverlay(canvas, g, eraseT, redoT)
        drawParticles(canvas, now)

        if (caseAnimStart >= 0L && caseT >= 1f) caseAnimStart = -1L
        if (anyAnimActive(now)) kickAnim()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (width == 0 || height == 0) return true

        val g = geo()
        val edgeZone = 24 * resources.displayMetrics.density
        val now = SystemClock.uptimeMillis()
        if (clipMenuOpen) {
            if (clipMenuJustOpened && event.actionMasked == MotionEvent.ACTION_UP) {
                clipMenuJustOpened = false
                return true
            }
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> return true
                MotionEvent.ACTION_UP -> {
                    val x = event.getX(0); val y = event.getY(0)
                    val r = clipMenuRect()
                    val rows = clipMenuRows()
                    val rowH = r.height() / 6f
                    if (r.contains(x, y) && rows.isNotEmpty()) {
                        val idx = ((y - r.top) / rowH).toInt()
                        if (idx in rows.indices) {
                            val word = rows[idx]
                            if (x > r.right - px(48f)) {
                                Prefs.removeClipEntry(context, word)
                                cachedClipMenuRows = null
                                tutEvent?.invoke("clipTrash")
                                refreshSuggestions()
                                if (clipMenuRows().isEmpty()) clipMenuOpen = false
                            } else {
                                textTarget?.commit(word)
                                Prefs.touchClipEntry(context, word)
                                undoStack.clear()
                                clipMenuOpen = false
                                refreshSuggestions()
                            }
                        }
                    } else {
                        clipMenuOpen = false
                    }
                    invalidate()
                    return true
                }
                else -> return true
            }
        }
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
                val leftEdge = g.leftOrigin + 3 * g.colW + 2 * g.gapU
                if (symbolLayer && y >= g.gridTop && y <= g.capsuleTop &&
                    x >= g.contentLeft && x <= g.contentLeft + g.contentWidth
                ) {
                    zone = Zone.SYMBOL
                    val colW4 = (g.contentWidth - 10 * g.gapU) / 9f
                    val rowH4 = (g.capsuleTop - g.gridTop - 5 * g.gapU) / 4f
                    val c4 = ((x - g.contentLeft - g.gapU) / (colW4 + g.gapU)).toInt().coerceIn(0, 8)
                    val r4 = ((y - g.gridTop - g.gapU) / (rowH4 + g.gapU)).toInt().coerceIn(0, 3)
                    cellIndex = r4 * 16 + c4
                } else when {
                    y < g.suggestRect.height() && x >= g.contentLeft &&
                            x <= g.contentLeft + g.contentWidth -> {
                        if (barP > 0.5f) {
                            zone = Zone.BAR
                            cellIndex = ((x - g.contentLeft) / (g.contentWidth / 10f))
                                .toInt().coerceIn(0, 9)
                        } else {
                            zone = Zone.SUGGEST
                            cellIndex = ((x - g.contentLeft) / (g.contentWidth / 3f))
                                .toInt().coerceIn(0, 2)
                        }
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
                    g.pillRect.contains(x, y) -> {
                        zone = Zone.SPACE
                    }
                    x < leftEdge && x >= g.contentLeft -> {
                        side = GridSide.LEFT
                        zone = Zone.LEFT
                        col = ((x - g.leftOrigin) / (g.colW + g.gapU)).toInt().coerceIn(0, 2)
                        row = ((y - (g.gridTop + g.padV)) / (g.rowH + g.gapU)).toInt().coerceIn(0, 2)
                    }
                    x >= g.rightOrigin && x <= g.contentLeft + g.contentWidth -> {
                        side = GridSide.RIGHT
                        zone = Zone.RIGHT
                        col = ((x - g.rightOrigin) / (g.colW + g.gapU)).toInt().coerceIn(0, 2)
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
                if (zone == Zone.SUGGEST) {
                    if (cellIndex == 2 && clipHistOn && !sensitiveField) {
                        sugPressPointer = id
                        sugOpenRunnable = Runnable {
                            tryOpenClipMenu(true)
                            sugPressPointer = -1
                        }
                        handler.postDelayed(sugOpenRunnable!!, LONG_PRESS_MS)
                        kickAnim()
                    }
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
                    if (sugManagePointer == id && sugManageIndex >= 0) {
                        sugManageDx = x - state.startX
                        kickAnim()
                    }
                    if ((sugPressPointer == id || sugManagePointer == id) &&
                        sugManageIndex < 0 &&
                        hypot(x - state.startX, y - state.startY) > swipeThreshold) {
                        sugOpenRunnable?.let { handler.removeCallbacks(it) }
                        sugOpenRunnable = null
                        sugPressPointer = -1
                        sugManageRunnable?.let { handler.removeCallbacks(it) }
                        sugManageRunnable = null
                        sugManagePointer = -1
                    }
                    if (state.zone == Zone.SUGGEST && sugManageIndex < 0) {
                        val ddx = x - state.startX
                        val ddy = y - state.startY
                        if (abs(ddx) > abs(ddy) && hypot(ddx, ddy) > swipeThreshold) {
                            sugManageIndex = state.cellIndex
                            sugManagePointer = id
                            sugManageDx = ddx
                            kickAnim()
                        }
                    }

                    if (state.zone == Zone.SLIDER) {
                        if (state.knobDrag) {
                            val travel = knobTravel(g)
                            val p = ((x - state.grabX) / travel).coerceIn(-1f, 1f)
                            val deltaChars = (p - state.knobP) * sensChars
                            state.knobP = p
                            knobP = p
                            state.cursorAcc += deltaChars
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
                                        if (erasePaused()) return@repeat
                                        if (state.deleted == 0 && undoWhole && lastAutocorrect != null) {
                                            deleteOneUnit()
                                            state.deleted++
                                            tutEvent?.invoke("delete")
                                            return@repeat
                                        }
                                        val ch = textTarget?.deleteBefore() ?: ""
                                        if (ch.isNotEmpty()) {
                                            undoStack.addLast(ch); state.deleted++
                                            tutEvent?.invoke("delete"); noteBoundary(ch)
                                        }
                                    }
                                }
                            } else if (dxEvent > 0f) {
                                state.redoAcc += dxEvent / pxPerEraseUnit
                                val n = state.redoAcc.toInt()
                                if (n > 0) {
                                    state.redoAcc -= n
                                    repeat(n) {
                                        if (erasePaused()) return@repeat
                                        val ch = undoStack.removeLastOrNull() ?: return@repeat
                                        textTarget?.commit(ch)
                                        state.redone++
                                        tutEvent?.invoke("redo"); noteBoundary(ch)
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
                if (sugPressPointer == id) {
                    sugOpenRunnable?.let { handler.removeCallbacks(it) }
                    sugOpenRunnable = null
                    sugPressPointer = -1
                }
                var managed = false
                if (sugManagePointer == id) {
                    sugManageRunnable?.let { handler.removeCallbacks(it) }
                    sugManageRunnable = null
                    sugManagePointer = -1
                    if (sugManageIndex >= 0) {
                        managed = true
                        val word = suggestions.getOrElse(
                            when (sugManageIndex) { 0 -> 1; 1 -> 0; else -> 2 }) { "" }
                        val th = px(60f)
                        if (word.isNotEmpty() && word != TUTORIAL_SENTINEL) {
                            when {
                                sugManageDx < -th -> {
                                    Prefs.addBlacklist(context, word)
                                    Prefs.purgeLearnedValue(context, word)
                                    tutEvent?.invoke("manageAdd")
                                    Prefs.removePersonal(context, word)
                                    spawnShreds()
                                }
                                sugManageDx > th -> {
                                    Prefs.addPersonal(context, word)
                                    tutEvent?.invoke("manageAdd")
                                    Prefs.removeBlacklist(context, word)
                                    spawnPop()
                                }
                            }
                            blacklist = Prefs.blacklist(context)
                            personal = Prefs.personal(context)
                            SpellEngine.setPersonal(personal)
                        }
                        sugManageIndex = -1
                        sugManageDx = 0f
                        refreshSuggestions()
                        invalidate()
                    }
                }
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
                if (!managed) resolvePointer(state)
            }

            MotionEvent.ACTION_CANCEL -> {
                activePointers.keys.toList().forEach { stopEdgeRepeat(it) }
                activePointers.keys.toList().forEach { id ->
                    presses.remove(id)?.let { p -> p.release = now; fadingPresses.add(p) }
                }
                sugOpenRunnable?.let { handler.removeCallbacks(it) }
                sugOpenRunnable = null
                sugPressPointer = -1
                sugManageRunnable?.let { handler.removeCallbacks(it) }
                sugManageRunnable = null
                sugManagePointer = -1
                sugManageIndex = -1
                sugManageDx = 0f
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
            lastCenterTapTime = 0L
            symbolLayer = !symbolLayer
            barVisible = false
            tutEvent?.invoke(if (symbolLayer) "symopen" else "symclose")
        } else {
            lastCenterTapTime = now
            barVisible = !barVisible
            barPage = 0
            if (barVisible) tutEvent?.invoke("bar")
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
            Zone.SUGGEST -> {
                if (dir == SwipeDir.S && state.cellIndex == 2 && clipHistOn) {
                    tryOpenClipMenu(false)
                    return
                }
                if (dir != SwipeDir.TAP) return
                val candIdx = when (state.cellIndex) { 0 -> 1; 1 -> 0; else -> 2 }
                if (candIdx == 2 && clipFull.isNotEmpty() && currentToken().isEmpty()) {
                    textTarget?.commit(clipFull)
                    tutEvent?.invoke("clipPaste")
                    Prefs.touchClipEntry(context, clipFull)
                    undoStack.clear()
                    refreshSuggestions()
                    invalidate()
                    return
                }
                val word = suggestions.getOrElse(candIdx) { "" }
                if (word.isEmpty()) return
                if (word == TUTORIAL_SENTINEL) {
                    val tut = Intent(context, TutorialActivity::class.java)
                    tut.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(tut)
                    return
                }
                if (currentToken().isEmpty()) {
                    tutEvent?.invoke("predictTap")
                    textTarget?.commit(word)
                    textTarget?.commit(" ")
                    undoStack.clear()
                    refreshSuggestions()
                    invalidate()
                    return
                }
                applySuggestion(word)
            }

            Zone.SYMBOL -> {
                if (dir != SwipeDir.TAP) return
                val r = state.cellIndex / 16
                val c = state.cellIndex % 16
                val sym = symbolRows.getOrNull(r)?.getOrNull(c) ?: return
                when (sym) {
                    "123" -> {
                        barVisible = true
                        barPage = 0
                        barTarget = 1f
                        barAnimFrom = barP
                        barAnimStart = SystemClock.uptimeMillis()
                        if (!animStrong) barP = 1f
                        tutEvent?.invoke("bar")
                        kickAnim()
                    }
                    "ABC" -> {
                        symbolLayer = false
                        tutEvent?.invoke("symclose")
                        kickAnim()
                    }
                    "SPACE" -> { textTarget?.commit(" "); undoStack.clear() }
                    "BACK" -> {
                        val ch = textTarget?.deleteBefore() ?: ""
                        if (ch.isNotEmpty()) undoStack.addLast(ch)
                    }
                    "ENTER" -> { textTarget?.enter(); undoStack.clear() }
                    else -> commitChar(sym)
                }
            }

            Zone.BAR -> {
                if (dir == SwipeDir.TAP) {
                    commitChar(barPages[barPage][state.cellIndex])
                } else if (dir == SwipeDir.W) {
                    barPage = (barPage + 1).coerceAtMost(barPages.size - 1)
                    tutEvent?.invoke("barpage")
                    invalidate()
                } else if (dir == SwipeDir.E) {
                    barPage = (barPage - 1).coerceAtLeast(0)
                    tutEvent?.invoke("barpage")
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
                    val applied = maybeAutocorrect()
                    if (!applied) {
                        val finished = currentToken()
                        val b = textTarget?.peekBefore(2) ?: ""
                        val blocked = autoSpace && b.length == 2 && b[1] == ' ' && b[0] in autoSpaceChars
                        if (!blocked) {
                            textTarget?.commit(" ")
                            tutEvent?.invoke("commit: ")
                            undoStack.clear()
                            lastAutocorrect = null
                            spawnFeedback(null, 0, 0, true, "␣")
                        }
                        handleBoundary(finished)
                    }
                }
                SwipeDir.W -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        val ch = deleteOneUnit()
                        if (ch.isNotEmpty()) {
                            tutEvent?.invoke("delete")
                            noteBoundary(ch.last().toString())
                        }
                    }
                    slides.add(GlyphSlide(Icon.BACK, dir, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                SwipeDir.E -> {
                    if (state.deleted == 0 && state.redone == 0) {
                        undoStack.removeLastOrNull()?.let {
                            textTarget?.commit(it)
                            tutEvent?.invoke("redo")
                            noteBoundary(it)
                        }
                    }
                    slides.add(GlyphSlide(Icon.REDO, dir, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                SwipeDir.S, SwipeDir.SE, SwipeDir.SW -> {
                    textTarget?.enter()
                    autoCapOff = false
                    undoStack.clear()
                    tutEvent?.invoke("enter")
                    slides.add(GlyphSlide(Icon.ENTER, SwipeDir.S, SystemClock.uptimeMillis()))
                    kickAnim()
                }
                else -> {
                    val now = SystemClock.uptimeMillis()
                    val prevMode = shiftMode
                    val capsNow = prevMode != ShiftMode.OFF || shouldAutoCap()
                    val isDouble = now - lastUpSwipeTime < doubleWindowMs.toLong()
                    shiftMode = when {
                        prevMode == ShiftMode.CAPS -> {
                            autoCapOff = true
                            ShiftMode.OFF
                        }
                        isDouble -> {
                            lastUpSwipeTime = 0L
                            ShiftMode.CAPS
                        }
                        capsNow -> {
                            autoCapOff = true
                            ShiftMode.OFF
                        }
                        else -> {
                            lastUpSwipeTime = now
                            ShiftMode.ONCE
                        }
                    }
                    val capsAfter = shiftMode != ShiftMode.OFF || shouldAutoCap()
                    if (capsAfter && !capsNow) tutEvent?.invoke("capsOn")
                    if (!capsAfter && capsNow) tutEvent?.invoke("capsOff")
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
        refreshSuggestions()
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
