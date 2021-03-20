package nz.co.olliechick.scumgraph.util

class ChartPlayerHistory(
    val name: String, /* Name of player */
    val series: ArrayList<ChartScoreForRound>
) {
    override fun toString(): String {
        val firstRound = series[0].name
        val seriesRep = when {
            series.size == 0 -> "0"
            firstRound > 0 -> arrayOfNulls<Int>(firstRound).joinToString(",") + "," + series.joinToString(",")
            else -> series.joinToString(",")
        }
        return "[\"$name\",$seriesRep]"
    }
}
