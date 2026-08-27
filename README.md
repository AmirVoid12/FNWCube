# 🧊 FNWCube

<p align="center">
  <img src="https://skillicons.dev/icons?i=java" />
</p>

<p align="center">
  <img alt="Version" src="https://img.shields.io/badge/version-1.0--SNAPSHOT-blue?style=flat-square" />
  <img alt="Minecraft" src="https://img.shields.io/badge/minecraft-1.20%2B-brightgreen?style=flat-square" />
  <img alt="Status" src="https://img.shields.io/badge/status-mostly%20working%2C%20changes%20incoming-yellow?style=flat-square" />
  <img alt="License" src="https://img.shields.io/badge/license-private-lightgrey?style=flat-square" />
</p>

**FNWCube** is a **regenerating cube / mining-economy plugin** for the **FNW** Minecraft server network. It generates self-contained cuboid "cubes" (ore blocks, custom loot tables, and a virtual GUI-based inventory), lets players mine and automine them, and hooks into economy/shop plugins to sell the output — all with self-regeneration over time.

👨‍💻 Developed by: [AmirVoid12](https://amirvoid12.ir) — Tabriz 🇮🇷

---

## ⚠️ Project Status — Different From the Rest

Unlike FNWCore, FNWLimbo, FNWLobby, and FNWProxy, **FNWCube is not part of the same 1.8.8-based codebase and is architected differently**:

- It targets **Minecraft 1.20+** (`api-version: 1.20`) and **Java 21**, not Spigot 1.8.8 / Java 8.
- It's a standalone gameplay/economy plugin rather than network infrastructure — it doesn't talk to Redis or the rest of the FNW network stack directly.
- It's **more functionally complete than the network-glue plugins** — most core features (cube generation, mining, regeneration, selling, auto-miner, bank) are working — but it is still **not considered fully stable**, and **significant changes are planned**. Expect API/config shifts, rebalancing, and behavior changes in upcoming updates.

👉 In short: it's in a better working state than FNWCore/FNWLimbo/FNWLobby/FNWProxy, but it's not "done" — treat it as **usable but evolving**.

---

## 📜 About This Repository

This project was originally private and belonged entirely to **FlameNetwork** 🔒. For private reasons, FlameNetwork has been shut down 🛑, and as a result these sources have been made **public** 🌍. It is still being **debugged and updated continuously** 🔧, so expect frequent changes, fixes, and improvements over time.

---

## 🧩 Independent Plugin

FNWCube is **separate from FNWCore, FNWProxy, FNWLimbo, and FNWLobby**. It is a **standalone plugin** that does not depend on the FNW network stack, Redis, or a proxy to function — it can be installed and run on its own on any compatible Spigot/Paper server.

---

## ✨ Features

- 🧊 **Cube generation** — configurable cuboid structures (`cubes.yml`) with per-block-type spawn percentages, custom border materials, and adjustable size
- 🔄 **Self-regeneration** — cubes rebuild over time using `LINEAL` or `RANDOM` patterns, with configurable regeneration timing and quantity (percentage or fixed block count)
- ⛏️ **Auto-Miner** — automatically mines a cube's blocks into its virtual inventory on a tick loop, stopping when full
- 📦 **Virtual cube inventory (GUI)** — mined blocks go into a per-cube GUI inventory instead of dropping on the ground (optional, configurable)
- 💰 **Cube Seller** — sells inventory contents automatically, with pluggable pricing backends: **EssentialsX**, **ShopGUIPlus**, or **AutoSell**
- 🔥 **Cube Smelter** — auto-smelts smeltable items in the cube inventory using vanilla furnace recipes
- 🗜️ **Cube Compressor** — compresses raw materials into their block form (9x → block) for more efficient storage/selling
- 🧹 **Cube Sorter** — sorts and merges stacks inside the cube inventory
- 🏦 **Cube Bank** — a per-player virtual bank/account system tied to cube economy
- 🌍 **Region plugin compatibility** — integrates with **WorldGuard**, **PlotSquared**, and **SuperiorSkyblock2/IridiumSkyblock/BentoBox**-style island protections
- ⚡ **FastAsyncWorldEdit (FAWE)** integration for efficient cube building/regeneration, with a pure-Bukkit fallback if FAWE isn't installed
- 💵 **Vault economy** integration for purchases, upgrades, and sells
- 🗃️ **AdvancedChests** compatibility detection
- ⚙️ `/gencubes` (aliases `gc`, `gcs`) and `/cubes` commands with tab-completion

---

## 🖥️ Supported Versions

```
Minecraft 1.20+   (Spigot / Paper, Java 21)
```

---

## 🚀 Usage / Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/AmirVoid12/FNWCube.git
   cd FNWCube
   ```

2. **Build the project**
   FNWCube pulls the Spigot 1.20.2 API and all its soft-dependencies (Vault, FAWE, EssentialsX, ShopGUIPlus, AutoSell, etc.) directly from Maven/JitPack repositories — there's no manual `server.jar` step required:
   ```bash
   ./gradlew build
   ```
   The compiled plugin jar will be generated in `build/libs/`.

3. **Drop the jar** into your Spigot/Paper server's `plugins/` folder, start it once to generate `config.yml` and `cubes.yml`, then configure your cube types, generation percentages, and regeneration timings.

4. **Install soft-dependencies as needed** — none are strictly required to load the plugin, but functionality depends on them:
   - **Vault** + an economy plugin → required for selling/upgrades
   - **FastAsyncWorldEdit** → strongly recommended for performance during cube building/regeneration
   - **EssentialsX**, **ShopGUIPlus**, or **AutoSell** → pick one as your sell-price backend
   - **WorldGuard** / **PlotSquared** / **SuperiorSkyblock2** → optional, for region-aware cube placement/protection

### ⚠️ Known Issue — Java Version

FNWCube targets **Java 21**, not Java 8 like the 1.8.8-based FNW plugins. Make sure your server is running on a Java 21-compatible build (Paper 1.20+ recommended) — running it against an older Java runtime will prevent the plugin from loading.

---

## 🛠️ Built With

<p>
  <img src="https://skillicons.dev/icons?i=java" />
</p>

- ☕ Java 21
- 🐘 Gradle (Shadow plugin)
- 🎮 Spigot API 1.20.2
- 💵 Vault API
- ⚡ FastAsyncWorldEdit / WorldEdit
- 🧩 XSeries (cross-version material handling)
- 🔌 EssentialsX, ShopGUIPlus, AutoSell (optional, sell backends)

---

## 📄 License

This project was originally private and belongs to **FlameNetwork**. Due to private reasons, FlameNetwork has been shut down, and these sources have since been made public. The code is provided as-is and is continuously debugged and updated.

---

<p align="center">
  Made with ❤️ by <a href="https://amirvoid12.ir">AmirVoid12</a>
</p>

<p align="center">
  ⭐ <b>Don't forget to star this repository!</b> ⭐
</p>
