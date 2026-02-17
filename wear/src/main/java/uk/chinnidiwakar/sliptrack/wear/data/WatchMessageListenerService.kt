package uk.chinnidiwakar.sliptrack.wear.data

import android.util.Log
import androidx.wear.tiles.TileService
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import uk.chinnidiwakar.sliptrack.wear.presentation.SlipTrackTileService

class WatchMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {

        if (messageEvent.path == "/slip_count") {

            val count = String(messageEvent.data).toInt()

            Log.d("SlipTrack", "Received count: $count")

            SlipCountStore.save(this, count)

            TileService.getUpdater(this)
                .requestUpdate(SlipTrackTileService::class.java)
        }
    }
}
