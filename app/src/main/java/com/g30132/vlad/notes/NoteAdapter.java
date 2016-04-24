package com.g30132.vlad.notes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by vlad on 4/23/16.
 */
public class NoteAdapter extends ArrayAdapter<Note> {

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Note note = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_note_item, parent, false);
        }
        // Lookup view for data population
        TextView noteTitle = (TextView) convertView.findViewById(R.id.NoteTitle);
        // Populate the data into the template view using the data object
        noteTitle.setText(note.getTitle());
        // Return the completed view to render on screen
        convertView.setTag(note.getId());

        return convertView;
    }
}