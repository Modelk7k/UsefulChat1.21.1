package net.friendlyfire.betterchat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatDataStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type DATA_TYPE = new TypeToken<Map<UUID, PlayerChatData>>(){}.getType();
    private static Map<UUID, PlayerChatData> dataMap = new HashMap<>();
    private static File dataFile;

    public static void init(MinecraftServer server) {
        LevelResource worldRoot = LevelResource.ROOT;
        File saveDir = server.getWorldPath(worldRoot).toFile(); // This gives us the world directory
        File betterChatDir = new File(saveDir, "betterchat");
        if (!betterChatDir.exists()) {
            betterChatDir.mkdirs();
        }
        dataFile = new File(betterChatDir, "chat_data.json");
        if (dataFile.exists()) {
            try (FileReader reader = new FileReader(dataFile)) {
                dataMap = GSON.fromJson(reader, DATA_TYPE);
                if (dataMap == null) {
                    dataMap = new HashMap<>();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            GSON.toJson(dataMap, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static PlayerChatData get(UUID uuid) {
        return dataMap.getOrDefault(uuid, new PlayerChatData("", "", ""));
    }
    public static void set(UUID uuid, PlayerChatData data) {
        dataMap.put(uuid, data);
        save();  // Save immediately after setting data
    }
    public static void remove(UUID uuid) {
        dataMap.remove(uuid);
        save();
    }
}
