package app.clauncher.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import app.clauncher.R
import app.clauncher.helper.ColorPalette
import app.clauncher.helper.dpToPx
import app.clauncher.helper.getColorFromAttr

class ColorPickerDialog(
    context: Context,
    private val currentColor: Int,
    private val onColorSelected: (Int) -> Unit
) : Dialog(context) {

    private var selectedColor: Int =
        if (currentColor == 0) android.graphics.Color.WHITE else currentColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val padding = 20.dpToPx()
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(padding, padding, padding, padding)
        }

        val title = TextView(context).apply {
            text = context.getString(R.string.select_color)
            setTextAppearance(android.R.style.TextAppearance_Material_Title)
            try {
                setTextColor(context.getColorFromAttr(R.attr.primaryColor))
            } catch (_: Exception) {
            }
        }
        root.addView(title)

        val preview = FrameLayout(context).apply {
            val p = 48.dpToPx()
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, p
            ).apply { topMargin = 12.dpToPx(); bottomMargin = 12.dpToPx() }
            background = GradientDrawable().apply {
                setColor(selectedColor)
                cornerRadius = 12.dpToPx().toFloat()
            }
        }
        val previewLabel = TextView(context).apply {
            text = "Sample Text"
            gravity = Gravity.CENTER
            setTextColor(selectedColor)
        }
        preview.addView(
            previewLabel,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        root.addView(preview)

        val grid = GridLayout(context).apply {
            columnCount = 4
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        ColorPalette.options.forEach { (color, _) ->
            val cell = FrameLayout(context).apply {
                val size = 56.dpToPx()
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    setMargins(6.dpToPx(), 6.dpToPx(), 6.dpToPx(), 6.dpToPx())
                }
                isClickable = true
                isFocusable = true
            }
            val dot = View(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                    setStroke(
                        if (selectedColor == color) 6 else 2,
                        if (selectedColor == color) context.getColorFromAttr(R.attr.primaryColor)
                        else android.graphics.Color.GRAY
                    )
                }
            }
            cell.addView(dot)
            cell.setOnClickListener {
                selectedColor = color
                preview.background = GradientDrawable().apply {
                    setColor(selectedColor)
                    cornerRadius = 12.dpToPx().toFloat()
                }
                previewLabel.setTextColor(selectedColor)
                for (i in 0 until grid.childCount) {
                    val c = grid.getChildAt(i) as FrameLayout
                    val d = c.getChildAt(0)
                    val idxColor = ColorPalette.options[i].first
                    d.background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(idxColor)
                        setStroke(
                            if (selectedColor == idxColor) 6 else 2,
                            if (selectedColor == idxColor) context.getColorFromAttr(R.attr.primaryColor)
                            else android.graphics.Color.GRAY
                        )
                    }
                }
            }
            grid.addView(cell)
        }
        root.addView(grid)

        val buttons = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, 12.dpToPx(), 0, 0)
        }
        val cancel = TextView(context).apply {
            text = context.getString(R.string.cancel)
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
            setOnClickListener { dismiss() }
        }
        val apply = TextView(context).apply {
            text = context.getString(R.string.apply)
            setPadding(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 12.dpToPx())
            try {
                setTextColor(context.getColorFromAttr(R.attr.primaryColor))
            } catch (_: Exception) {
            }
            setOnClickListener {
                onColorSelected(selectedColor)
                dismiss()
            }
        }
        buttons.addView(cancel)
        buttons.addView(apply)
        root.addView(buttons)

        setContentView(root)
        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}
