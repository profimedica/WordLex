package com.profimedica.wordlex;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.AsyncTask;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private final static int RC_SIGN_IN_REQUEST_CODE = 22;
    private final static int RESOLVE_CONNECTION_REQUEST_CODE = 5;

    String FbUserId;
    String FbName;
    String FbEmail;
    String FbGender;
    String FbBirthday;

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

    LoginButton loginButton;
    CallbackManager callbackManager;
    GoogleApiClient mGoogleApiClient;

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
    public void onConnected(Bundle connectionHint) {
        int i =0;
        Log.e("Login: ", "Connected with GoogleApiClient");
        /*IntentSender intentSender = Drive.DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[] { "text/plain", "text/html" })
                .build(mGoogleApiClient);
        try {
            startIntentSenderForResult(
                    intentSender, 55, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.e(" cd ", "Unable to send intent drive", e);
        }*/

        mGoogleApiClient.connect();
        //updateUI(true);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        int i =0;
        // The connection has been interrupted.
        // The connection has been interrupted.
        // Disable any UI components that depend on Google APIs
        // until onConnected() is called.
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

    private void signIn() {
        if (mGoogleApiClient.hasConnectedApi(Drive.API)) {
            Log.e("Google signIn", "Get file");
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle("New folder").build();
            Drive.DriveApi.getRootFolder(mGoogleApiClient).createFolder(
                    mGoogleApiClient, changeSet).setResultCallback(null);

            DriveFile file = file = Drive.DriveApi.getFile(mGoogleApiClient, DriveId.zzcQ("1OouR4nRNwA-7By8_MxhFREwo45CgBh9nU0jkAIEcDF8"));
            file.open(mGoogleApiClient, DriveFile.MODE_READ_WRITE, null)
                    .setResultCallback(contentsOpenedCallback);
        }
        else if(mGoogleApiClient.hasConnectedApi(Auth.GOOGLE_SIGN_IN_API)) {
            Log.e("Google signIn", "signIn method");
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN_REQUEST_CODE);
        }
        else
        {
            Log.e("Google signIn", "Connect again");
            mGoogleApiClient.connect();
            /*DriveFile file = file = Drive.DriveApi.getFile(mGoogleApiClient, DriveId.zzcQ("1OouR4nRNwA-7By8_MxhFREwo45CgBh9nU0jkAIEcDF8"));
            file.open(mGoogleApiClient, DriveFile.MODE_READ_WRITE, null)
                    .setResultCallback(contentsOpenedCallback);*/
        }
    }

    public GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e("Google sign in or drive", "Error: "+ connectionResult.getErrorMessage());
            Log.e("Drive.FIle", "OpenDoc faild");
            if (connectionResult.hasResolution()) {
                try {
                   connectionResult.startResolutionForResult(LoginActivity.this, RESOLVE_CONNECTION_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    Log.e("Drive.FIle", e.getMessage());
                    // Unable to resolve, message user appropriately
                }
            } else {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), LoginActivity.this, 0).show();
                Log.e("Drive.FIle", "OpenDoc "+String.valueOf(connectionResult.getErrorCode()));
            }}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize FbSDK before layout
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        final TextView infoTextView = (TextView)findViewById(R.id.info);
        mContentView = findViewById(R.id.logoImage);

        // Without Login
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoTextView.setText("Please wait...");
                updateUI(true);
            }
        });

        // Visit address
        infoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/adrian-cumpanasu-333820107"));
                startActivity(browserIntent);
            }
        });

        // Facebook login
        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday, user_friends", "user_posts", "user_likes"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                infoTextView.setText("FB: Please wait...");
                Log.e("LoginActivity", loginResult.toString());
                FbUserId = loginResult.getAccessToken().getUserId();

                /*
                if (AccessToken.getCurrentAccessToken() == null) {
                    waitForFacebookSdk();
                } else {
                    DoFB();
                }
                */
                updateUI(true);

            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken(getString(R.string.server_client_id))
                .requestScopes(Drive.SCOPE_FILE)
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, onConnectionFailedListener)
                //.addApi(Drive.API)
                //.addScope(Drive.SCOPE_FILE)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Drive.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();


        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.sign_in_button:
                        signIn();
                        break;
                }
            }
        });

    }

    // Request code to identify the response of a web request
    int HTTP_REQUEST_CODE = 98;
    // Words to be discovered
    List<Word> wordsToBeDiscovered = new ArrayList<>();

    private void DoFB() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender, birthday");
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
            /* handle the result */
                        Log.e("FacebookGraphResponse", response.getJSONObject().toString() + "");
                    }
                }
        ).executeAsync();


    }

    private void waitForFacebookSdk() {
        AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] params) {
                int tries = 0;
                while (tries < 3) {
                    if (AccessToken.getCurrentAccessToken() == null) {
                        tries++;
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                DoFB();
            }
        };
        asyncTask.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;
            case RC_SIGN_IN_REQUEST_CODE:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            case 55:
                Log.e("Google API", String.valueOf(resultCode) + " + " +  data.getDataString());
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.e("Google Sign in", "handleSignInResult:" + String.valueOf(result.isSuccess()));// + result.getSignInAccount().getDisplayName() + " - " + result.isSuccess());
        if (result.isSuccess()) {


            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String sss = acct.getDisplayName();
            updateUI(true);
        } else {
            // Signed out, show unauthenticated UI.

            updateUI(false);
        }
    }

    private void updateUI(boolean update)
    {
        if(update) {
            //Intent intent = new Intent(LoginActivity.this, QuizActivity.class);
            Intent intent = new Intent(LoginActivity.this, SelectLex.class);
            intent.putExtra("FbUserId", FbUserId);
            startActivity(intent);
        }
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
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        //        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        // mHideHandler.removeCallbacks(mHidePart2Runnable);
        // mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
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