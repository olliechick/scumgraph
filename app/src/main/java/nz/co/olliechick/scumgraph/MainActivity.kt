package nz.co.olliechick.scumgraph

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.start_game_dialog.view.*
import nz.co.olliechick.scumgraph.util.ColourOption
import nz.co.olliechick.scumgraph.util.Colours.Companion.colourOptionToInt
import nz.co.olliechick.scumgraph.util.MiddlemanOption
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.generateMiddlemenOptions


class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val players = arrayListOf(
        Player("", colourOptionToInt(ColourOption.BLUE)),
        Player("", colourOptionToInt(ColourOption.RED))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CreatePlayerListAdapter(players, viewManager as LinearLayoutManager, this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter

        }
    }

    @SuppressLint("InflateParams")
    fun startGame(view1: View) {
        if (players.size > 3) {
            val view = layoutInflater.inflate(R.layout.start_game_dialog, null)

            AlertDialog.Builder(this).run {
                setView(view)
                setTitle(getString(R.string.game_setup))
                setPositiveButton("Start game") { _, _ -> openGameScreen((view.spinner.selectedItem as MiddlemanOption).number) }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

                ArrayAdapter<MiddlemanOption>(
                    applicationContext,
                    android.R.layout.simple_spinner_item,
                    generateMiddlemenOptions(players.size)
                ).also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    view.spinner.adapter = adapter
                }

                create()
                show()
            }
        } else {
            var numberOfMiddlemen = 0
            if (players.size == 3) numberOfMiddlemen = 1
            openGameScreen(numberOfMiddlemen)
        }
    }

    private fun openGameScreen(numberOfMiddlemen: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("Number of middlemen", numberOfMiddlemen)
        intent.putExtra("Players", Gson().toJson(players))
        startActivity(intent)
    }
}