package com.graz.signalreseau.model

data class WifiSignalInfo(
    val isConnected: Boolean = false,
    val isWifiEnabled: Boolean = false,
    val ssid: String = "--",
    val bssid: String = "--",
    val rssi: Int = -127,
    val signalPercentage: Int = 0,
    val level: Int = 0,
    val qualityLabel: String = "Déconnecté",
    val qualityColor: Long = 0xFF9E9E9E,
    val frequencyMHz: Int = 0,
    val channel: Int = 0,
    val bandLabel: String = "--",
    val linkSpeedMbps: Int = 0,
    val ipAddress: String = "--",
    val gateway: String = "--",
    val wifiStandard: String = "--"
) {
    companion object {
        fun calculatePercentage(rssi: Int): Int {
            if (rssi <= -100) return 0
            if (rssi >= -30) return 100
            return ((rssi - (-100)) * 100 / 70).coerceIn(0, 100)
        }

        fun getQualityLabel(rssi: Int): Pair<String, Long> {
            return when {
                rssi >= -50 -> Pair("Excellent", 0xFF00E676)
                rssi >= -60 -> Pair("Très bon", 0xFF69F0AE)
                rssi >= -70 -> Pair("Bon", 0xFFFFD600)
                rssi >= -80 -> Pair("Faible", 0xFFFF9100)
                rssi > -100 -> Pair("Très faible", 0xFFFF5252)
                else -> Pair("Aucun signal", 0xFF9E9E9E)
            }
        }

        fun frequencyToChannel(freqMHz: Int): Int {
            return when {
                freqMHz == 2484 -> 14
                freqMHz in 2412..2472 -> (freqMHz - 2407) / 5
                freqMHz in 5170..5825 -> (freqMHz - 5000) / 5
                freqMHz in 5945..7105 -> (freqMHz - 5950) / 5
                else -> 0
            }
        }

        fun frequencyToBand(freqMHz: Int): String {
            return when {
                freqMHz in 2400..2500 -> "2.4 GHz"
                freqMHz in 4900..5900 -> "5 GHz"
                freqMHz in 5925..7125 -> "6 GHz (Wi-Fi 6E)"
                else -> if (freqMHz > 0) "${freqMHz} MHz" else "--"
            }
        }
    }
}
