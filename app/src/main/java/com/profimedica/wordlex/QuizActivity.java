package com.profimedica.wordlex;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QuizActivity extends AppCompatActivity {

    boolean reverseLanguages = false;
    int totalHits = 0;
    int initialWordsCount = 0;
    int goodButtonNumber = 0;
    List<Word> words = new ArrayList<Word>();
    TextView TextToTranslate = null;
    Word currentWord = null;
    int currentWordIndex = 0;
    Random rnd = new Random();
    Button[] buttons = new Button[5];

    TextView SwitchLanguagesButton = null;

    TextView SortByDifficultyButton;
    TextView Difficulty0Button;
    TextView Difficulty1Button;
    TextView Difficulty3Button;
    TextView Difficulty5Button;

    TextView GoodLabel = null;
    TextView BadLabel = null;
    TextView TotalWordsNumber = null;
    TextView GuesedWordsNumber = null;
    TextView WrongWordsNumber = null;
    TextView LeftWordsLabel = null;
    int leftWordsNumber = 0;
    int wrongHits = 0;
    TextView LastTranslation = null;

    protected View.OnClickListener buttonListener0 = new View.OnClickListener(){
        @Override
        public void onClick(View v){
            FillStatistics(goodButtonNumber == 0);
        }
    };

    protected View.OnClickListener buttonListener1 = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 1);
        }
    };
    protected View.OnClickListener buttonListener2 = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 2);
        }
    };
    protected View.OnClickListener buttonListener3 = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 3);
        }
    };
    protected View.OnClickListener buttonListener4 = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 4);
        }
    };
    protected View.OnClickListener difficultyButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            //FillStatistics(goodButtonNumber == 4);
        }
    };
    protected View.OnClickListener difficulty0ButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FilterRecords(0);
        }
    };
    protected View.OnClickListener difficulty1ButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FilterRecords(1);
        }
    };
    protected View.OnClickListener difficulty3ButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FilterRecords(3);
        }
    };
    protected View.OnClickListener difficulty5ButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            FilterRecords(5);
        }
    };

    private void FilterRecords(int i) {
        ReadSQL(this, "", i);
        initialWordsCount = words.size(); //TODO count unanswared
        currentWord = CreateQuiz(words);
        leftWordsNumber = initialWordsCount;
        LeftWordsLabel.setText(String.valueOf(leftWordsNumber));
        totalHits=0;
    }

    protected View.OnClickListener buttonListenerSwitchLanguages = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            totalHits--;
            reverseLanguages = !reverseLanguages;
            if(reverseLanguages)
            {
                //SwitchLanguagesButton.setText("En -> Fr");
            }
            else
            {
                //SwitchLanguagesButton.setText("Fr -> En");
            }
            FillStatistics(false);
        }
    };

    private void FillStatistics(boolean guessed) {
        totalHits++;

        if(guessed)
        {
            leftWordsNumber--;
            // words.remove(currentWordIndex);
            currentWord.Good++;
            //LastTranslation.setTextColor(Color.GREEN);
        }
        else {
            wrongHits++;
            currentWord.Bad++;
            //LastTranslation.setTextColor(Color.MAGENTA);
        }
        //GuesedWordsNumber.setText(String.valueOf(initialWordsCount-words.size()));
        WrongWordsNumber.setText(String.valueOf(wrongHits));
        LeftWordsLabel.setText(String.valueOf(leftWordsNumber));
        //LastTranslation.setText(String.valueOf(currentWord.Native + " = " + currentWord.Foreign));

        currentWord = CreateQuiz(words);
        //LastTranslation.invalidate();
    }

    public Word CreateQuiz(List<Word> words) {
        currentWordIndex = rnd.nextInt(words.size() - 1);
        currentWord = words.get(currentWordIndex);
        goodButtonNumber = rnd.nextInt(5);
        GoodLabel.setText(String.valueOf(currentWord.Good));
        BadLabel.setText(String.valueOf(currentWord.Bad));
        if(reverseLanguages)
        {
            buttons[goodButtonNumber].setText(currentWord.Native);
            TextToTranslate.setText(currentWord.Foreign);
        }
        else {
            buttons[goodButtonNumber].setText(currentWord.Foreign);
            TextToTranslate.setText(currentWord.Native);
        }

        for(int i = 0; i<buttons.length; i++)
        {
            if(i != goodButtonNumber)
            {
                if(reverseLanguages) {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Native);
                }
                else {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Foreign);
                }
            }
        }

        return words.get(currentWordIndex);
    }

    public boolean WriteCSV(Context context, List<Word> words, String fileName) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for(Iterator<Word> i = words.iterator(); i.hasNext(); ) {
            Word word = i.next();

            // Create a new map of values, where column names are the keys
            if(word.Id == null) {
                ContentValues values = new ContentValues();
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_NATIVE, word.Native);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN, word.Foreign);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_GOOD, word.Good);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_BAD, word.Bad);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_SPENT, word.TimeSpend);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY, word.Dictionary);

                // Insert the new row, returning the primary key value of the new row
                long newRowId;

                newRowId = db.insert(
                        WordReaderContract.WordEntry.TABLE_NAME,
                        null,
                        values);
            }
            else {
                ContentValues values = new ContentValues();
                values.put(WordReaderContract.WordEntry._ID, word.Id);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_NATIVE, word.Native);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN, word.Foreign);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_GOOD, word.Good);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_BAD, word.Bad);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_SPENT, word.TimeSpend);
                values.put(WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY, word.Dictionary);

                // Insert the new row, returning the primary key value of the new row
                long newRowId;

                newRowId = db.update(
                        WordReaderContract.WordEntry.TABLE_NAME,
                        values,
                        " WHERE 'id' == " + WordReaderContract.WordEntry._ID + " ", null
                );
            }
        }
        return true;
    }


    public List<Word> EmptyTable(Context context, String tableName) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "DELETE FROM "+ WordReaderContract.WordEntry.TABLE_NAME;
        db.execSQL(SQL);
        return null;
    }

    public List<Word> ReadSQL(Context context, String tableName, int difficulty){
        words.clear();
            WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
            SQLiteDatabase db = mDbHelper.getReadableDatabase();


            // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                WordReaderContract.WordEntry._ID,
                WordReaderContract.WordEntry.COLUMN_NAME_NATIVE,
                WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN,
                WordReaderContract.WordEntry.COLUMN_NAME_BAD,
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD,
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT,
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " DESC";

        Cursor cursor = db.query(
                WordReaderContract.WordEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " >= difficulty",                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        cursor.moveToFirst();
        //long itemId = cursor.getLong(
        //        cursor.getColumnIndexOrThrow(WordReaderContract.WordEntry._ID)
        //);
        ArrayList<String> array_list = new ArrayList<String>();

        while(cursor.isAfterLast() == false){
            Word word = new Word(
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_NATIVE)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN)),
                    cursor.getInt(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_GOOD)),
                    cursor.getInt(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_BAD)),
                    cursor.getLong(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_SPENT)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY))
                    );
            words.add(word);
            cursor.moveToNext();
        }
        return words;
    }

    public List<Word> ReadCSV(Context context, String fileName) {
        List<String[]> questionList = new ArrayList<String[]>();
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open(fileName+ ".csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                questionList.add(line);
                Word word = new Word(null, line[1], line[2], 0, 0, Long.valueOf(0), "FrEn");
                words.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }

    protected void onDestroy() {

       // WriteCSV(this, words, "Saved");
        super.onDestroy();
    }

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_quiz);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        SortByDifficultyButton = (TextView)findViewById(R.id.sort_by_difficulty_button);
        Difficulty0Button = (TextView)findViewById(R.id.difficulty_0_button);
        Difficulty1Button = (TextView)findViewById(R.id.difficulty_1_button);
        Difficulty3Button = (TextView)findViewById(R.id.difficulty_3_button);
        Difficulty5Button = (TextView)findViewById(R.id.difficulty_5_button);

        //View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        BadLabel = (TextView)findViewById(R.id.GoodLabel);
        GoodLabel = (TextView)findViewById(R.id.BadLabel);
        TextToTranslate = (TextView)findViewById(R.id.fullscreen_content);
        //TotalWordsNumber = (TextView)findViewById(R.id.TotalWordsNumber);
        //GuesedWordsNumber = (TextView)findViewById(R.id.GuesedWordsNumber);
        WrongWordsNumber = (TextView)findViewById(R.id.WrongWordsNumber);
        LeftWordsLabel = (TextView)findViewById(R.id.LeftWordsLabel);
        //LastTranslation = (TextView)findViewById(R.id.LastTranslation);
        //SwitchLanguagesButton = (TextView)findViewById(R.id.switchLanguages);

        buttons[0] = (Button)findViewById(R.id.button0);
        buttons[1] = (Button)findViewById(R.id.button1);
        buttons[2] = (Button)findViewById(R.id.button2);
        buttons[3] = (Button)findViewById(R.id.button3);
        buttons[4] = (Button)findViewById(R.id.button4);
        buttons[0].setOnClickListener(buttonListener0);
        buttons[1].setOnClickListener(buttonListener1);
        buttons[2].setOnClickListener(buttonListener2);
        buttons[3].setOnClickListener(buttonListener3);
        buttons[4].setOnClickListener(buttonListener4);
        //SwitchLanguagesButton.setOnClickListener(buttonListenerSwitchLanguages);

        EmptyTable(this, "Basic");
        ReadCSV(this, "Basic");
        WriteCSV(this, words, "Saved");
        FilterRecords(0);
        //return rootView;

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}