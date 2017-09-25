package project.bsts.semut.services;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

import org.apache.commons.lang3.RandomUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import project.bsts.semut.LoginActivity;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.setup.Constants;

public class CheckStatusJob extends JobService {
    @Override
    public boolean onStartJob(JobParameters job) {
        Log.i("job", "start job");
        PreferencesManager preferencesManager = new PreferencesManager(getApplicationContext());
        String objectID = preferencesManager.getString(Constants.PREFS_WAITING_STATUS_OBJECT_ID);
        new RequestRest(getApplicationContext(), (pResult, type) -> {
            if(type.contains(Constants.REST_CHECK_PENUMPANG)){
                RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                if(requestStatus.getSuccess()){
                    try {
                        JSONObject object = new JSONObject(pResult);
                        JSONArray array = object.getJSONArray("data");
                        if(array.length() > 0){
                            jobFinished(job, false);
                            showNotification();
                        }else {
                            new RequestRest(getApplicationContext(), (pResult1, type1) -> {
                                Log.i("Job", "Update to finish");
                                jobFinished(job, false);
                                cancelNotification();
                            }).updatePenumpang(objectID);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }else Log.i("Job", requestStatus.getMessage());
            }else Log.i("Job", Constants.MESSAGE_HTTP_ERROR);
        }).checkStatusPenumpang(objectID);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        Log.i("job", "stop job");

        return true;
    }

    private void cancelNotification(){
        NotificationManager manager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Constants.WAITING_NOTIFICATION_ID);
    }

    private void showNotification(){
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setTicker("Semut Angkot")
                .setContentTitle("Reminder : Anda sedang menunggu angkot")
                .setContentText("Reminder : Anda sedang menunggu angkot")
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(RandomUtils.nextInt(90, 9999), b.build());
    }



}
