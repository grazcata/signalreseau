package com.graz.signalreseau.monitor

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.graz.signalreseau.model.CellularSignalInfo
import com.graz.signalreseau.model.WifiSignalInfo
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

class SignalMonitor(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val telephonyManager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
    private val connectivityManager = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun getWifiInfo(): WifiSignalInfo {
        if (wifiManager == null) return WifiSignalInfo()

        val isEnabled = try {
            wifiManager.isWifiEnabled
        } catch (e: Exception) {
            false
        }

        if (!isEnabled) {
            return WifiSignalInfo(
                isConnected = false,
                isWifiEnabled = false,
                qualityLabel = "Wi-Fi désactivé",
                qualityColor = 0xFF78909C
            )
        }

        val connInfo = try {
            @Suppress("DEPRECATION")
            wifiManager.connectionInfo
        } catch (e: Exception) {
            null
        }

        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val hasWifiTransport = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (connInfo == null || !hasWifiTransport || connInfo.networkId == -1) {
            return WifiSignalInfo(
                isConnected = false,
                isWifiEnabled = true,
                qualityLabel = "Non connecté",
                qualityColor = 0xFF9E9E9E
            )
        }

        var ssid = connInfo.ssid ?: "--"
        if (ssid.startsWith("\"") && ssid.endsWith("\"") && ssid.length >= 2) {
            ssid = ssid.substring(1, ssid.length - 1)
        }
        if (ssid == "<unknown ssid>" || ssid.isBlank()) {
            ssid = "Réseau Wi-Fi (Masqué/Sans nom)"
        }

        val bssid = connInfo.bssid ?: "--"
        val rssi = connInfo.rssi
        val percentage = WifiSignalInfo.calculatePercentage(rssi)
        val (label, color) = WifiSignalInfo.getQualityLabel(rssi)
        val freq = connInfo.frequency
        val channel = WifiSignalInfo.frequencyToChannel(freq)
        val band = WifiSignalInfo.frequencyToBand(freq)
        val linkSpeed = connInfo.linkSpeed

        val wifiStandardStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            when (connInfo.wifiStandard) {
                ScanResult.WIFI_STANDARD_11AX -> "Wi-Fi 6 (802.11ax)"
                ScanResult.WIFI_STANDARD_11AC -> "Wi-Fi 5 (802.11ac)"
                ScanResult.WIFI_STANDARD_11N -> "Wi-Fi 4 (802.11n)"
                ScanResult.WIFI_STANDARD_LEGACY -> "Wi-Fi Hérité (802.11a/b/g)"
                else -> "Standard Wi-Fi"
            }
        } else {
            if (freq > 4900) "Wi-Fi 5 GHz" else "Wi-Fi 2.4 GHz"
        }

        // IP Address & Gateway
        var ipStr = "--"
        var gatewayStr = "--"
        try {
            @Suppress("DEPRECATION")
            val ipInt = connInfo.ipAddress
            if (ipInt != 0) {
                val bytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()
                ipStr = InetAddress.getByAddress(bytes).hostAddress ?: "--"
            }

            @Suppress("DEPRECATION")
            val dhcp = wifiManager.dhcpInfo
            if (dhcp != null && dhcp.gateway != 0) {
                val gwBytes = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(dhcp.gateway).array()
                gatewayStr = InetAddress.getByAddress(gwBytes).hostAddress ?: "--"
            }
        } catch (_: Exception) {}

        @Suppress("DEPRECATION")
        val level = WifiManager.calculateSignalLevel(rssi, 5)

        return WifiSignalInfo(
            isConnected = true,
            isWifiEnabled = true,
            ssid = ssid,
            bssid = bssid,
            rssi = rssi,
            signalPercentage = percentage,
            level = level,
            qualityLabel = label,
            qualityColor = color,
            frequencyMHz = freq,
            channel = channel,
            bandLabel = band,
            linkSpeedMbps = linkSpeed,
            ipAddress = ipStr,
            gateway = gatewayStr,
            wifiStandard = wifiStandardStr
        )
    }

    fun getCellularInfo(): CellularSignalInfo {
        if (telephonyManager == null) return CellularSignalInfo()

        val simStateStr = when (telephonyManager.simState) {
            TelephonyManager.SIM_STATE_READY -> "Prête"
            TelephonyManager.SIM_STATE_ABSENT -> "Absente"
            TelephonyManager.SIM_STATE_PIN_REQUIRED -> "Code PIN requis"
            TelephonyManager.SIM_STATE_PUK_REQUIRED -> "Code PUK requis"
            TelephonyManager.SIM_STATE_NETWORK_LOCKED -> "Bloquée réseau"
            else -> "Non disponible"
        }

        if (telephonyManager.simState != TelephonyManager.SIM_STATE_READY) {
            return CellularSignalInfo(
                isAvailable = false,
                simState = simStateStr,
                qualityLabel = "Carte SIM inactive",
                qualityColor = 0xFF78909C
            )
        }

        var operatorName = telephonyManager.networkOperatorName
        if (operatorName.isNullOrBlank()) {
            operatorName = telephonyManager.simOperatorName
        }
        if (operatorName.isNullOrBlank()) {
            operatorName = "Réseau Mobile"
        }

        val networkType = resolveNetworkType()
        val isRoaming = telephonyManager.isNetworkRoaming

        var detectedDbm = -140
        var detectedLevel = 0
        var cellDetails = "--"

        if (hasLocationPermission()) {
            try {
                val cellInfos = telephonyManager.allCellInfo
                if (!cellInfos.isNullOrEmpty()) {
                    for (cell in cellInfos) {
                        if (cell.isRegistered) {
                            when (cell) {
                                is CellInfoLte -> {
                                    detectedDbm = cell.cellSignalStrength.dbm
                                    detectedLevel = cell.cellSignalStrength.level
                                    val ci = cell.cellIdentity.ci
                                    val pci = cell.cellIdentity.pci
                                    cellDetails = "LTE CID: $ci, PCI: $pci"
                                    break
                                }
                                is CellInfoWcdma -> {
                                    detectedDbm = cell.cellSignalStrength.dbm
                                    detectedLevel = cell.cellSignalStrength.level
                                    cellDetails = "3G/UMTS CID: ${cell.cellIdentity.cid}"
                                    break
                                }
                                is CellInfoGsm -> {
                                    detectedDbm = cell.cellSignalStrength.dbm
                                    detectedLevel = cell.cellSignalStrength.level
                                    cellDetails = "GSM CID: ${cell.cellIdentity.cid}"
                                    break
                                }
                                else -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && cell is android.telephony.CellInfoNr) {
                                        val ss = cell.cellSignalStrength as? android.telephony.CellSignalStrengthNr
                                        detectedDbm = ss?.dbm ?: -140
                                        detectedLevel = ss?.level ?: 0
                                        cellDetails = "5G NR New Radio"
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: SecurityException) {
            } catch (_: Exception) {}
        }

        // Fallback via SignalStrength if available
        if (detectedDbm <= -140) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val ss = telephonyManager.signalStrength
                    if (ss != null) {
                        detectedLevel = ss.level
                        // Approximation from level 0-4
                        detectedDbm = when (detectedLevel) {
                            4 -> -75
                            3 -> -88
                            2 -> -100
                            1 -> -112
                            else -> -125
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val percentage = CellularSignalInfo.calculatePercentage(detectedDbm, networkType)
        val (label, color) = CellularSignalInfo.getQualityLabel(detectedDbm)

        return CellularSignalInfo(
            isAvailable = true,
            operatorName = operatorName,
            networkType = networkType,
            dbm = detectedDbm,
            level = detectedLevel,
            signalPercentage = percentage,
            qualityLabel = label,
            qualityColor = color,
            simState = simStateStr,
            isRoaming = isRoaming,
            cellDetails = cellDetails
        )
    }

    private fun resolveNetworkType(): String {
        if (telephonyManager == null) return "Inconnu"
        return try {
            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                telephonyManager.dataNetworkType
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.networkType
            }

            when (type) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G (NR)"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G (LTE)"
                TelephonyManager.NETWORK_TYPE_HSPAP,
                TelephonyManager.NETWORK_TYPE_HSPA,
                TelephonyManager.NETWORK_TYPE_HSDPA,
                TelephonyManager.NETWORK_TYPE_HSUPA,
                TelephonyManager.NETWORK_TYPE_UMTS -> "3G (HSPA/UMTS)"
                TelephonyManager.NETWORK_TYPE_EDGE,
                TelephonyManager.NETWORK_TYPE_GPRS -> "2G (EDGE/GPRS)"
                else -> "Cellulaire"
            }
        } catch (_: Exception) {
            "Cellulaire"
        }
    }
}
