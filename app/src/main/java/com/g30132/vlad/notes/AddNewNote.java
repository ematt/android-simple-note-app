package com.g30132.vlad.notes;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AddNewNote extends AppCompatActivity {

    protected Toolbar toolbar;
    protected EditText titleEditBox;
    protected LinedEditText contentEditBox;
    protected Boolean titleEdited = false;
    protected Boolean saved = true;
    protected Boolean discarded = false;
    protected Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);

        intent = getIntent();

        setupUI();

        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null)
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            }
    }

    private void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            contentEditBox.setText( sharedText );
        }
    }

    protected void setupUI() {
        TextWatcher textWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                titleEdited = true;
                saved = false;
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        };

        titleEditBox = (EditText) findViewById(R.id.toolbarNoteTitle);
        assert titleEditBox != null;
        titleEditBox.addTextChangedListener(textWatcher);

        toolbar = (Toolbar) findViewById(R.id.editToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        TextWatcher textWatcherContent = new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                saved = false;
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        };

        contentEditBox = (LinedEditText) findViewById(R.id.editText);
        assert contentEditBox != null;
        contentEditBox.requestFocus();
        contentEditBox.addTextChangedListener(textWatcherContent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_all_notes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_save:
                saveNote();
                finish();
                return true;
            case R.id.action_discard:
                discarded = true;
                finish();
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {

        saveNote();

        super.onPause();
    }

    protected void saveNote() {
        if( saved ) return;
        if (discarded) return;
        if (!validNote()) return;


        String noteContent = contentEditBox.getText().toString();
        String noteTitle = extractNoteTitle(noteContent);

        Note note = new Note(noteTitle, noteContent);
        note.save();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        saved = true;
    }

    @NonNull
    protected String extractNoteTitle(String noteContent) {
        String noteTitle = "";
        if( titleEdited ) {
            noteTitle = titleEditBox.getText().toString();
        } else {
            String[] resultnl = noteContent.split("\\r?\\n", 3);
            if ( resultnl[0].length() > 10) {
                String[] result = noteContent.split(" ", 5);
                int i = 0;
                while (noteTitle.length() < 10 && i < result.length) {
                    noteTitle += result[i++];
                    if( i < result.length)
                        noteTitle += " " + result[i++];
                }
            } else {
                noteTitle = resultnl[0];
            }
        }
        return noteTitle;
    }

    protected boolean validNote() {
        return contentEditBox.getText().length() != 0;
    }
}
