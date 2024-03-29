package com.jonahbauer.qed.layoutStuff;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CompoundButton;

import com.jonahbauer.qed.R;

/**
 * Base on https://stackoverflow.com/a/40939367/3950497 answer.
 */
public class CheckBoxTriStates extends android.support.v7.widget.AppCompatCheckBox {

    static public final int UNKNOW = -1;

    static public final int UNCHECKED = 0;

    static public final int CHECKED = 1;

    private int state;

    /**
     * This is the listener set to the super class which is going to be evoke each
     * time the check state has changed.
     */
    private final OnCheckedChangeListener privateListener = new CompoundButton.OnCheckedChangeListener() {

        // checkbox status is changed from uncheck to checked.
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (state) {
                case UNKNOW:
                    setState(UNCHECKED);
                    break;
                case UNCHECKED:
                    setState(CHECKED);
                    break;
                case CHECKED:
                    setState(UNKNOW);
                    break;
            }
        }
    };

    /**
     * Holds a reference to the listener set by a client, if any.
     */
    private OnCheckedChangeListener clientListener;

    /**
     * This flag is needed to avoid accidentally changing the current {@link #state} when
     * {@link #onRestoreInstanceState(Parcelable)} calls {@link #setChecked(boolean)}
     * evoking our {@link #privateListener} and therefore changing the real state.
     */
    private boolean restoring;

    public CheckBoxTriStates(Context context) {
        super(context);
        init();
    }

    public CheckBoxTriStates(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CheckBoxTriStates(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        if(!this.restoring && this.state != state) {
            this.state = state;

            if(this.clientListener != null) {
                this.clientListener.onCheckedChanged(this, this.isChecked());
            }

            updateBtn(false);
        }
    }

    @Override
    public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {

        // we never truly set the listener to the client implementation, instead we only hold
        // a reference to it and evoke it when needed.
        if(this.privateListener != listener) {
            this.clientListener = listener;
        }

        // always use our implementation
        super.setOnCheckedChangeListener(privateListener);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);

        ss.state = state;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        this.restoring = true; // indicates that the ui is restoring its state
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setState(ss.state);
        requestLayout();
        this.restoring = false;
    }

    private void init() {
        state = UNKNOW;
        updateBtn(true);
        setOnCheckedChangeListener(this.privateListener);
    }

    private void updateBtn(boolean isInit) {
        int btnDrawable = R.drawable.ic_checkbox_checked_to_unknown;
        switch (state) {
            case UNKNOW:
                if (isInit) btnDrawable = R.drawable.ic_checkbox_unknown;
                else btnDrawable = R.drawable.ic_checkbox_checked_to_unknown_animated;
                break;
            case UNCHECKED:
                if (isInit) btnDrawable = R.drawable.ic_checkbox_unchecked;
                else btnDrawable = R.drawable.ic_checkbox_unknown_to_unchecked_animated;
                break;
            case CHECKED:
                if (isInit) btnDrawable = R.drawable.ic_checkbox_checked;
                else btnDrawable = R.drawable.ic_checkbox_unchecked_to_checked_animated;
                break;
        }
        setButtonDrawable(btnDrawable);
        if (getButtonDrawable() instanceof Animatable) ((Animatable)getButtonDrawable()).start();
    }

    static class SavedState extends BaseSavedState {
        int state;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            state = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(state);
        }

        @Override
        public String toString() {
            return "CheckboxTriState.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " state=" + state + "}";
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}