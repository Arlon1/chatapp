package com.jonahbauer.qed.qeddb.person;

import android.support.v4.util.Pair;

import com.jonahbauer.qed.qeddb.event.Event;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Person {
    public int id;
    public String firstName;
    public String lastName;
    public String birthday;
    public String email;
    public String homeStation;
    public String railcard;
    public boolean member;
    public boolean active;
    public String memberSince;
    public List<Pair<String, String>> phoneNumbers;
    public List<String> addresses;
    public List<Pair<Event,String>> events;
    public List<Pair<String, Pair<String,String>>> management;

    public Person() {
        phoneNumbers = new ArrayList<>();
        addresses = new ArrayList<>();
        events = new ArrayList<>();
        management = new ArrayList<>();
    }

    @Override
    public String toString() {
        Field[] fields = Person.class.getDeclaredFields();
        Object[] fieldsObject = Arrays.stream(fields).filter(field -> {
            try {
                return !(field.getName().equals("$change") || field.getName().equals("serialVersionUID") || field.get(this) == null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return false;
        }).toArray();
        fields = Arrays.copyOf(fieldsObject, fieldsObject.length, Field[].class);

        try {
            StringBuilder builder = new StringBuilder();
            builder.append("{");

            for (int i = 0; i < fields.length; i++) {
                Object value = fields[i].get(this);
                builder.append("\"").append(fields[i].getName()).append("\":\"").append(value.toString()).append("\"");

                if (i < fields.length - 1) builder.append(", ");
                else builder.append("}");
            }

            return builder.toString();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("unused")
    public static Person interpretJSONPerson(String jsonPerson) {
        try {
            JSONObject json = new JSONObject(jsonPerson);
            Person person = new Person();

            Field[] fields = Person.class.getDeclaredFields();

            for (Field field : fields) {
                if (field.getName().equals("$change") || field.getName().equals("serialVersionUID")) continue;
                try {
                    if (boolean.class.equals(field.getType()))
                        field.setBoolean(person, Boolean.valueOf(json.getString(field.getName())));
                    else if (int.class.equals(field.getType()))
                        field.setInt(person, Integer.valueOf(json.getString(field.getName())));
                    else if (!json.getString(field.getName()).equals("null"))
                        field.set(person, json.get(field.getName()));
                } catch (IllegalArgumentException | JSONException ignored) {}
            }

            return person;
        } catch (JSONException | IllegalAccessException ignored) {}
        return null;
    }
}
