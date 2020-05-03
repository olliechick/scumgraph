package nz.co.olliechick.scumgraph

import android.util.Log
import com.google.android.gms.cast.Cast.MessageReceivedCallback
import com.google.android.gms.cast.CastDevice


internal class PlayerListChannel : MessageReceivedCallback {
    val namespace: String
        get() = "urn:x-cast:nz.co.olliechick.scumgraph.playerlist"

    override fun onMessageReceived(
        castDevice: CastDevice, namespace: String,
        message: String
    ) {
        Log.d("scumgraph", "onMessageReceived: $message")
    }
}