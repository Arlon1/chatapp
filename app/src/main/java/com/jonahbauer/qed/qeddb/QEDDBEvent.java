package com.jonahbauer.qed.qeddb;

import android.os.AsyncTask;

import com.jonahbauer.qed.Application;
import com.jonahbauer.qed.R;
import com.jonahbauer.qed.qeddb.event.Event;
import com.jonahbauer.qed.qeddb.person.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class QEDDBEvent extends AsyncTask<Object, Void, String[]> {
    private QEDDBEventReceiver receiver;

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMANY);
    private final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMANY);

    private Event event;

    @Override
    protected String[] doInBackground(Object...objects) {
        char[] sessionId;
        char[] cookie;
        String eventId;

        if (objects.length < 4) return null;
        if (objects[0] instanceof QEDDBEventReceiver) receiver = (QEDDBEventReceiver) objects[0];
        else return null;

        if (objects[1] instanceof char[]) sessionId = (char[]) objects[1];
        else return null;

        if (objects[2] instanceof char[]) cookie = (char[]) objects[2];
        else return null;

        if (objects[3] instanceof String) {
            event = new Event();
            eventId = (String) objects[3];
        } else if (objects[3] instanceof Event) {
            event = (Event) objects[3];
            eventId = event.id;
        } else return null;

        String[] result = new String[2];
        for (int i = 1 ; i <= 2; i++) {
            try {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(String.format(Application.getContext().getString(R.string.database_server_event), String.valueOf(sessionId), eventId, String.valueOf(i))).openConnection();
                httpsURLConnection.setRequestMethod("POST");
                httpsURLConnection.setDoOutput(true);
                httpsURLConnection.setInstanceFollowRedirects(false);
                httpsURLConnection.setRequestProperty("Cookie", String.valueOf(cookie));
                httpsURLConnection.setUseCaches(false);
                httpsURLConnection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(httpsURLConnection.getInputStream()));
                String inputLine;
                StringBuilder builder = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    builder.append(inputLine);
                }
                in.close();

                httpsURLConnection.disconnect();

                result[i - 1] = builder.toString();
            } catch (IOException e) {
                return null;
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String[] string) {
        if (string == null) {
            receiver.onEventReceived(null);
            return;
        }

        String overview = string[0];
        String[] tableRows = overview.split("(?:</tr><tr>)|(?:<tr>)|(?:</tr>)");
        for (String row : tableRows) {
            String[] params = row.split("(?:<div class=\' veranstaltung_)|(?: cell\' title=\'\'>)|(?:&nbsp;</div>)");
            if (params.length > 2 && params[2] != null && !params[2].trim().equals("")) switch (params[1]) {
                case "titel":
                    event.name = params[2];
                    break;
                case "start":
                    try {
                        event.start = simpleDateFormat.parse(params[2]);
                    } catch (ParseException ignored) {}
                    event.startString = params[2];
                    break;
                case "ende":
                    try {
                        event.end = simpleDateFormat.parse(params[2]);
                    } catch (ParseException ignored) {}
                    event.endString = params[2];
                    break;
                case "kosten":
                    event.cost = params[2];
                    break;
                case "anmeldeschluss":
                    try {
                        event.deadline = simpleDateFormat2.parse(params[2]);
                    } catch (ParseException ignored) {}
                    event.deadlineString = params[2];
                    break;
                case "max_anzahl_teilnehmer":
                    event.maxMember = params[2];
                    break;
            }
        }


        for (String str : overview.split("</tr>")) {
            Matcher matcher = Pattern.compile("<tr class=\"data\">.*").matcher(str);
            while (matcher.find()) {
                String data = matcher.group();
                if (data.contains("rolle")) {
                    Person person = new Person();
                    Matcher matcher2 = Pattern.compile("<div class=\"[^\"]*\" title=\"([^\"]*)\">([^&]*)&nbsp;</div>").matcher(data);
                    while (matcher2.find()) {
                        String title = matcher2.group(1);
                        String value = matcher2.group(2);
                        switch (title) {
                            case "Vorname":
                                person.firstName = value;
                                break;
                            case "Nachname":
                                person.lastName = value;
                                break;
                        }
                    }
                    event.organizer.add(person);
                }
            }
        }

        receiver.onEventReceived(event);
    }
}
