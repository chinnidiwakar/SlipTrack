package uk.chinnidiwakar.sliptrack.data

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WearMessageListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/add_slip") {

            Log.d("SlipTrack", "Add slip requested from watch")

            CoroutineScope(Dispatchers.IO).launch {

                // TODO: Replace this with your actual repository call
                insertSlip()

                val updatedCount = getTodaySlipCount()

                sendCountBackToWatch(updatedCount)
            }
        }
    }

    private fun insertSlip() {
        // Call your SlipRepository here
    }

    private fun getTodaySlipCount(): Int {
        // Replace with real DB query
        return 5
    }

    private fun sendCountBackToWatch(count: Int) {

        val messageClient = Wearable.getMessageClient(this)
        val nodeClient = Wearable.getNodeClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(
                    node.id,
                    "/slip_count",
                    count.toString().toByteArray()
                )
            }
        }
    }
}
