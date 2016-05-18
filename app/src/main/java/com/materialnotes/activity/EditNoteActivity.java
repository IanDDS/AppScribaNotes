package com.materialnotes.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.materialnotes.R;
import com.materialnotes.data.Note;
import com.materialnotes.util.Strings;
import com.shamanland.fab.FloatingActionButton;

import java.util.Date;

import no.nordicsemi.android.scriba.hrs.HRSActivity;
import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * Edit notes activity
 **/
@ContentView(R.layout.activity_edit_note)
public class EditNoteActivity extends RoboActionBarActivity {

    private static final String EXTRA_NOTE = "EXTRA_NOTE";
    private static final int FILTER_ID = 1;
    private static final String TAG = "Mode: ";

    @InjectView(R.id.note_title)   private EditText noteTitleText;
    @InjectView(R.id.note_content) private EditText noteContentText;
    @InjectView(R.id.popup_button) private FloatingActionButton popupButton;


    private Note note;
    private SpannableStringBuilder ssbtitle,ssbcontent;
    public TextView valTv, valTv2;
    private ActionMode mActionMode = null;

    Vibrator v;

    public Thread t;
    public Thread myThread;

    TextView tv;
    int count;//variable to keep track of number of times popup menu opens when condition is met i.e. when value is between 0 and 300.

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onActionModeStarted(final ActionMode mode) {
        if (mActionMode == null) {
            mActionMode = mode;
            Menu menu = mode.getMenu();
            //menu.removeItem(android.R.id.selectAll);
            //menu.add(0, R.id.clear1, 1, "Clear Format");
            // Remove the default menu items (select all, copy, paste, search)
            //menu.clear();

            // Inflate your own menu items
            mode.getMenuInflater().inflate(R.menu.my_custom_menu, menu);
        }


        myThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                valTv2.setText(String.valueOf(HRSActivity.mHrmValue));
                                deselectText();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        myThread.start();

        super.onActionModeStarted(mode);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onContextualMenuItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear1:
                unboldcontent(item);
                break;
            default:
                break;
        }

        // This will likely always be true, but check it anyway, just in case
        if (mActionMode != null) {
            mActionMode.finish();
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        restartMode();

        mActionMode = null;
        super.onActionModeFinished(mode);
    }

    /**
     * Makes the intent to call the activity with an existing note
     *
     * @param context the context
     * @param note the note to edit
     * @return the Intent.
     */
    public static Intent buildIntent(Context context, Note note) {
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EXTRA_NOTE, note);
        return intent;
    }

    /** Makes the intent to call the activity for creating a note
     *
     * @param context the context that calls the activity
     * @return the Intent.
     */
    public static Intent buildIntent(Context context) {
        return buildIntent(context, null);
    }

    /**
     * Gets the edited note
     *
     * @param intent the intent from onActivityResult
     * @return the updated note
     */
    public static Note getExtraNote(Intent intent) {
        return (Note) intent.getExtras().get(EXTRA_NOTE);
    }

    /** {@inheritDoc} */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ssbtitle= (SpannableStringBuilder) noteTitleText.getText();
        ssbcontent= (SpannableStringBuilder) noteContentText.getText();

        valTv = (TextView)findViewById(R.id.valTv);
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/airstrike.ttf");
        valTv.setTypeface(type);

        valTv2 = (TextView)findViewById(R.id.valTv2);
        valTv2.setText(String.valueOf(100.0));

        tv = new TextView(this);

        popupButton = (FloatingActionButton) findViewById(R.id.popup_button);
        //popupButton.setColor(Color.GRAY);//set color of floating action button
        //popupButton.initBackground();//called to initialize the changes to the background
        //popupButton.setShadow(false);
        popupButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onClick(View v) {
                showPopup(v);
            }
        });//closing the setOnClickListener method

		// Starts the components //////////////////////////////////////////////////////////////
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Shows the go back arrow
        note = (Note) getIntent().getSerializableExtra(EXTRA_NOTE); // gets the note from the intent
        if (note != null) { // Edit existing note
            noteTitleText.setText(com.materialnotes.activity.Html.fromHtml(note.getTitle()));
            noteContentText.setText(com.materialnotes.activity.Html.fromHtml(note.getContent()));
        } else { // New note

            note = new Note();
            note.setCreatedAt(new Date());
        }


        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                //valTv.setText(String.valueOf(HRSActivity.mHrmValue));
                                tv.setText(String.valueOf(HRSActivity.mHrmValue));
                                format();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        noteTitleText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int startSelection = noteTitleText.getSelectionStart();
                int endSelection = noteTitleText.getSelectionEnd();

                noteTitleText.setSelection(startSelection, endSelection);

                return false;
            }
        });

        noteContentText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int startSelection = noteContentText.getSelectionStart();
                int endSelection = noteContentText.getSelectionEnd();

                noteContentText.setSelection(startSelection, endSelection);

                return false;
            }
        });

        // Get instance of Vibrator from current Context
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }


        /** {@inheritDoc} */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_note, menu);
        //tv.setText(String.valueOf(HRSActivity.mHrmValue));
        tv.setTextColor(getResources().getColor(R.color.white_circle));
        //tv.setOnClickListener((View.OnClickListener) this);
        tv.setPadding(5, 0, 5, 0);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextSize(14);
        menu.add(0, FILTER_ID, 1, String.valueOf(HRSActivity.mHrmValue)).setActionView(tv).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_save:
                if (isNoteFormOk()) {
                    setNoteResult();
                    finish();
                } else validateNoteForm();
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    /** @return {@code true} is the note has title and content; {@code false} every other case */
    private boolean isNoteFormOk() {
        //noteTitleText.setTypeface(null, Typeface.NORMAL);
        return !Strings.isNullOrBlank(noteTitleText.getText().toString()) && !Strings.isNullOrBlank(noteContentText.getText().toString());
    }

    /**
     * Updates the note content with the layout texts and it makes the object as a result of the activity
     */
    private void setNoteResult() {
        note.setTitle(com.materialnotes.activity.Html.toHtml(noteTitleText.getText()));
        note.setContent(com.materialnotes.activity.Html.toHtml(noteContentText.getText()));
        note.setUpdatedAt(new Date());
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_NOTE, note);
        setResult(RESULT_OK, resultIntent);
    }

    /** Shows validating messages  */
    private void validateNoteForm() {
        StringBuilder message = null;
        if (Strings.isNullOrBlank(noteTitleText.getText().toString())) {
            message = new StringBuilder().append(getString(R.string.title_required));
        }
        if (Strings.isNullOrBlank(noteContentText.getText().toString())) {
            if (message == null) message = new StringBuilder().append(getString(R.string.content_required));
            else message.append("\n").append(getString(R.string.content_required));
        }
        if (message != null) {
            Toast.makeText(getApplicationContext(),
                    message,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onBackPressed() {
        // Note not created or updated
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }

    public void boldcontent(){
        ssbcontent=(SpannableStringBuilder)noteContentText.getText();
        ssbcontent.setSpan(new StyleSpan(Typeface.BOLD),noteContentText.getSelectionStart(),noteContentText.getSelectionEnd(),0);

    }

    public void boldtitle(){
        ssbtitle=(SpannableStringBuilder)noteTitleText.getText();
        ssbtitle.setSpan(new StyleSpan(Typeface.BOLD),noteTitleText.getSelectionStart(),noteTitleText.getSelectionEnd(),0);
    }

    public void italictitle(){
        ssbtitle=(SpannableStringBuilder)noteTitleText.getText();
        ssbtitle.setSpan(new StyleSpan(Typeface.ITALIC),noteTitleText.getSelectionStart(),noteTitleText.getSelectionEnd(),0);

    }

    public void italiccontent(){
        ssbcontent=(SpannableStringBuilder)noteContentText.getText();
        ssbcontent.setSpan(new StyleSpan(Typeface.ITALIC),noteContentText.getSelectionStart(),noteContentText.getSelectionEnd(),0);
    }

    public void underlinecontent(){
        ssbcontent=(SpannableStringBuilder)noteContentText.getText();
        ssbcontent.setSpan(new UnderlineSpan(),noteContentText.getSelectionStart(),noteContentText.getSelectionEnd(),0);
    }

    public void underlinetitle(){
        ssbtitle=(SpannableStringBuilder)noteTitleText.getText();
        ssbtitle.setSpan(new UnderlineSpan(),noteTitleText.getSelectionStart(),noteTitleText.getSelectionEnd(),0);
    }

    public void highlightTitle(){
        int startSelection = noteTitleText.getSelectionStart();
        int endSelection = noteTitleText.getSelectionEnd();

        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
        for(int i = 0; i < bgSpan.length; i++){
            ssbtitle.removeSpan(bgSpan[i]);
        }
        ssbtitle=(SpannableStringBuilder)noteTitleText.getText();
        ssbtitle.setSpan(new BackgroundColorSpan(Color.YELLOW),noteTitleText.getSelectionStart(),noteTitleText.getSelectionEnd(),0);
    }

    public void highlightContent(){
        int startSelection = noteContentText.getSelectionStart();
        int endSelection = noteContentText.getSelectionEnd();

        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
        for(int i = 0; i < bgSpan.length; i++){
            ssbcontent.removeSpan(bgSpan[i]);
        }
        ssbcontent=(SpannableStringBuilder)noteContentText.getText();
        ssbcontent.setSpan(new BackgroundColorSpan(Color.YELLOW),noteContentText.getSelectionStart(),noteContentText.getSelectionEnd(),0);
    }

    public void unboldcontent(MenuItem item){
        if (noteContentText.hasFocus()) {
            int startSelection = noteContentText.getSelectionStart();
            int endSelection = noteContentText.getSelectionEnd();

            Spannable str = noteContentText.getText();
            StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);

            for (int i = 0; i < ss.length; i++) {
                if (ss[i].getStyle() == Typeface.BOLD || ss[i].getStyle() == Typeface.ITALIC) {
                    str.removeSpan(ss[i]);
                }
            }

            UnderlineSpan[] ulSpan = str.getSpans(startSelection, endSelection, UnderlineSpan.class);
            for (int i = 0; i < ulSpan.length; i++) {
                str.removeSpan(ulSpan[i]);
            }

            BackgroundColorSpan[] bgSpan = str.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
            for(int i = 0; i < bgSpan.length; i++){
                str.removeSpan(bgSpan[i]);
            }

            noteContentText.setText(str);
            //noteContentText.setSelection(startSelection, endSelection);
        }else{
            int startSelection = noteTitleText.getSelectionStart();
            int endSelection = noteTitleText.getSelectionEnd();

            Spannable str = noteTitleText.getText();
            StyleSpan[] ss = str.getSpans(startSelection, endSelection, StyleSpan.class);

            for (int i = 0; i < ss.length; i++) {
                if (ss[i].getStyle() == Typeface.BOLD || ss[i].getStyle() == Typeface.ITALIC){
                    str.removeSpan(ss[i]);
                }
            }

            UnderlineSpan[] ulSpan = str.getSpans(startSelection, endSelection, UnderlineSpan.class);
            for (int i = 0; i < ulSpan.length; i++) {
                str.removeSpan(ulSpan[i]);
            }

            BackgroundColorSpan[] bgSpan = str.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
            for(int i = 0; i < bgSpan.length; i++){
                str.removeSpan(bgSpan[i]);
            }

            noteTitleText.setText(str);
            //noteTitleText.setSelection(startSelection, endSelection);
        }
    }

    public void format() {
        if (HRSActivity.mHrmValue > 600 && HRSActivity.mHrmValue < 901) {
            valTv.setText("Mode: Bold");
            if (noteTitleText.hasSelection()) {
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                boldtitle();
            } else if(noteContentText.hasSelection()) {
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                boldcontent();
            }
        } else if (HRSActivity.mHrmValue > 300 && HRSActivity.mHrmValue < 601) {
            valTv.setText("Mode: Underline");
            if (noteTitleText.hasSelection()) {
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                underlinetitle();
            } else if(noteContentText.hasSelection()) {
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                underlinecontent();
            }
        } else if (HRSActivity.mHrmValue < 301 && HRSActivity.mHrmValue > 50) {
            valTv.setText("Mode: Highlight");
            if (noteTitleText.hasSelection()) {
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                //underlinetitle();
                //showPopup(popupButton);
                highlightTitle();
            } else if(noteContentText.hasSelection()) {
                //underlinecontent();
                t.interrupt();
                // Vibrate for 500 milliseconds/1/2 second
                v.vibrate(250);
                if(count == 0) {
                    showPopup(popupButton);
                }
                count++;
                highlightContent();
            }
        /*}else if(HRSActivity.mHrmValue < 101 && HRSActivity.mHrmValue > 0){
            if(noteTitleText.hasFocus()){
                //showPopup(popupButton);
                //highlightTitle();
                noteTitleText.clearFocus();
            }else{
                //showPopup(popupButton);
                //highlightContent();
                noteContentText.clearFocus();
            }
            valTv.setText("Mode: Deselect");*/
        }else if(HRSActivity.mHrmValue > 900){
            valTv.setText("Mode:");
        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showPopup(final View v) {
        final IconizedMenu popup = new IconizedMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.popup_menu, popup.getMenu());

        popup.setOnDismissListener(new IconizedMenu.OnDismissListener() {
            @Override
            public void onDismiss(IconizedMenu menu) {
                count = 0;
            }
        });

        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Toast.makeText(EditNoteActivity.this,"You Clicked : " + item.getTitle(),Toast.LENGTH_SHORT).show();
                Snackbar.make(v, "You Chose : " + item.getTitle(), Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();

                if(item.getTitle().equals("Green")) {
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.green_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);

                    } else if(noteContentText.hasSelection()) {
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.green_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }
                }else if(item.getTitle().equals("Light Green")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_green_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);

                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_green_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Red")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.red_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.red_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Light Red")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_red_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_red_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Blue")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.blue_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.blue_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Light Blue")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_blue_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.light_blue_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }
                }else if(item.getTitle().equals("Orange")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.orange_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.orange_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }
                }else if(item.getTitle().equals("Yellow")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.yellow_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.yellow_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Pink")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.pink_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.pink_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Purple")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.purple_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.purple_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("Black")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.black_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.black_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }

                }else if(item.getTitle().equals("White")){
                    if (noteTitleText.hasSelection()) {
                        int startSelection = noteTitleText.getSelectionStart();
                        int endSelection = noteTitleText.getSelectionEnd();

                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();

                        BackgroundColorSpan[] bgSpan = ssbtitle.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbtitle.removeSpan(bgSpan[i]);
                        }
                        ssbtitle = (SpannableStringBuilder) noteTitleText.getText();
                        ssbtitle.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.white_circle)), noteTitleText.getSelectionStart(), noteTitleText.getSelectionEnd(), 0);
                    } else if(noteContentText.hasSelection()){
                        int startSelection = noteContentText.getSelectionStart();
                        int endSelection = noteContentText.getSelectionEnd();

                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();

                        BackgroundColorSpan[] bgSpan = ssbcontent.getSpans(startSelection, endSelection, BackgroundColorSpan.class);
                        for(int i = 0; i < bgSpan.length; i++){
                            ssbcontent.removeSpan(bgSpan[i]);
                        }
                        ssbcontent = (SpannableStringBuilder) noteContentText.getText();
                        ssbcontent.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.white_circle)), noteContentText.getSelectionStart(), noteContentText.getSelectionEnd(), 0);
                    }
                }
                //popup.dismiss();
                count = 0;
                return true;
            }
        });

        popup.show();
    }

    public void restartMode(){

        v.vibrate(250);

        //rerun the thread
        t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // update TextView here!
                                tv.setText(String.valueOf(HRSActivity.mHrmValue));
                                format();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();

        myThread.interrupt();
    }

    public void deselectText(){
        if(Float.valueOf(valTv2.getText().toString()) < 50){
            restartMode();
            Log.d(TAG, "Restarted");
            mActionMode.finish();
        }
    }

}