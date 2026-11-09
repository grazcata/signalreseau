package com.graz.signalreseau

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.graz.signalreseau.model.CellularSignalInfo
import com.graz.signalreseau.model.WifiSignalInfo
import com.graz.signalreseau.monitor.SignalMonitor
import com.graz.signalreseau.ui.screens.CellularScreen
import com.graz.signalreseau.ui.screens.SignalFinderScreen
import com.graz.signalreseau.ui.screens.WifiScreen
import com.graz.signalreseau.ui.theme.DarkBackground
import com.graz.signalreseau.ui.theme.DarkSurface
import com.graz.signalreseau.ui.theme.SignalCyan
import com.graz.signalreseau.ui.theme.SignalGreen
import com.graz.signalreseau.ui.theme.SignalReseauTheme
import com.graz.signalreseau.ui.theme.SignalYellow
import com.graz.signalreseau.ui.theme.TextMuted
import com.graz.signalreseau.ui.theme.TextPrimary
import com.graz.signalreseau.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SignalReseauTheme {
                MainContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent() {
    val context = LocalContext.current
    val monitor = remember { SignalMonitor(context) }

    var selectedTab by remember { mutableIntStateOf(0) }
    var wifiInfo by remember { mutableStateOf(WifiSignalInfo()) }
    var cellularInfo by remember { mutableStateOf(CellularSignalInfo()) }
    var hasPermission by remember { mutableStateOf(monitor.hasLocationPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    // Auto-request location permissions once on launch
    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_PHONE_STATE
                )
            )
        }
    }

    // 1-second continuous monitoring loop
    LaunchedEffect(Unit) {
        while (isActive) {
            wifiInfo = monitor.getWifiInfo()
            cellularInfo = monitor.getCellularInfo()
            delay(1000)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(SignalGreen, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Signal Réseau",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Wifi, contentDescription = "Wi-Fi") },
                    label = { Text("Wi-Fi") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SignalGreen,
                        selectedIconColor = Color.Black,
                        selectedTextColor = SignalGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CellTower, contentDescription = "Cellulaire") },
                    label = { Text("Mobile") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SignalCyan,
                        selectedIconColor = Color.Black,
                        selectedTextColor = SignalCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Détecteur") },
                    label = { Text("Détecteur") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = SignalGreen,
                        selectedIconColor = Color.Black,
                        selectedTextColor = SignalGreen,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Permission request banner if missing
            AnimatedVisibility(visible = !hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .background(DarkSurface, shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = SignalYellow,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Localisation requise par Android",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Nécessaire pour mesurer le signal Wi-Fi et les antennes 4G/5G.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SignalYellow, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Activer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tab navigation switch
            when (selectedTab) {
                0 -> WifiScreen(wifiInfo = wifiInfo)
                1 -> CellularScreen(cellularInfo = cellularInfo)
                2 -> SignalFinderScreen(wifiInfo = wifiInfo, cellularInfo = cellularInfo)
            }
        }
    }
}
