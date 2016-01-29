package com.profimedica.wordlex;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
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

    private final static String DROPBOX_FILE_DIR = "WordLex";
    private final static String ACCOUNT_PREFS_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY_NAME = "35gyybmvlysajru";
    private final static String ACCESS_SECRET_NAME = "vc49hinri58o6q4";
    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;
    DropboxAPI dropboxApi;
    ProgressDialog progress;
    String dictionaryPath = "";
    static final int RESOLVE_CONNECTION_REQUEST_CODE = 42;
    static final int RSS_DOWNLOAD_REQUEST_CODE = 98;
    static final int GET_FILES_REQUEST_CODE = 55;

    int hits = 0;
    String FbUserId = "1102342866462716";
    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onConnected(Bundle connectionHint) {
        int i =0;
        Log.e("Quiz", "Connected with GoogleApiClient");

    }

    ResultCallback<DriveApi.DriveContentsResult> contentsOpenedCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {
                    if (!result.getStatus().isSuccess()) {
                        // display an error saying file can't be opened
                        return;
                    }
                    // DriveContents object contains pointers
                    // to the actual byte stream
                    Log.e("Drive.FIle", "OpenDoc result successfuly");
                    DriveContents contents = result.getDriveContents();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(contents.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    }catch(Exception e){}
                    String contentsAsString = builder.toString();
                    Log.e("Drive.FIle", contentsAsString);

                }
            };

    @Override
    public void onConnectionSuspended(int cause) {
        int i =0;
        Log.e("Drive.FIle", "OpenDoc suspended");
        // The connection has been interrupted.
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Drive.FIle", "OpenDoc faild");
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                Log.e("Drive.FIle", e.getMessage());
                // Unable to resolve, message user appropriately
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0).show();
            Log.e("Drive.FIle", "OpenDoc "+String.valueOf(connectionResult.getErrorCode()));
        }
    }

    int difficulty = 0;
    boolean higherLevelDoesNotExist = false;
    boolean standBy = false;
    TextView Timer;
    TextView ScoreInfo;
    int score=0;
    ImageView Settings;
    ImageView SpeackerSwitch;
    Boolean SpeackerSwitchMuted = true;
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
    protected View.OnClickListener SpeackerSwitchListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Go to settings screen
            SpeackerSwitchMuted = !SpeackerSwitchMuted;
            Settings.setImageResource((SpeackerSwitchMuted? R.drawable.speacker_muted : R.drawable.speacker));
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
        if(wordsToBeDiscovered.size() < 6) {
                ReadSQL(this, "", i+1);
                if(wordsToBeDiscovered.size() < 5) {
                    if(difficulty > 0) {
                        difficulty--;
                        FilterRecords(difficulty);
                    }
                }
            else {
                    difficulty++;
                    FilterRecords(difficulty);
                }
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
        score += guessed ? 5 : -3;
        hits ++;
        if (hits > 50) {
            GetAward();
        }
        if(score < 0) score=0;
        ScoreInfo.setText("scored " + String.valueOf(score) + " points");
        //if (!guessed) {
            standBy = false;
        //}
        ScoreInfo.setBackgroundColor(guessed ? Color.BLUE : Color.MAGENTA);

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
        //if(hits/50 ==0 )
    }

    private void GetAward() {
        //int id = getResources().getIdentifier("yourpackagename:drawable/" + , null, null);
        Settings.setImageResource(R.drawable.settings_orange);
        new CreateImage().execute(FbUserId, String.valueOf(score));
        return;
        /*
        Intent myIntent = new Intent(this, ShareScoreActivity.class);
        myIntent.putExtra("SCORE",score);
        myIntent.putExtra("FbUserId", FbUserId);
        startActivity(myIntent);*/
    }

    public void playBeep(int Sound) {
        try {
            if (m.isPlaying()) {
                m.stop();
                m.release();
                m = new MediaPlayer();
            }
            return;
           /* AssetFileDescriptor descriptor = getAssets().openFd("good.m4a");
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(true);
            m.start();*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Word CreateQuiz(List<Word> words) {
        if(words.size() < 6)
        {
            if(!higherLevelDoesNotExist)
            FilterRecords(difficulty++);
        }
        higherLevelDoesNotExist = false;
        currentWordIndex = rnd.nextInt(words.size() - 1);
        currentWord = words.get(currentWordIndex);

        //Toast.makeText(getApplicationContext(), currentWord.Native, Toast.LENGTH_SHORT).show();
        if(!SpeackerSwitchMuted) {
            if (nativeFirst) {
                TTSnative.speak(currentWord.Native, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                TTSforeign.speak(currentWord.Foreign, TextToSpeech.QUEUE_FLUSH, null);
            }
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

        if(standBy && !SpeackerSwitchMuted) {
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
        if (word.Id == null || word.Id == -1 ) {
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
            if (word.Unsaved || word.Id < 1) {
                WriteWordInDatabase(db, word);
            }
        }
        for (Iterator<Word> i = wordsAlreadyDiscovered.iterator(); i.hasNext(); ) {
            Word word = i.next();
            WriteWordInDatabase(db, word);
        }
        return true;
    }

    public void EmptyTable() {
        if(mDbHelper == null) {
            mDbHelper = new WordReaderDbHelper(this);
        }
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String SQL = "DELETE FROM " + WordReaderContract.WordEntry.TABLE_NAME + " WHERE " + WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY + " = '" + dictionaryPath + "'";
        db.execSQL(SQL);
        return;
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
                " WHERE " + WordReaderContract.WordEntry.COLUMN_NAME_DICTIONARY + " = '" + dictionaryPath + "' AND " ;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GET_FILES_REQUEST_CODE:
                Log.e("Google API", String.valueOf(resultCode) + " + " +  data.getDataString());
                break;
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    //mGoogleApiClient.connect();
                }
                break;
            case RSS_DOWNLOAD_REQUEST_CODE:
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

                    wordsToBeDiscovered = Utils.ConsumeString(result);
                    AnteNative.setText("DataSource");
                    AnteForeign.setText("WEB");
                    WriteSQL(this, "Saved");
                    FilterRecords(difficulty);

                    break;
            }
        }
    }

    protected void onDestroy() {

        // WriteCSV(this, words, "Saved");
        progress.setTitle("Dropbox");
        progress.setMessage("Getting list...");
        //progress.show();

        WriteSQL(QuizActivity.this, "Saved");
        UpdateFile();
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
        progress.setTitle("Dropbox");
        progress.setMessage("Getting list...");
        progress.show();

        WriteSQL(QuizActivity.this, "Saved");
        UpdateFile();
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

    private void UpdateFile() {
        FilterRecords(0);
        File file;
        file = new File(getApplication().getFilesDir().getPath()+ "/" + dictionaryPath + ".txt");
         FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        }
        catch(FileNotFoundException ex)
        {
            //Log.d(TAG, "InexistentLocalFile : " + ex.getMessage());
        }
        try {
            String newContent = Utils.PrepareString(wordsToBeDiscovered);
            outputStream.write(Utils.PrepareString(wordsToBeDiscovered).getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        DropboxUploadFile uploadFile = new DropboxUploadFile(dropboxApi, getApplication().getFilesDir().getPath()+ "/" + dictionaryPath + ".txt", handlerUpload);
        uploadFile.execute();
        progress.dismiss();
    }

    private final Handler handlerUpload = new Handler(){
        public void handleMessage(Message message)
        {
            progress.dismiss();
        }
    };

    public void OpenDoc(View view)
    {
        Log.e("Drive.FIle", "OpenDoc command");
        DriveFile file = file = Drive.DriveApi.getFile(mGoogleApiClient, DriveId.decodeFromString("1OouR4nRNwA-7By8_MxhFREwo45CgBh9nU0jkAIEcDF8"));
        file.open(mGoogleApiClient, DriveFile.MODE_READ_WRITE, null)
                .setResultCallback(contentsOpenedCallback);
    }

    public static Bitmap convertToMutable(final Context context, final Bitmap imgIn) {
        final int width = imgIn.getWidth(), height = imgIn.getHeight();
        final Bitmap.Config type = imgIn.getConfig();
        File outputFile = null;
        final File outputDir = context.getCacheDir();
        try {
            outputFile = File.createTempFile(Long.toString(System.currentTimeMillis()), null, outputDir);
            outputFile.deleteOnExit();
            final RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile, "rw");
            final FileChannel channel = randomAccessFile.getChannel();
            final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
            imgIn.copyPixelsToBuffer(map);
            imgIn.recycle();
            final Bitmap result = Bitmap.createBitmap(width, height, type);
            map.position(0);
            result.copyPixelsFromBuffer(map);
            channel.close();
            randomAccessFile.close();
            outputFile.delete();
            return result;
        } catch (final Exception e) {
        } finally {
            if (outputFile != null)
                outputFile.delete();
        }
        return null;
    }

    private List<Bitmap> GetImagesArray(int score) {
        List<Bitmap> images = new ArrayList<Bitmap>();
        while(score > 0) {
            int rest  = score % 10;
            score = (int)(score / 10);
            switch(rest) {
                case 0:
                    Bitmap letterImage0 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number0);
                    images.add(letterImage0);
                    break;
                case 1:
                    Bitmap letterImage1 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number1);
                    images.add(letterImage1);
                    break;
                case 2:
                    Bitmap letterImage2 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number2);
                    images.add(letterImage2);
                    break;
                case 3:
                    Bitmap letterImage3 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number3);
                    images.add(letterImage3);
                    break;
                case 4:
                    Bitmap letterImage4 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number4);
                    images.add(letterImage4);
                    break;
                case 5:
                    Bitmap letterImage5 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number5);
                    images.add(letterImage5);
                    break;
                case 6:
                    Bitmap letterImage6 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number6);
                    images.add(letterImage6);
                    break;
                case 7:
                    Bitmap letterImage7 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number7);
                    images.add(letterImage7);
                    break;
                case 8:
                    Bitmap letterImage8 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number8);
                    images.add(letterImage8);
                    break;
                case 9:
                    Bitmap letterImage9 =BitmapFactory.decodeResource(getResources(),
                            R.drawable.number9);
                    images.add(letterImage9);
                    break;
            }
        }
        return images;
    }


    private class CreateImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            String FbUserId = params[0];
            int score = Integer.valueOf(params[1]);
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap image =BitmapFactory.decodeResource(getResources(),
                    R.drawable.score, op);
            if (!image.isMutable()) {
                image = convertToMutable(QuizActivity.this, image);
            }
            //image =image.copy(Bitmap.Config.ARGB_8888, true);
            //context = getApplicationContext();
            List<Bitmap> letters = GetImagesArray(score);
            String regenerated = "";

            Canvas mComboImage = new Canvas(image);
            int xPosition = 2000;
            int yPosition = 600;
            Bitmap processedImage = image;

            // Add profilImage
            Bitmap profilImage = getPhotoFacebook(FbUserId);
            //SaveImg(profilImage, score);
            if (!profilImage.isMutable()) {
                profilImage = convertToMutable(QuizActivity.this, profilImage);
            }
            profilImage = Bitmap.createScaledBitmap(profilImage, 465, 450, true);
            mComboImage.drawBitmap(profilImage.copy(Bitmap.Config.ARGB_8888, true), 1775, 85, null);
            BitmapDrawable profilImageDrawable = new BitmapDrawable(profilImage);
            processedImage = ((BitmapDrawable) profilImageDrawable).getBitmap();

            // Add score
            for (Bitmap letterImage : letters) {
                mComboImage.drawBitmap(letterImage.copy(Bitmap.Config.ARGB_8888, true), xPosition, yPosition, null);
                xPosition -= 280;
                BitmapDrawable mBitmapDrawable = new BitmapDrawable(image);
                processedImage = ((BitmapDrawable) mBitmapDrawable).getBitmap();
            }
            //SaveImg(processedImage, score);
            return processedImage;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            Settings.setImageResource(R.drawable.settings_white);
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(result)
                    .setCaption("Learn german with WordLex: https://play.google.com/store/apps/details?id=com.profimedica.wordlex")
                    .build();
            List<String> peoples = new ArrayList<>();
            peoples.add(FbUserId);
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .setPeopleIds(peoples)
                    .setRef("https://play.google.com/store/apps/details?id=com.profimedica.wordlex")
                    .addPhoto(photo)
                            //.setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.profimedica.wordlex"))
                    .build();
            ShareDialog shareDialog = new ShareDialog(QuizActivity.this);
            //if (ShareDialog.canShow(SharePhotoContent.class))
            {
                shareDialog.show(content);
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public Bitmap getPhotoFacebook(final String id) {

        Bitmap bitmap=null;
        final String nomimg = "https://graph.facebook.com/"+id+"/picture?type=large";
        URL imageURL = null;

        try {
            imageURL = new URL(nomimg);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) imageURL.openConnection();
            connection.setDoInput(true);
            connection.setInstanceFollowRedirects( true );
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            //img_value.openConnection().setInstanceFollowRedirects(true).getInputStream()
            bitmap = BitmapFactory.decodeStream(inputStream);

        } catch (IOException e) {

            e.printStackTrace();
        }
        return bitmap;

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progress = new ProgressDialog(this);
        Intent intent = getIntent();
        FbUserId = intent.getExtras().getString("FbUserId");
        dictionaryPath = intent.getExtras().getString("LexPath");
        if(intent.getExtras().getParcelableArrayList("Lex") != null) {
            wordsToBeDiscovered = intent.getExtras().getParcelableArrayList("Lex");
        }
        //EmptyTable();
        if(mDbHelper == null)
            mDbHelper = new WordReaderDbHelper(this);
        //EmptyTable(this, "");
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
                if(dictionaryPath == "DeEn") {
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
                if(dictionaryPath == "FrEn") {
                    TTSnative.setLanguage(Locale.FRENCH);
                    new Thread(new Runnable() {
                        public void run() {
                            //if (status != TextToSpeech.ERROR)
                            {
                                TTSnative.setLanguage(Locale.FRENCH);
                            }
                        }
                    });
                }
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

        ScoreInfo = (TextView) findViewById( R.id.scoreInfo );
        Timer = (TextView) findViewById( R.id.timer );
        Timer.setOnClickListener(timerButtonListner);
        Settings = (ImageView) findViewById( R.id.settings);
        Settings.setOnClickListener(settingsButtonListner);
        SpeackerSwitch = (ImageView) findViewById( R.id.SpeackerSwitch);
        SpeackerSwitch.setOnClickListener(SpeackerSwitchListner);
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

                if( millis == 5 || millis == 10 || millis == 15 || millis == 10 || millis == 25)
                {
                   if(!standBy) {
                       score--;
                       if(score < 0)
                       {
                           score = 0;
                       }
                       ScoreInfo.setText("scored " + String.valueOf(score) + " points");
                   }
                }
            }




            public void onFinish() {
                millis = 0;
                currentWord = CreateQuiz(wordsToBeDiscovered);
                standBy = true;
            }
        };

        /*if (wordsToBeDiscovered.size() == 0 && getLastInsertId(db, "") < 0) {
            if (false) {
                ReadCSV(this, "DeEn");

                WriteSQL(this, "Saved");
                FilterRecords(difficulty);
            } else {
                ReadCSV(this, "Online DeEn");
            }

        }
        else {
        */
            WriteSQL(this, "Saved");
            FilterRecords(difficulty);
        //}

        AppKeyPair appKeyPair =  new AppKeyPair(ACCESS_KEY_NAME, ACCESS_SECRET_NAME);
        AndroidAuthSession session;
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);

        if(key != null && secret != null) {
            AccessTokenPair token = new AccessTokenPair(key, secret);
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, token);
        }
        else
        {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        }
        dropboxApi = new DropboxAPI(session);

        session = (AndroidAuthSession)dropboxApi.getSession();
        if(session.authenticationSuccessful())
        {
            try{
                session.finishAuthentication();
                storeAuth(session);
                //loggedIn(true);
            } catch (IllegalStateException e)
            {
                Toast.makeText(this, "Dropbox auth error", Toast.LENGTH_SHORT).show();
            }
        }

        Log.e("Drive.FIle", "OpenDoc mGoogleApiClient");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
    // Request code to identify the response of a web request
    int HTTP_REQUEST_CODE = 98;

    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
            return;
        }
    }

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
                wordsToBeDiscovered = Utils.ConsumeString(input);
                AnteNative.setText("DataSource");
                AnteForeign.setText("Basic");
            } else {
                //Toast.makeText(getApplicationContext(), "Generated from SQLite", Toast.LENGTH_SHORT).show();
                InputStream csvStream = assetManager.open(fileName + ".csv");
                // InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
                String input = convertStreamToString(csvStream);
                wordsToBeDiscovered = Utils.ConsumeString(input);
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
        if(!SpeackerSwitchMuted) {
            if (nativeFirst) {
                TTSnative.speak(currentWord.Native, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                TTSforeign.speak(currentWord.Foreign, TextToSpeech.QUEUE_FLUSH, null);
            }
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
        //mGoogleApiClient.connect();
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

