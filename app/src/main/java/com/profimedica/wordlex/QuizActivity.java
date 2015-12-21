package com.profimedica.wordlex;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import android.speech.tts.TextToSpeech;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class QuizActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(Bundle connectionHint) {
        int i =0;
        Log.e(" cd ", "Connected with GoogleApiClient");
        IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", "text/html" })
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, 55, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(" cd ", "Unable to send intent drive", e);
        }


    }

    @Override
    public void onConnectionSuspended(int cause) {
        int i =0;
        // The connection has been interrupted.
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                // !!!
                result.startResolutionForResult(this, result.getErrorCode());
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }
    }

    int difficulty = 0;
    boolean standBy = false;
    TextView Timer;
    ImageView Settings;
    CountDownTimer countDownTimer;
    long millis = 0;
    boolean displayCumulatedConsumeForCurentWord;
    TextToSpeech TTSnative;
    TextToSpeech TTSforeign;
    boolean speacking = false;
    // Media player for reading words
    MediaPlayer m = new MediaPlayer();

    // Native to foreign or foreign to native
    static boolean nativeFirst = true;

    // Total hits for a language direction
    int totalHits = 0;

    // Total words to be discovered in a difficulty specified level
    int initialWordsCount = 0;

    WordReaderDbHelper mDbHelper;

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
    ImageView HigherDifficulty;
    ImageView LowerDifficulty;
    TextView LevelIndicator;

    TextView GoodLabel = null;
    TextView BadLabel = null;
    TextView AnteForeign = null;
    TextView AnteNative = null;
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
    protected View.OnClickListener settingsButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Go to settings screen
            Intent intent = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(intent);
        }
    };
    protected View.OnClickListener timerButtonListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            displayCumulatedConsumeForCurentWord = !displayCumulatedConsumeForCurentWord;
            Timer.setTextColor(displayCumulatedConsumeForCurentWord ? Color.BLACK : Color.GRAY);
        }
    };
    protected View.OnClickListener HigherDifficultyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            WriteSQL(null, "Saved");
            difficulty++;

            FilterRecords(difficulty);
        }
    };
    protected View.OnClickListener LowerDifficultyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(difficulty == 0)
            {
                return;
            }
            WriteSQL(null, "Saved");
            difficulty--;
            FilterRecords(difficulty);
        }
    };

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private void FilterRecords(int i) {
        ReadSQL(this, "", i);
        if(wordsToBeDiscovered.size() < 5)
        {
            difficulty--;
            FilterRecords(difficulty);
        }
        else
        {
            LevelIndicator.setText(" " + String.valueOf(difficulty) + " ");
        }
        initialWordsCount = wordsToBeDiscovered.size(); //TODO count unanswared
        WrongWordsNumber.setText(String.valueOf(0));
        currentWord = CreateQuiz(wordsToBeDiscovered);
        leftWordsNumber = initialWordsCount;
        LeftWordsLabel.setText(String.valueOf(leftWordsNumber));
        totalHits = 0;
    }

    protected View.OnClickListener buttonListenerSwitchLanguages = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            totalHits--;
            leftWordsNumber++;
            nativeFirst = !nativeFirst;
            if (nativeFirst) {
                SwitchLanguagesButton.setText("De -> En");
            } else {
                SwitchLanguagesButton.setText("En -> De");
            }
            WriteSQL(QuizActivity.this, "Saved");
            FilterRecords(difficulty);//handleError(data);
            currentWord = CreateQuiz(wordsToBeDiscovered);
        }
    };

    private void FillStatistics(boolean guessed) {
        //if (!guessed) {
            standBy = false;
        //}
        if(!guessed)
        {
            AnteNative.setTextColor(Color.MAGENTA);
            AnteForeign.setTextColor(Color.MAGENTA);
        }
        else
        {
            AnteNative.setTextColor(Color.GRAY);
            AnteForeign.setTextColor(Color.GRAY);
        }
        AnteNative.setText(currentWord.Native);
        AnteForeign.setText(currentWord.Foreign);

        totalHits++;
        currentWord.Unsaved = true;
        currentWord.TimeSpend += millis;
        if (guessed) {
            leftWordsNumber--;
            wordsToBeDiscovered.remove(currentWord);
            wordsAlreadyDiscovered.add(currentWord);
            if (nativeFirst) {
                currentWord.Good++;
            } else {
                currentWord.FGood++;
            }
            playBeep(SoundEffectConstants.CLICK);
            //LastTranslation.setTextColor(Color.GREEN);
        } else {
            wrongHits++;
            if (nativeFirst) {
                currentWord.Bad++;
            } else {
                currentWord.FBad++;
            }
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

        //Toast.makeText(getApplicationContext(), currentWord.Native, Toast.LENGTH_SHORT).show();
        if(nativeFirst)
        {
            TTSnative.speak(currentWord.Native, TextToSpeech.QUEUE_FLUSH, null);
        }
        else {
            TTSforeign.speak(currentWord.Foreign, TextToSpeech.QUEUE_FLUSH, null);
        }

        goodButtonNumber = rnd.nextInt(5);
        if (nativeFirst) {
            GoodLabel.setText(String.valueOf(currentWord.Good));
            BadLabel.setText(String.valueOf(currentWord.Bad));
        } else {
            GoodLabel.setText(String.valueOf(currentWord.FGood));
            BadLabel.setText(String.valueOf(currentWord.FBad));
        }
        if (nativeFirst) {
            TextToTranslate.setText(currentWord.Native);
            buttons[goodButtonNumber].setText(currentWord.Foreign);
        } else {
            TextToTranslate.setText(currentWord.Foreign);
            buttons[goodButtonNumber].setText(currentWord.Native);
        }

        for (int i = 0; i < buttons.length; i++) {
            if (i != goodButtonNumber) {
                if (nativeFirst) {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Foreign);
                } else {
                    buttons[i].setText(words.get(rnd.nextInt(words.size() - 1)).Native);
                }
            }
        }
        countDownTimer.cancel();
        millis = 0;
        countDownTimer.start();

        if(standBy) {
            AnteNative.setText("MODE");
            AnteForeign.setText("StandBy");
            if (nativeFirst) {
                TTSforeign.speak(currentWord.Foreign, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                TTSnative.speak(currentWord.Native, TextToSpeech.QUEUE_FLUSH, null);
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
                    WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                    " ) VALUES ( '" +
                    word.Native + "' , '" +
                    word.Foreign + "' , " +
                    word.Good + " , " +
                    word.Bad + " , " +
                    word.FGood + " , " +
                    word.FBad + " , " +
                    word.TimeSpend + " , " +
                    word.FSpend + " , '" +
                    word.Dictionary +
                    "' ) ";
            db.execSQL(SQL);
            word.Id = getLastInsertId(db, WordReaderContract.WordEntry.TABLE_NAME);
            boolean ee = false;
        } else {
            String SQL = "UPDATE " + WordReaderContract.WordEntry.TABLE_NAME +
                    " SET " +
                    WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " = '" + word.Native + "' , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " = '" + word.Foreign + "' , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " = " + word.Good + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_BAD + " = " + word.Bad + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + " = " + word.FGood + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " = " + word.FBad + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " = " + word.TimeSpend + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + " = " + word.FSpend + " , " +
                    WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY + " = '" + word.Dictionary + "' WHERE " +
                    WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " = " + word.Id;
            db.execSQL(SQL);
        }
    }

    public boolean WriteSQL(Context context, String fileName) {
        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
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
        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "DELETE FROM " + WordReaderContract.WordEntry.TABLE_NAME;
        db.execSQL(SQL);
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        //FilterRecords(difficulty);//handleError(data);
        //currentWord = CreateQuiz(wordsToBeDiscovered);
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(QuizActivity.this);
        if (statusCode != ConnectionResult.SUCCESS) {
            Log.e("statuscode", statusCode + "");
            if (GooglePlayServicesUtil.isUserRecoverableError(statusCode)) {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                        statusCode,
                        QuizActivity.this,
                        0);

                // If Google Play services can provide an error dialog
                if (errorDialog != null) {
                    errorDialog.show();
                }
            } else {
                //Toast.makeText(this, "test", Toast.LENGTH_LONG).show();
            }
        }
    }

    public List<Word> ReadSQL(Context context, String tableName, int difficulty) {
        wordsAlreadyDiscovered.clear();
        wordsToBeDiscovered.clear();
        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "SELECT " +
                WordReaderContract.WordEntry.COLUMN_NAME_ENTRY_ID + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_NATIVE + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FOREIGN + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_GOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_BAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FGOOD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_SPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_FSPENT + " , " +
                WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY +
                " FROM " + WordReaderContract.WordEntry.TABLE_NAME +
                " WHERE ";
        if (nativeFirst) {
            SQL += WordReaderContract.WordEntry.COLUMN_NAME_BAD;
        } else {
            SQL += WordReaderContract.WordEntry.COLUMN_NAME_FBAD;
        }
        if(difficulty>0) {
            SQL += " >= " + difficulty + " ORDER BY ";
        }
        else
        {
            SQL += " = " + difficulty + " ORDER BY ";
        }
            SQL += WordReaderContract.WordEntry.COLUMN_NAME_FBAD + " DESC";
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
                    cursor.getLong(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_FSPENT)),
                    cursor.getString(cursor.getColumnIndex(WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY))*/
                    cursor.getLong(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getInt(3),
                    cursor.getInt(4),
                    cursor.getInt(5),
                    cursor.getInt(6),
                    cursor.getLong(7),
                    cursor.getLong(8),
                    cursor.getString(9)
            );
            wordsToBeDiscovered.add(word);
            cursor.moveToNext();
        }
        return wordsToBeDiscovered;
    }

    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

    int RSS_DOWNLOAD_REQUEST_CODE = 98;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 55) {
            Log.e("Google API", String.valueOf(resultCode) + " + " +  data.getDataString());
        }
        if (requestCode == RSS_DOWNLOAD_REQUEST_CODE) {
                switch (resultCode) {
                case DownloadIntentService.INVALID_URL_CODE:
                    //handleInvalidURL();
                    break;
                case DownloadIntentService.ERROR_CODE:
                    ReadCSV(this, "DeEn");
                    WriteSQL(this, "Saved");
                    ///oast.makeText(getApplicationContext(), "Generated from SQLite", Toast.LENGTH_SHORT).show();
                    AnteNative.setText("DataSource");
                    AnteForeign.setText("Web Error SQL used instead");
                    FilterRecords(difficulty);//handleError(data);
                    break;
                case DownloadIntentService.RESULT_CODE:
                    //handleRSS(data);

                    String result = data.getStringExtra("url");

                    ConsumeString(result);
                    AnteNative.setText("DataSource");
                    AnteForeign.setText("WEB");
                    WriteSQL(this, "Saved");
                    FilterRecords(difficulty);

                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ConsumeString(String result)
    {
        String[] inputLines = result.split("\\r?\\n");
        for(int i=1; i<inputLines.length; i++){
            String[] splitedLine = inputLines[i].split("\\ = ");
            if(splitedLine.length > 1) {
                Word word = new Word(null, splitedLine[0], splitedLine[1], 0, 0, 0, 0, Long.valueOf(0), Long.valueOf(0), "DeEn");
                word.Unsaved = true;
                wordsToBeDiscovered.add(word);
            }
        }
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

    public void onPause(){
        WriteSQL(QuizActivity.this, "Saved");
        if(TTSnative !=null){
            TTSnative.stop();
            TTSnative.shutdown();
        }
        if(TTSforeign !=null){
            TTSforeign.stop();
            TTSforeign.shutdown();
        }
        super.onPause();
    }

    public void OpenDoc(View view)
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
        EmptyTable(this, "");
        setContentView(R.layout.activity_quiz);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        AnteNative = (TextView)findViewById(R.id.AnteNative);
        AnteForeign = (TextView)findViewById(R.id.AnteForeign);



        TTSforeign = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                TTSforeign.setLanguage(Locale.ENGLISH);
                new Thread(new Runnable() {
                    public void run() {
                        //if (status != TextToSpeech.ERROR) {
                        TTSforeign.setLanguage(Locale.ENGLISH);
                    }
                });
            }
        });
        try {
            wait(Long.valueOf(10000));
        }catch(Exception e){}
        TTSnative = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                TTSnative.setLanguage(Locale.GERMANY);
                new Thread(new Runnable() {
                    public void run() {
                        //if (status != TextToSpeech.ERROR)
                        {
                            TTSnative.setLanguage(Locale.GERMANY);
                        }
                    }
                });
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        hide();

        Timer = (TextView) findViewById( R.id.timer );
        Timer.setOnClickListener(timerButtonListner);
        Settings = (ImageView) findViewById( R.id.settings);
        Settings.setOnClickListener(settingsButtonListner);
        HigherDifficulty = (ImageView) findViewById(R.id.difficulty_higher);
        LowerDifficulty = (ImageView) findViewById(R.id.difficulty_lower);
        LevelIndicator = (TextView) findViewById(R.id.level);
        HigherDifficulty.setOnClickListener(HigherDifficultyListener);
        LowerDifficulty.setOnClickListener(LowerDifficultyListener);
        BadLabel = (TextView) findViewById(R.id.GoodLabel);
        GoodLabel = (TextView) findViewById(R.id.BadLabel);
        TextToTranslate = (TextView) findViewById(R.id.fullscreen_content);
        WrongWordsNumber = (TextView) findViewById(R.id.WrongWordsNumber);
        LeftWordsLabel = (TextView) findViewById(R.id.LeftWordsLabel);
        SwitchLanguagesButton = (TextView) findViewById(R.id.switchLanguagesButton);

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
        SwitchLanguagesButton.setOnClickListener(buttonListenerSwitchLanguages);

        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                millis++;
                if(displayCumulatedConsumeForCurentWord)
                {
                    Timer.setText(new SimpleDateFormat("mm:ss:SS").format(new Date(currentWord.TimeSpend + millis)));
                }
                else
                {
                    Timer.setText(new SimpleDateFormat("mm:ss:SS").format(new Date(millis)));
                }
            }

            public void onFinish() {
                millis = 0;
                currentWord = CreateQuiz(wordsToBeDiscovered);
                standBy = true;
            }
        };

        if (wordsToBeDiscovered.size() == 0 && getLastInsertId(db, "") < 0) {
            if (false) {
                ReadCSV(this, "DeEn");

                WriteSQL(this, "Saved");
                FilterRecords(difficulty);
            } else {
                ReadCSV(this, "Online DeEn");
            }

        }
        else {
            WriteSQL(this, "Saved");
            FilterRecords(difficulty);
        }
    }
    // Request code to identify the response of a web request
    int HTTP_REQUEST_CODE = 98;


    //static String currentLine = "";
    public List<Word> ReadCSV(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();

        try {
            if (fileName.equals("Online DeEn")) {
                String URL = "http://profimedica.ro/de/de.csv";
                PendingIntent pendingResult = createPendingResult(HTTP_REQUEST_CODE, new Intent(), 0);
                Intent intent = new Intent(getApplicationContext(), DownloadIntentService.class);
                intent.putExtra(DownloadIntentService.URL_EXTRA, URL);
                intent.putExtra(DownloadIntentService.PENDING_RESULT_EXTRA, pendingResult);
                startService(intent);
            }
            if (fileName.equals("Basic")) {
                InputStream csvStream = assetManager.open(fileName + ".csv");
                // InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
                String input = convertStreamToString(csvStream);
                ConsumeString(input);
                AnteNative.setText("DataSource");
                AnteForeign.setText("Basic");
            } else {
                //Toast.makeText(getApplicationContext(), "Generated from SQLite", Toast.LENGTH_SHORT).show();
                InputStream csvStream = assetManager.open(fileName + ".csv");
                // InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
                String input = convertStreamToString(csvStream);
                ConsumeString(input);
            }
        } catch (IOException e) {
            //Log.e(">>>>>", currentLine );
            e.printStackTrace();
        }

        return wordsToBeDiscovered;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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
        if(nativeFirst)
        {
            TTSnative.speak(currentWord.Native, TextToSpeech.QUEUE_FLUSH, null);
        }
        else {
            TTSforeign.speak(currentWord.Foreign, TextToSpeech.QUEUE_FLUSH, null);
        }


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
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        //mHideHandler.removeCallbacks(mHidePart2Runnable);
        //mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
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

