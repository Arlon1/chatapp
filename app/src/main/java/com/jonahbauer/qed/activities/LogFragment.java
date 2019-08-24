package com.jonahbauer.qed.activities;

import android.graphics.Shader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jonahbauer.qed.R;
import com.jonahbauer.qed.chat.Message;
import com.jonahbauer.qed.chat.MessageAdapter;
import com.jonahbauer.qed.database.ChatDatabase;
import com.jonahbauer.qed.database.ChatDatabaseReceiver;
import com.jonahbauer.qed.layoutStuff.TileDrawable;
import com.jonahbauer.qed.logs.LogGetter;
import com.jonahbauer.qed.logs.LogReceiver;

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LogFragment extends Fragment implements LogReceiver, ChatDatabaseReceiver {
    public static final String LOG_MODE_KEY = "mode";
    public static final String LOG_CHANNEL = "channel";
    public static final String LOG_LAST = "last";
    public static final String LOG_FROM = "from";
    public static final String LOG_TO = "to";

    public static final String LOG_MODE_RECENT_POSTS = "postrecent";
    public static final String LOG_MODE_RECENT_DATE = "daterecent";
    public static final String LOG_MODE_INTERVAL_DATE = "dateinterval";

    private String options;
    private LogGetter logGetter = null;
    private MessageAdapter messageAdapter;

    private ProgressBar searchProgressBar;
    private ProgressBar saveProgressBar;
    private ListView messageListView;

    private ChatDatabase database;

    public static LogFragment newInstance(String mode, String channel, String... data) {
        Bundle args = new Bundle();
        args.putString(LOG_MODE_KEY, mode);
        args.putString(LOG_CHANNEL, channel);

        switch (mode) {
            case LOG_MODE_RECENT_POSTS:
                if (data.length > 0) args.putString(LOG_LAST, data[0]);
                break;
            case LOG_MODE_RECENT_DATE:
                if (data.length > 0) args.putString(LOG_LAST, data[0]);
                break;
            case LOG_MODE_INTERVAL_DATE:
                if (data.length > 0) args.putString(LOG_FROM, data[0]);
                if (data.length > 1) args.putString(LOG_TO, data[1]);
                break;
        }

        LogFragment fragment = new LogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_log, container, false);

        ImageView background = view.findViewById(R.id.background);
        searchProgressBar = view.findViewById(R.id.search_progress);
        saveProgressBar = view.findViewById(R.id.save_progress);
        messageListView = view.findViewById(R.id.log_message_container);

        TileDrawable tileDrawable  = new TileDrawable(view.getContext().getDrawable(R.drawable.background_part), Shader.TileMode.REPEAT);
        background.setImageDrawable(tileDrawable);

        Bundle arguments = getArguments();
        assert arguments != null;

        String mode = arguments.getString(LOG_MODE_KEY);
        String subtitle = "";
        assert mode != null;

        try {
            switch (mode) {
                case LOG_MODE_RECENT_POSTS:
                    options = "mode=" + LOG_MODE_RECENT_POSTS;
                    options += "&last=" + arguments.getString(LOG_LAST);
                    subtitle = MessageFormat.format(getString(R.string.log_subtitle_post_recent), Integer.valueOf(arguments.getString(LOG_LAST)));
                    break;
                case LOG_MODE_RECENT_DATE:
                    options = "mode=" + LOG_MODE_RECENT_DATE;
                    options += "&last=" + arguments.getString(LOG_LAST);
                    subtitle = MessageFormat.format(getString(R.string.log_subtitle_date_recent), Integer.valueOf(arguments.getString(LOG_LAST)) / 3600);
                    break;
                case LOG_MODE_INTERVAL_DATE:
                    options = "mode=" + LOG_MODE_INTERVAL_DATE;
                    options += "&from=" + arguments.getString(LOG_FROM);
                    options += "&to=" + arguments.getString(LOG_TO);

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                    Date dateFrom = dateFormat.parse(arguments.getString(LOG_FROM));
                    Date dateTo = dateFormat.parse(arguments.getString(LOG_TO));

                    subtitle = MessageFormat.format(getString(R.string.log_subtitle_date_interval), dateFrom, dateTo);
                    break;
            }
            options += "&channel=" + arguments.getString(LOG_CHANNEL);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ((TextView) view.findViewById(R.id.log_subtitle)).setText(subtitle);

        messageAdapter = new MessageAdapter(view.getContext(), new ArrayList<>());
        messageListView.setAdapter(messageAdapter);

        messageListView.setPadding(messageListView.getPaddingLeft(),
                messageListView.getPaddingTop(),
                messageListView.getPaddingRight() + messageListView.getVerticalScrollbarWidth(),
                messageListView.getPaddingBottom());

        setHasOptionsMenu(true);

        if (logGetter==null)
            logGetter = new LogGetter();
        logGetter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,options, this, view.getContext());

        database = new ChatDatabase();
        database.init(view.getContext(), this);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_log, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void onDestroy() {
        database.close();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (logGetter!=null) logGetter.cancel(true);
    }

    @Override
    public void onReceiveLogs(List<Message> messages) {
        messages.forEach(messageAdapter::add);
        messageAdapter.notifyDataSetChanged();

        searchProgressBar.setVisibility(View.GONE);
        messageListView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLogError() {
        searchProgressBar.setVisibility(View.GONE);
        Toast.makeText(getContext(), R.string.log_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onReceiveResult(List<Message> messages) {}

    @Override
    public void onDatabaseError() {}

    @Override
    public void onInsertAllUpdate(int done, int total) {
        if (done < total) {
            saveProgressBar.setVisibility(View.VISIBLE);
            saveProgressBar.setProgress(done * 100 / total, true);
        } else {
            saveProgressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), getString(R.string.database_save_done), Toast.LENGTH_SHORT).show();
        }
    }
}
