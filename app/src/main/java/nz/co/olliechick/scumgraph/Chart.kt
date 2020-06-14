package nz.co.olliechick.scumgraph

import nz.co.olliechick.scumgraph.util.Player

class Chart(players: ArrayList<Player>) {
    val playerHistories = generateEmptyHistory(players)

    private fun generateEmptyHistory(players: ArrayList<Player>): ArrayList<PlayerHistory> {
        val playerHistories = arrayListOf<PlayerHistory>()
        players.forEach { player ->
            playerHistories.add(PlayerHistory(player.name, arrayListOf(ScoreForRound(0, 0))))
        }
        return playerHistories
    }
}

class PlayerHistory(
    val name: String, /* Name of player */
    val series: ArrayList<ScoreForRound>
)

class ScoreForRound(
    val name: Int, /* Round number */
    val value: Int /* Score */
)