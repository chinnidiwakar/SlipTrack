package uk.chinnidiwakar.sliptrack.ui.insights

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModel
import uk.chinnidiwakar.sliptrack.InsightsViewModelFactory
import uk.chinnidiwakar.sliptrack.domain.RiskLevel

@Composable
fun InsightsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: InsightsViewModel = viewModel(factory = InsightsViewModelFactory(context))

    val insights by viewModel.insights.collectAsState()
    val weeklyReport by viewModel.weeklyReport.collectAsState()

    val configuration = LocalConfiguration.current
    val isWideLayout = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 840

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(if (isWideLayout) 0.95f else 1f)
                        .align(Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

            Spacer(Modifier.height(8.dp))

            Text(
                "Insights",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )

            // --- Weekly Summary Glass Card ---
                    insights?.riskAssessment?.let { risk ->

                        val accentColor = when (risk.level) {
                            RiskLevel.STABLE -> Color(0xFF66BB6A)
                            RiskLevel.CAUTION -> Color(0xFFFFA726)
                            RiskLevel.HIGH -> Color(0xFFEF5350)
                        }

                        val animatedProgress = animateFloatAsState(
                            targetValue = risk.score / 100f,
                            animationSpec = tween(1000, easing = FastOutSlowInEasing),
                            label = "risk_circle"
                        )

                        // Pulse for HIGH risk
                        val pulseScale = if (risk.level == RiskLevel.HIGH) {
                            androidx.compose.animation.core.animateFloatAsState(
                                targetValue = 1.05f,
                                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                ),
                                label = "pulse"
                            ).value
                        } else 1f

                        // Subtle glow for STABLE
                        val glowAlpha = if (risk.level == RiskLevel.STABLE) 0.15f else 0f

                        GlassCard(
                            opacity = 0.28f,
                            modifier = Modifier
                                .background(
                                    accentColor.copy(alpha = glowAlpha),
                                    RoundedCornerShape(24.dp)
                                )
                        ) {

                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // LEFT SIDE — TEXT
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {

                                    Text(
                                        "Weekly Report",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Gray
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        "${weeklyReport.cleanDaysThisWeek} Clean • ${weeklyReport.victoriesThisWeek} Won • ${weeklyReport.slipsThisWeek} Slips",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        when (risk.level) {
                                            RiskLevel.STABLE -> "Stable — keep going"
                                            RiskLevel.CAUTION -> "Moderate — stay sharp"
                                            RiskLevel.HIGH -> "High risk — protect your streak"
                                        },
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                }

                                // RIGHT SIDE — MINI CIRCLE
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                                    contentAlignment = Alignment.Center
                                ) {

                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {

                                        val strokeWidth = 10.dp.toPx()
                                        val diameter = size.minDimension
                                        val radius = diameter / 2

                                        drawCircle(
                                            color = Color.White.copy(alpha = 0.05f),
                                            radius = radius,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                                        )

                                        drawArc(
                                            color = accentColor,
                                            startAngle = -90f,
                                            sweepAngle = 360 * animatedProgress.value,
                                            useCenter = false,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = strokeWidth,
                                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                                            )
                                        )
                                    }

                                    Text(
                                        "${risk.score}%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

            if (insights == null) {
                WillpowerMeter(weeklyReport.victoriesThisWeek, weeklyReport.slipsThisWeek)
            } else {
                Text("Pattern Analysis", style = MaterialTheme.typography.labelLarge, color = Color.Gray)

                // --- Danger Zones (Side by Side Glass) ---
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniInsightCard("Danger Hour", insights!!.mostCommonHour ?: "--", Modifier.weight(1f))
                    MiniInsightCard("Hardest Day", insights!!.mostCommonDay ?: "--", Modifier.weight(1f))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniInsightCard("Current Streak", insights!!.currentStreak ?: "--", Modifier.weight(1f))
                    MiniInsightCard("Risk Window", insights!!.hardestWindow ?: "--", Modifier.weight(1f))
                }

                insights?.recentSlipRate?.let {
                    InsightCard("Recent Slip Velocity", it)
                }

                // --- Smart Recovery Plan (Glow Glass) ---
                insights?.suggestedAction?.let { action ->
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Smart Recovery Plan", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text(action, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                WillpowerMeter(score = insights!!.willpowerScore)
            }
                    Spacer(Modifier.height(32.dp)) }
        }
    }
}


// --- REUSABLE GLASS CONTAINER ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    opacity: Float = 0.2f,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = opacity),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 2.dp
    ) {
        content()
    }
}

@Composable
fun InsightCard(title: String, value: String) {
    GlassCard(opacity = 0.25f) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun WillpowerMeter(score: Int) {
    val floatScore = score / 100f

    GlassCard(opacity = 0.2f) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Overall Resilience", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("$score%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))

            // Modernized Progress Bar
            Box(contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(100))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(floatScore)
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100))
                )
            }

            Text("Defeated $score% of recorded urges", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun WillpowerMeter(victories: Int, slips: Int) {
    val total = (victories + slips).coerceAtLeast(1)
    val score = ((victories.toFloat() / total) * 100).toInt()
    WillpowerMeter(score = score)
}

@Composable
fun MiniInsightCard(title: String, value: String, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, opacity = 0.25f) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun RiskMeter(riskScore: Int, level: RiskLevel) {

    val animatedProgress = androidx.compose.animation.core.animateFloatAsState(
        targetValue = riskScore / 100f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 900,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "risk_animation"
    )

    val accentColor = when (level) {
        RiskLevel.STABLE -> Color(0xFF66BB6A)
        RiskLevel.CAUTION -> Color(0xFFFFA726)
        RiskLevel.HIGH -> Color(0xFFEF5350)
    }

    GlassCard(opacity = 0.30f) {

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "Relapse Risk",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(140.dp)
            ) {

                val strokeWidth = 14.dp.toPx()
                val diameter = size.minDimension
                val radius = diameter / 2

                // Background circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = radius,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth)
                )

                // Progress arc
                drawArc(
                    color = accentColor,
                    startAngle = -90f,
                    sweepAngle = 360 * animatedProgress.value,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "$riskScore%",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )

            Text(
                when (level) {
                    RiskLevel.STABLE -> "Stable — Keep Going"
                    RiskLevel.CAUTION -> "Stay Focused"
                    RiskLevel.HIGH -> "High Risk — Be Alert"
                },
                fontSize = 13.sp,
                color = accentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}