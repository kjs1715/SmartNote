package com.littleboss.smartnote;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

@RunWith(RobolectricTestRunner.class)
public class NoteDatabaseTest {
    private NoteDatabase database = null;
    @Before
    public void setup() {
        NoteDatabase.dropDatabaseIfExist();
        database = NoteDatabase.getInstance();
    }

    @Test
    public void test() throws Exception {
        String q1 = database.getNotesByTitle("title");
        database.saveNoteByTitle("", "title", "content", null);
        database.saveNoteByTitle("title", "title", "content2", null);
        String q2 = database.getNotesByTitle("title");
        Collection<Tag> tags = new HashSet<Tag>();
        tags.add(new Tag("tag1"));
        tags.add(new Tag("tag2"));
        database.saveNoteByTitle("title", "title", "content2", "tag1 tag2");
        LinkedList<ListData> l1 = database.getNotesTitleListContainTags(tags, true);
        LinkedList<ListData> l2 = database.getNotesTitleListContainTags(tags, false);
        LinkedList<ListData> l3 = database.getNotesTitleListContainKeywords("title title2");
        LinkedList<ListData> l4 = new LinkedList<ListData>();
        l4.add(new ListData("title", null, null, null));
        database.deleteNotesTitleList(l4);
    }

    @After
    public void after() {
        database.closeConnection();
    }
}
