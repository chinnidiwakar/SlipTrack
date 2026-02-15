package uk.chinnidiwakar.sliptrack.wearapp.sync

import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject
import uk.chinnidiwakar.sliptrack.wearapp.tile.SlipTrackTileService

class WearStateListenerService : WearableListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path != WearSyncProtocol.PATH_STATE_SYNC) return

        scope.launch {
            val json = JSONObject(String(messageEvent.data, Charsets.UTF_8))
            WearStateStore(applicationContext).save(
                WearState(
                    currentStreak = json.optInt("currentStreak", 0),
                    longestStreak = json.optInt("longestStreak", 0),
                    shieldCharges = json.optInt("shieldCharges", 0)
                )
            )
            TileService.getUpdater(applicationContext).requestUpdate(SlipTrackTileService::class.java)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
