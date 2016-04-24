package com.g30132.vlad.notes;


import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.orm.SugarContext;

/**
 * Created by vlad on 4/23/16.
 */
public class EditNote  extends AddNewNote {

    protected Long noteID;
    protected Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_note);
        setupUI();

        SugarContext.init(this);

        noteID = intent.getLongExtra("noteID", -1);
        if (noteID == -1) {
            discarded = true;
            finish();
        }
        Toast.makeText(EditNote.this, noteID+"", Toast.LENGTH_SHORT).show();
        note = Note.findById(Note.class, noteID);
        if (note == null) {
            Toast.makeText(EditNote.this, "Note not found", Toast.LENGTH_SHORT).show();
            discarded = true;
            finish();
        } else {
            titleEditBox.setText(note.getTitle());
            contentEditBox.setText(note.getContent());
        }
        saved = true;
    }

    protected void saveNote() {
        if( saved ) return;
        if (discarded) return;
        if (!validNote()) return;

        String noteContent = contentEditBox.getText().toString();
        String noteTitle = extractNoteTitle(noteContent);

        note.title = noteTitle;
        note.content = noteContent;
        note.save();

        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        saved = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.action_delete) {
            note.delete();
            discarded = true;
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_delete, menu);
        return true;
    }
}
