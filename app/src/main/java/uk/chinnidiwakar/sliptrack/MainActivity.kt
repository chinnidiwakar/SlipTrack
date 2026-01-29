package uk.chinnidiwakar.sliptrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import uk.chinnidiwakar.sliptrack.navigation.AppNavigation
import uk.chinnidiwakar.sliptrack.ui.theme.RelapseTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RelapseTrackerTheme {
                AppNavigation()
            }
        }
    }
}