package uk.chinnidiwakar.sliptrack.wear.presentation

import android.app.Activity
import android.os.Bundle
import com.google.android.gms.wearable.Wearable

class AddSlipProxyActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val messageClient = Wearable.getMessageClient(this)
        val nodeClient = Wearable.getNodeClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                messageClient.sendMessage(
                    node.id,
                    "/add_slip",
                    ByteArray(0)
                )
            }
            finish()
        }
    }
}
