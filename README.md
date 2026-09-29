# ☀️ SolarHub

Configurable hub/lobby plugin for **Paper 26.3**: spawn, server selector, double jump, launchpads, scoreboard, tablist, world and item protection, and build mode.

![Paper](https://img.shields.io/badge/Paper-26.3-blue)
![Java](https://img.shields.io/badge/Java-25-orange)
![Build](https://img.shields.io/badge/build-Maven-red)
![Status](https://img.shields.io/badge/status-0.1--BETA-yellow)

> ⚠️ SolarHub is in **beta** (`0.1-BETA`). Expect changes and please report any bug you find.

---

## ✨ Features

- 🏠 **Spawn**: set the hub spawn and send players there.
- 🧭 **Server selector**: menu to move players between servers.
- 🦘 **Double jump**: configurable extra jump for players.
- 🚀 **Launchpads**: launch players into the air by stepping on a pad.
- 📊 **Scoreboard**: custom sidebar for the hub.
- 📋 **Tablist**: custom header/footer for the player list.
- 🛡️ **World protection**: prevents players from modifying or damaging the hub.
- 🎒 **Item protection**: prevents moving or dropping the hub items.
- 🛠️ **Build mode**: lets authorized staff build in the hub without turning protection off for everyone.

---

## 📦 Requirements

| Requirement | Version |
|-------------|---------|
| Server | Paper 26.3 |
| Java | 25 |
| Build tool | Maven |

---

## 🚀 Installation

1. Download the latest `.jar` from the [Releases](https://github.com/haroldrodass/SolarHub/releases) page, or [build it yourself](#-building).
2. Put it in your server's `plugins/` folder.
3. Restart the server.
4. Edit the generated files in `plugins/SolarHub/`.
5. Set the hub spawn in the place where you want players to appear.

---

## 🛠️ Building

Requires **JDK 25** and **Maven**.

```bash
git clone https://github.com/haroldrodass/SolarHub.git
cd SolarHub
mvn clean package
```

The compiled plugin is created in `target/` (`SolarHub-0.1-BETA.jar`). The project uses the Maven Shade plugin, so the final jar is ready to drop into `plugins/`.

**Project coordinates**

```xml
<groupId>dev.eychro</groupId>
<artifactId>SolarHub</artifactId>
<version>0.1-BETA</version>
```

**Paper API**

```xml
<repository>
    <id>papermc-repo</id>
    <url>https://repo.papermc.io/repository/maven-public/</url>
</repository>

<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>[26.3.build,)</version>
    <scope>provided</scope>
</dependency>
```

---

## 📜 Commands & Permissions


| Command   | Description               | Permission     |
|-----------|---------------------------|----------------|
| `/reload` | Reloads the plugin config | `Solar.Reload` |

---

## ⚙️ Configuration

All settings live in `plugins/SolarHub/`. Reload or restart the server after editing them.

<!-- Add a short example of your config.yml / selector menu here -->

---

## 🤝 Contributing

1. Fork the repository.
2. Create a branch: `git checkout -b feature/my-change`.
3. Commit your changes and push the branch.
4. Open a Pull Request.

## 🐛 Issues

Found a bug or have a suggestion? Open an [issue](https://github.com/haroldrodass/SolarHub/issues) with your Paper version, the SolarHub version and the server log.

---

<p align="center">Made with ☀️ for Minecraft servers</p>