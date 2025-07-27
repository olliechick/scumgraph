package nz.co.olliechick.scumgraph.util

class Player(
    var name: String,
    var colour: Int
) {
    override fun toString(): String = "$name (colour: $colour)"
}
