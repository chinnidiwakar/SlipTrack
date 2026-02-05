package uk.chinnidiwakar.sliptrack.ui.emergency

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmergencyScreen() {
    var step by remember { mutableStateOf(0) }

    val steps = listOf(
        "Step 1: Pause. Take 10 slow breaths before any action.",
        "Step 2: Change your environment now (stand up, leave room, cold water).",
        "Step 3: Run a 10-minute replacement (walk, pushups, journaling, shower).",
        "Step 4: Message one trusted person: 'I need a quick check-in.'",
        "Step 5: If urge remains, log it as a victory with trigger + intensity."
    )

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Emergency protocol", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Use this when you feel close to slipping. One step at a time.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Text(
                    text = steps[step],
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Button(
                onClick = { if (step < steps.lastIndex) step += 1 else step = 0 },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (step < steps.lastIndex) "Next step" else "Restart protocol")
            }
        }
    }
}
