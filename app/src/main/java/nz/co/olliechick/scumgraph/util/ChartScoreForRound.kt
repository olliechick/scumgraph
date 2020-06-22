package nz.co.olliechick.scumgraph.util

class ChartScoreForRound(
    val name: Int, /* Round number */
    val value: Int /* Score */
) {
    override fun toString(): String {
        return value.toString()
    }
}