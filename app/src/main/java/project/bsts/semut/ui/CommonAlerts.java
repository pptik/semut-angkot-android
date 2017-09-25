package project.bsts.semut.ui;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.content.IntentCompat;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import project.bsts.semut.LoginActivity;
import project.bsts.semut.R;
import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.setup.Constants;

public class CommonAlerts {

    public static void commonError(Context context, String msg){
        FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(context)
                .setImageDrawable(new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_cloud_off)
                        .sizeDp(100)
                        .color(context.getResources().getColor(R.color.cochineal_red)))
                .setTextTitle("Gagal memuat permintaan")
                .setTextSubTitle("Permintaan Anda tidak dapat diproses")
                .setBody(msg)
                .setAutoHide(false)
                .setPositiveButtonText("OK")
                .setPositiveColor(R.color.primary_dark)
                .setOnPositiveClicked((view, dialog) -> {
                    ((Activity)context).finish();
                })
                .build();
        alert.show();
    }


    public static void errorSession(Context context, String msg){
        FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(context)
                .setImageDrawable(new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_hourglass_empty)
                        .sizeDp(100)
                        .color(context.getResources().getColor(R.color.cochineal_red)))
                .setTextTitle("Gagal memuat permintaan")
                .setTextSubTitle("Sesi Anda telah berakhir, silahkan login kembali untuk menggunakan aplikasi")
                .setBody(msg)
                .setAutoHide(false)
                .setPositiveButtonText("OK")
                .setPositiveColor(R.color.primary_dark)
                .setOnPositiveClicked((view, dialog) -> {
                  /*  PreferencesManager preferencesManager = new PreferencesManager(context);
                    preferencesManager.save(false, Constants.IS_LOGGED_IN);
                    preferencesManager.apply();
                    context.startActivity(new Intent(context, LoginActivity.class));
                    ((Activity)context).finish(); */


                    PreferencesManager preferencesManager = new PreferencesManager(context);
                    preferencesManager.save(false, Constants.IS_LOGGED_IN);
                    preferencesManager.apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ComponentName cn = intent.getComponent();
                    Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                    context.startActivity(mainIntent);
                })
                .build();
        alert.show();
    }


    public static void gspIsDisable(Context context){
        FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(context)
                .setImageDrawable(new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_location_disabled)
                        .sizeDp(100)
                        .color(context.getResources().getColor(R.color.cochineal_red)))
                .setTextTitle("LOKASI ANDA TIDAK AKTIF")
                .setTextSubTitle("Aplikasi membutuhkan lokasi Anda")
                .setBody("Aplikasi ini membutuhkan lokasi Anda yang saat ini sedang non-aktif. Aktifkan terlebih dahulu untuk melanjutkan")
                .setAutoHide(false)
                .setPositiveButtonText("OK")
                .setPositiveColor(R.color.primary_dark)
                .setOnPositiveClicked((view, dialog) -> {
                    Intent callGPSSettingIntent = new Intent(
                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(callGPSSettingIntent);
                    ((Activity)context).finish();
                }).setNegativeButtonText("Keluar")
                .setOnNegativeClicked(((view, dialog) -> {
                    ((Activity)context).finish();
                }))
                .build();


        alert.show();
    }


    public static void internetIsDisabled(Context context){
        FancyAlertDialog.Builder alert = new FancyAlertDialog.Builder(context)
                .setImageDrawable(new IconicsDrawable(context)
                        .icon(GoogleMaterial.Icon.gmd_cloud_off)
                        .sizeDp(100)
                        .color(context.getResources().getColor(R.color.cochineal_red)))
                .setTextTitle("INTERNET ANDA TIDAK AKTIF")
                .setTextSubTitle("Aplikasi membutuhkan koneksi internet Anda")
                .setBody("Aplikasi ini membutuhkan koneksi internet Anda yang saat ini sedang non-aktif. Aktifkan terlebih dahulu untuk melanjutkan")
                .setAutoHide(false)
                .setPositiveButtonText("OK")
                .setPositiveColor(R.color.primary_dark)
                .setOnPositiveClicked((view, dialog) -> {
                    Intent callGPSSettingIntent = new Intent(
                            Settings.ACTION_WIFI_SETTINGS);
                    context.startActivity(callGPSSettingIntent);
                    ((Activity)context).finish();
                }).setNegativeButtonText("Keluar")
                .setOnNegativeClicked(((view, dialog) -> {
                    ((Activity)context).finish();
                }))
                .build();


        alert.show();
    }
}
