package com.materialnotes.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by Ian C on 13/05/2016.
 */
public class EditTextCursorWatcher extends EditText {
    public EditTextCursorWatcher(Context context) {
        super(context);
    }

    public EditTextCursorWatcher(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextCursorWatcher(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
            /*if(EditNoteActivity.t.isInterrupted()) {
                EditNoteActivity.t = new Thread();
                EditNoteActivity.t.start();
                Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();
            }*/

    }
}
