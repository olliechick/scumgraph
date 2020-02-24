package nz.co.olliechick.scumgraph.util

import android.graphics.Color
import androidx.annotation.ColorInt
import java.lang.Long.parseLong
import kotlin.math.roundToInt

class Colours {
    companion object {
        /**
         * Returns true if the text color should be white, given a background color
         *
         * @param color background color
         * @return true if the text should be white, false if the text should be black
         */
        fun isWhiteText(@ColorInt color: Int): Boolean {
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)

            // https://en.wikipedia.org/wiki/YIQ
            // https://24ways.org/2010/calculating-color-contrast/
            val yiq = (red * 299 + green * 587 + blue * 114) / 1000
            return yiq < 192
        }

        /**
         * Returns the colour the text should be (black or white), based on the background colour.
         */
        fun getTextColour(@ColorInt backgroundColour: Int): Int {
            return if (isWhiteText(backgroundColour)) Color.WHITE else Color.BLACK
        }

        fun colourOptionToInt(colourOption: ColourOption): Int {
            return colourOptionToInt(colourOption.colour)
        }

        fun colourOptionToInt(rgb: String): Int {
            var red = parseLong(rgb.substring(1, 3), 16).toInt()
            var green = parseLong(rgb.substring(3, 5), 16).toInt()
            var blue = parseLong(rgb.substring(5, 7), 16).toInt()

            red = red shl 16 and 0x00FF0000 //Shift red 16-bits and mask out other stuff
            green = green shl 8 and 0x0000FF00 //Shift Green 8-bits and mask out other stuff
            blue = blue and 0x000000FF //Mask out anything not blue.

            return -0x1000000 or red or green or blue //0xFF000000 for 100% Alpha. Bitwise OR everything together.
        }

        fun getAllColourOptions(): ArrayList<String> {
            val colourOptions = arrayListOf<String>()
            ColourOption.values().forEach { colourOption ->
                colourOptions.add(ColourOption.valueOf(colourOption.toString()).colour)
            }
            return colourOptions
        }

        fun getNextFreeColour(players: ArrayList<Player>): Int {
            val colourUsed = mutableMapOf<Int, Boolean>()

            val colourOptions = getAllColourOptions()
            colourOptions.forEach { colourOption ->
                colourUsed[colourOptionToInt(colourOption)] = false
            }

            players.forEach { player ->
                colourUsed[player.colour] = true
            }

            for ((colour, isUsed) in colourUsed) {
                if (!isUsed) return colour
            }

            return 0
        }

        @ColorInt
        fun adjustAlpha(@ColorInt color: Int, factor: Double): Int {
            val alpha = (Color.alpha(color) * factor).roundToInt()
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }
}