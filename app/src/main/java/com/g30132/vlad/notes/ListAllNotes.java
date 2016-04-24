package com.g30132.vlad.notes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.orm.SugarContext;

import java.util.List;

public class ListAllNotes extends AppCompatActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_notes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ListAllNotes.this, AddNewNote.class);
                startActivity(i);
            }
        });

        SugarContext.init(this);

        populateListView();

    }

    private void populateListView() {
        List<Note> noteList = Note.listAll(Note.class);
        noteList = Lists.reverse(noteList);

        list = (ListView)findViewById(R.id.listViewNotes);

        if (list != null) {
            NoteAdapter noteadapter = new NoteAdapter(this, noteList);
            list.setAdapter(noteadapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Long noteID = (Long) view.getTag();
                    viewNoteProcedure(noteID);
                }
            });
            registerForContextMenu(list);
        }
    }

    @Override
    public void onResume() {
        populateListView();
        super.onResume();
    }

    private void editNoteProcedure( Long noteID ) {
        Intent intent = new Intent(ListAllNotes.this, EditNote.class);
        intent.putExtra("noteID", noteID);
        startActivity(intent);
    }

    private void viewNoteProcedure( Long noteID) {
        Intent intent = new Intent(ListAllNotes.this, ViewNote.class);
        intent.putExtra("noteID", noteID);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId()==R.id.listViewNotes) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.note_list_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        View view = info.targetView;
        Long noteID = (Long) view.getTag();
        switch(item.getItemId()) {
            case R.id.context_menu_edit:
                editNoteProcedure(noteID);
                return true;
            case R.id.context_menu_delete:
                deleteNoteProcedure(noteID);
                return true;
            case R.id.context_menu_share:
                shareNoteProcedure(noteID);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void shareNoteProcedure(Long noteID) {
        Note note = Note.findById(Note.class, noteID);
        if (note == null) {
            Toast.makeText(ListAllNotes.this, "Note not found", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, note.getTitle() + '\n' + note.getContent());
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }
    }

    private void deleteNoteProcedure(final Long noteID) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    Note note = Note.findById(Note.class, noteID);
                    if (note == null) {
                        Toast.makeText(ListAllNotes.this, "Note not found", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        if (note.delete())
                            Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                        populateListView();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}
