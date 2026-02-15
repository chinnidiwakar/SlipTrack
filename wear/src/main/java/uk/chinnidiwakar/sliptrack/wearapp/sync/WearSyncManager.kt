package uk.chinnidiwakar.sliptrack.wearapp.sync

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class WearSyncManager(
    private val context: Context,
    private val stateStore: WearStateStore
) : MessageClient.OnMessageReceivedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        Wearable.getMessageClient(context).addListener(this)
        scope.launch { requestLatestState() }
    }

    fun stop() {
        Wearable.getMessageClient(context).removeListener(this)
    }

    override fun onMessageReceived(event: com.google.android.gms.wearable.MessageEvent) {
        if (event.path != WearSyncProtocol.PATH_STATE_SYNC) return
        val json = JSONObject(String(event.data, Charsets.UTF_8))
        val updatedState = WearState(
            currentStreak = json.optInt("currentStreak", 0),
            longestStreak = json.optInt("longestStreak", 0),
            shieldCharges = json.optInt("shieldCharges", 0)
        )
        scope.launch { stateStore.save(updatedState) }
    }

    suspend fun sendLog(isResist: Boolean, intensity: Int = 0, trigger: String? = null, note: String? = null) {
        val payload = JSONObject()
            .put("timestamp", System.currentTimeMillis())
            .put("isResist", isResist)
            .put("intensity", intensity)
            .put("trigger", trigger ?: "")
            .put("note", note ?: "")
            .toString()
            .toByteArray(Charsets.UTF_8)

        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            Wearable.getMessageClient(context)
                .sendMessage(node.id, WearSyncProtocol.PATH_LOG_EVENT, payload)
                .await()
        }
    }

    suspend fun requestLatestState() {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.forEach { node ->
            Wearable.getMessageClient(context)
                .sendMessage(node.id, WearSyncProtocol.PATH_REQUEST_STATE, byteArrayOf())
                .await()
        }
    }
}
