package nz.co.olliechick.scumgraph

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.api.Status
import com.google.gson.Gson
import kotlinx.android.synthetic.main.start_game_dialog.view.*
import nz.co.olliechick.scumgraph.util.ColourOption
import nz.co.olliechick.scumgraph.util.Colours.Companion.colourOptionToInt
import nz.co.olliechick.scumgraph.util.MiddlemanOption
import nz.co.olliechick.scumgraph.util.Player
import nz.co.olliechick.scumgraph.util.PlayerList
import nz.co.olliechick.scumgraph.util.ScumHelpers.Companion.generateMiddlemenOptions
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private val players = arrayListOf(
        Player("", colourOptionToInt(ColourOption.BLUE)),
        Player("", colourOptionToInt(ColourOption.RED)),
        Player("", colourOptionToInt(ColourOption.GREEN))
    )
    private var castContext: CastContext? = null
    private var sessionManagerListener: SessionManagerListener<CastSession>? = null
    private var castSession: CastSession? = null
    private var playerListChannel: PlayerListChannel? = null

    private val TAG = "scumgraphlog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = CreatePlayerListAdapter(
            players, viewManager as LinearLayoutManager, this, ::updatePlayerList
        )

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    override fun onResume() {
        super.onResume()

        castContext = CastContext.getSharedInstance(this)
        sessionManagerListener = getSessionManagerListener(this)
        castContext?.sessionManager?.addSessionManagerListener(
            sessionManagerListener as SessionManagerListener<CastSession>,
            CastSession::class.java
        )
        updatePlayerList()
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

        if (castSession?.isConnected == true && playerListChannel == null) {
            castSession?.let {
                playerListChannel = PlayerListChannel()
                it.setMessageReceivedCallbacks(
                    playerListChannel?.namespace,
                    playerListChannel
                )
            }
        }
    }

    private fun updatePlayerList() {
        createChannel()
        val players = JSONObject(Gson().toJson(PlayerList(players))).toString()
        playerListChannel?.let {
            castContext?.sessionManager?.currentCastSession
                ?.sendMessage(it.namespace, players)
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

    private fun getSessionManagerListener(context: MainActivity): SessionManagerListener<CastSession> {
        return object : SessionManagerListener<CastSession> {
            override fun onSessionStarting(castSession: CastSession?) {}

            override fun onSessionStarted(castSession: CastSession?, sessionId: String) {
                context.castSession = castSession
                if (castSession != null) {
                    try {
                        createChannel()
                        try {
                            updatePlayerList()
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

    @SuppressLint("InflateParams")
    fun startGame(view1: View) {
        if (players.size > 3) {
            val view = layoutInflater.inflate(R.layout.start_game_dialog, null)

            AlertDialog.Builder(this).run {
                setView(view)
                setTitle(getString(R.string.game_setup))
                setPositiveButton("Start game") { _, _ -> openGameScreen((view.spinner.selectedItem as MiddlemanOption).number) }
                setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }

                ArrayAdapter(
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