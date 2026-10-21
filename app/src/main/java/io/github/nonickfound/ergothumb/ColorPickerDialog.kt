package io.github.nonickfound.ergothumb

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.slider.Slider

class ColorPickerDialog(
    private val context: Context,
    private val isDark: Boolean,
    initial: Int,
    private val onSaved: (Int) -> Unit
) {
    private var current: Int = (initial and 0x00FFFFFF)

    private fun channel(c: Int, idx: Int) = (c shr (16 - 8 * idx)) and 0xFF

    private fun setChannel(c: Int, idx: Int, v: Int): Int {
        val shift = 16 - 8 * idx
        return (c and (0xFF shl shift).inv()) or ((v and 0xFF) shl shift)
    }

    fun show() {
        val pole = if (isDark) Color.BLACK else Color.WHITE
        val full = { c: Int -> c or (0xFF shl 24).toInt() }

        val preview = View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 260
            )
        }
        val sampleViews = listOf(0, 1, 2).map {
            View(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, 110, 1f).apply { marginEnd = 12 }
            }
        }
        val samplesRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 20, 0, 0)
            sampleViews.forEach { addView(it) }
        }
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 36, 56, 12)
            addView(preview)
            addView(samplesRow)
        }

        val refresh = {
            preview.setBackgroundColor(full(current))
            sampleViews[0].setBackgroundColor(full(current))
            sampleViews[1].setBackgroundColor(blendColor(current, pole, 0.35f))
            sampleViews[2].setBackgroundColor(blendColor(current, pole, 0.70f))
            Unit
        }

        val names = listOf("R", "G", "B")
        for (i in 0..2) {
            val tv = TextView(context).apply {
                text = "${names[i]}: ${channel(current, i)}"
                textSize = 14f
                setTextColor(Color.WHITE)
                setPadding(0, 20, 0, 0)
            }
            val sl = Slider(context).apply {
                valueFrom = 0f
                valueTo = 255f
                stepSize = 1f
                value = channel(current, i).toFloat()
                addOnChangeListener { _, value, _ ->
                    tv.text = "${names[i]}: ${value.toInt()}"
                    current = setChannel(current, i, value.toInt())
                    refresh()
                }
            }
            container.addView(tv)
            container.addView(sl)
        }

        refresh()

        AlertDialog.Builder(context)
            .setTitle(if (isDark) R.string.color_dark_row else R.string.color_light_row)
            .setView(container)
            .setPositiveButton(R.string.color_save) { _, _ -> onSaved(full(current)) }
            .setNeutralButton(R.string.color_default) { _, _ -> onSaved(0) }
            .setNegativeButton(R.string.bigrams_warning_cancel, null)
            .show()
    }
}