package nz.co.olliechick.scumgraph.colorpicker

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.GridView
import android.widget.TextView
import nz.co.olliechick.scumgraph.R
import nz.co.olliechick.scumgraph.util.Colours

class ColorPickerDialog(context: Context, private var initialColor: Int = Color.BLACK) : Dialog(context) {

    private var onColorPickedListener: ((Int) -> Unit)? = null
    private lateinit var colorGridView: GridView
    private var selectedColor: Int = initialColor
    private lateinit var adapter: ColorGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.dialog_color_picker, null)
        setContentView(view)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams

        colorGridView = view.findViewById(R.id.color_grid)
        val selectButton: Button = view.findViewById(R.id.select_button)
        val cancelButton: Button = view.findViewById(R.id.cancel_button)

        val colorOptions = ColourOption.entries.map { Colours.colourOptionToInt(it) }
        adapter = ColorGridAdapter(context, colorOptions.toTypedArray(), selectedColor) { color ->
            selectedColor = color
            adapter.setSelectedColor(selectedColor)
        }
        colorGridView.adapter = adapter

        selectButton.setOnClickListener {
            onColorPickedListener?.invoke(selectedColor)
            dismiss()
        }

        cancelButton.setOnClickListener { dismiss() }
    }

    fun setOnColorPickedListener(listener: (Int) -> Unit) {
        onColorPickedListener = listener
    }

    private class ColorGridAdapter(private val context: Context, private val colors: Array<Int>, private var selectedColor: Int, private val onColorSelected: (Int) -> Unit) : BaseAdapter() {

        override fun getCount(): Int = colors.size

        override fun getItem(position: Int): Any = colors[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val colorView: TextView
            if (convertView == null) {
                colorView = LayoutInflater.from(context).inflate(R.layout.color_circle_item, parent, false) as TextView
                colorView.layoutParams = ViewGroup.LayoutParams(120, 120) // Size of the color circle
            } else {
                colorView = convertView as TextView
            }

            val color = colors[position]
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(color)
            colorView.background = drawable

            if (color == selectedColor) {
                colorView.text = "✓"
                colorView.setTextColor(Colours.getTextColour(color))
            } else {
                colorView.text = ""
            }

            colorView.setOnClickListener { onColorSelected(color) }

            return colorView
        }

        fun setSelectedColor(color: Int) {
            selectedColor = color
            notifyDataSetChanged()
        }
    }
}
