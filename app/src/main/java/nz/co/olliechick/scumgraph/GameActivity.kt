package nz.co.olliechick.scumgraph

import android.content.Context
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_game.*
import nz.co.olliechick.scumgraph.draggablelist.OnStartDragListener
import nz.co.olliechick.scumgraph.draggablelist.SimpleItemTouchHelperCallback
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.PlayerList
import org.json.JSONObject
import java.io.IOException
import java.util.*


class GameActivity : AppCompatActivity(), OnStartDragListener {

    var roundNumber = 1
    var numberOfMiddlemen = 0
    var players = arrayListOf<Player>()
    private var touchHelper: ItemTouchHelper? = null
    private var castContext: CastContext? = null

    private val TAG = "scumgraph"

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
        val adapter = GamePlayerListAdapter(numberOfMiddlemen, players, this, this)
        recyclerView.adapter = adapter
        val callback = SimpleItemTouchHelperCallback(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper?.attachToRecyclerView(recyclerView)

        castContext = CastContext.getSharedInstance(this)
        castContext?.sessionManager?.addSessionManagerListener(
            getSessionManagerListener(this),
            CastSession::class.java
        )
    }

    private fun getSessionManagerListener(context: Context): SessionManagerListener<CastSession> {
        return object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(castSession: CastSession?) {
                Log.i(TAG, "session starting")
            }

            override fun onSessionStarted(castSession: CastSession?, s: String) {
                Log.i(TAG, "session started")
                if (castSession != null) {
                    try {
                        Log.i(TAG, "Creating channel")
                        val playerListChannel = PlayerListChannel()
                        castSession.setMessageReceivedCallbacks(
                            playerListChannel.namespace,
                            playerListChannel
                        )

                        try {
                            val players = JSONObject(Gson().toJson(PlayerList(players))).toString()
                            castSession.sendMessage(playerListChannel.namespace, players)
                                .setResultCallback { result ->
                                    if (!result.isSuccess) Log.e(TAG, "Sending message failed")
                                }
                        } catch (e: Exception) {
                            Log.e(TAG, "Exception while sending message", e)
                        }
                    } catch (e: IOException) {
                        Log.e(TAG, "Exception while creating channel", e)
                    }
                }
            }

            override fun onSessionStartFailed(castSession: CastSession?, i: Int) {}
            override fun onSessionEnding(castSession: CastSession?) {}
            override fun onSessionEnded(castSession: CastSession?, i: Int) {}
            override fun onSessionResuming(castSession: CastSession?, s: String) {}
            override fun onSessionResumed(castSession: CastSession?, b: Boolean) {}
            override fun onSessionResumeFailed(castSession: CastSession?, i: Int) {}
            override fun onSessionSuspended(castSession: CastSession?, i: Int) {}
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

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder?) {
        if (viewHolder != null) touchHelper?.startDrag(viewHolder)
    }

    fun nextRound(view: View) {
        roundNumber++
        findViewById<TextView>(R.id.roundNumber).text = getString(R.string.round_n, roundNumber)
    }
}