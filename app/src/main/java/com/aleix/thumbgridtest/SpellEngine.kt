package com.aleix.thumbgridtest

import android.content.Context
import java.util.zip.GZIPInputStream
import android.util.Log

object SpellEngine {

    private var lang = ""
    private val words = HashSet<String>()
    private val byFreq = ArrayList<String>()
    private val rank = HashMap<String, Int>()
    private val freq = HashMap<String, Int>()
    private val unaccent = HashMap<String, String>()
    fun load(ctx: Context, code: String) {
        if (code == lang && byFreq.isNotEmpty()) return
        lang = code
        words.clear(); byFreq.clear(); rank.clear(); freq.clear(); unaccent.clear()
        try {
            val reader = try {
                GZIPInputStream(ctx.assets.open("dicts/$code.txt.gz"))
                    .bufferedReader(Charsets.UTF_8)
            } catch (_: Exception) {
                ctx.assets.open("dicts/$code.txt")
                    .bufferedReader(Charsets.UTF_8)
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
        } catch (e: Exception) {
            Log.e("ThumbGrid", "dict load failed for $code", e)
        }
        for (w in byFreq) {
            val stripped = stripAccents(w)
            if (stripped != w) {
                val cur = unaccent[stripped]
                if (cur == null || (freq[w] ?: 0) > (freq[cur] ?: 0)) unaccent[stripped] = w
            }
        }
        words.addAll(byFreq)
        for ((i, w) in byFreq.withIndex()) rank[w] = i
    }

    fun freqOf(w: String): Int = freq[w] ?: 0

    fun accentBetter(token: String): String? {
        val target = unaccent[token] ?: return null
        return if (freqOf(target) > freqOf(token) * 3) target else null
    }

    private fun stripAccents(s: String): String =
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}"), "")

    fun isWord(w: String): Boolean = words.contains(w)

    fun confidence(word: String): Float {
        val r = rank[word] ?: return 0f
        return 1f - r.toFloat() / maxOf(1, byFreq.size)
    }

    fun candidates(token: String, limit: Int): List<String> {
        if (byFreq.isEmpty()) return emptyList()
        val t = token.lowercase()
        if (t.isEmpty()) return emptyList()
        val found = LinkedHashSet<String>()
        for (w in byFreq) {
            if (w != t && w.startsWith(t)) {
                found.add(w)
                if (found.size >= limit * 4) break
            }
        }
        if (found.size < limit) {
            for (n in neighbors(t)) if (n != t && words.contains(n)) found.add(n)
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
    fun size(): Int = byFreq.size
}