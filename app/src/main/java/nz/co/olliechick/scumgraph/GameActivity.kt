package nz.co.olliechick.scumgraph

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
import kotlinx.android.synthetic.main.activity_game.*
import nz.co.olliechick.scumgraph.draggablelist.OnStartDragListener
import nz.co.olliechick.scumgraph.draggablelist.SimpleItemTouchHelperCallback
import nz.co.olliechick.scumgraph.util.Chart
import nz.co.olliechick.scumgraph.util.ChartScoreForRound
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.calculateScore
import org.json.JSONObject
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

    private inline fun <reified T> Gson.fromJson(json: String) =
        fromJson<T>(json, object : TypeToken<T>() {}.type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        numberOfMiddlemen = intent.getIntExtra("Number of middlemen", 0)

        val playersString = intent.getStringExtra("Players")
        players = if (playersString != null) Gson().fromJson<ArrayList<Player>>(playersString)
        else arrayListOf()
        chartData = Chart(players)

        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = GamePlayerListAdapter(numberOfMiddlemen, players, this, this, ::updateChart)
        recyclerView.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(recyclerView)
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
        findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)
        findViewById<TextView>(R.id.submitRoundButton).text =
            getString(R.string.submit_round_n, roundNumber)
        findViewById<Button>(R.id.undoRoundButton).text =
            getString(R.string.go_back_to_round_n, roundNumber - 1)

        chartData.playerHistories.forEach { playerHistory ->
            var position = 0
            players.forEachIndexed { i, player ->
                if (player.name == playerHistory.name) position = i
            }
            val currentScore = playerHistory.series.last().value
            val newScore = currentScore + calculateScore(position, numberOfMiddlemen, players.size)
            playerHistory.series.add(ChartScoreForRound(roundNumber - 1, newScore))
        }

        updateChart()
    }

    fun undoRound(@Suppress("UNUSED_PARAMETER") view: View) {
        if (roundNumber > 1) {
            roundNumber--

            findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)
            findViewById<Button>(R.id.submitRoundButton).text =
                getString(R.string.submit_round_n, roundNumber)
            if (roundNumber == 1) findViewById<Button>(R.id.undoRoundButton).text =
                getString(R.string.go_back_to_player_selection)
            else findViewById<Button>(R.id.undoRoundButton).text =
                getString(R.string.go_back_to_round_n, roundNumber - 1)

            chartData.playerHistories.forEach { playerHistory ->
                playerHistory.series.removeAt(playerHistory.series.size - 1)
            }

            updateChart()

        } else {
            val intent = Intent(this, PlayerSelectionActivity::class.java)
            intent.putExtra("Players", Gson().toJson(players))
            startActivity(intent)
        }
    }

    fun shareGame(@Suppress("UNUSED_PARAMETER") item: MenuItem) {
        val params: String = URLEncoder.encode(chartData.toString(), "utf-8")
        val url = "https://olliechick.co.nz/scumgraph/graph?chartdata=$params"
        val message = "I just played a game of scum, you can go to $url to see the graph!\n\n" +
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
}