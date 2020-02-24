package nz.co.olliechick.scumgraph.util

import android.content.Context
import nz.co.olliechick.scumgraph.R


class ScumHelpers {
    companion object {
        fun generateMiddlemenOptions(numberOfPlayers: Int): ArrayList<MiddlemanOption> {
            val options = arrayListOf<MiddlemanOption>()
            return if (numberOfPlayers % 2 == 0) {
                // Even number
                var numberOfPresidents = numberOfPlayers / 2
                options.add(
                    MiddlemanOption(
                        "$numberOfPresidents president${if (numberOfPresidents != 1) "s" else ""}, No middlemen, $numberOfPresidents scum",
                        0
                    )
                )
                var numberOfNonMiddlemen = numberOfPlayers
                var numberOfMiddlemen = 0
                while (numberOfNonMiddlemen > 2) {
                    numberOfNonMiddlemen -= 2
                    numberOfMiddlemen += 2
                    numberOfPresidents = numberOfNonMiddlemen / 2
                    options.add(
                        MiddlemanOption(
                            "$numberOfPresidents president${if (numberOfPresidents != 1) "s" else ""}, $numberOfMiddlemen middlemen, $numberOfPresidents scum",
                            numberOfMiddlemen
                        )
                    )
                }
                options
            } else {
                // Odd number
                var numberOfPresidents = (numberOfPlayers - 1) / 2
                options.add(
                    MiddlemanOption(
                        "$numberOfPresidents president${if (numberOfPresidents != 1) "s" else ""}, 1 middleman, $numberOfPresidents scum",
                        1
                    )
                )
                var numberOfNonMiddlemen = numberOfPlayers - 1
                var numberOfMiddlemen = 1
                while (numberOfNonMiddlemen > 2) {
                    numberOfNonMiddlemen -= 2
                    numberOfMiddlemen += 2
                    numberOfPresidents = numberOfNonMiddlemen / 2
                    options.add(
                        MiddlemanOption(
                            "$numberOfPresidents president${if (numberOfPresidents != 1) "s" else ""}, $numberOfMiddlemen middlemen, $numberOfPresidents scum",
                            numberOfMiddlemen
                        )
                    )
                }
                options
            }
        }

        fun generateTitle(position: Int, numberOfMiddlemen: Int, numberOfPlayers: Int, context: Context): String {
            val numberOfScum = (numberOfPlayers - numberOfMiddlemen) / 2
            if (position < numberOfScum) return context.getString(R.string.president)
            else if (position < (numberOfPlayers - numberOfScum)) return context.getString(R.string.middleman)
            else return context.getString(R.string.scum)
        }
    }
}