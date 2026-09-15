package com.aleix.thumbgridtest

import android.content.Context
import android.util.Log
import java.util.zip.GZIPInputStream

object SpellEngine {

    private var lang = ""
    private val words = HashSet<String>()
    private val byFreq = ArrayList<String>()
    private val byAlpha = ArrayList<String>()
    private val rank = HashMap<String, Int>()
    private val freq = HashMap<String, Int>()
    private val unaccent = HashMap<String, String>()

    fun size(): Int = byFreq.size

    fun load(ctx: Context, code: String) {
        if (code == lang && byFreq.isNotEmpty()) return
        lang = code
        words.clear(); byFreq.clear(); byAlpha.clear(); rank.clear(); freq.clear(); unaccent.clear()
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
                    val n = line.substring(sp + 1).toIntOrNull() ?: 1
                    byFreq.add(w)
                    freq[w] = n
                }
            }
            for (w in byFreq) {
                val stripped = stripAccents(w)
                if (stripped != w) {
                    val cur = unaccent[stripped]
                    if (cur == null || (freq[w] ?: 0) > (freq[cur] ?: 0)) unaccent[stripped] = w
                }
            }
            byAlpha.addAll(byFreq)
            byAlpha.sort()
        } catch (e: Exception) {
            Log.e("ThumbGrid", "dict load failed for $code", e)
        }
        words.addAll(byFreq)
        for ((i, w) in byFreq.withIndex()) rank[w] = i
    }

    fun isWord(w: String): Boolean = words.contains(w)

    fun freqOf(w: String): Int = freq[w] ?: 0

    fun confidence(word: String): Float {
        val r = rank[word] ?: return 0f
        return 1f - r.toFloat() / maxOf(1, byFreq.size)
    }

    fun accentBetter(token: String): String? {
        val target = unaccent[token] ?: return null
        return if (freqOf(target) > freqOf(token) * 3) target else null
    }

    private fun stripAccents(s: String): String =
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")

    fun candidates(token: String, limit: Int): List<String> {
        if (byFreq.isEmpty()) return emptyList()
        val t = token.lowercase()
        if (t.isEmpty()) return emptyList()
        val found = LinkedHashSet<String>()
        // 1) completions de prefix (encara que el token sigui paraula vàlida)
        addPrefix(t, found, limit * 6)
        found.remove(t)
        // 2-4) només si el token NO és una paraula (correcció d'errors)
        if (!isWord(t)) {
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
        return byFreq.filter { found.contains(it) }.take(limit)
    }

    private fun neighbors(t: String): List<String> {
        val letters = "abcdefghijklmnopqrstuvwxyzàèéíòóúüçñ"
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
        var lo = 0
        var hi = byAlpha.size
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (byAlpha[mid] < prefix) lo = mid + 1 else hi = mid
        }
        return lo
    }

    private fun addPrefix(prefix: String, out: LinkedHashSet<String>, max: Int) {
        var i = lowerBound(prefix)
        var added = 0
        while (i < byAlpha.size && byAlpha[i].startsWith(prefix) && added < max) {
            out.add(byAlpha[i])
            i++
            added++
        }
    }
}