package uk.chinnidiwakar.sliptrack.wear

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearSyncClient(private val context: Context) {

    suspend fun pushState(payload: WearStatePayload) {
        val nodeClient = Wearable.getNodeClient(context)
        val messageClient = Wearable.getMessageClient(context)
        val nodes = nodeClient.connectedNodes.await()
        val bytes = payload.toBytes()

        nodes.forEach { node ->
            messageClient.sendMessage(node.id, WearSyncProtocol.PATH_STATE_SYNC, bytes).await()
        }
    }

    suspend fun pushStateToNode(nodeId: String, payload: WearStatePayload) {
        Wearable.getMessageClient(context)
            .sendMessage(nodeId, WearSyncProtocol.PATH_STATE_SYNC, payload.toBytes())
            .await()
    }
}
