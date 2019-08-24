package com.jonahbauer.qed.activities;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SectionIndexer;
import android.widget.TabHost;
import android.widget.TableRow;
import android.widget.TextView;

import com.jonahbauer.qed.R;
import com.jonahbauer.qed.layoutStuff.AnimatedTabHostListener;
import com.jonahbauer.qed.qeddb.QEDDBEvent;
import com.jonahbauer.qed.qeddb.QEDDBEventReceiver;
import com.jonahbauer.qed.qeddb.event.Event;
import com.jonahbauer.qed.qeddb.person.Person;

import java.text.MessageFormat;
import java.util.List;

public class EventBottomSheet extends BottomSheetDialogFragment implements QEDDBEventReceiver {
    public static final String EXTRA_SESSION_ID = "sessionId";
    public static final String EXTRA_EVENT_ID = "eventId";
    public static final String EXTRA_COOKIE = "cookie";

    private Event event;

    private ProgressBar progressBar;
    private ViewGroup tabcontent;
    private TextView nameBigTextView;
    private TextView timeTextView;
    private TextView deadlineTextView;
    private TextView costTextView;
    private TextView maxMembersTextView;
    private TextView hotelTextView;
    private TableRow hotelTableRow;
    private TableRow orgaTableRow;
    private LinearLayout orgaLinearLayout;

    public static EventBottomSheet newInstance(char[] sessionId, char[] cookie, String eventId) {
        Bundle args = new Bundle();
        args.putCharArray(EXTRA_SESSION_ID, sessionId);
        args.putCharArray(EXTRA_COOKIE, cookie);
        args.putString(EXTRA_EVENT_ID, eventId);
        EventBottomSheet fragment = new EventBottomSheet();
        fragment.setArguments(args);
        return fragment;
    }

    public static EventBottomSheet newInstance(char[] sessionId, char[] cookie, Event event) {
        Bundle args = new Bundle();
        args.putCharArray(EXTRA_SESSION_ID, sessionId);
        args.putCharArray(EXTRA_COOKIE, cookie);
        args.putString(EXTRA_EVENT_ID, event.id);
        EventBottomSheet fragment = new EventBottomSheet();
        fragment.event = event;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        assert args != null;
        char[] sessionId = args.getCharArray(EXTRA_SESSION_ID);
        String eventId = args.getString(EXTRA_EVENT_ID);
        char[] cookie = args.getCharArray(EXTRA_COOKIE);

        args.putCharArray(EXTRA_SESSION_ID, new char[0]);
        args.putCharArray(EXTRA_COOKIE, new char[0]);

        QEDDBEvent qeddbEvent = new QEDDBEvent();
        qeddbEvent.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this, sessionId, cookie, event != null ? event : eventId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_event, container);

        progressBar = view.findViewById(R.id.progress_bar);
        tabcontent = view.findViewById(android.R.id.tabcontent);
        TabHost tabHost = view.findViewById(R.id.tabHost);

        nameBigTextView = view.findViewById(R.id.event_name_big);
        timeTextView = view.findViewById(R.id.event_time_text);
        deadlineTextView = view.findViewById(R.id.event_deadline_text);
        costTextView = view.findViewById(R.id.event_cost_text);
        maxMembersTextView = view.findViewById(R.id.event_max_member_text);
        hotelTextView = view.findViewById(R.id.event_hotel_text);
        hotelTableRow = view.findViewById(R.id.event_hotel_row);
        orgaTableRow = view.findViewById(R.id.event_orga_row);
        orgaLinearLayout = view.findViewById(R.id.event_orga_list);

        tabHost.setup();

        //Tab 1
        TabHost.TabSpec spec = tabHost.newTabSpec(getString(R.string.event_tab_overview));
        spec.setContent(R.id.tab1);
        spec.setIndicator(getString(R.string.event_tab_overview));
        tabHost.addTab(spec);

        //Tab 2
        spec = tabHost.newTabSpec(getString(R.string.event_tab_members));
        spec.setContent(R.id.tab2);
        spec.setIndicator(getString(R.string.event_tab_members));
        tabHost.addTab(spec);

        tabHost.setOnTabChangedListener(new AnimatedTabHostListener(getContext(), tabHost));

        return view;
    }


    @Override
    public void onEventReceived(Event event) {
        nameBigTextView.setText(event.name);
        if (event.start != null && event.end != null)
            timeTextView.setText(MessageFormat.format("{0,date,dd.MM.yyyy} - {1,date,dd.MM.yyyy}", event.start, event.end));
        else
            timeTextView.setText(MessageFormat.format("{0} - {1}", event.startString, event.endString));
        if (event.deadline != null)
            deadlineTextView.setText(MessageFormat.format("{0,date,dd.MM.yyyy HH:mm:ss}", event.deadline));
        else
            deadlineTextView.setText(event.deadlineString);
        costTextView.setText(event.cost);
        maxMembersTextView.setText(event.maxMember);
        if (event.hotel != null) {
            hotelTableRow.setVisibility(View.VISIBLE);
            hotelTextView.setText(event.hotel);
        }

        for (Person person : event.organizer) {
            addListItem(orgaLinearLayout, orgaTableRow, person.firstName + " " + person.lastName, "Orga");
        }

        progressBar.setVisibility(View.GONE);
        tabcontent.setVisibility(View.VISIBLE);
    }

    public void addListItem(LinearLayout list, TableRow row, String title, String subtitle) {
        row.setVisibility(View.VISIBLE);

        assert getContext() != null;

        TextView titleTextView = new TextView(getContext());
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        titleTextView.setTextColor(getContext().getColor(android.R.color.black));
        titleTextView.setText(title);
        list.addView(titleTextView);

        if (subtitle != null) {
            TextView subtitleTextView = new TextView(getContext());
            subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            subtitleTextView.setText(subtitle);
            list.addView(subtitleTextView);
        }

        ImageView dividerImageView = new ImageView(getContext());
        dividerImageView.setImageResource(R.drawable.person_divider);
        LinearLayout.LayoutParams dividerLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics())
        );
        dividerLayoutParams.setMargins(
                0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()),
                0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics())
        );
        list.addView(dividerImageView, dividerLayoutParams);
    }

    private class PersonListAdapter extends ArrayAdapter<Pair<Person,String>> implements SectionIndexer {
        private List<Pair<Person, String>> personList;
        private Context context;

        PersonListAdapter(Context context, @NonNull List<Pair<Person,String>> personList) {
            super(context, 0, personList);
            this.personList = personList;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            return null;
        }

        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }
}
