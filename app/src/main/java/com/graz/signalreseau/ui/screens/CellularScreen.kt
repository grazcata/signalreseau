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
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.graz.signalreseau.model.CellularSignalInfo
import com.graz.signalreseau.ui.components.InfoCard
import com.graz.signalreseau.ui.components.InfoRow
import com.graz.signalreseau.ui.components.SignalGauge
import com.graz.signalreseau.ui.theme.SignalCyan
import com.graz.signalreseau.ui.theme.SignalGreen
import com.graz.signalreseau.ui.theme.SignalPurple
import com.graz.signalreseau.ui.theme.SignalYellow

@Composable
fun CellularScreen(
    cellularInfo: CellularSignalInfo,
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
        // Main Live Cellular Meter
        SignalGauge(
            dbm = cellularInfo.dbm,
            percentage = cellularInfo.signalPercentage,
            qualityLabel = cellularInfo.qualityLabel,
            qualityColorHex = cellularInfo.qualityColor,
            unitLabel = "dBm (Cellulaire)"
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Operator & Network Technology
        InfoCard(title = "Opérateur Mobile") {
            InfoRow(
                label = "Opérateur",
                value = cellularInfo.operatorName,
                icon = Icons.Default.CellTower,
                iconTint = SignalPurple
            )
            InfoRow(
                label = "Type de réseau",
                value = cellularInfo.networkType,
                icon = Icons.Default.NetworkCell,
                iconTint = SignalCyan
            )
            InfoRow(
                label = "Niveau de signal",
                value = "${cellularInfo.level} / 4 barres",
                icon = Icons.Default.Info,
                iconTint = SignalGreen
            )
        }

        // SIM & Roaming
        InfoCard(title = "Ligne & Itinérance") {
            InfoRow(
                label = "État de la SIM",
                value = cellularInfo.simState,
                icon = Icons.Default.SimCard,
                iconTint = SignalYellow
            )
            InfoRow(
                label = "Itinérance (Roaming)",
                value = if (cellularInfo.isRoaming) "Activée" else "Désactivée (National)",
                icon = Icons.Default.Public,
                iconTint = if (cellularInfo.isRoaming) SignalYellow else SignalGreen
            )
            InfoRow(
                label = "Identifiant Cellule",
                value = cellularInfo.cellDetails,
                icon = Icons.Default.Info,
                iconTint = SignalCyan
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
