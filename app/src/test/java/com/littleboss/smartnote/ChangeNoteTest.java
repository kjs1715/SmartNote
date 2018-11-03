package com.littleboss.smartnote;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ChangeNoteTest {
    private final String newTitle = "newTitle" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    private final String content = "...content...";
    @Test
    public void startTest() {
        NoteDatabase noteDatabase = NoteDatabase.getInstance();
        String result = "";
        try {
            noteDatabase.saveNoteByTitle("", newTitle, content);
            result = noteDatabase.getNotesByTitle(newTitle);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(result, content);
    }
}
