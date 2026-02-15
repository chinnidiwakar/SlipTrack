package uk.chinnidiwakar.sliptrack.wearapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.wearapp.sync.WearStateStore
import uk.chinnidiwakar.sliptrack.wearapp.sync.WearSyncManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val store = WearStateStore(this)
        val syncManager = WearSyncManager(this, store)

        setContent {
            WearScreen(
                stateStore = store,
                syncManager = syncManager,
                onSlip = {
                    lifecycleScope.launch {
                        runCatching { syncManager.sendLog(isResist = false, trigger = "Wear quick action") }
                            .onSuccess { Toast.makeText(this@MainActivity, "Slip sent", Toast.LENGTH_SHORT).show() }
                            .onFailure { Toast.makeText(this@MainActivity, "Phone not reachable", Toast.LENGTH_SHORT).show() }
                    }
                },
                onResist = {
                    lifecycleScope.launch {
                        runCatching { syncManager.sendLog(isResist = true, intensity = 2, trigger = "Wear quick action") }
                            .onSuccess { Toast.makeText(this@MainActivity, "Victory sent", Toast.LENGTH_SHORT).show() }
                            .onFailure { Toast.makeText(this@MainActivity, "Phone not reachable", Toast.LENGTH_SHORT).show() }
                    }
                }
            )
        }
    }
}

@Composable
private fun WearScreen(
    stateStore: WearStateStore,
    syncManager: WearSyncManager,
    onSlip: () -> Unit,
    onResist: () -> Unit
) {
    val state by stateStore.state.collectAsState(initial = uk.chinnidiwakar.sliptrack.wearapp.sync.WearState())

    DisposableEffect(syncManager) {
        syncManager.start()
        onDispose { syncManager.stop() }
    }

    MaterialTheme {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item { Text("SlipTrack", style = MaterialTheme.typography.title3) }
            item { Text("Current: ${state.currentStreak}d") }
            item { Text("Best: ${state.longestStreak}d") }
            item { Text("🛡 ${state.shieldCharges}") }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = onResist) { Text("Resist") }
                    Button(onClick = onSlip) { Text("Slip") }
                }
            }
        }
    }
}
