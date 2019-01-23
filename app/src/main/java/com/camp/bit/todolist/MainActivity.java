package com.camp.bit.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.camp.bit.todolist.beans.Note;
import com.camp.bit.todolist.beans.State;
import com.camp.bit.todolist.db.TodoContract;
import com.camp.bit.todolist.db.TodoDbHelper;
import com.camp.bit.todolist.debug.DebugActivity;
import com.camp.bit.todolist.ui.NoteListAdapter;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD = 1002;
    private static final int REQUEST_CODE_EDIT = 1005;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper mDbHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbHelper = new TodoDbHelper(getApplicationContext());
        db = mDbHelper.getWritableDatabase();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) {
                MainActivity.this.updateNode(note);
            }

            @Override
            public void editNote(Note note) {
                MainActivity.this.editNote(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        } else if (requestCode == REQUEST_CODE_EDIT
                && resultCode == Activity.RESULT_OK) {
            notesAdapter.refresh(loadNotesFromDatabase());
        }
    }

    private List<Note> loadNotesFromDatabase() {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        if (db == null) {
            return Collections.emptyList();
        }
        List<Note> result = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = db.query(TodoContract.TodoEntry.TABLE_NAME,
                    new String[]{TodoContract.TodoEntry.COLUMN_CONTENT,
                            TodoContract.TodoEntry.COLUMN_DATE,
                            TodoContract.TodoEntry.COLUMN_STATE,
                            TodoContract.TodoEntry.COLUMN_PRIORITY,
                            TodoContract.TodoEntry._ID},
                    null, null,
                    null, null,
                    TodoContract.TodoEntry.COLUMN_PRIORITY + " DESC, " + TodoContract.TodoEntry.COLUMN_DATE + " DESC");
            while (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_CONTENT));
                long dateTime = cursor.getLong(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_DATE));
                int intState = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_STATE));
                int priority = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_PRIORITY));
                long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));

                Note note = new Note(id);
                note.setContent(content);
                note.setDate(new Date(dateTime));
                note.setState(State.from(intState));
                note.setPriority(priority);

                result.add(note);

            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    private void deleteNote(Note note) {
        // TODO 删除数据
        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = { "" + note.id };
        db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) {
        // 更新数据
        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_CONTENT, note.getContent());
        values.put(TodoContract.TodoEntry.COLUMN_DATE, note.getDate().getTime());
        values.put(TodoContract.TodoEntry.COLUMN_STATE, note.getState().intValue);
        values.put(TodoContract.TodoEntry.COLUMN_PRIORITY, note.getPriority());

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = { "" + note.id };

        db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void editNote(Note note) {
        long id = note.id;
        //TODO: editNote
        Intent intent = new Intent(MainActivity.this, NoteActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLong("ID", id);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE_EDIT);
    }

}
