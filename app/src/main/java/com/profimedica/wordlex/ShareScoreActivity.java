package com.profimedica.wordlex;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ShareScoreActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    int score = 0;
    String UserId;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    public static Context context;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private TextView mContentView;
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

    private class CreateImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
                String UserId = params[0];
                int score = Integer.valueOf(params[1]);
                BitmapFactory.Options op = new BitmapFactory.Options();
                op.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap image =BitmapFactory.decodeResource(getResources(),
                        R.drawable.score, op);
                if (!image.isMutable()) {
                    image = convertToMutable(ShareScoreActivity.this, image);
                }
                //image =image.copy(Bitmap.Config.ARGB_8888, true);
                context = getApplicationContext();
                List<Bitmap> letters = GetImagesArray(score);
                String regenerated = "";

                Canvas mComboImage = new Canvas(image);
                int xPosition = 2000;
                int yPosition = 600;
                Bitmap processedImage = image;

                // Add profilImage
                Bitmap profilImage = getPhotoFacebook(UserId);
                //SaveImg(profilImage, score);
                if (!profilImage.isMutable()) {
                    profilImage = convertToMutable(ShareScoreActivity.this, profilImage);
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
                SaveImg(processedImage, score);
            return processedImage;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_share_score);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = (TextView)findViewById(R.id.fullscreen_content);

        Intent intent = getIntent();
        score = intent.getExtras().getInt("SCORE");
        UserId = intent.getExtras().getString("UserId");
        new CreateImage().execute(UserId, String.valueOf(score));
        /*mContentView.setText(regenerated);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image)
                .build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo)
                .setContentUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.profimedica.wordlex"))
                .build();
        ShareDialog shareDialog = new ShareDialog(this);
        if (ShareDialog.canShow(SharePhotoContent.class)) {
            shareDialog.show(content);
        }
        */
        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
    }

    int HTTP_REQUEST_CODE = 98;
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

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
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

    private void SaveImg(Bitmap mNewSaving, int score) {
        MediaStore.Images.Media.insertImage(getContentResolver(), mNewSaving, "WordLex", "Scored " + String.valueOf(score) + " points");
/*
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/data");
        if(!myDir.exists()) {
            boolean created = myDir.mkdirs();
            created = ! created;
        }

        String fname = "Img.bmp";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            for (Word word: words)
            {
                out.write((word.Native + " = " + word.Foreign + " = " + String.valueOf(word.Bad) + " = " + String.valueOf(word.Bad) + " = " + String.valueOf(word.TimeSpend) + " = " + String.valueOf(word.FGood) + " = " + String.valueOf(word.FBad) + " = " + String.valueOf(word.FSpend) + "\n").getBytes());
            }
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }*/
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
