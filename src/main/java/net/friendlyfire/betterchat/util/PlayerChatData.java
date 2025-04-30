package net.friendlyfire.betterchat.util;

public class PlayerChatData {
    public String nickname;
    public String prefix;
    public String chatColor;

    // Constructor
    public PlayerChatData(String nickname, String prefix, String chatColor) {
        this.nickname = nickname;
        this.prefix = prefix;
        this.chatColor = chatColor;
    }
}