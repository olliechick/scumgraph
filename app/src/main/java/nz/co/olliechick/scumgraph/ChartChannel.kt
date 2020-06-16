package nz.co.olliechick.scumgraph

import android.util.Log
import com.google.android.gms.cast.Cast
import com.google.android.gms.cast.CastDevice

internal class ChartChannel : Cast.MessageReceivedCallback {
    val namespace: String get() = "urn:x-cast:nz.co.olliechick.scumgraph.chart"

    override fun onMessageReceived(
        castDevice: CastDevice, namespace: String, message: String
    ) {
    }
}
