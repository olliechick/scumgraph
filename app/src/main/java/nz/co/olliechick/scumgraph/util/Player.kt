package nz.co.olliechick.scumgraph.util

import androidx.annotation.ColorInt

class Player(
    var name: String,
    @ColorInt var colour: Int
) {
    override fun toString(): String = "$name (colour: $colour)"
}
