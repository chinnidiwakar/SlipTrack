package uk.chinnidiwakar.sliptrack.wear.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.chinnidiwakar.sliptrack.wear.data.WatchMessageSender
import uk.chinnidiwakar.sliptrack.wear.presentation.theme.SlipTrackTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}


@Composable
fun WearApp() {
    val context = LocalContext.current
    SlipTrackTheme {
        AppScaffold {
            ScreenScaffold {
                TransformingLazyColumn {

                    item {
                        Text(
                            text = "SlipTrack",
                            style = androidx.wear.compose.material3.MaterialTheme.typography.titleMedium
                        )
                    }

                    item {
                        Text(
                            text = "Today",
                            style = androidx.wear.compose.material3.MaterialTheme.typography.bodyMedium
                        )
                    }

                    item {
                        Text(
                            text = "0",
                            style = androidx.wear.compose.material3.MaterialTheme.typography.displayLarge
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    WatchMessageSender.sendAddSlip(context)
                                }
                            },

                                    modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Slip")
                        }
                    }
                }
            }
        }
    }
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp()
}
