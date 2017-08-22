package project.bsts.semut;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import project.bsts.semut.ui.CommonAlerts;
import project.bsts.semut.utilities.CheckService;
import android.Manifest;

public class SplashScreenActivity extends AppCompatActivity {


    private static final int SPLASH_TIME = 2 * 1000;// 3 * 1000
    private Context context;
    boolean isApprove;

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
        Intent intent = new Intent(SplashScreenActivity.this, TrackerActivity.class);
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
            RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
            if(requestStatus.getSuccess()){
                // to main
                toDashboard();
            }else {
                CommonAlerts.errorSession(context, requestStatus.getMessage());
            }
        }).checkStatus();
    }


    @Override
    public void onBackPressed() {
       // this.finish();
        super.onBackPressed();
    }
}