package uk.chinnidiwakar.sliptrack.wear.data

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object WatchMessageSender {

    suspend fun sendAddSlip(context: Context) {
        val nodeClient = Wearable.getNodeClient(context)
        val messageClient = Wearable.getMessageClient(context)

        val nodes = nodeClient.connectedNodes.await()

        for (node in nodes) {
            messageClient.sendMessage(
                node.id,
                "/add_slip",
                ByteArray(0)
            ).await()
        }
    }
}
