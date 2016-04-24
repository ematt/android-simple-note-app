package com.g30132.vlad.notes;

import com.orm.SugarRecord;

/**
 * Created by vlad on 4/23/16.
 */
public class Note extends SugarRecord {
    String title;
    String content;

    public Note() {}

    public Note( String title, String text ) {
        this.title = title;
        this.content = text;
    }

    public String getContent() {
        return content;
    }

    public String toString() {
        return title + "-" + content;
    }


    public String getTitle() {
        return title;
    }

    
}
