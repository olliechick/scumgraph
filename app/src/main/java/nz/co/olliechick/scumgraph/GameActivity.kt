package nz.co.olliechick.scumgraph

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.api.Status
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nz.co.olliechick.scumgraph.databinding.ActivityGameBinding
import nz.co.olliechick.scumgraph.databinding.CreatePlayerItemBinding
import nz.co.olliechick.scumgraph.draggablelist.OnStartDragListener
import nz.co.olliechick.scumgraph.draggablelist.SimpleItemTouchHelperCallback
import nz.co.olliechick.scumgraph.util.*
import nz.co.olliechick.scumgraph.util.Colours.Companion.getNextFreeColour
import nz.co.olliechick.scumgraph.util.Colours.Companion.getTextColour
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.calculateScore
import org.json.JSONObject
//import petrov.kristiyan.colorpicker.ColorPicker
import java.io.IOException
import java.net.URLEncoder
import java.util.*


class GameActivity : AppCompatActivity(), OnStartDragListener {
    private val logTag = "scumgraphlog"
    var roundNumber = 1
    var numberOfMiddlemen = 0
    var players = arrayListOf<Player>()
    var chartData = Chart(players)

    private var touchHelper: ItemTouchHelper? = null

    private var castContext: CastContext? = null
    private var sessionManagerListener: SessionManagerListener<CastSession>? = null
    private var castSession: CastSession? = null
    private var chartChannel: ChartChannel? = null

    private lateinit var binding: ActivityGameBinding // Binding for activity_game.xml

    private inline fun <reified T> Gson.fromJson(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        numberOfMiddlemen = intent.getIntExtra("Number of middlemen", 0)

        val playersString = intent.getStringExtra("Players")
        players = if (playersString != null) Gson().fromJson<ArrayList<Player>>(playersString)
        else arrayListOf()
        chartData = Chart(players)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GamePlayerListAdapter(numberOfMiddlemen, players, this, this, ::updateChart)
        binding.recyclerView.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(binding.recyclerView)
    }

    override fun onResume() {
        super.onResume()

        castContext = CastContext.getSharedInstance(this)
        sessionManagerListener = getSessionManagerListener(this)
        castContext?.sessionManager?.addSessionManagerListener(
            sessionManagerListener as SessionManagerListener<CastSession>,
            CastSession::class.java
        )
        updateChart()
    }

    override fun onPause() {
        super.onPause()
        sessionManagerListener?.let {
            castContext?.sessionManager?.removeSessionManagerListener(
                it,
                CastSession::class.java
            )
        }
    }

    override fun onBackPressed() {
        if (roundNumber > 1) {
            AlertDialog.Builder(this).run {
                setTitle(getString(R.string.are_you_sure_quit_game))
                setMessage(getString(R.string.you_will_lose_progress))
                setPositiveButton(getString(R.string.quit)) { _, _ -> super.onBackPressed() }
                setNegativeButton(getString(R.string.stay)) { _, _ -> }
                show()
            }
        } else super.onBackPressed()
    }

    private fun createChannel() {
        if (castSession == null) castSession = castContext?.sessionManager?.currentCastSession

        if (castSession?.isConnected == true && chartChannel == null) {
            castSession?.let {
                chartChannel = ChartChannel()
                it.setMessageReceivedCallbacks(
                    chartChannel?.namespace,
                    chartChannel
                )
            }
        }
    }

    private fun updateChart() {
        createChannel()
        chartChannel?.let {
            castContext?.sessionManager?.currentCastSession
                ?.sendMessage(it.namespace, JSONObject(Gson().toJson(chartData)).toString())
                ?.setResultCallback(fun(result: Status) {
                    if (!result.isSuccess) Log.e(logTag, "Sending message failed")
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.game_menu, menu)
        CastButtonFactory.setUpMediaRouteButton(
            applicationContext,
            menu,
            R.id.media_route_menu_item
        )
        return true
    }

    private fun getSessionManagerListener(context: GameActivity): SessionManagerListener<CastSession> {
        return object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(castSession: CastSession?) {}

            override fun onSessionStarted(castSession: CastSession?, sessionId: String) {
                context.castSession = castSession
                if (castSession != null) {
                    try {
                        createChannel()
                        try {
                            updateChart()
                        } catch (e: Exception) {
                            Log.e(logTag, "Exception while sending message", e)
                        }
                    } catch (e: IOException) {
                        Log.e(logTag, "Exception while creating channel", e)
                    }
                }
            }

            override fun onSessionStartFailed(castSession: CastSession?, i: Int) {
                context.castSession = null
            }

            override fun onSessionEnding(castSession: CastSession?) {}
            override fun onSessionEnded(castSession: CastSession?, i: Int) {
                context.castSession = null
            }

            override fun onSessionResuming(castSession: CastSession?, s: String) {}
            override fun onSessionResumed(castSession: CastSession?, b: Boolean) {}
            override fun onSessionResumeFailed(castSession: CastSession?, i: Int) {}
            override fun onSessionSuspended(castSession: CastSession?, i: Int) {}
        }
    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) touchHelper?.startDrag(viewHolder)
    }

    fun nextRound(@Suppress("UNUSED_PARAMETER") view: View) {
        roundNumber++
        binding.roundNumber.text = getString(R.string.round_n, roundNumber)
        binding.submitRoundButton.text = getString(R.string.submit_round_n, roundNumber)
        binding.undoRoundButton.text = getString(R.string.go_back_to_round_n, roundNumber - 1)

        chartData.playerHistories.forEach { playerHistory ->
            var position = 0
            var stillInGame = false
            players.forEachIndexed { i, player ->
                if (player.name == playerHistory.name) {
                    position = i
                    stillInGame = true
                }
            }
            if (stillInGame) {
                val currentScore = playerHistory.series.last().value
                val newScore =
                    currentScore + calculateScore(position, numberOfMiddlemen, players.size)
                playerHistory.series.add(ChartScoreForRound(roundNumber - 1, newScore))
            }
        }

        updateChart()
    }

    fun undoRound(@Suppress("UNUSED_PARAMETER") view: View) {
        if (roundNumber > 1) {
            roundNumber--

            binding.roundNumber.text = getString(R.string.round_n, roundNumber)
            binding.submitRoundButton.text = getString(R.string.submit_round_n, roundNumber)
            if (roundNumber == 1) binding.undoRoundButton.text =
                getString(R.string.go_back_to_player_selection)
            else binding.undoRoundButton.text =
                getString(R.string.go_back_to_round_n, roundNumber - 1)

            chartData.playerHistories.forEach { playerHistory ->
                playerHistory.series.removeAt(playerHistory.series.size - 1)
            }

            updateChart()

        } else {
            val intent = Intent(this, PlayerSelectionActivity::class.java)
            intent.putExtra("Players", Gson().toJson(players))
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    @SuppressLint("InflateParams")
    fun addPlayer(@Suppress("UNUSED_PARAMETER") view1: View) {
        // --- START View Binding for create_player_item.xml ---
        val createPlayerItemBinding = CreatePlayerItemBinding.inflate(layoutInflater)
        val viewToSet = createPlayerItemBinding.root // Get the root view for AlertDialog

        var newColour = getNextFreeColour(players)

        AlertDialog.Builder(this).let {
            it.setView(viewToSet) // Set the inflated view to the AlertDialog
            it.setTitle(getString(R.string.add_new_player))
            it.setPositiveButton(getString(R.string.add_player)) { _, _ ->
                val name = createPlayerItemBinding.playerName.text.toString() // Use binding
                players.add(Player(name, newColour))
                binding.recyclerView.adapter?.notifyDataSetChanged() // Main activity's RecyclerView

                numberOfMiddlemen =
                    if (numberOfMiddlemen == 0) 1 else (createPlayerItemBinding.spinner.selectedItem as MiddlemanOption).number // Use binding
                (binding.recyclerView.adapter as GamePlayerListAdapter).numberOfMiddlemen = // Main activity's RecyclerView
                    numberOfMiddlemen

                this.chartData.colours.add(newColour)
                this.chartData.playerHistories.add(
                    ChartPlayerHistory(
                        name,
                        arrayListOf(ChartScoreForRound(roundNumber - 1, 0))
                    )
                )
            }
            it.setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            createPlayerItemBinding.pickColourButton?.let { // Use binding
                ViewCompat.setBackgroundTintList(it, ColorStateList.valueOf(newColour))
            }
            createPlayerItemBinding.pickColourButton?.setTextColor(getTextColour(newColour)) // Use binding

            // The ColorPicker block remains commented out, but if uncommented, you'd use
            // createPlayerItemBinding.pickColourButton instead of view.pickColourButton
//            view.pickColourButton?.setOnClickListener {
//                val colorPicker = ColorPicker(this)
//                colorPicker.setOnChooseColorListener(object : ColorPicker.OnChooseColorListener {
//                    override fun onChooseColor(position: Int, colour: Int) {
//                        try {
//                            newColour = colour
//                            ViewCompat.setBackgroundTintList(
//                                createPlayerItemBinding.pickColourButton, // Use binding
//                                ColorStateList.valueOf(colour)
//                            )
//                            createPlayerItemBinding.pickColourButton.setTextColor(getTextColour(colour)) // Use binding
//                        } catch (e: ArrayIndexOutOfBoundsException) {
//                        }
//                    }
//
//                    override fun onCancel() {}
//                })
//                    .setColors(Colours.getAllColourOptions(players, newColour))
//                    .setDefaultColorButton(newColour)
//                    .setRoundColorButton(true)
//                    .show()
//            }

            ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item,
                ScumHelpers.generateMiddlemenOptionsForMidgame(numberOfMiddlemen, players.size + 1)
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                createPlayerItemBinding.spinner.adapter = adapter // Use binding
            }

            if (numberOfMiddlemen == 0) {
                createPlayerItemBinding.spinner.visibility = View.GONE // Use binding
            }

            it.create()
            val dialog = it.show()
            val addPlayerButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addPlayerButton.isEnabled = false

            createPlayerItemBinding.playerName.addTextChangedListener(object : TextWatcher { // Use binding
                override fun afterTextChanged(s: Editable) {
                    val newName = s.toString()
                    when {
                        newName.isEmpty() -> {
                            addPlayerButton.isEnabled = false
                            createPlayerItemBinding.errorText.text = getString(R.string.error_enter_name) // Use binding
                            createPlayerItemBinding.errorText.visibility = View.VISIBLE // Use binding
                        }
                        newName in players.map { player -> player.name } -> {
                            addPlayerButton.isEnabled = false
                            createPlayerItemBinding.errorText.text = // Use binding
                                getString(R.string.error_another_player_has_name, newName)
                            createPlayerItemBinding.errorText.visibility = View.VISIBLE // Use binding
                        }
                        else -> {
                            createPlayerItemBinding.errorText.visibility = View.GONE // Use binding
                            addPlayerButton.isEnabled = true
                            createPlayerItemBinding.errorText.text = "" // Use binding
                        }
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence, start: Int, before: Int, count: Int
                ) = Unit
            })
        }
        // --- END View Binding for create_player_item.xml ---
    }

    fun shareGame(@Suppress("UNUSED_PARAMETER") item: MenuItem) {
        val params: String = URLEncoder.encode(chartData.toString(), "utf-8")
        val url = "https://olliechick.co.nz/scumgraph/graph?chartdata=$params"
        val message = "I just played a game of scum, here's the graph: $url\n\n" +
                "Go to https://play.google.com/store/apps/details?id=nz.co.olliechick.scumgraph " +
                "to download the app."

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, "Scum graph")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun openGraph(@Suppress("UNUSED_PARAMETER") item: MenuItem) {
        val params: String = URLEncoder.encode(chartData.toString(), "utf-8")
        val url = "https://olliechick.co.nz/scumgraph/graph?chartdata=$params"

        val openGraphIntent: Intent = Intent().apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(url)
        }

        startActivity(openGraphIntent)
    }
}