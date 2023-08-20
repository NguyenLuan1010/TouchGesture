package com.dyson.tech.touchgesture.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.dyson.tech.touchgesture.model.Notes;

import java.util.List;

@Dao
public interface NotesDAO {

    @Insert
    void insertNote(Notes notes);

    @Query("SELECT * FROM notes ORDER BY timeStart DESC")
    LiveData<List<Notes>> getAllNotes();

    @Query("SELECT * FROM notes WHERE timeStart BETWEEN :start AND :end ORDER BY timeStart DESC ")
    List<Notes> getNoteByTime(long start, long end);

    @Query("SELECT * FROM notes WHERE id =:id")
    Notes getNoteById(int id);

    @Query("DELETE FROM notes WHERE id = :id")
    void deleteNote(int id);

    @Query("UPDATE notes SET title= :title, " +
            "timeStart = :start, " +
            "timeEnd = :end, " +
            "description = :description WHERE id = :id")
    void updateNotes(int id, String title, long start, long end, String description);

    @Query("UPDATE notes SET status =:status WHERE id= :id")
    void updateStatus(int id, int status);
}
