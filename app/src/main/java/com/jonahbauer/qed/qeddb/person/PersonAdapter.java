package com.jonahbauer.qed.qeddb.person;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jonahbauer.qed.R;
import com.jonahbauer.qed.layoutStuff.FixedHeaderAdapter;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class PersonAdapter extends FixedHeaderAdapter<Person, Character> {
    public PersonAdapter(Context context, @NonNull List<Person> itemList, @NonNull Function<Person, Character> headerMap, Comparator<? super Person> comparator, View fixedHeader) {
        super(context, itemList, headerMap, comparator, fixedHeader);
    }

    @NonNull
    @Override
    protected View getItemView(Person person, @Nullable View convertView, @NonNull ViewGroup parent, LayoutInflater inflater) {
        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            view = Objects.requireNonNull(inflater).inflate(R.layout.list_item_person, parent, false);
        }

        ImageView initialsCircle = view.findViewById(R.id.person_initials_circle);
        switch ((person.firstName + person.lastName).chars().sum()%10) {
            case 0:
                initialsCircle.setColorFilter(Color.argb(255, 0x33, 0xb5, 0xe5));
                break;
            case 1:
                initialsCircle.setColorFilter(Color.argb(255, 0x99, 0xcc, 0x00));
                break;
            case 2:
                initialsCircle.setColorFilter(Color.argb(255, 0xff, 0x44, 0x44));
                break;
            case 3:
                initialsCircle.setColorFilter(Color.argb(255, 0x00, 0x99, 0xcc));
                break;
            case 4:
                initialsCircle.setColorFilter(Color.argb(255, 0x66, 0x99, 0x00));
                break;
            case 5:
                initialsCircle.setColorFilter(Color.argb(255, 0xcc, 0x00, 0x00));
                break;
            case 6:
                initialsCircle.setColorFilter(Color.argb(255, 0xaa, 0x66, 0xcc));
                break;
            case 7:
                initialsCircle.setColorFilter(Color.argb(255, 0xff, 0xbb, 0x33));
                break;
            case 8:
                initialsCircle.setColorFilter(Color.argb(255, 0xff, 0x88, 0x00));
                break;
            case 9:
                initialsCircle.setColorFilter(Color.argb(255, 0x00, 0xdd, 0xff));
                break;
        }


        String name = person.firstName + " " + person.lastName;
        String email = person.email;
        String active = !person.active ? "(inaktiv)" : (!person.member ? "(kein Mitglied)" : "");
        ((TextView)view.findViewById(R.id.person_name)).setText(name);
        ((TextView)view.findViewById(R.id.person_email)).setText(email);
        ((TextView)view.findViewById(R.id.person_active)).setText(active);
        ((TextView)view.findViewById(R.id.person_initials)).setText(String.format("%s%s", String.valueOf(person.firstName.charAt(0)), String.valueOf(person.lastName).charAt(0)));
        ((TextView)view.findViewById(R.id.header)).setText("");

        return view;
    }

    @Override
    protected void setHeader(@NonNull View view, Character header) {
        ((TextView)view.findViewById(R.id.header)).setText(String.valueOf(header));
    }
}
