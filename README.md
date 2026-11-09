# Signal Réseau (Network & Wi-Fi Signal Meter) 📶

Application Android native développée en **Kotlin** et **Jetpack Compose / Material 3**, conçue pour mesurer, analyser et optimiser en temps réel la force du signal Wi-Fi et des réseaux mobiles (5G / 4G / 3G).

---

## ✨ Fonctionnalités

### 1. Mesureur Wi-Fi Haute Précision
* **Jauge dynamique en temps réel** : puissance RSSI en dBm et conversion précise en pourcentage (0 à 100%).
* **Statut de qualité** : étiquette d'état instantanée (*Excellent, Très bon, Bon, Faible, Très faible*).
* **Informations détaillées du réseau** :
  * SSID (Nom du réseau) et BSSID (Adresse MAC de la borne).
  * Fréquence & détection automatique du canal (2.4 GHz, 5 GHz, 6 GHz Wi-Fi 6E).
  * Vitesse de liaison négociée (*Link Speed* en Mbps).
  * Norme Wi-Fi active (Wi-Fi 6 802.11ax, Wi-Fi 5 802.11ac, etc.).
  * Configuration IP locale & Passerelle routeur.

### 2. Mesureur Réseau Mobile (4G LTE / 5G NR)
* **Signal cellulaire en dBm** et niveau système (0 à 4 barres).
* **Identification de l'opérateur** et de la technologie active (*5G NR, 4G LTE, 3G HSPA, 2G*).
* **Surveillance SIM & Roaming** : état de la carte SIM et détection d'itinérance.
* **Détails techniques de l'antenne** : Cell ID (CID), Physical Cell ID (PCI).

### 3. Mode Détecteur & Exploration ("Sweet Spot Finder")
* **Historique glissant en direct** : graphique à barres des 30 derniers échantillons.
* **Métriques statistiques du parcours** : calcul en temps réel du **Maximum**, **Minimum** et de la **Moyenne** du signal.
* **Retour haptique (Vibration)** : notification tactile lors du passage dans une zone de réception optimale (≥ -65 dBm).
* **Bouton de réinitialisation rapide** pour comparer différentes pièces ou emplacements.

---

## 🛠 Spécifications Techniques

* **Langage** : Kotlin (Kotlin DSL `.gradle.kts`)
* **Interface** : Jetpack Compose + Material 3 (Thème sombre natif)
* **Architecture** : Clean architecture découplée, Flow réactif, State Hoisting
* **Target SDK** : Android 14 (API 34)
* **Min SDK** : Android 8.0 Oreo (API 26)

---

## 🚀 Installation

Téléchargez directement le fichier APK signé depuis la section [Releases](https://github.com/grazcata/signalreseau/releases) du dépôt GitHub.
