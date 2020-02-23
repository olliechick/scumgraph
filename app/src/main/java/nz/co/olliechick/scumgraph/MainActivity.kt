package nz.co.olliechick.scumgraph

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import nz.co.olliechick.scumgraph.util.ColourOption
import nz.co.olliechick.scumgraph.util.Colours.Companion.colourOptionToInt
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val players = arrayListOf(Player("", colourOptionToInt(ColourOption.BLUE)), Player("", colourOptionToInt(ColourOption.RED)))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = PlayerListAdapter(players, viewManager as LinearLayoutManager, this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }

    fun startGame(view: View) {
        toast("Not yet supported.")
    }
}