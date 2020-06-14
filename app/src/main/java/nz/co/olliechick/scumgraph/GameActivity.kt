package nz.co.olliechick.scumgraph

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
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
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.calculateScore
import org.json.JSONObject
import java.io.IOException
import java.util.*


class GameActivity : AppCompatActivity(), OnStartDragListener {
    var roundNumber = 1
    var numberOfMiddlemen = 0
    var players = arrayListOf<Player>()
    var chartData = Chart(players)

    private var touchHelper: ItemTouchHelper? = null

    private var castContext: CastContext? = null
    private var sessionManagerListener: SessionManagerListener<CastSession>? = null
    private var castSession: CastSession? = null
    private var chartChannel: ChartChannel? = null

    private val TAG = "scumgraphlog"

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

    private fun createChannel() {
        if (castSession == null) castSession = castContext?.sessionManager?.currentCastSession

        if (castSession?.isConnected == true && chartChannel == null) {
            castSession?.let {
                chartChannel = ChartChannel()
                Log.i(TAG, "creating chart channel")
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
                    if (!result.isSuccess) Log.e(TAG, "Sending message failed")
                })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.gamemenu, menu)
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
                            Log.e(TAG, "Exception while sending message", e)
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception while creating channel", e)
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

    fun nextRound(view: View) {
        roundNumber++
        findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)

        chartData.playerHistories.forEach { playerHistory ->
            var position = 0
            players.forEachIndexed { i, player ->
                if (player.name == playerHistory.name) position = i
            }
            val currentScore = playerHistory.series.last().value
            val newScore = currentScore + calculateScore(position, numberOfMiddlemen, players.size)
            playerHistory.series.add(ScoreForRound(roundNumber - 1, newScore))
        }

        updateChart()
    }
}