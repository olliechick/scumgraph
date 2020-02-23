package nz.co.olliechick.scumgraph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class GameActivity : AppCompatActivity() {

    var roundNumber = 1
    var numberOfMiddlemen = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        numberOfMiddlemen = getIntent().getIntExtra("Number of middlemen", 0)
        findViewById<TextView>(R.id.textView3).text = "$numberOfMiddlemen middlemen"
    }

    fun nextRound(view: View) {
        roundNumber++
        findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)
    }
}
