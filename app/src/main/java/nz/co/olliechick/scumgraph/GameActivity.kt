package nz.co.olliechick.scumgraph

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_game.*
import nz.co.olliechick.scumgraph.draggablelist.OnStartDragListener
import nz.co.olliechick.scumgraph.draggablelist.SimpleItemTouchHelperCallback
import nz.co.olliechick.scumgraph.util.Player
import java.util.*


class GameActivity : AppCompatActivity(),
    OnStartDragListener {

    var roundNumber = 1
    var numberOfMiddlemen = 0
    var players = arrayListOf<Player>()
    private var touchHelper: ItemTouchHelper? = null

    private inline fun <reified T> Gson.fromJson(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        numberOfMiddlemen = intent.getIntExtra("Number of middlemen", 0)

        val playersString = intent.getStringExtra("Players")
        players = if (playersString != null) Gson().fromJson<ArrayList<Player>>(playersString)
        else arrayListOf()

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GamePlayerListAdapter(players, this, this)
        recyclerView.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(recyclerView)
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) touchHelper?.startDrag(viewHolder)
    }

    fun nextRound(view: View) {
        roundNumber++
        findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)
    }
}