package com.profimedica.wordlex;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.opencsv.CSVReader;

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

    MediaPlayer m = new MediaPlayer();
    // Native to foreign or foreign to native
    boolean reverseLanguages = false;

    // Total hits for a language direction
    int totalHits = 0;

    // Total words to be discovered in a difficulty specified level
    int initialWordsCount = 0;

    // Total good answares
    int goodButtonNumber = 0;

    // Words to be discovered
    List<Word> wordsToBeDiscovered = new ArrayList<>();
    // Words to be discovered
    List<Word> wordsAlreadyDiscovered = new ArrayList<>();

    // Holder for the current word in the quiz
    TextView TextToTranslate = null;

    // Current word in the quiz
    Word currentWord = null;

    // The index of the current word used to remove it from the list of words if guessed
    int currentWordIndex = 0;

    // Random nmber generator
    Random rnd = new Random();

    // The 5 option buttons
    Button[] buttons = new Button[5];

    TextView SwitchLanguagesButton = null;

    TextView SortByDifficultyButton;
    TextView Difficulty0Button;
    TextView Difficulty1Button;
    TextView Difficulty3Button;
    TextView Difficulty5Button;

    TextView GoodLabel = null;
    TextView BadLabel = null;
    TextView WrongWordsNumber = null;
    TextView LeftWordsLabel = null;
    int leftWordsNumber = 0;
    int wrongHits = 0;
    TextView LastTranslation = null;

    protected View.OnClickListener buttonListener0 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 0);
        }
    };

    protected View.OnClickListener buttonListener1 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 1);
        }
    };
    protected View.OnClickListener buttonListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 2);
        }
    };
    protected View.OnClickListener buttonListener3 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 3);
        }
    };
    protected View.OnClickListener buttonListener4 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FillStatistics(goodButtonNumber == 4);
        }
    };
    protected View.OnClickListener difficultyButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //FillStatistics(goodButtonNumber == 4);
        }
    };
    protected View.OnClickListener difficulty0ButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WriteSQL(null, "Saved");
            FilterRecords(0);
        }
    };
    protected View.OnClickListener difficulty1ButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WriteSQL(null, "Saved");
            FilterRecords(1);
        }
    };
    protected View.OnClickListener difficulty3ButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WriteSQL(null, "Saved");
            FilterRecords(3);
        }
    };
    protected View.OnClickListener difficulty5ButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WriteSQL(null, "Saved");
            FilterRecords(5);
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private void FilterRecords(int i) {
        ReadSQL(this, "", i);
        initialWordsCount = wordsToBeDiscovered.size(); //TODO count unanswared
        currentWord = CreateQuiz(wordsToBeDiscovered);
        leftWordsNumber = initialWordsCount;
        LeftWordsLabel.setText(String.valueOf(leftWordsNumber));
        totalHits = 0;
    }

    protected View.OnClickListener buttonListenerSwitchLanguages = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            totalHits--;
            reverseLanguages = !reverseLanguages;
            if (reverseLanguages) {
                //SwitchLanguagesButton.setText("En -> Fr");
            } else {
                //SwitchLanguagesButton.setText("Fr -> En");
            }
            FillStatistics(false);
        }
    };

    private void FillStatistics(boolean guessed) {
        totalHits++;
        currentWord.Unsaved = true;
        if (guessed) {
            leftWordsNumber--;
            wordsToBeDiscovered.remove(currentWord);
            wordsAlreadyDiscovered.add(currentWord);
            currentWord.Good++;
            playBeep(SoundEffectConstants.CLICK);
            //LastTranslation.setTextColor(Color.GREEN);
        } else {
            wrongHits++;
            currentWord.Bad++;
            playBeep(SoundEffectConstants.NAVIGATION_RIGHT);
            //LastTranslation.setTextColor(Color.MAGENTA);
        }
        //GuesedWordsNumber.setText(String.valueOf(initialWordsCount-words.size()));
        WrongWordsNumber.setText(String.valueOf(wrongHits));
        LeftWordsLabel.setText(String.valueOf(leftWordsNumber));
        //LastTranslation.setText(String.valueOf(currentWord.Native + " = " + currentWord.Foreign));

        currentWord = CreateQuiz(wordsToBeDiscovered);
        //LastTranslation.invalidate();
    }

    public void playBeep(int Sound) {
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }

            AssetFileDescriptor descriptor = getAssets().openFd("good.m4a");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(true);
            m.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Word CreateQuiz(List<Word> words) {
        currentWordIndex = rnd.nextInt(words.size() - 1);
        currentWord = words.get(currentWordIndex);
        goodButtonNumber = rnd.nextInt(5);
        GoodLabel.setText(String.valueOf(currentWord.Good));
        BadLabel.setText(String.valueOf(currentWord.Bad));
        if (reverseLanguages) {
            buttons[goodButtonNumber].setText(currentWord.Native);
            TextToTranslate.setText(currentWord.Foreign);
        } else {
            buttons[goodButtonNumber].setText(currentWord.Foreign);
            TextToTranslate.setText(currentWord.Native);
        }

        for (int i = 0; i < buttons.length; i++) {
            if (i != goodButtonNumber) {
                if (reverseLanguages) {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Native);
                } else {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Foreign);
                }
            }
        }

        return words.get(currentWordIndex);
    }

    public long getLastInsertId(SQLiteDatabase db, String tablename) {
        String query = "SELECT " + WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " FROM " + WordReaderContract.WordEntry.TABLE_NAME + " ORDER BY " + WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + "  DESC LIMIT 1";
        long index = -1;
        Cursor c = db.rawQuery(query, null);
        if (c != null && c.moveToFirst()) {
            index = c.getLong(0); //The 0 is the column index, we only have 1 column, so the index is 0
        }
        c.close();
        return index;
        /*
        Cursor cursor = db.query(
                "sqlite_sequence",
                new String[]{"seq"},
                "name = ?",
                new String[]{tablename},
                null,
                null,
                null,
                null
        );
        if (cursor.moveToFirst()) {
            index = cursor.getLong(cursor.getColumnIndex("seq"));
        }
        */
    }

    private void WriteWordInDatabase(SQLiteDatabase db, Word word) {
        // Create a new map of values, where column names are the keys
        if (word.Id == null) {
            String SQL = "INSERT INTO " + WordReaderContract.WordEntry.TABLE_NAME +
                    " (" +
                    WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_BAD + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                    " ) VALUES ( '" +
                    word.Native + "' , '" +
                    word.Foreign + "' , " +
                    word.Good + " , " +
                    word.Bad + " , " +
                    word.TimeSpend + " , '" +
                    word.Dictionary +
                    "' ) ";
            db.execSQL(SQL);
            word.Id = getLastInsertId(db, WordReaderContract.WordEntry.TABLE_NAME);
        } else {
            String SQL = "UPDATE " + WordReaderContract.WordEntry.TABLE_NAME +
                    " SET " +
                    WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " = '" + word.Native + "' , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " = '" + word.Foreign + "' , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " = " + word.Good + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_BAD + " = " + word.Bad + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " = " + word.TimeSpend + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY + " = '" + word.Dictionary + "' WHERE " +
                    WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " = " + word.Id;
            db.execSQL(SQL);
        }
    }

    public boolean WriteSQL(Context context, String fileName) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        for (Iterator<Word> i = wordsToBeDiscovered.iterator(); i.hasNext(); ) {
            Word word = i.next();
            if (word.Unsaved) {
                WriteWordInDatabase(db, word);
            }
        }
        for (Iterator<Word> i = wordsAlreadyDiscovered.iterator(); i.hasNext(); ) {
            Word word = i.next();
            WriteWordInDatabase(db, word);
        }
        return true;
    }

    public List<Word> EmptyTable(Context context, String tableName) {
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "DELETE FROM " + WordReaderContract.WordEntry.TABLE_NAME;
        db.execSQL(SQL);
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(QuizActivity.this);
        if( statusCode != ConnectionResult.SUCCESS)
        {
            Log.e("statuscode", statusCode + "");
            if(GooglePlayServicesUtil.isUserRecoverableError(statusCode))
            {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                        statusCode,
                        QuizActivity.this,
                        0);

                // If Google Play services can provide an error dialog
                if (errorDialog != null) {
                    errorDialog.show();
                }
            }
            else
            {
                Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
            }
        }
    }

    public List<Word> ReadSQL(Context context, String tableName, int difficulty) {
        wordsAlreadyDiscovered.clear();
        wordsToBeDiscovered.clear();
        WordReaderDbHelper mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "SELECT " +
                WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                " FROM " + WordReaderContract.WordEntry.TABLE_NAME +
                " WHERE " + WordReaderContract.WordEntry.COLUMN_NAME_BAD +
                " >= " + difficulty + " ORDER BY " +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " DESC";
        Cursor cursor = db.rawQuery(SQL, null);
    /*
        // How you want the results sorted in the resulting Cursor
        String sortOrder =


        Cursor cursor = db.query(
                WordReaderContract.WordEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " >= " +difficulty,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );
*/
        cursor.moveToFirst();
        //long itemId = cursor.getLong(
        //        cursor.getColumnIndexOrThrow(WordReaderContract.WordEntry._ID)
        //);
        ArrayList<String> array_list = new ArrayList<String>();

        while (cursor.isAfterLast() == false) {
            Word word = new Word(
                    /*cursor.getLong(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_NATIVE)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN)),
                    cursor.getInt(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_GOOD)),
                    cursor.getInt(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_BAD)),
                    cursor.getLong(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_SPENT)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY))*/
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getLong(5),
                    cursor.getString(6)
            );
            wordsToBeDiscovered.add(word);
            cursor.moveToNext();
        }
        return wordsToBeDiscovered;
    }

    public List<Word> ReadCSV(Context context, String fileName) {
        List<String[]> questionList = new ArrayList<String[]>();
        AssetManager assetManager = context.getAssets();

        try {
            InputStream csvStream = assetManager.open(fileName + ".csv");
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
            CSVReader csvReader = new CSVReader(csvStreamReader);
            String[] line;

            // throw away the header
            csvReader.readNext();

            while ((line = csvReader.readNext()) != null) {
                questionList.add(line);
                Word word = new Word(null, line[1], line[2], 0, 0, Long.valueOf(0), "FrEn");
                word.Unsaved = true;
                wordsToBeDiscovered.add(word);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return wordsToBeDiscovered;
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


        //SortByDifficultyButton = (TextView) findViewById(R.id.sort_by_difficulty_button);
        Difficulty0Button = (TextView) findViewById(R.id.difficulty_0_button);
        Difficulty1Button = (TextView) findViewById(R.id.difficulty_1_button);
        Difficulty3Button = (TextView) findViewById(R.id.difficulty_3_button);
        Difficulty5Button = (TextView) findViewById(R.id.difficulty_5_button);
        Difficulty0Button.setOnClickListener(difficulty0ButtonListener);
        Difficulty1Button.setOnClickListener(difficulty1ButtonListener);
        Difficulty3Button.setOnClickListener(difficulty3ButtonListener);
        Difficulty5Button.setOnClickListener(difficulty5ButtonListener);
        //View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
        BadLabel = (TextView) findViewById(R.id.GoodLabel);
        GoodLabel = (TextView) findViewById(R.id.BadLabel);
        TextToTranslate = (TextView) findViewById(R.id.fullscreen_content);
        //TotalWordsNumber = (TextView)findViewById(R.id.TotalWordsNumber);
        //GuesedWordsNumber = (TextView)findViewById(R.id.GuesedWordsNumber);
        WrongWordsNumber = (TextView) findViewById(R.id.WrongWordsNumber);
        LeftWordsLabel = (TextView) findViewById(R.id.LeftWordsLabel);
        //LastTranslation = (TextView)findViewById(R.id.LastTranslation);
        //SwitchLanguagesButton = (TextView)findViewById(R.id.switchLanguages);

        buttons[0] = (Button) findViewById(R.id.button0);
        buttons[1] = (Button) findViewById(R.id.button1);
        buttons[2] = (Button) findViewById(R.id.button2);
        buttons[3] = (Button) findViewById(R.id.button3);
        buttons[4] = (Button) findViewById(R.id.button4);
        buttons[0].setOnClickListener(buttonListener0);
        buttons[1].setOnClickListener(buttonListener1);
        buttons[2].setOnClickListener(buttonListener2);
        buttons[3].setOnClickListener(buttonListener3);
        buttons[4].setOnClickListener(buttonListener4);
        //SwitchLanguagesButton.setOnClickListener(buttonListenerSwitchLanguages);

        EmptyTable(this, "Basic");
        ReadCSV(this, "Basic");
        WriteSQL(this, "Saved");
        ReadSQL(this, "", 0);
        FilterRecords(0);
        //return rootView;

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        // client = new com.google.android.gms.common.api.GoogleApiClient.Builder(this).addApi(com.google.android.gms.appindexing.AppIndex.API).build();
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

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void LunchStatisticsActivity(View view)
    {
        Intent intent = new Intent(this, ItemListActivity.class);
        startActivity(intent);
    }
}