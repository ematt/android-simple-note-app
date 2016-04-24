package com.g30132.vlad.notes;

/**
 * Created by vlad on 4/24/16.
 */
public class NoteImage extends Note {
    String imagePath;

    NoteImage() {};

    public NoteImage( String title, String text, String imagePath ) {
        super(title, text);
        this.imagePath = imagePath;
    }
}
