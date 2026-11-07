<div align="center">

# CarPlay Maps VC for MHI2: MU1427 / AID 795B

**Apple CarPlay maps and native route guidance on the Volkswagen Active Info Display (12.3"), tailored specifically for MHI2 firmware MU1427.**

[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-MHI2%20%2F%20QNX%206.5-2f6f9f?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Firmware](https://img.shields.io/badge/firmware-MU1427%20(K3342)-green?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Cluster](https://img.shields.io/badge/cluster-791%20%2F%20795B%20AID%2012.3-green?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Release](https://img.shields.io/badge/release-v1.2.1--experimental-orange?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B/releases/tag/v1.2.1-experimental)

[Features](#features) • [Installation](#installation) • [Display & Tuning](#display--tuning-options) • [Recovery](#uninstallation--recovery) • [Credits](#credits--acknowledgments)

</div>

---

> [!WARNING]
> **Use entirely at your own risk.** This modification touches internal runtime scripts and configuration on your vehicle's infotainment unit. Ensure you have a stable power supply (battery charger or engine idling) before installation. Neither the authors nor contributors are responsible for any damage or malfunctions.

---

## Overview

This repository provides an optimized distribution of **CarPlay Maps VC**, **Native Route Guidance**, and **Now Playing Audio Metadata** for Volkswagen MIB2 High / MIB2.5 infotainment systems running **MU1427** (`MHI2_ER_VWG11_K3342`), specifically tuned for the **12.3" Active Info Display (AID 791 / 795B)** virtual cockpit.

> [!NOTE]
> **v1.2.1-experimental:**
> - **Seamless Reroute Holding:** Eliminates the route-initialization loading circle animation when Google Maps or Apple Maps recalculates/reroutes mid-journey. While calculating the new route, route guidance remains active with a straight placeholder arrow, preserving remaining distance and ETA without interruption.
> - **Now Playing Audio Text:** Displays native Track Title, Artist, and Album in the Virtual Cockpit and HUD during CarPlay playback. Built upon the vehicle-verified v1.1.0 native route guidance foundation.

It routes the secondary CarPlay map video feed (Apple Maps, Google Maps, Waze) to the center of the digital cluster and translates CarPlay navigation events into native Volkswagen instrument cluster graphics (arrows, distances, and route totals on the AID and HUD).

---

## Features

- **Seamless Reroute Holding:** Keeps route guidance continuously active with straight placeholder arrows during mid-journey recalculations, preventing the cluster from dropping to idle and restarting the route loading circle animation.
- **Smooth Cluster Map Feed:** 30 FPS target with forced IDR keyframes every 5 frames, eliminating map freezes and video lag on the virtual cockpit.
- **AID 795B Safe Area Fix:** Custom safe area height of 340 px ($y=96$, bottom edge at 436 px) preventing phone cards and UI elements from clipping into the AID's bottom clock, remaining distance, and travel time bar.
- **Steering Wheel Zoom Neutralized:** Neutralized accidental scroll-wheel map zoom commands that cause cluster video freezes and crashes during normal instrument cluster menu navigation.
- **MOST Bus Recovery:** Intelligent frame queue flushing on the fiber-optic bus to quickly recover from bus congestion during rapid UI or cluster view switching.
- **Silenced `/ramdisk` Logging:** Prevents rapid exhaustion of the volatile head-unit memory and random reboots during longer journeys.
- **Native Turn-by-Turn Arrows:** Injects maneuver turn arrows and distances directly into the speedometer dials and Head-Up Display (HUD) via native BAP protocols.
- **Native Lane Guidance Arrows:** Injects live lane recommendations (`[ ↑ | ↑ | ↱ ]`) directly into the digital cluster display.
- **Top-Bar Street / Road Names:** Transmits live current road and street names into the top status bar of the instrument cluster via native BAP `TurnToInfo`.
- **Native Now Playing Track Text:** Displays real-time Track Title, Artist, and Album in the Virtual Cockpit Audio/Media screen and HUD via native BAP `CurrentStationInfo` when playing music via CarPlay (Spotify, Apple Music, etc.).
- **Native Route Totals:** Displays remaining travel duration and distance to destination natively across the cluster.
- **MU1427 AES Encryption Fix:** Resolves the AirPlay 210.81 session key offset mismatch (`session + 0x1c0`) specific to MU1427.
- **Clean MQB Toolbox Integration:** Fully integrated into the Green Engineering Menu (GEM) under `Customization -> CarPlay Maps VC`.

---

## Compatibility

| Component | Target Specification |
| :--- | :--- |
| **Infotainment Unit** | Harman MIB2 High / MIB2.5 (`MHI2`) |
| **Firmware Train** | `MHI2_ER_VWG11_K3342` (**MU1427**) |
| **Factory JXE File** | `/ifs/lsd.jxe` = exactly **52,751,492 bytes** |
| **Virtual Cockpit** | Active Info Display 12.3" (**AID 791 / 795B**) |
| **Prerequisites** | [MQB Toolbox](https://github.com/jilleb/mqb-pm) installed with Green Engineering Menu enabled |

---

## Navigation App Support

| App | Cluster Map Feed | Turn Arrows & Distance | Lane Guidance & Street Names | Notes / Real-World Status |
| :--- | :---: | :---: | :---: | :--- |
| **Google Maps** ⭐ | ✅ | ✅ | ✅ | **100% Working & Verified.** Turn arrows, countdown distance, route totals, U-turns, and road names operate flawlessly. |
| **Apple Maps** | ✅ | ⚠️ | ✅ / ⚠️ | **Partial Support.** The top bar works completely: current street name, remaining distance, and ETA all display normally. Only the maneuver turn arrow in the speedometer dial defaults to a straight arrow due to iOS source-name flipping mid-route. |
| **Waze** | ✅ | ❌ | ❌ | **iOS App Limitation.** Waze has not implemented the CarPlay cluster navigation protocol (`iAP2 0x5201/0x5202/0x5204`). |

---

## Installation

### 1. Preparation
1. Download the latest release: [`CarPlay_Maps_VC_MU1427_795B_AID_v1.2.1-experimental.zip`](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B/releases/tag/v1.2.1-experimental) *(or the stable [`v1.1.0 release`](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B/releases/tag/v1.1.0))*.
2. Format an SD card (32 GB or smaller) to **FAT32**.
3. Extract the contents of the ZIP directly to the root of the SD card.

### 2. Vehicle Installation

> [!IMPORTANT]
> **Installation Order (Required for Fresh Installs):**
> Step 4 (`Install 791/795B AID 12.3 (AUTO MU)`) **must** be installed before Step 5 (`Install full native guidance`). The base video installer sets up the core runtime and unit profile (`MU1427/791`) that the native guidance script requires. Attempting to install native guidance first on an unpatched unit will halt with `CPMAP_GUIDANCE_FAIL=INSTALL_791_AUTO_MU_FIRST`.
> *(Existing users already running CarPlay Maps VC can skip Step 4 and proceed directly to Step 5).*

1. Insert the SD card into slot 1 of your head unit.
2. Ensure your phone is **disconnected** from USB.
3. Open the **Green Engineering Menu (GEM)**:
   - On the main MQB Toolbox screen, tap **`Update Toolbox`** to copy the latest scripts into the head unit.
   - Re-open GEM and navigate to:
     $$\text{MQB Toolbox} \rightarrow \text{Customization} \rightarrow \text{CarPlay Maps VC}$$
4. **Base Profile:** Select **`Install 791/795B AID 12.3 (AUTO MU)`**.
5. **Native Guidance:** Select **`Install full native guidance (MU1427 only)`**.
6. **Now Playing Text (Optional):** Select **`Install Now Playing text (MU1427 only)`** (also available under the dedicated `CarPlay Now Playing Audio` submenu) to show live track title, artist, and album in the cluster dials and HUD.
7. **Fully reboot the unit:** Press and hold the volume/power knob for **10–15 seconds** until the screen restarts.

### 3. Verification
- Start the car and let the factory virtual cockpit map initialize.
- Connect your iPhone via USB and launch CarPlay.
- When navigation is started on Apple Maps or Google Maps, the map stream will project in the virtual cockpit, and native turn arrows will populate your gauges and HUD.

---

## Display & Tuning Options

Out of the box, `Install 791/795B AID 12.3 (AUTO MU)` defaults to **Map-Only (No Card)** for an unobstructed 100% full-screen map, since native guidance already injects all turn arrows, distances, and street names natively into the dials and HUD.

If you prefer having the CarPlay maneuver turn-box floating on the map screen, you can switch tuning options anytime without reinstalling. In GEM, navigate to:
$$\text{MQB Toolbox} \rightarrow \text{Customization} \rightarrow \text{CarPlay Maps VC} \rightarrow \text{Tuning}$$

| Tuning Option | Description |
| :--- | :--- |
| **Map-Only (No Card)** ⭐ *(Default)* | Completely disables the phone turn card box (`.../map` URL), giving you a **100% unobstructed full-screen map** while the car displays all arrows, distances, and road names natively in the cluster. |
| **Narrow Card** | Displays a 400 px narrow maneuver turn card box positioned cleanly between top and bottom status bars. |
| **Wide Card** | Displays a 480 px wider maneuver turn card box for higher-visibility route cues. |

> [!NOTE]
> All three tuning options share the exact same 340 px safe area height tuned for the 795B cluster. After changing a tuning option, reboot the head unit (hold power knob for 10–15 seconds).

---

## Uninstallation & Recovery

To remove native guidance, now playing text, or restore the factory system:
- **Remove Now Playing Text:** In GEM, select `Remove Now Playing text (reboot)` to remove the audio metadata listener and restore stock BAP audio handling.
- **Temporary Disable Guidance:** In GEM, select `Disable native guidance now (until reboot)` to temporarily disable native guidance without touching installed files.
- **Remove Guidance:** Select `Remove native guidance (reboot)` to restore the factory iAP2 driver and configuration while keeping base map video streaming.
- **Full Factory Restore:** Select `Restore factory files` to remove all modifications and revert the head unit to stock.

Emergency removal can also be executed directly via QNX terminal without the SD card:
```sh
/bin/ksh /mnt/app/carplay-route-guidance/control.sh uninstall
```

---

## Credits & Acknowledgments

- **[omonob (QCDWJ)](https://github.com/omonob):** Creator of the original [MHI2-Carplay-Maps](https://github.com/omonob/MHI2-Carplay-Maps) project, video streaming pipeline, and steering wheel control hooks.
- **[tomasz-twardzicki](https://github.com/tomasz-twardzicki):** Development of the MU1427 port, AirPlay AES offset patch, BAP native guidance bridges, and AID 791 adaptation.
- **diasdio & ramasauskas:** MOST bridge queue stability research and keyframe handling.
- **[LuKa](https://github.com/luka-dev):** Reference research for iAP2 route guidance maneuver decoding on VAG platforms.
- **[jilleb](https://github.com/jilleb):** Creator of the MQB / MIB High Toolbox framework.

---

<div align="center">

Maintained by **[Bazsolf](https://github.com/Bazsolf)** • Licensed under **GPL-3.0**

</div>
