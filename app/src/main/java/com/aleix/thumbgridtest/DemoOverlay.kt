package com.aleix.thumbgridtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.view.View
import kotlin.math.sin

class DemoOverlay(context: Context) : View(context) {

    var grid: DualGridView? = null
    var targetWord: String = ""
    private var demo = -1
    private var startMs = 0L

    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 170 }
    private val trail = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f; color = Color.WHITE; alpha = 90
    }

    private val segMs = 700L
    private val tapMs = 450L
    private val liftMs = 400L

    fun setDemo(d: Int) {
        demo = d
        startMs = SystemClock.uptimeMillis()
        invalidate()
    }

    private fun c(side: Int, r: Int, col: Int): RectF? = grid?.demoCell(side, r, col)
    private fun center(r: RectF): FloatArray = floatArrayOf(r.centerX(), r.centerY())

    private fun strokesFor(d: Int): List<List<FloatArray>> {
        val g = grid ?: return emptyList()
        return when (d) {
            1 -> targetWord.uppercase().mapNotNull { ch ->
                grid?.demoCellForChar(ch)?.let { listOf(center(it)) }
            }
            2 -> {
                val s = c(0, 1, 1) ?: return emptyList()
                val f = center(s)
                listOf(listOf(f, floatArrayOf(f[0] + s.width() * 0.45f, f[1])))
            }
            4 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX() - 0.25f, p.centerY())
                ))
            }
            5 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX() + 0.25f, p.centerY())
                ))
            }
            6 -> {
                val p = g.demoPill() ?: return emptyList()
                val ex = 0.015f; val ey = p.centerY()
                listOf(listOf(
                    floatArrayOf(p.centerX(), ey),
                    floatArrayOf(ex, ey), floatArrayOf(ex, ey), floatArrayOf(ex, ey)
                ))
            }
            7 -> {
                val p = g.demoPill() ?: return emptyList()
                val cx = p.centerX(); val cy = p.centerY()
                listOf(
                    listOf(floatArrayOf(cx, cy), floatArrayOf(cx, cy - 0.12f)),
                    listOf(floatArrayOf(cx, cy), floatArrayOf(cx, cy - 0.12f))
                )
            }
            73 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX(), p.centerY() - 0.12f)
                ))
            }
            81 -> {
                val k = g.demoKnob() ?: return emptyList()
                listOf(listOf(floatArrayOf(k[0], k[1]), floatArrayOf(k[0] - 0.18f, k[1])))
            }
            82 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX(), p.centerY() + 0.06f)
                ))
            }
            91 -> { val k = g.demoKnob() ?: return emptyList(); listOf(listOf(k)) }
            92 -> {
                val sy = g.demoStripY()
                listOf(listOf(floatArrayOf(0.62f, sy), floatArrayOf(0.30f, sy)))
            }
            101 -> {
                val k = g.demoKnob() ?: return emptyList()
                listOf(listOf(k), listOf(k))
            }
            102 -> listOf(listOf(floatArrayOf(0.5f, 0.40f)))
            11 -> g.demoCapsule()?.let { listOf(listOf(it)) } ?: emptyList()
            else -> emptyList()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val strokes = strokesFor(demo)
        if (strokes.isEmpty() || width == 0 || height == 0) return
        val r = width * 0.035f
        val durs = strokes.map { if (it.size == 1) tapMs else segMs * (it.size - 1) }
        val cycle = durs.sum() + liftMs * strokes.size
        val t = (SystemClock.uptimeMillis() - startMs) % cycle
        var acc = 0L
        for (i in strokes.indices) {
            val d = durs[i]
            if (t < acc + d) {
                drawStroke(canvas, strokes[i], t - acc, r)
                invalidate()
                return
            }
            acc += d + liftMs
        }
        invalidate()
    }

    private fun drawStroke(canvas: Canvas, pts: List<FloatArray>, local: Long, r: Float) {
        if (pts.size == 1) {
            val pulse = 1f + 0.25f * sin(local / 120f)
            canvas.drawCircle(pts[0][0] * width, pts[0][1] * height, r * pulse, dot)
            return
        }
        val seg = (local / segMs).toInt().coerceIn(0, pts.size - 2)
        val f = ((local - seg * segMs).toFloat() / segMs).coerceIn(0f, 1f)
        val a = pts[seg]; val b = pts[seg + 1]
        val x = (a[0] + (b[0] - a[0]) * f) * width
        val y = (a[1] + (b[1] - a[1]) * f) * height
        canvas.drawLine(pts[0][0] * width, pts[0][1] * height, x, y, trail)
        val stationary = a[0] == b[0] && a[1] == b[1]
        val rr = if (stationary) r * (1f + 0.2f * sin(local / 120f)) else r
        canvas.drawCircle(x, y, rr, dot)
    }
}