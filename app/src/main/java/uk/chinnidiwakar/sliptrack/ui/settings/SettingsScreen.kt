package uk.chinnidiwakar.sliptrack.ui.settings

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.chinnidiwakar.sliptrack.HomeViewModel

private val DEFAULT_JOURNEY_OPTIONS = listOf(
    "Drugs",
    "Porn",
    "Mobile",
    "Alcohol",
    "Smoking",
    "Gambling",
    "Sugar",
    "Social Media"
)

@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    onNavigateBack: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
) {
    val journeyName by viewModel.journeyName.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val appVersion = remember(context) { getAppVersion(context) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // CATEGORY: MINDSET (Sattvic / Hindu focus)
                item {
                    SettingsSection(title = "Mindset & Purpose") {
                        JourneyNameEditor(
                            currentName = journeyName,
                            onSave = { viewModel.updateJourneyName(it) }
                        )
                        HorizontalDivider(color = Color.White.copy(0.05f))
                        // Suggestion: A "Sankalpa" (Intention) reminder toggle
                        SettingsToggleRow(
                            title = "Daily Sankalpa",
                            subtitle = "Morning reminder of your intention",
                            checked = true, // You'd bind this to a pref
                            icon = Icons.Default.SelfImprovement,
                            onCheckedChange = { /* Toggle logic */ }
                        )
                    }
                }

                // CATEGORY: DATA CONTROL
                item {
                    SettingsSection(
                        title = "Vault",
                        footer = "Your progress is sacred. It stays on this device."
                    ) {
                        SettingsClickableRow("Export Journey", "Backup your data", Icons.Default.History, onExport)
                        HorizontalDivider(color = Color.White.copy(0.05f))
                        SettingsClickableRow("Import Journey", "Restore progress", Icons.Default.Restore, onImport)
                    }
                }

                // CATEGORY: VISUALS
                item {
                    SettingsSection(title = "Environment") {
                        SettingsToggleRow(
                            title = "AMOLED Sky",
                            subtitle = "Zero light pollution",
                            checked = themeMode == "sky",
                            icon = Icons.Default.NightsStay,
                            onCheckedChange = { isSky -> viewModel.updateTheme(if (isSky) "sky" else "material") }
                        )
                    }
                }

                // INFO
                item {
                    SettingsSection(title = "App Info") {
                        SettingsInfoRow("Version", appVersion)
                        SettingsInfoRow("Developer", "FalconRising")
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
fun JourneyNameEditor(currentName: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    var isEditing by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    if (!isEditing) {
        SettingsClickableRow(
            title = "Current Goal",
            subtitle = currentName,
            icon = Icons.Default.Flag,
            onClick = { isEditing = true }
        )
    } else {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Choose your focus",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DEFAULT_JOURNEY_OPTIONS.forEach { option ->
                    FilterChip(
                        selected = selectedOption == option,
                        onClick = {
                            selectedOption = option
                            text = option
                        },
                        label = { Text(option) }
                    )
                }
            }

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    selectedOption = DEFAULT_JOURNEY_OPTIONS.firstOrNull { option ->
                        option.equals(it.trim(), ignoreCase = true)
                    }
                },
                label = { Text("Custom goal (or edit selection)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                TextButton(
                    onClick = {
                        text = currentName
                        selectedOption = null
                        isEditing = false
                    }
                ) { Text("Cancel") }
                Button(
                    onClick = {
                        val finalName = text.trim().ifEmpty { currentName }
                        onSave(finalName)
                        isEditing = false
                    }
                ) { Text("Save") }
            }
        }
    }
}

// --- GLASSMOPHISM UTILS ---

@Composable
fun SettingsSection(title: String, footer: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 12.dp, bottom = 8.dp), letterSpacing = 1.sp)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White.copy(alpha = 0.05f), // True Glass look
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(content = content)
        }
        footer?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(12.dp)) }
    }
}

@Composable
fun SettingsClickableRow(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggleRow(title: String, subtitle: String, checked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsInfoRow(title: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, fontWeight = FontWeight.Medium)
        Text(value, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}

fun getAppVersion(context: Context): String {
    return try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" } catch (e: Exception) { "1.0" }
}
