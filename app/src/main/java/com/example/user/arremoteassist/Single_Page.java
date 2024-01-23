package com.example.user.arremoteassist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

public
class Single_Page extends AppCompatActivity implements ScreenSharingWrapper.RunningStateListener {

    private static final String TAG = "TravelActivity";
    private static final int REQUEST_CODE_RECORD_AUDIO_PERMISSIONS = 1;
    private static final int REQUEST_CODE_CAMERA_PERMISSIONS = 2;
    private static final int REQUEST_CODE_PICK_FILE = 201;
    private static final int REQUEST_CODE_TAKE_PICTURE = 202;
    EditText code;
    Button startB, stopB;
    private Uri m_pendingPictureUri;

    private MenuItem m_menuItemHelp;
    private MenuItem m_menuItemShare;
    private AlertDialog m_sessionCodeDialog;
    private Snackbar m_snackbar = null;
    ProgressBar pleaseWait;

    @Override
    protected
    void onCreate ( Bundle savedInstanceState ) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_single_page );
        code = findViewById ( R.id.code );
        startB = findViewById ( R.id.start );
        stopB = findViewById ( R.id.stop );
        pleaseWait = findViewById ( R.id.pleaseWait );
        pleaseWait.setVisibility ( View.INVISIBLE );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar ( toolbar );
//        pleaseWait.setVisibility ( View.VISIBLE );





        Intent intent = getIntent ( );
        if ( intent != null && intent.getData ( ) != null ) {
            String receivedData = intent.getData ( ).getQueryParameter ( "message" );
            // Use the receivedData in your app
            code.setText ( receivedData );
            ScreenSharingWrapper.getInstance ( ).startTeamViewerSession ( Single_Page.this ,
                    "s" + code.getText ( ).toString ( ) );
            startB.setEnabled ( false );

            pleaseWait.setVisibility ( View.VISIBLE );
        }

        startB.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public
            void onClick ( View v ) {
                ScreenSharingWrapper.getInstance ( ).startTeamViewerSession ( Single_Page.this ,
                        "s" + code.getText ( ).toString ( ) );
                startB.setEnabled ( false );
                pleaseWait.setVisibility ( View.VISIBLE );
            }
        } );
        stopB.setOnClickListener ( new View.OnClickListener ( ) {
            @Override
            public
            void onClick ( View v ) {
                ScreenSharingWrapper.getInstance ( ).stopRunningSession ( );
                startB.setEnabled ( true );
                pleaseWait.setVisibility ( View.INVISIBLE );
            }
        } );
    }


    @Override
    protected
    void onResume () {
        super.onResume ( );
        // ensure the correct reference is listening
        ScreenSharingWrapper.getInstance ( ).setRunningStateListener ( this );
//        updateSnackbar(ScreenSharingWrapper.getInstance().isSessionRunning());

        handleOverlayPermission ( );

    }

    @Override
    protected
    void onPause () {
        super.onPause ( );
        // don't keep the reference when activity is destroyed
        ScreenSharingWrapper.getInstance ( ).setRunningStateListener ( null );
    }

    private
    void handleOverlayPermission () {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays ( this ) ) {
            showOverlayPermissionDialog ( );
        }
    }

    private
    void showOverlayPermissionDialog () {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder ( this );
        alertBuilder.setTitle ( "Special app access required" );
        alertBuilder.setMessage ( "Travel app needs permission to display over other app" );
        alertBuilder.setNegativeButton ( "Cancel" ,
                ( dialog , i ) -> {
                    dialog.dismiss ( );
                } );
        alertBuilder.setCancelable ( false );
        alertBuilder.setPositiveButton ( "Settings" ,
                ( dialog , i ) -> openSettingsIntent ( ) );
        alertBuilder.show ( );
    }

    private
    void openSettingsIntent () {
        if ( android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ) {
            Intent intent = new Intent ( Settings.ACTION_MANAGE_OVERLAY_PERMISSION ,
                    Uri.parse ( "package:" + getPackageName ( ) ) );
            if ( intent.resolveActivity ( getPackageManager ( ) ) != null ) {
                startActivity ( intent );
            } else {
                Log.e ( TAG ,
                        "Failed to display overlay permission screen" );
            }
        }
    }

    @Override
    public
    void onRunningStateChange ( @NonNull SessionState sessionState ) {
        // saving the state is not necessary and
        // missed events between #onPause() and #onResume()
        // are intercepted by querying the session state in
        // #updateMenuItemState(MenuItem)
        invalidateOptionsMenu ( );
//        updateSnackbar(sessionState != SessionState.NoSession);

        //Request record audio permission if needed
        if ( sessionState == SessionState.ScreenSharing &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission ( Single_Page.this ,
                        android.Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED ) {
            requestPermissions ( new String[]{Manifest.permission.RECORD_AUDIO} ,
                    REQUEST_CODE_RECORD_AUDIO_PERMISSIONS );
        }
    }
}