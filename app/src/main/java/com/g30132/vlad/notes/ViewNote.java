package com.g30132.vlad.notes;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ViewNote extends AppCompatActivity {

    protected Intent intent;
    protected Long noteID;
    protected Note note;
    private TextView noteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        noteTextView = (TextView) findViewById(R.id.viewNoteTextView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareNoteProcedure();
            }
        });

        intent = getIntent();
        noteID = intent.getLongExtra("noteID", -1);
        if (noteID == -1) {
            finish();
        }

        setNote2GUI();

    }

    private void setNote2GUI() {
        note = Note.findById(Note.class, noteID);
        if (note == null) {
            Toast.makeText(ViewNote.this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            noteTextView.setText(note.getContent());
            ((CollapsingToolbarLayout)findViewById(R.id.toolbar_layout)).setTitle( note.getTitle() );
        }
    }

    private void shareNoteProcedure() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + '\n' + note.getContent());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void editNoteProcedure( Long noteID ) {
        Intent intent = new Intent(ViewNote.this, EditNote.class);
        intent.putExtra("noteID", noteID);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        setNote2GUI();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_note, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
                note.delete();
                finish();
                return true;
            case R.id.action_edit:
                editNoteProcedure(noteID);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
