package nz.co.olliechick.scumgraph.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ColorPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private var selectedColor: Int = Color.BLACK

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = selectedColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    fun setSelectedColor(color: Int) {
        selectedColor = color
        invalidate() // Redraw the view
    }

    fun getSelectedColor(): Int {
        return selectedColor
    }
}
