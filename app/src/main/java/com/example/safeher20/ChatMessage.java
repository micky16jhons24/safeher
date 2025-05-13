package com.example.safeher20;

public class ChatMessage {
    public static final int TYPE_LEFT = 0;
    public static final int TYPE_RIGHT = 1;
    public static final int TYPE_AUDIO_LEFT = 2;

    private String message;
    private String time;
    private int type;
    public static final int TYPE_AUDIO = 2;

    public ChatMessage(String message, String time, int type) {
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public int getType() {
        return type;
    }
}

