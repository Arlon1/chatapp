package com.jonahbauer.qed.chat;

import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Message {
    private static final String KEY_NAME = "name";
    private static final String KEY_MESSAGE = "background_message_extended";
    private static final String KEY_DATE = "date";
    private static final String KEY_USERID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_COLOR = "color";
    private static final String KEY_ID = "id";
    private static final String KEY_BOTTAG = "bottag";
    private static final String KEY_CHANNEL = "channel";

    public final String name;
    public final String message;
    public final String date;
    public final int userId;
    public final String userName;
    public final String color;
    public final String channel;
    public final int bottag;
    public final int id;

    public Message(String name, String message, String date, int userId, String userName, String color, int id, int bottag, String channel) {
        this.name = name;
        this.message = message;
        this.date = date;
        this.userId = userId;
        this.userName = userName;
        this.color = color;
        this.id = id;
        this.bottag = bottag;
        this.channel = channel;
    }

    public String toString() {
        return "{"
                + "\"id\":\"" + id + "\", "
                + "\"name\":\"" + name + "\", "
                + "\"message\":\"" + message + "\", "
                + "\"date\":\"" + date + "\", "
                + "\"userId\":\"" + userId + "\", "
                + "\"userName\":\"" + userName + "\", "
                + "\"color\":\"" + color + "\", "
                + "\"bottag\":\"" + bottag + "\", "
                + "\"channel\":\"" + channel + "\"}";
    }

    public int compareTo(Message other) {
        return Integer.compare(id,other.id);
    }

    public static Message interpretJSONMessage(String jsonMessage) {
        return interpretJSONMessage(jsonMessage, true);
    }

    @SuppressWarnings("WeakerAccess")
    public static Message interpretJSONMessage(String jsonMessage, boolean format) {
        try {
            JSONObject json = new JSONObject(jsonMessage);
            String name = json.getString("name");
            String message = json.getString("message");
            String username = json.getString("username");
            String color = json.getString("color");
            String date = json.getString("date");
            String channel = json.getString("channel");
            int userid = json.optInt("user_id", -1);
            int id = json.getInt("id");
            int bottag = json.getInt("bottag");
            if (format) {
                name = new String(name.getBytes("ISO-8859-1"), "UTF-8");
                message = new String(message.getBytes("ISO-8859-1"), "UTF-8");
                username = new String(username.getBytes("ISO-8859-1"), "UTF-8");
                color = new String(color.getBytes("ISO-8859-1"), "UTF-8");
                date = new String(date.getBytes("ISO-8859-1"), "UTF-8");
                channel = new String(channel.getBytes("ISO-8859-1"), "UTF-8");
            }
            name = name.trim();
            if ("null".equals(username)) username = null;
            return new Message(name, message, date, userid, username, color, id, bottag, channel);
        } catch (JSONException | UnsupportedEncodingException ignored) {}
        return null;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public void storeToIntent(Intent intent) {
        intent.putExtra(KEY_ID, id);
        intent.putExtra(KEY_NAME, name);
        intent.putExtra(KEY_MESSAGE, message);
        intent.putExtra(KEY_DATE, date);
        intent.putExtra(KEY_USERID, userId);
        intent.putExtra(KEY_USERNAME, userName);
        intent.putExtra(KEY_COLOR, color);
        intent.putExtra(KEY_BOTTAG, bottag);
        intent.putExtra(KEY_CHANNEL, channel);
    }

    public static Message getFromIntent(Intent intent) {
        try {
            int id = intent.getIntExtra(KEY_ID, -1);
            String name = intent.getStringExtra(KEY_NAME);
            String message = intent.getStringExtra(KEY_MESSAGE);
            String date = intent.getStringExtra(KEY_DATE);
            int userId = intent.getIntExtra(KEY_USERID, -1);
            String userName = intent.getStringExtra(KEY_USERNAME);
            String color = intent.getStringExtra(KEY_COLOR);
            int bottag = intent.getIntExtra(KEY_BOTTAG, 0);
            String channel = intent.getStringExtra(KEY_CHANNEL);
            return new Message(name, message, date, userId, userName, color, id, bottag, channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
