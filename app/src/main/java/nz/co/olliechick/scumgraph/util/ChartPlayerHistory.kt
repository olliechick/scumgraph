package nz.co.olliechick.scumgraph.util

class ChartPlayerHistory(
    val name: String, /* Name of player */
    val series: ArrayList<ChartScoreForRound>
) {
    override fun toString(): String {
        return "[\"$name\",${series.joinToString(",")}]"
    }
}
