<div align="center">

# CarPlay Maps VC for MHI2: MU1427 / AID 795B

**Apple CarPlay maps and native route guidance on the Volkswagen Active Info Display (12.3"), tailored specifically for MHI2 firmware MU1427.**

[![License](https://img.shields.io/badge/license-GPL--3.0-blue?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-MHI2%20%2F%20QNX%206.5-2f6f9f?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Firmware](https://img.shields.io/badge/firmware-MU1427%20(K3342)-green?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Cluster](https://img.shields.io/badge/cluster-791%20%2F%20795B%20AID%2012.3-green?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B)
[![Release](https://img.shields.io/badge/release-v1.0.0-orange?style=flat-square)](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B/releases/tag/v1.0.0)

[Features](#features) • [Installation](#installation) • [Display & Tuning](#display--tuning-options) • [Recovery](#uninstallation--recovery) • [Credits](#credits--acknowledgments)

</div>

---

> [!WARNING]
> **Use entirely at your own risk.** This modification touches internal runtime scripts and configuration on your vehicle's infotainment unit. Ensure you have a stable power supply (battery charger or engine idling) before installation. Neither the authors nor contributors are responsible for any damage or malfunctions.

---

## Overview

This repository provides an optimized, stability-tested distribution of **CarPlay Maps VC** and **Native Route Guidance** for Volkswagen MIB2 High / MIB2.5 infotainment systems running **MU1427** (`MHI2_ER_VWG11_K3342`), specifically tuned for the **12.3" Active Info Display (AID 791 / 795B)** virtual cockpit.

It routes the secondary CarPlay map video feed (Apple Maps, Google Maps, Waze) to the center of the digital cluster and translates CarPlay navigation events into native Volkswagen instrument cluster graphics (arrows, distances, and route totals on the AID and HUD).

---

## Features

- **Smooth Cluster Map Feed:** 30 FPS target with forced IDR keyframes every 5 frames, eliminating map freezes and video lag on the virtual cockpit.
- **AID 795B Safe Area Fix:** Custom safe area height of 340 px ($y=96$, bottom edge at 436 px) preventing phone cards and UI elements from clipping into the AID's bottom clock, remaining distance, and travel time bar.
- **Steering Wheel Zoom Neutralized:** Neutralized accidental scroll-wheel map zoom commands that cause cluster video freezes and crashes during normal instrument cluster menu navigation.
- **MOST Bus Recovery:** Intelligent frame queue flushing on the fiber-optic bus to quickly recover from bus congestion during rapid UI or cluster view switching.
- **Silenced `/ramdisk` Logging:** Prevents rapid exhaustion of the volatile head-unit memory and random reboots during longer journeys.
- **Native Turn-by-Turn Arrows:** Injects maneuver turn arrows and distances directly into the speedometer dials and Head-Up Display (HUD) via native BAP protocols.
- **Native Route Totals:** Displays remaining travel duration and distance to destination natively across the top bar.
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

## Installation

### 1. Preparation
1. Download the latest release: [`CarPlay_Maps_VC_MU1427_795B_AID_v1.0.0.zip`](https://github.com/Bazsolf/MHI2-Carplay-Maps-795B/releases/tag/v1.0.0).
2. Format an SD card (32 GB or smaller) to **FAT32**.
3. Extract the contents of the ZIP directly to the root of the SD card.

### 2. Vehicle Installation
1. Insert the SD card into slot 1 of your head unit.
2. Ensure your phone is **disconnected** from USB.
3. Open the **Green Engineering Menu (GEM)** and navigate to:
   $$\text{MQB Toolbox} \rightarrow \text{Customization} \rightarrow \text{CarPlay Maps VC}$$
4. Select **`Install 791/795B AID 12.3 (AUTO MU)`**.
5. Next, select **`Install full native guidance (MU1427 only)`**.
6. **Fully reboot the unit:** Press and hold the volume/power knob for **10–15 seconds** until the screen reboots.

### 3. Verification
- Start the car and let the factory virtual cockpit map initialize.
- Connect your iPhone via USB and launch CarPlay.
- When navigation is started on Apple Maps or Google Maps, the map stream will project in the virtual cockpit, and native turn arrows will populate your gauges and HUD.

---

## Display & Tuning Options

You can adjust how the map and maneuver cards are displayed at any time without reinstalling. In GEM, navigate to:
$$\text{MQB Toolbox} \rightarrow \text{Customization} \rightarrow \text{CarPlay Maps VC} \rightarrow \text{Tuning}$$

| Tuning Option | Description |
| :--- | :--- |
| **Default (Narrow Card)** | Displays a 400 px narrow maneuver turn card box positioned cleanly between top and bottom status bars. |
| **Wide Card** | Displays a 480 px wider maneuver turn card box for higher-visibility route cues. |
| **Map-Only (No Card)** ⭐ | Completely disables the phone turn card box (`.../map` URL), giving you a **100% unobstructed full-screen map** while the car displays all arrows and distances natively in the dials. |

> [!NOTE]
> All three tuning options share the exact same 340 px safe area height tuned for the 795B cluster. After changing a tuning option, reboot the head unit (hold power knob for 10–15 seconds).

---

## Uninstallation & Recovery

To remove native guidance or restore the factory system:
- **Temporary Disable:** In GEM, select `Disable native guidance now (until reboot)` to temporarily disable native guidance without touching installed files.
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
