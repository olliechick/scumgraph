package nz.co.olliechick.scumgraph.util

class Chart(players: ArrayList<Player>) {
    val playerHistories = generateEmptyHistory(players)
    val colours = generateColours(players)

    private fun generateEmptyHistory(players: ArrayList<Player>): ArrayList<ChartPlayerHistory> {
        val playerHistories = arrayListOf<ChartPlayerHistory>()
        players.forEach { player ->
            playerHistories.add(
                ChartPlayerHistory(player.name, arrayListOf(ChartScoreForRound(0, 0)))
            )
        }
        return playerHistories
    }

    private fun generateColours(players: ArrayList<Player>): ArrayList<Int> {
        return players.map(Player::colour) as ArrayList<Int>
    }

    override fun toString(): String {
        return "[[${colours.joinToString(",")}],${playerHistories.joinToString(",")}]"
    }
}