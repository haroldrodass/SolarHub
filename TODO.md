# SolarHub TODO (Features)

## ✅ Done
- [x] Spawn
- [x] Server selector
- [x] Double jump
- [x] Launchpads
- [x] Scoreboard
- [x] Tablist
- [x] World protection
- [x] Item protection
- [x] Build mode

## 🔧 Improve existing features
- [x] Spawn: teleport on join, on void fall and on respawn
- [x] Spawn: `/spawn` command with optional teleport delay
- [x] Server selector: animated or live player counts per server
- [x] Server selector: server status (online / offline / full) in the item
- [x] Server selector: fill empty slots with decorative glass
- [x] Double jump: configurable cooldown, sound and particles
- [x] Scoreboard: animated title and per-player placeholders
- [x] Tablist: header/footer animation and rank-based prefix
- [x] World protection: toggles for weather, time, mob spawning, fire spread, etc.
- [x] Build mode: per-player toggle command with a visible indicator (actionbar)

## 🆕 New features
- [x] Hotbar items: configurable join items (selector, cosmetics, profile, etc.)
- [X] Player visibility toggle (show/hide players)
- [x] Join and quit messages, plus a join title and sound
- [x] Chat formatting and optional chat cooldown in the hub
- [x] Custom join fireworks / effects
- [X] Ride-a-player / pick up players (optional)
- [X] Custom hologram or NPC selectors
- [X] Parkour with checkpoints and timer
- [x] Cosmetics: particles, trails and hats
- [X] Hub selector: multiple hubs (Hub-1, Hub-2...) with `/hub` switching
- [x] Fly mode for ranks/staff
- [x] Vanish for staff
- [X] Daily rewards or a simple quest system
- [X] Multi-language messages

## 🔗 Integrations
- [x] Velocity / BungeeCord messaging
- [x] PlaceholderAPI support
- [x] LuckPerms rank placeholders through PlaceholderAPI in tablist and scoreboard

> Vault economy and full MiniMessage rendering were removed from the core roadmap:
> they are not required by a hub plugin and the current legacy color format keeps
> existing configurations compatible. MiniMessage remains available for titles.

## ⚙️ Configuration & tools
- [x] `messages.yml` for all texts
- [x] `/solarhub reload` for reloading
- [x] Per-feature enable/disable in the config
- [x] Permission per feature, plus a staff bypass