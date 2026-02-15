package uk.chinnidiwakar.sliptrack.wear

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.DatabaseProvider
import uk.chinnidiwakar.sliptrack.PreferenceManager
import uk.chinnidiwakar.sliptrack.StreakCalculator

class WearDataLayerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            WearSyncProtocol.PATH_LOG_EVENT -> handleLogEvent(messageEvent)
            WearSyncProtocol.PATH_REQUEST_STATE -> handleStateRequest(messageEvent.sourceNodeId)
            else -> Unit
        }
    }

    private fun handleLogEvent(messageEvent: MessageEvent) {
        serviceScope.launch {
            val dao = DatabaseProvider.get(applicationContext).slipDao()
            val payload = WearLogEventPayload.fromBytes(messageEvent.data)
            dao.insertSlip(payload.toSlipEvent())
            handleStateRequest(messageEvent.sourceNodeId)
        }
    }

    private fun handleStateRequest(nodeId: String) {
        serviceScope.launch {
            val dao = DatabaseProvider.get(applicationContext).slipDao()
            val prefs = PreferenceManager(applicationContext)
            val allEvents = dao.getAllSlips()
            val slips = allEvents.filter { !it.isResist }
            val statePayload = WearStatePayload(
                currentStreak = if (slips.isEmpty()) 0 else StreakCalculator.currentStreak(slips),
                longestStreak = if (slips.isEmpty()) 0 else StreakCalculator.longestStreak(slips),
                shieldCharges = prefs.shieldCharges.first(),
                updatedAt = System.currentTimeMillis()
            )
            WearSyncClient(applicationContext).pushStateToNode(nodeId, statePayload)
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}
