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
    private var step = -1
    private var startMs = 0L

    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; alpha = 170 }
    private val trail = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f; color = Color.WHITE; alpha = 90
    }

    private val segMs = 700L    // durada d'un segment de lliscada
    private val tapMs = 450L    // durada d'un tap (dit a sota)
    private val liftMs = 400L   // dit aixecat entre traços: no es dibuixa RES

    fun setStep(s: Int) {
        step = s
        startMs = SystemClock.uptimeMillis()
        invalidate()
    }

    private fun c(side: Int, r: Int, col: Int): RectF? = grid?.demoCell(side, r, col)
    private fun center(r: RectF): FloatArray = floatArrayOf(r.centerX(), r.centerY())

    // Cada pas = llista de traços; cada traç = llista de punts recorreguts amb el dit A SOTA.
    private fun strokesFor(step: Int): List<List<FloatArray>> {
        val g = grid ?: return emptyList()
        return when (step) {
            // "casa": quatre taps separats
            1 -> listOfNotNull(
                c(0, 1, 2)?.let { listOf(center(it)) },
                c(1, 1, 0)?.let { listOf(center(it)) },
                c(0, 1, 1)?.let { listOf(center(it)) },
                c(1, 1, 0)?.let { listOf(center(it)) }
            )
            // lliscar sobre la S cap a la Q
            2 -> {
                val s = c(0, 1, 1) ?: return emptyList()
                val f = center(s)
                listOf(listOf(f, floatArrayOf(f[0] + s.width() * 0.45f, f[1])))
            }
            // Esborrar: COMENÇA SOBRE l'espai i llisca esquerra
            4 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX() - 0.25f, p.centerY())
                ))
            }
            // Refer: COMENÇA SOBRE l'espai i llisca dreta
            5 -> {
                val p = g.demoPill() ?: return emptyList()
                listOf(listOf(
                    floatArrayOf(p.centerX(), p.centerY()),
                    floatArrayOf(p.centerX() + 0.25f, p.centerY())
                ))
            }
            // Mantenir a la vora: viatge des de l'espai fins a la vora + manteniment
            6 -> {
                val p = g.demoPill() ?: return emptyList()
                val ex = 0.015f
                val ey = p.centerY()
                listOf(listOf(
                    floatArrayOf(p.centerX(), ey),
                    floatArrayOf(ex, ey),
                    floatArrayOf(ex, ey),
                    floatArrayOf(ex, ey)
                ))
            }
            // doble llisc amunt
            7 -> {
                val p = g.demoPill() ?: return emptyList()
                val cx = p.centerX(); val cy = p.centerY()
                listOf(
                    listOf(floatArrayOf(cx, cy), floatArrayOf(cx, cy - 0.12f)),
                    listOf(floatArrayOf(cx, cy), floatArrayOf(cx, cy - 0.12f))
                )
            }
            // arrossegar knob + llisc avall
            8 -> {
                val k = g.demoKnob() ?: return emptyList()
                val p = g.demoPill() ?: return emptyList()
                listOf(
                    listOf(floatArrayOf(k[0], k[1]), floatArrayOf(k[0] - 0.18f, k[1])),
                    listOf(floatArrayOf(p.centerX(), p.centerY()),
                        floatArrayOf(p.centerX(), p.centerY() + 0.06f))
                )
            }
            // 1 clic al knob + lliscar la franja CAP A L'ESQUERRA (pàgina següent)
            9 -> {
                val k = g.demoKnob() ?: return emptyList()
                val sy = g.demoStripY()
                listOf(
                    listOf(floatArrayOf(k[0], k[1])),
                    listOf(floatArrayOf(0.62f, sy), floatArrayOf(0.30f, sy))
                )
            }
            // doble clic al knob + tap a un símbol
            10 -> {
                val k = g.demoKnob() ?: return emptyList()
                listOf(
                    listOf(floatArrayOf(k[0], k[1])),
                    listOf(floatArrayOf(k[0], k[1])),
                    listOf(floatArrayOf(0.5f, 0.40f))
                )
            }
            // càpsula d'idioma
            11 -> g.demoCapsule()?.let { listOf(listOf(it)) } ?: emptyList()
            else -> emptyList()
        }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val strokes = strokesFor(step)
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
            acc += d + liftMs   // finestra de "dit aixecat": no dibuixem res
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