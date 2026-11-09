package com.graz.signalreseau.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graz.signalreseau.model.CellularSignalInfo
import com.graz.signalreseau.model.WifiSignalInfo
import com.graz.signalreseau.ui.components.InfoCard
import com.graz.signalreseau.ui.theme.DarkOutline
import com.graz.signalreseau.ui.theme.DarkSurface
import com.graz.signalreseau.ui.theme.SignalCyan
import com.graz.signalreseau.ui.theme.SignalGreen
import com.graz.signalreseau.ui.theme.SignalOrange
import com.graz.signalreseau.ui.theme.SignalPurple
import com.graz.signalreseau.ui.theme.SignalRed
import com.graz.signalreseau.ui.theme.SignalYellow
import com.graz.signalreseau.ui.theme.TextMuted
import com.graz.signalreseau.ui.theme.TextPrimary
import com.graz.signalreseau.ui.theme.TextSecondary

enum class FinderSource {
    WIFI, CELLULAR
}

@Composable
fun SignalFinderScreen(
    wifiInfo: WifiSignalInfo,
    cellularInfo: CellularSignalInfo,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSource by remember { mutableStateOf(FinderSource.WIFI) }
    var hapticFeedbackEnabled by remember { mutableStateOf(false) }

    val history = remember { mutableStateListOf<Int>() }
    val maxHistorySize = 30

    val currentDbm = if (selectedSource == FinderSource.WIFI) {
        wifiInfo.rssi
    } else {
        cellularInfo.dbm
    }

    // Update history when dBm changes
    LaunchedEffect(currentDbm, selectedSource) {
        if (currentDbm > -135) {
            history.add(currentDbm)
            if (history.size > maxHistorySize) {
                history.removeAt(0)
            }

            // Haptic trigger on strong signal if enabled
            if (hapticFeedbackEnabled && currentDbm >= -65) {
                try {
                    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                        vm?.defaultVibrator
                    } else {
                        @Suppress("DEPRECATION")
                        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(50)
                    }
                } catch (_: Exception) {}
            }
        }
    }

    val validSamples = history.filter { it > -135 }
    val maxDbm = validSamples.maxOrNull() ?: currentDbm
    val minDbm = validSamples.minOrNull() ?: currentDbm
    val avgDbm = if (validSamples.isNotEmpty()) validSamples.average().toInt() else currentDbm

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Source Selector (Wi-Fi vs Cellular)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, shape = RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    selectedSource = FinderSource.WIFI
                    history.clear()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSource == FinderSource.WIFI) SignalGreen else Color.Transparent,
                    contentColor = if (selectedSource == FinderSource.WIFI) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Wi-Fi", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    selectedSource = FinderSource.CELLULAR
                    history.clear()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSource == FinderSource.CELLULAR) SignalPurple else Color.Transparent,
                    contentColor = if (selectedSource == FinderSource.CELLULAR) Color.White else TextSecondary
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mobile 4G/5G", fontWeight = FontWeight.Bold)
            }
        }

        // Live History Graph
        InfoCard(title = "Historique en Temps Réel (${history.size} échantillons)") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFF0D1424), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                if (history.isEmpty()) {
                    Text(
                        text = "En attente de mesures...",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Grid lines for -50, -70, -90, -110 dBm
                        val gridDbm = listOf(-50, -70, -90, -110)
                        for (dbmLine in gridDbm) {
                            val fraction = 1f - ((dbmLine - (-120f)) / 90f).coerceIn(0f, 1f)
                            val y = fraction * canvasHeight
                            drawLine(
                                color = DarkOutline.copy(alpha = 0.4f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val barWidth = (canvasWidth / maxHistorySize.toFloat()) * 0.7f
                        val slotWidth = canvasWidth / maxHistorySize.toFloat()

                        history.forEachIndexed { index, sampleDbm ->
                            val x = index * slotWidth + (slotWidth - barWidth) / 2f
                            val normalized = ((sampleDbm - (-120f)) / 90f).coerceIn(0.05f, 1f)
                            val barHeight = normalized * canvasHeight
                            val y = canvasHeight - barHeight

                            val barColor = when {
                                sampleDbm >= -55 -> SignalCyan
                                sampleDbm >= -68 -> SignalGreen
                                sampleDbm >= -80 -> SignalYellow
                                sampleDbm >= -95 -> SignalOrange
                                else -> SignalRed
                            }

                            drawLine(
                                color = barColor,
                                start = Offset(x + barWidth / 2f, canvasHeight),
                                end = Offset(x + barWidth / 2f, y),
                                strokeWidth = barWidth,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }

        // Stats Summary: Max, Min, Moyenne
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatMetricBox(
                label = "MAXIMUM",
                value = if (maxDbm > -130) "$maxDbm" else "--",
                unit = "dBm",
                color = SignalGreen,
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                label = "MOYENNE",
                value = if (avgDbm > -130) "$avgDbm" else "--",
                unit = "dBm",
                color = SignalCyan,
                modifier = Modifier.weight(1f)
            )
            StatMetricBox(
                label = "MINIMUM",
                value = if (minDbm > -130) "$minDbm" else "--",
                unit = "dBm",
                color = SignalOrange,
                modifier = Modifier.weight(1f)
            )
        }

        // Haptic Toggle & Reset Card
        InfoCard(title = "Options de Détection") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Vibration,
                        contentDescription = null,
                        tint = if (hapticFeedbackEnabled) SignalGreen else TextMuted
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Vibration sur zone optimale", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Signale les zones à fort signal (≥ -65 dBm)", fontSize = 11.sp, color = TextMuted)
                    }
                }
                Switch(
                    checked = hapticFeedbackEnabled,
                    onCheckedChange = { hapticFeedbackEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignalGreen,
                        checkedTrackColor = SignalGreen.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { history.clear() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réinitialiser le relevé de parcours")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatMetricBox(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(DarkSurface, shape = RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
        Text(text = unit, fontSize = 11.sp, color = TextMuted)
    }
}
