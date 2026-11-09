package com.graz.signalreseau.model

data class CellularSignalInfo(
    val isAvailable: Boolean = false,
    val operatorName: String = "--",
    val networkType: String = "--",
    val dbm: Int = -140,
    val level: Int = 0,
    val signalPercentage: Int = 0,
    val qualityLabel: String = "Aucun service",
    val qualityColor: Long = 0xFF9E9E9E,
    val simState: String = "Inconnue",
    val isRoaming: Boolean = false,
    val cellDetails: String = "--"
) {
    companion object {
        fun calculatePercentage(dbm: Int, networkType: String): Int {
            val minDbm = if (networkType.contains("5G") || networkType.contains("4G")) -130 else -115
            val maxDbm = if (networkType.contains("5G") || networkType.contains("4G")) -65 else -60
            if (dbm <= minDbm) return 0
            if (dbm >= maxDbm) return 100
            val range = maxDbm - minDbm
            return ((dbm - minDbm) * 100 / range).coerceIn(0, 100)
        }

        fun getQualityLabel(dbm: Int): Pair<String, Long> {
            return when {
                dbm >= -80 -> Pair("Excellent", 0xFF00E676)
                dbm >= -95 -> Pair("Très bon", 0xFF69F0AE)
                dbm >= -105 -> Pair("Moyen", 0xFFFFD600)
                dbm >= -115 -> Pair("Faible", 0xFFFF9100)
                dbm > -140 -> Pair("Critique", 0xFFFF5252)
                else -> Pair("Hors portée", 0xFF9E9E9E)
            }
        }
    }
}
