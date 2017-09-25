package project.bsts.semut;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.view.Window;
import android.widget.Toast;

import com.google.gson.Gson;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import project.bsts.semut.connections.rest.IConnectionResponseHandler;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.helper.PermissionHelper;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.services.LocationService;
import project.bsts.semut.services.LocationUpdatesService;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.CommonAlerts;
import project.bsts.semut.utilities.CheckService;
import project.bsts.semut.utilities.Utils;

import android.Manifest;

public class SplashScreenActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{


    private static final int SPLASH_TIME = 2 * 1000;// 3 * 1000
    private Context context;
    boolean isApprove;
    private static final String TAG = SplashScreenActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private LocationUpdatesService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;



    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationUpdatesService.LocalBinder binder = (LocationUpdatesService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        context = this;

        new Handler().postDelayed(() -> {

            if(CheckService.isInternetAvailable(context)) {
                if (CheckService.isGpsEnabled(this)) {
                    checkPermission();
                } else CommonAlerts.gspIsDisable(this);
            }else CommonAlerts.internetIsDisabled(this);


        }, SPLASH_TIME);

        new Handler().postDelayed(() -> {
        }, SPLASH_TIME);

    }



    @Override
    protected void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        Log.i(TAG, "service running : "+Utils.requestingLocationUpdates(this));

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, LocationUpdatesService.class), mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }





    private void checkPermission(){
        isApprove = false;
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.ACCESS_WIFI_STATE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                for (PermissionGrantedResponse response : report.getGrantedPermissionResponses()) {
                    isApprove = true;
                    Log.i("PERMISSION", "Good Job, all permission granted");

                }

                for (PermissionDeniedResponse response : report.getDeniedPermissionResponses()) {
                    //Log.i("PERMISSION", "permission denied");
                    isApprove = false;
                    new AlertDialog.Builder(SplashScreenActivity.this).setTitle("Persetujuan Dibutuhkan")
                            .setMessage("Aplikasi ini membutuhkan fitur yang memerlukan persetujuan Anda")
                            .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                                dialog.dismiss();
                                finish();

                            })
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                dialog.dismiss();
                                checkPermission();

                            })
                            .setOnDismissListener(dialog -> finish())
                            .show();
                }

                if(isApprove){
                    checkStatus();
                }
            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                showPermissionRationale(token);
            }
        }).check();
    }


    private void toDashboard(){
        if(!Utils.requestingLocationUpdates(this))
            mService.requestLocationUpdates();
        else {
            mService.removeLocationUpdates();
            mService.requestLocationUpdates();

        }
        Intent intent = new Intent(SplashScreenActivity.this, CheckInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    public void showPermissionRationale(final PermissionToken token) {
        new AlertDialog.Builder(this).setTitle("Persetujuan Dibutuhkan")
                .setMessage("Aplikasi ini membutuhkan fitur yang memerlukan persetujuan Anda")
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                    token.cancelPermissionRequest();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                    token.continuePermissionRequest();
                })
                .setOnDismissListener(dialog -> token.cancelPermissionRequest())
                .show();
    }


    private void checkStatus(){
        new RequestRest(context, (pResult, type) -> {
            if(type.equals(Constants.REST_USER_STATUS)) {
                RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                if (requestStatus.getSuccess()) {
                    // to main
                    toDashboard();
                } else {
                    CommonAlerts.errorSession(context, requestStatus.getMessage());
                }
            }else CommonAlerts.commonError(context, Constants.MESSAGE_HTTP_ERROR);
        }).checkStatus();
    }




    @Override
    public void onBackPressed() {
       // this.finish();
        super.onBackPressed();
    }


    /*private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(LocationUpdatesService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(SplashScreenActivity.this, Utils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
        }
    } */



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Utils.KEY_REQUESTING_LOCATION_UPDATES)) {

        }
    }
}