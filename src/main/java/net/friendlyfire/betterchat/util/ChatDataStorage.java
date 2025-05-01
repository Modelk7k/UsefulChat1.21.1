package net.friendlyfire.betterchat.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatDataStorage {
    private final Map<UUID, PlayerChatData> dataMap = new HashMap<>();
    private final File dataFile;
    public ChatDataStorage(File worldDir) {
        File betterChatDir = new File(worldDir, "betterchat");
        if (!betterChatDir.exists()) {
            betterChatDir.mkdirs();
        }
        this.dataFile = new File(betterChatDir, "chat_data.json");
        if (this.dataFile.exists()) {
            try (FileReader reader = new FileReader(this.dataFile)) {
                Type type = new TypeToken<Map<UUID, PlayerChatData>>(){}.getType();
                Map<UUID, PlayerChatData> loaded = new Gson().fromJson(reader, type);
                if (loaded != null) {
                    getDataMap().putAll(loaded);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void save() {
        try (FileWriter writer = new FileWriter(dataFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(getDataMap(), writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public PlayerChatData get(UUID uuid) {
        return getDataMap().getOrDefault(uuid, new PlayerChatData("", "", ""));
    }
    public void set(UUID uuid, PlayerChatData data) {
        getDataMap().put(uuid, data);
        save();
    }
    public void remove(UUID uuid) {
        getDataMap().remove(uuid);
        save();
    }
    public Map<UUID, PlayerChatData> getDataMap() {
        return dataMap;
    }
}