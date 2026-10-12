package com.aleix.thumbgridtest

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.zip.GZIPInputStream

object SpellEngine {

    @Volatile private var lang = ""
    @Volatile private var words: Set<String> = emptySet()
    @Volatile private var byFreq: List<String> = emptyList()
    @Volatile private var byAlpha: List<String> = emptyList()
    @Volatile private var rank: Map<String, Int> = emptyMap()
    @Volatile private var freq: Map<String, Int> = emptyMap()
    @Volatile private var unaccent: Map<String, String> = emptyMap()
    @Volatile private var personal: Set<String> = emptySet()
    @Volatile private var loading = false

    fun size(): Int = byFreq.size

    fun setPersonal(s: Set<String>) { personal = s }
    fun isPersonal(w: String): Boolean = personal.contains(w)

    fun loadAsync(ctx: Context, code: String, onDone: () -> Unit) {
        if (code == lang && byFreq.isNotEmpty()) { onDone(); return }
        if (loading) return
        loading = true
        val appCtx = ctx.applicationContext
        Thread({
            try { load(appCtx, code) } finally { loading = false }
            Handler(Looper.getMainLooper()).post(onDone)
        }, "SpellEngine-load").start()
    }

    private fun load(ctx: Context, code: String) {
        if (code == lang && byFreq.isNotEmpty()) return
        val bf = ArrayList<String>()
        val fq = HashMap<String, Int>()
        try {
            val reader = try {
                GZIPInputStream(ctx.assets.open("dicts/$code.txt.gz"))
                    .bufferedReader(Charsets.UTF_8)
            } catch (_: Exception) {
                ctx.assets.open("dicts/$code.txt").bufferedReader(Charsets.UTF_8)
            }
            reader.useLines { lines ->
                for (line in lines) {
                    val sp = line.indexOf(' ')
                    if (sp <= 0) continue
                    val w = line.substring(0, sp)
                    if (!w.first().isLetter() || !w.last().isLetter()) continue
                    bf.add(w)
                    fq[w] = line.substring(sp + 1).toIntOrNull() ?: 1
                }
            }
        } catch (e: Exception) {
            Log.e("ThumbGrid", "dict load failed for $code", e)
            return
        }
        val allSet = HashSet(bf)
        val keep = ArrayList<String>(bf.size)
        for (w in bf) {
            if ('-' in w) {
                val noHyphen = w.replace("-", "")
                val parts = w.split("-").filter { it.isNotEmpty() }.toSet()
                val redup = parts.size == 1 && allSet.contains(parts.first())
                if (allSet.contains(noHyphen) || redup) continue
            }
            keep.add(w)
        }
        bf.clear()
        bf.addAll(keep)
        val ua = HashMap<String, String>()
        for (w in bf) {
            val stripped = stripAccents(w)
            if (stripped != w) {
                val cur = ua[stripped]
                if (cur == null || (fq[w] ?: 0) > (fq[cur] ?: 0)) ua[stripped] = w
            }
        }
        val ba = ArrayList(bf)
        ba.sort()
        val rk = HashMap<String, Int>(bf.size * 2)
        for ((i, w) in bf.withIndex()) rk[w] = i
        lang = code
        words = HashSet(bf)
        byFreq = bf
        byAlpha = ba
        rank = rk
        freq = fq
        unaccent = ua
    }

    fun isWord(w: String): Boolean = words.contains(w) || isCompoundForm(w)

    private val CLITICS = listOf("n", "l", "ls", "m", "t", "ns", "s")
    private val PRONS = listOf("me", "te", "se")

    private val PRON_CLITICS = listOf("n", "l", "ls")
    private val ELID_PRON = listOf("hi", "ha")

    private fun cliticForm(x: String): String? {
        for (p in PRONS) {
            if (x.startsWith(p)) {
                val rest = x.substring(p.length)
                if (rest == "n" || rest == "l" || rest == "ls") return "$p'$rest"
            }
        }
        for (c in listOf("m", "t", "n", "s")) {
            if (x.startsWith(c)) {
                val rest = x.substring(1)
                if (rest == "hi" || rest == "ha") {
                    if (words.contains(rest)) return "$c'$rest"
                }
            }
        }
        for (c in listOf("l", "d")) {
            if (x.startsWith(c)) {
                val rest = x.substring(1)
                val ok = rest.length >= 2 && rest != "hi" &&
                        (rest[0] in "aeiouhàèéíòóúü") &&
                        !(c == "d" && rest == "ha")
                if (ok && words.contains(rest)) {
                    val restAcc = accentBetter(rest) ?: rest
                    return "$c'$restAcc"
                }
            }
        }
        return null
    }
    private fun isValidRight(x: String): Boolean =
        words.contains(x) || x in CLITIC_RIGHT || cliticForm(x.replace("'", "")) == x
    fun isCompoundForm(w: String): Boolean {
        if ('-' in w) {
            val parts = w.split("-")
            if (parts.size != 2) return false
            return words.contains(parts[0]) && isValidRight(parts[1])
        }
        if ('\'' in w) return cliticForm(w.replace("'", "")) == w
        return false
    }

    private val CLITIC_RIGHT = listOf("me", "te", "se", "hi", "li", "els", "les", "nos", "vos")

    fun compoundForms(t: String): List<String> {
        val out = ArrayList<String>(3)
        for (i in 1 until t.length - 1) {
            val a = t.substring(0, i)
            val b = t.substring(i)
            if (!words.contains(a)) continue
            val cf = cliticForm(b)
            when {
                cf != null -> out.add("$a-$cf")
                b in CLITIC_RIGHT -> out.add("$a-$b")
            }
        }
        cliticForm(t)?.let { out.add(it) }
        return out
    }
    fun freqOf(w: String): Int = freq[w] ?: 0

    fun confidence(word: String): Float {
        val r = rank[word] ?: return 0f
        return 1f - r.toFloat() / maxOf(1, byFreq.size)
    }

    fun accentBetter(token: String): String? {
        val target = unaccent[token] ?: return null
        return if (freqOf(target) > freqOf(token) * 3) target else null
    }
    fun autocorrectTarget(token: String, confidenceMin: Float): String? {
        val t = token.lowercase()
        if (t.length < 2) return null
        if (isWord(t)) {
            val a = accentBetter(t) ?: return null
            return if (confidence(a) * 100f >= confidenceMin) a else null
        }
        val comps = compoundForms(t)
        if (comps.size == 1) return comps[0]
        val cand = candidates(t, 3).firstOrNull()?.takeIf { it != t } ?: return null
        return if (confidence(cand) * 100f >= confidenceMin) cand else null
    }

    private const val ACCENTED = "àáâäãèéêëìíîïòóôöõøùúûüçñÀÁÂÄÃÈÉÊËÌÍÎÏÒÓÔÖÕØÙÚÛÜÇÑ"
    private const val PLAIN =    "aaaaaeeeeiiiioooooouuuucnaaaaaeeeeiiiioooooouuuucn"

    private fun stripAccents(s: String): String {
        val sb = StringBuilder(s.length)
        for (ch in s) {
            val i = ACCENTED.indexOf(ch)
            sb.append(if (i >= 0) PLAIN[i] else ch)
        }
        return sb.toString()
    }

    fun candidates(token: String, limit: Int): List<String> {
        if (byFreq.isEmpty()) return emptyList()
        val t = token.lowercase()
        if (t.isEmpty()) return emptyList()
        val found = LinkedHashSet<String>()
        for (p in personal) if (p.startsWith(t)) found.add(p)
        addPrefix(t, found, limit * 6)
        found.remove(t)
        if (!isWord(t)) {
            found.addAll(compoundForms(t))
            if (found.size < limit) {
                var seen = 0
                for (n in neighbors(t)) {
                    if (n.isEmpty() || n == t) continue
                    addPrefix(n, found, limit * 2)
                    seen++
                    if (seen >= 400 || found.size >= limit * 6) break
                }
            }
            if (found.size < limit) {
                for (i in t.indices) for (j in i + 1 until t.length) {
                    val p = t.removeRange(i, i + 1).removeRange(j - 1, j)
                    if (p.length >= 2) addPrefix(p, found, limit * 2)
                }
            }
            if (found.size < limit) {
                for (n in neighbors(t)) if (n != t && words.contains(n)) found.add(n)
            }
            found.remove(t)
        }
        val base = byFreq.filter { found.contains(it) }
        val extra = found.filter { !words.contains(it) }
        val pers = personal.filter { it.startsWith(t) && !base.contains(it) && it !in extra }
        return (extra + pers + base).take(limit)
    }

    private fun neighbors(t: String): List<String> {
        val letters = "abcdefghijklmnopqrstuvwxyzàèéíòóúüçñ-'"
        val out = ArrayList<String>()
        for (i in t.indices) out.add(t.removeRange(i, i + 1))
        for (i in 0 until t.length - 1)
            out.add(t.substring(0, i) + t[i + 1] + t[i] + t.substring(i + 2))
        for (i in t.indices) for (c in letters)
            out.add(t.substring(0, i) + c + t.substring(i + 1))
        for (i in 0..t.length) for (c in letters)
            out.add(t.substring(0, i) + c + t.substring(i))
        return out
    }

    private fun lowerBound(prefix: String): Int {
        val ba = byAlpha
        var lo = 0
        var hi = ba.size
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (ba[mid] < prefix) lo = mid + 1 else hi = mid
        }
        return lo
    }

    private fun addPrefix(prefix: String, out: LinkedHashSet<String>, max: Int) {
        val ba = byAlpha
        var i = lowerBound(prefix)
        var added = 0
        while (i < ba.size && ba[i].startsWith(prefix) && added < max) {
            out.add(ba[i]); i++; added++
        }
    }
}