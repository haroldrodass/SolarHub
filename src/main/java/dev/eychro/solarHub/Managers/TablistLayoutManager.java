package dev.eychro.solarHub.Managers;

import dev.eychro.solarHub.SolarHub;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages virtual and fixed slots in the tablist grid.
 * Packet operations are reflected dynamically to maintain compatibility
 * across Paper and Minecraft versions.
 */
public final class TablistLayoutManager {

    /**
     * Hash of the solid black skin hosted on Mojang's texture server.
     * The texture JSON is generated in code, so there is no hand-written base64 to get wrong.
     * The host MUST be "textures.minecraft.net" (with the "s"), otherwise the client can't
     * download it and falls back to the random Steve/Alex default skins.
     */
    private static final String BUILTIN_BLACK_TEXTURE_HASH =
            "7d086f2eb48b124a592c47c5d417057052e48a3c2c68f8752787a835640c54b1";
    private static final String TEXTURE_BASE_URL = "http://textures.minecraft.net/texture/";
    private static final SkinData BUILTIN_BLACK_SKIN =
            SkinData.fromTextureUrl(TEXTURE_BASE_URL + BUILTIN_BLACK_TEXTURE_HASH);

    private final SolarHub plugin;
    private final Map<UUID, Set<UUID>> sentEntries = new HashMap<>();
    private boolean unavailableLogged;
    private volatile SkinData defaultSkin = BUILTIN_BLACK_SKIN;
    private volatile boolean skinLoadStarted;

    @SuppressWarnings("rawtypes")
    private EnumSet cachedFullActions;
    @SuppressWarnings("rawtypes")
    private EnumSet cachedListedActionSet;

    public TablistLayoutManager(SolarHub plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getFiles().getConfig().getBoolean("Tablist.layout.enabled", false);
    }

    public void updateAll() {
        if (!isEnabled()) {
            if (!sentEntries.isEmpty()) {
                clearAll();
            }
            return;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            update(viewer);
        }
    }

    /** Forces the default skin to be re-read from the config on the next update. */
    public void reloadSkin() {
        defaultSkin = BUILTIN_BLACK_SKIN;
        skinLoadStarted = false;
    }

    /**
     * Sends the layout to a viewer. In layout mode NO real players are shown:
     * every slot is either a fixed text slot or an empty (black) slot, and all real
     * players are hidden from the list. Players only appear in normal mode.
     */
    public void update(Player viewer) {
        if (!isEnabled()) {
            if (sentEntries.containsKey(viewer.getUniqueId())) {
                clear(viewer);
            }
            return;
        }

        Layout layout = readLayout();
        if (layout == null) {
            clear(viewer);
            return;
        }

        try {
            loadDefaultSkinIfConfigured();
            SkinData layoutSkin = resolveSkin(layout.defaultSkinDefinition(), defaultSkin);

            List<Object> entries = new ArrayList<>(layout.slotCount());
            Set<UUID> currentEntryIds = new HashSet<>();

            for (int slot = 1; slot <= layout.slotCount(); slot++) {
                int translated = layout.translatedSlot(slot);
                // The client sorts tab entries by -listOrder (higher value = rendered first),
                // so earlier slots get higher order numbers.
                int order = 10000 - translated;

                UUID id = UUID.nameUUIDFromBytes(
                        ("solarhub-layout:" + layout.name() + ":" + slot).getBytes(StandardCharsets.UTF_8)
                );
                FixedSlot fixedSlot = layout.fixedSlots().get(slot);

                Object entry;
                if (fixedSlot != null) {
                    entry = createFixedEntry(viewer, id, fixedSlot.text(), translated, order,
                            fixedSlot.latency(layout.emptySlotPing()),
                            resolveSkin(fixedSlot.skin(), layoutSkin));
                } else {
                    entry = createEmptyEntry(viewer, id, translated, order, layout.emptySlotPing(), layoutSkin);
                }
                currentEntryIds.add(id);
                entries.add(entry);
            }

            // Remove previously sent dummy entries that are no longer part of the layout
            Set<UUID> previousIds = sentEntries.get(viewer.getUniqueId());
            if (previousIds != null) {
                List<UUID> toRemove = new ArrayList<>();
                for (UUID prevId : previousIds) {
                    if (!currentEntryIds.contains(prevId)) {
                        toRemove.add(prevId);
                    }
                }
                if (!toRemove.isEmpty()) {
                    sendRemovePacket(viewer, toRemove);
                }
            }

            // One batched packet for the whole layout to avoid flickering
            sendPacket(viewer, createUpdatePacket(getFullActions(), entries));
            sentEntries.put(viewer.getUniqueId(), currentEntryIds);

            // Hide ALL real players while the layout is active
            List<Object> unlistEntries = new ArrayList<>();
            for (Player target : Bukkit.getOnlinePlayers()) {
                unlistEntries.add(createListedEntry(target.getUniqueId(), false));
            }
            if (!unlistEntries.isEmpty()) {
                sendPacket(viewer, createUpdatePacket(getUpdateListedActionSet(), unlistEntries));
            }

        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!unavailableLogged) {
                unavailableLogged = true;
                plugin.getLogger().warning("No se pudo activar el layout de tablist en esta versión de Paper: "
                        + exception.getMessage());
            }
        }
    }

    public void clearAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            clear(viewer);
        }
        sentEntries.clear();
    }

    public void clear(Player viewer) {
        Set<UUID> ids = sentEntries.remove(viewer.getUniqueId());
        try {
            if (ids != null && !ids.isEmpty() && viewer.isOnline()) {
                sendRemovePacket(viewer, new ArrayList<>(ids));
            }
            if (viewer.isOnline()) {
                showRealPlayers(viewer);
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!unavailableLogged) {
                unavailableLogged = true;
                plugin.getLogger().warning("No se pudo limpiar el layout de tablist: " + exception.getMessage());
            }
        }
    }

    private void showRealPlayers(Player viewer) throws ReflectiveOperationException {
        List<Object> listEntries = new ArrayList<>();
        for (Player target : Bukkit.getOnlinePlayers()) {
            listEntries.add(createListedEntry(target.getUniqueId(), true));
        }
        if (!listEntries.isEmpty()) {
            sendPacket(viewer, createUpdatePacket(getUpdateListedActionSet(), listEntries));
        }
    }

    private Layout readLayout() {
        ConfigurationSection layouts = plugin.getFiles().getConfig()
                .getConfigurationSection("Tablist.layout.layouts");
        if (layouts == null) {
            return null;
        }

        ConfigurationSection selected = layouts.getConfigurationSection("default");
        if (selected == null) {
            for (String name : layouts.getKeys(false)) {
                selected = layouts.getConfigurationSection(name);
                if (selected != null) {
                    break;
                }
            }
        }
        if (selected == null) {
            return null;
        }

        int slotCount = Math.min(80, Math.max(1, selected.getInt("slot-count", 80)));
        Map<Integer, FixedSlot> fixedSlots = new HashMap<>();
        for (String rawSlot : selected.getStringList("fixed-slots")) {
            String[] parts = rawSlot.split("\\|", -1);
            if (parts.length < 2) {
                continue;
            }

            try {
                int slot = Integer.parseInt(parts[0].trim());
                if (slot >= 1 && slot <= slotCount) {
                    fixedSlots.put(slot, new FixedSlot(
                            parts[1],
                            parts.length > 2 ? parts[2].trim() : "",
                            parts.length > 3 ? parts[3].trim() : ""
                    ));
                }
            } catch (NumberFormatException ignored) {
                plugin.getLogger().warning("Slot fijo inválido en Tablist.layout: " + rawSlot);
            }
        }

        return new Layout(
                selected.getName(),
                slotCount,
                selected.getInt("empty-slot-ping-value",
                        plugin.getFiles().getConfig().getInt("Tablist.layout.empty-slot-ping-value", 1000)),
                fixedSlots,
                plugin.getFiles().getConfig().getString("Tablist.layout.direction", "COLUMNS"),
                selected.getString("default-skin",
                        plugin.getFiles().getConfig().getString("Tablist.layout.default-skin", ""))
        );
    }

    private Object createFixedEntry(Player viewer, UUID id, String text, int translated, int order, int latency, SkinData skin)
            throws ReflectiveOperationException {
        String profileName = String.format("!sh_%03d", translated);
        Object profile = createGameProfile(id, profileName, skin != null ? skin : BUILTIN_BLACK_SKIN);
        Object component = toNmsComponent(FileManager.color(render(viewer, text)));
        return createEntry(profile, id, component, order, latency);
    }

    private Object createEmptyEntry(Player viewer, UUID id, int translated, int order, int latency, SkinData skin)
            throws ReflectiveOperationException {
        String profileName = String.format("!sh_%03d", translated);
        Object profile = createGameProfile(id, profileName, skin != null ? skin : BUILTIN_BLACK_SKIN);
        Object component = toNmsComponent(" ");
        return createEntry(profile, id, component, order, latency);
    }

    /** Parses builtin / hash: / url: / texture: definitions. Returns null if not an inline definition. */
    private SkinData parseInlineSkin(String definition) {
        if (definition == null) {
            return null;
        }
        String value = definition.trim();
        String lower = value.toLowerCase(Locale.ROOT);
        if (value.isEmpty() || lower.equals("builtin") || lower.equals("black")) {
            return BUILTIN_BLACK_SKIN;
        }
        if (lower.startsWith("hash:")) {
            String hash = value.substring("hash:".length()).trim().toLowerCase(Locale.ROOT);
            return hash.matches("[0-9a-f]{32,64}") ? SkinData.fromTextureUrl(TEXTURE_BASE_URL + hash) : null;
        }
        if (lower.startsWith("url:")) {
            return SkinData.fromTextureUrl(value.substring("url:".length()).trim());
        }
        if (lower.startsWith("texture:")) {
            String[] parts = value.substring("texture:".length()).split("\\|", 2);
            if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                return new SkinData(parts[0].trim(), parts[1].trim());
            }
        }
        return null;
    }

    private void loadDefaultSkinIfConfigured() {
        if (skinLoadStarted) {
            return;
        }

        String definition = plugin.getFiles().getConfig().getString("Tablist.layout.default-skin", "").trim();
        if (!definition.regionMatches(true, 0, "mineskin:", 0, "mineskin:".length())) {
            SkinData inline = parseInlineSkin(definition);
            defaultSkin = inline != null ? inline : BUILTIN_BLACK_SKIN;
            skinLoadStarted = true;
            verifyTextureAsync(defaultSkin);
            return;
        }

        String skinId = definition.substring("mineskin:".length()).trim();
        skinLoadStarted = true;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = URI.create("https://api.mineskin.org/get/id/" + skinId).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "SolarHub/1.0");
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    String json = reader.lines().reduce("", (left, right) -> left + right);
                    String value = jsonValue(json, "value");
                    String signature = jsonValue(json, "signature");
                    if (value == null || signature == null) {
                        throw new IllegalStateException("MineSkin no devolvió una textura firmada");
                    }
                    defaultSkin = new SkinData(value, signature);
                    Bukkit.getScheduler().runTask(plugin, this::updateAll);
                } finally {
                    connection.disconnect();
                }
            } catch (Exception exception) {
                // Keep BUILTIN_BLACK_SKIN safely
                plugin.getLogger().warning("No se pudo cargar Tablist.layout.default-skin (usando skin negra predeterminada): "
                        + exception.getMessage());
            }
        });
    }

    /** Warns in console if the skin URL doesn't exist (the client would then show random default skins). */
    private void verifyTextureAsync(SkinData skin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String json = new String(Base64.getDecoder().decode(skin.value()), StandardCharsets.UTF_8);
                Matcher matcher = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
                if (!matcher.find()) {
                    return;
                }
                HttpURLConnection connection = (HttpURLConnection) URI.create(matcher.group(1)).toURL().openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setRequestMethod("HEAD");
                int code = connection.getResponseCode();
                connection.disconnect();
                if (code >= 400) {
                    plugin.getLogger().warning("La skin del tablist (" + matcher.group(1) + ") respondió HTTP " + code
                            + ": el cliente mostrará skins default. Configura Tablist.layout.default-skin con una textura válida.");
                }
            } catch (Exception ignored) {
                // Sin internet o error temporal: no molestar.
            }
        });
    }

    private String jsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Object createGameProfile(UUID id, String name, SkinData skin) throws ReflectiveOperationException {
        Class<?> profileClass = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> propertyMapClass = Class.forName("com.mojang.authlib.properties.PropertyMap");
        Class<?> propertyClass = Class.forName("com.mojang.authlib.properties.Property");

        SkinData effectiveSkin = skin != null ? skin : BUILTIN_BLACK_SKIN;
        Object propertyMap = null;

        Object property = propertyClass.getConstructor(String.class, String.class, String.class)
                .newInstance("textures", effectiveSkin.value(), effectiveSkin.signature());

        // Modern authlib (Paper 1.20.5+ / 26.3) uses PropertyMap(Multimap) with ImmutableMultimap
        try {
            Class<?> immutableMultimapClass = Class.forName("com.google.common.collect.ImmutableMultimap");
            Method ofMethod = immutableMultimapClass.getMethod("of", Object.class, Object.class);
            Object multimap = ofMethod.invoke(null, "textures", property);
            Constructor<?> mapCtor = propertyMapClass.getConstructor(Class.forName("com.google.common.collect.Multimap"));
            propertyMap = mapCtor.newInstance(multimap);
        } catch (Throwable ignored) {
        }

        // Fallback for older authlib with parameterless constructor
        if (propertyMap == null) {
            try {
                Constructor<?> mapCtor = propertyMapClass.getConstructor();
                propertyMap = mapCtor.newInstance();
                Method putMethod = propertyMap.getClass().getMethod("put", Object.class, Object.class);
                putMethod.invoke(propertyMap, "textures", property);
            } catch (Throwable ignored) {
            }
        }

        // Try GameProfile(UUID, String, PropertyMap) if constructor exists and propertyMap is present
        if (propertyMap != null) {
            try {
                Constructor<?> profileCtor = profileClass.getConstructor(UUID.class, String.class, propertyMapClass);
                return profileCtor.newInstance(id, name, propertyMap);
            } catch (NoSuchMethodException ignored) {
            }
        }

        // Fallback to GameProfile(UUID, String)
        Constructor<?> profileCtor = profileClass.getConstructor(UUID.class, String.class);
        Object profile = profileCtor.newInstance(id, name);

        // If skin exists but wasn't passed in constructor, attempt direct mutation (legacy authlib)
        try {
            Method getPropsMethod;
            try {
                getPropsMethod = profile.getClass().getMethod("properties");
            } catch (NoSuchMethodException e) {
                getPropsMethod = profile.getClass().getMethod("getProperties");
            }
            Object props = getPropsMethod.invoke(profile);
            props.getClass().getMethod("put", Object.class, Object.class).invoke(props, "textures", property);
        } catch (Throwable ignored) {
        }

        return profile;
    }

    private SkinData resolveSkin(String definition, SkinData fallback) {
        SkinData base = fallback != null ? fallback : BUILTIN_BLACK_SKIN;
        if (definition == null || definition.isBlank()) {
            return base;
        }
        SkinData parsed = parseInlineSkin(definition);
        return parsed != null ? parsed : base;
    }

    private Object createEntry(Object profile, UUID id, Object component, int order, int latency)
            throws ReflectiveOperationException {
        Class<?> profileClass = Class.forName("com.mojang.authlib.GameProfile");
        Class<?> entryClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry"
        );

        Constructor<?> targetConstructor = null;
        for (Constructor<?> constructor : entryClass.getDeclaredConstructors()) {
            if (constructor.getParameterCount() >= 5) {
                targetConstructor = constructor;
                break;
            }
        }
        if (targetConstructor == null) {
            throw new IllegalStateException("No se encontró el constructor de Entry del tab");
        }

        Class<?>[] types = targetConstructor.getParameterTypes();
        Object[] arguments = new Object[types.length];
        int intCount = 0;
        for (int index = 0; index < types.length; index++) {
            if (types[index] == UUID.class) {
                arguments[index] = id;
            } else if (types[index].getName().equals(profileClass.getName())) {
                arguments[index] = profile;
            } else if (types[index].getName().equals("net.minecraft.network.chat.Component")) {
                arguments[index] = component;
            } else if (types[index] == boolean.class) {
                arguments[index] = true;
            } else if (types[index] == int.class) {
                intCount++;
                arguments[index] = (intCount == 1) ? latency : order;
            } else if (types[index].isEnum()) {
                arguments[index] = enumConstant(types[index], "SURVIVAL");
            } else if (!types[index].isPrimitive()) {
                arguments[index] = null;
            }
        }
        targetConstructor.setAccessible(true);
        return targetConstructor.newInstance(arguments);
    }

    private Object toNmsComponent(String text) throws ReflectiveOperationException {
        if (text == null || text.isEmpty()) {
            text = " ";
        }
        Class<?> craftChatMessage = Class.forName("org.bukkit.craftbukkit.util.CraftChatMessage");
        try {
            Method fromString = craftChatMessage.getMethod("fromStringOrNull", String.class);
            Object component = fromString.invoke(null, text);
            if (component != null) {
                return component;
            }
        } catch (NoSuchMethodException ignored) {
        }

        Class<?> componentClass = Class.forName("net.minecraft.network.chat.Component");
        return componentClass.getMethod("literal", String.class).invoke(null, text);
    }

    private Object createListedEntry(UUID id, boolean listed) throws ReflectiveOperationException {
        Class<?> entryClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Entry"
        );
        for (Constructor<?> constructor : entryClass.getDeclaredConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && types[0] == UUID.class && types[1] == boolean.class) {
                constructor.setAccessible(true);
                return constructor.newInstance(id, listed);
            }
        }
        // Fallback using general constructor
        return createEntry(null, id, toNmsComponent(" "), 0, 0);
    }

    private Object enumConstant(Class<?> enumType, String name) {
        Object[] constants = enumType.getEnumConstants();
        for (Object constant : constants) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
        return constants[0];
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Object createUpdatePacket(EnumSet actions, List<Object> entries) throws ReflectiveOperationException {
        Class<?> packetClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket"
        );
        for (Constructor<?> constructor : packetClass.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && EnumSet.class.isAssignableFrom(types[0]) && types[1] == List.class) {
                return constructor.newInstance(actions, entries);
            }
        }
        for (Constructor<?> constructor : packetClass.getConstructors()) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && EnumSet.class.isAssignableFrom(types[0])
                    && Collection.class.isAssignableFrom(types[1])
                    && types[1] != Collection.class) {
                return constructor.newInstance(actions, entries);
            }
        }
        throw new IllegalStateException("No se encontró el constructor de PlayerInfoUpdatePacket");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private EnumSet getFullActions() throws ReflectiveOperationException {
        if (cachedFullActions != null) {
            return cachedFullActions;
        }
        Class<?> actionClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action"
        );
        EnumSet actions = EnumSet.noneOf((Class<? extends Enum>) actionClass.asSubclass(Enum.class));
        for (String actionName : List.of("ADD_PLAYER", "UPDATE_GAME_MODE", "UPDATE_LISTED", "UPDATE_LATENCY", "UPDATE_DISPLAY_NAME", "UPDATE_LIST_ORDER", "UPDATE_HAT")) {
            try {
                actions.add(Enum.valueOf((Class<? extends Enum>) actionClass.asSubclass(Enum.class), actionName));
            } catch (IllegalArgumentException ignored) {
            }
        }
        cachedFullActions = actions;
        return actions;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private EnumSet getUpdateListedActionSet() throws ReflectiveOperationException {
        if (cachedListedActionSet != null) {
            return cachedListedActionSet;
        }
        Class<?> actionClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action"
        );
        Enum action = Enum.valueOf((Class<? extends Enum>) actionClass.asSubclass(Enum.class), "UPDATE_LISTED");
        cachedListedActionSet = EnumSet.of(action);
        return cachedListedActionSet;
    }

    private void sendRemovePacket(Player viewer, List<UUID> ids) throws ReflectiveOperationException {
        if (ids.isEmpty()) {
            return;
        }
        Class<?> packetClass = Class.forName(
                "net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket"
        );
        for (Constructor<?> constructor : packetClass.getConstructors()) {
            if (constructor.getParameterCount() == 1
                    && (constructor.getParameterTypes()[0] == List.class || Collection.class.isAssignableFrom(constructor.getParameterTypes()[0]))) {
                sendPacket(viewer, constructor.newInstance(ids));
                return;
            }
        }
        throw new IllegalStateException("No se encontró el constructor de eliminación del tab");
    }

    private void sendPacket(Player viewer, Object packet) throws ReflectiveOperationException {
        Object connection = getFieldOrMethod(getHandle(viewer), "connection");
        Method send = null;
        for (Method method : connection.getClass().getMethods()) {
            if (method.getName().equals("send") && method.getParameterCount() == 1
                    && method.getParameterTypes()[0].isAssignableFrom(packet.getClass())) {
                send = method;
                break;
            }
        }
        if (send == null) {
            throw new IllegalStateException("No se encontró el método de envío de paquetes");
        }
        send.invoke(connection, packet);
    }

    private Object getHandle(Player player) throws ReflectiveOperationException {
        return player.getClass().getMethod("getHandle").invoke(player);
    }

    private Object getFieldOrMethod(Object target, String name) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                var field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        return target.getClass().getMethod(name).invoke(target);
    }

    private String render(Player player, String text) {
        String result = text
                .replace("%player%", player.getName())
                .replace("%online%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%max_players%", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("%ping%", String.valueOf(player.getPing()))
                .replace("%world%", player.getWorld().getName());

        if (plugin.getServerStatusManager() != null) {
            Pattern serverPattern = Pattern.compile("%server_players_([a-zA-Z0-9_-]+)%");
            Matcher m = serverPattern.matcher(result);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String srv = m.group(1);
                int count = plugin.getServerStatusManager().getPlayerCount(srv);
                m.appendReplacement(sb, count >= 0 ? String.valueOf(count) : "0");
            }
            m.appendTail(sb);
            result = sb.toString();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            result = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, result);
        }

        String rank = "";
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            rank = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, "%luckperms_prefix%");
        }
        if (rank == null || rank.isBlank() || rank.equals("%luckperms_prefix%")) {
            rank = "Default";
        }
        result = result.replace("%rank%", rank).replace("%luckperms_prefix%", rank);

        return FileManager.color(result);
    }

    private record Layout(String name, int slotCount, int emptySlotPing,
                          Map<Integer, FixedSlot> fixedSlots, String direction,
                          String defaultSkinDefinition) {
        private int translatedSlot(int slot) {
            if ("ROWS".equalsIgnoreCase(direction)) {
                return ((slot - 1) % 4) * 20 + ((slot - 1) / 4) + 1;
            }
            return slot;
        }
    }

    private record SkinData(String value, String signature) {
        /** Builds an (unsigned) textures property pointing to a skin hosted on Mojang's servers. */
        private static SkinData fromTextureUrl(String url) {
            String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
            return new SkinData(Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)), null);
        }
    }

    private record FixedSlot(String text, String skin, String ping) {
        private int latency(int fallback) {
            if (ping.isBlank()) {
                return fallback;
            }
            try {
                return (int) Math.round(Double.parseDouble(ping));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }
}