package com.graz.signalreseau.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiChannel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graz.signalreseau.model.WifiSignalInfo
import com.graz.signalreseau.ui.components.InfoCard
import com.graz.signalreseau.ui.components.InfoRow
import com.graz.signalreseau.ui.components.SignalGauge
import com.graz.signalreseau.ui.theme.SignalCyan
import com.graz.signalreseau.ui.theme.SignalGreen
import com.graz.signalreseau.ui.theme.SignalYellow

@Composable
fun WifiScreen(
    wifiInfo: WifiSignalInfo,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Live Meter
        SignalGauge(
            dbm = wifiInfo.rssi,
            percentage = wifiInfo.signalPercentage,
            qualityLabel = wifiInfo.qualityLabel,
            qualityColorHex = wifiInfo.qualityColor,
            unitLabel = "dBm (Wi-Fi)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Identification Card
        InfoCard(title = "Point d'Accès") {
            InfoRow(
                label = "SSID (Nom)",
                value = wifiInfo.ssid,
                icon = Icons.Default.Wifi,
                iconTint = SignalGreen
            )
            InfoRow(
                label = "BSSID (MAC)",
                value = wifiInfo.bssid,
                icon = Icons.Default.Router,
                iconTint = SignalCyan
            )
            InfoRow(
                label = "Standard",
                value = wifiInfo.wifiStandard,
                icon = Icons.Default.Info,
                iconTint = SignalYellow
            )
        }

        // Frequencies & Speed Card
        InfoCard(title = "Performances & Canal") {
            InfoRow(
                label = "Bande de fréquence",
                value = wifiInfo.bandLabel,
                icon = Icons.Default.WifiChannel,
                iconTint = SignalCyan
            )
            InfoRow(
                label = "Canal Wi-Fi",
                value = if (wifiInfo.channel > 0) "Canal ${wifiInfo.channel} (${wifiInfo.frequencyMHz} MHz)" else "--",
                icon = Icons.Default.Numbers,
                iconTint = SignalYellow
            )
            InfoRow(
                label = "Vitesse de liaison",
                value = if (wifiInfo.linkSpeedMbps > 0) "${wifiInfo.linkSpeedMbps} Mbps" else "--",
                icon = Icons.Default.Speed,
                iconTint = SignalGreen
            )
        }

        // IP Network Details
        InfoCard(title = "Configuration Réseau") {
            InfoRow(
                label = "Adresse IP",
                value = wifiInfo.ipAddress,
                icon = Icons.Default.Tag,
                iconTint = SignalCyan
            )
            InfoRow(
                label = "Passerelle (Routeur)",
                value = wifiInfo.gateway,
                icon = Icons.Default.CellTower,
                iconTint = SignalGreen
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
