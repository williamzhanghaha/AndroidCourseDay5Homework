package com.camp.bit.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.camp.bit.todolist.beans.Note;
import com.camp.bit.todolist.beans.State;
import com.camp.bit.todolist.db.TodoContract;
import com.camp.bit.todolist.db.TodoDbHelper;

import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    public static final String TAG = "NoteActivity";
    public static final int EDITED_TRUE = 1;
    public static final int EDITED_FALSE = 2;
    public static final int EDITED_ERROR = 0;

    private EditText editText;
    private Button addBtn;
    private RadioGroup priorityRadioGroup;
    private RadioButton radioButton0;
    private RadioButton radioButton1;
    private RadioButton radioButton2;
    TodoDbHelper mDbHelper;
    SQLiteDatabase db;
    private boolean isNew = false;
    Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        mDbHelper = new TodoDbHelper(getApplicationContext());

        db = mDbHelper.getWritableDatabase();

        priorityRadioGroup = findViewById(R.id.rg_priority);
        radioButton0 = findViewById(R.id.rb_0);
        radioButton1 = findViewById(R.id.rb_1);
        radioButton2 = findViewById(R.id.rb_2);



        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        Bundle bundle = getIntent().getExtras();
        //TODO
        if (bundle == null) {
            isNew = true;
        }

        if (!isNew) {
            try {
                long id = bundle.getLong("ID");
                Cursor cursor = null;
                String selection = TodoContract.TodoEntry._ID + " LIKE ?";
                String[] selectionArgs = {"" + id};
                try {
                    cursor = db.query(TodoContract.TodoEntry.TABLE_NAME,
                            new String[]{TodoContract.TodoEntry.COLUMN_CONTENT,
                                    TodoContract.TodoEntry.COLUMN_DATE,
                                    TodoContract.TodoEntry.COLUMN_STATE,
                                    TodoContract.TodoEntry.COLUMN_PRIORITY,
                                    TodoContract.TodoEntry._ID},
                            selection, selectionArgs,
                            null, null,
                            TodoContract.TodoEntry.COLUMN_PRIORITY + " DESC, " + TodoContract.TodoEntry.COLUMN_DATE + " DESC");
                    while (cursor.moveToNext()) {
                        String content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_CONTENT));
                        long dateTime = cursor.getLong(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_DATE));
                        int intState = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_STATE));
                        int priority = cursor.getInt(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_PRIORITY));

                        note = new Note(id);
                        note.setContent(content);
                        note.setDate(new Date(dateTime));
                        note.setState(State.from(intState));
                        note.setPriority(priority);

                        editText.setText(content);
                        radioButton0.setChecked(false);
                        radioButton1.setChecked(false);
                        radioButton2.setChecked(false);

                        switch (priority) {
                            case 0:
                                radioButton0.setChecked(true);
                                break;
                            case 1:
                                radioButton1.setChecked(true);
                                break;
                            case 2:
                                radioButton2.setChecked(true);
                        }

                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "onCreate: bundle error.");
            }
        }

        addBtn = findViewById(R.id.btn_add);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isNew) {
                    boolean succeed = saveNote2Database(content.toString().trim());
                    if (succeed) {
                        Toast.makeText(NoteActivity.this,
                                "Note added", Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK);
                    } else {
                        Toast.makeText(NoteActivity.this,
                                "Error", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                } else {
                    int succeed = editNoteInDatabase(content.toString().trim());
                    switch (succeed) {
                        case EDITED_TRUE:
                            Toast.makeText(NoteActivity.this,
                                    "Note edited", Toast.LENGTH_SHORT).show();
                            setResult(Activity.RESULT_OK);
                            break;
                        case EDITED_ERROR:
                            Toast.makeText(NoteActivity.this,
                                    "Error", Toast.LENGTH_SHORT).show();
                            break;
                        case EDITED_FALSE:
                            setResult(Activity.RESULT_OK);
                            break;

                    }
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功

        Date date = new Date();
        long dateTime = date.getTime();

        int priority = 0;
        int checkedId = priorityRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_0) {
            priority = 0;
        } else if (checkedId == R.id.rb_1) {
            priority = 1;
        } else if (checkedId == R.id.rb_2) {
            priority = 2;
        }

        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_CONTENT, content);
        values.put(TodoContract.TodoEntry.COLUMN_STATE, 0);
        values.put(TodoContract.TodoEntry.COLUMN_DATE, dateTime);
        values.put(TodoContract.TodoEntry.COLUMN_PRIORITY, priority);

        long newRowId = db.insert(TodoContract.TodoEntry.TABLE_NAME, null, values);

        return newRowId != -1;
    }

    private int editNoteInDatabase(String content) {

        int priority = 0;
        int checkedId = priorityRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.rb_0) {
            priority = 0;
        } else if (checkedId == R.id.rb_1) {
            priority = 1;
        } else if (checkedId == R.id.rb_2) {
            priority = 2;
        }

        if (content.equals(note.getContent()) && priority == note.getPriority()) {
            return EDITED_FALSE;
        }

        Date date = new Date();
        long dateTime = date.getTime();

        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_CONTENT, content);
        values.put(TodoContract.TodoEntry.COLUMN_DATE, dateTime);
        values.put(TodoContract.TodoEntry.COLUMN_STATE, note.getState().intValue);
        values.put(TodoContract.TodoEntry.COLUMN_PRIORITY, priority);

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = { "" + note.id };

        int count = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        if (count != 0) {
            return EDITED_TRUE;
        }

        return EDITED_ERROR;
    }
}
