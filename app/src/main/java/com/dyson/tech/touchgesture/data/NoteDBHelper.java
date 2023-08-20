package com.dyson.tech.touchgesture.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.dyson.tech.touchgesture.model.Notes;

@Database(entities = {Notes.class}, version = 1)
public abstract class NoteDBHelper extends RoomDatabase{
    private static final String DATABASE_NAME = "notes.db";
    private static NoteDBHelper helper;

    public static synchronized  NoteDBHelper init(Context context){
        if(helper == null){
            helper = Room.databaseBuilder(context.getApplicationContext(),NoteDBHelper.class,DATABASE_NAME)
                    .allowMainThreadQueries()
                    .build();
        }
        return helper;
    }

  public abstract NotesDAO notesDAO();
}
