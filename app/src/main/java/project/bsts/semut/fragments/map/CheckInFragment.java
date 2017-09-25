package project.bsts.semut.fragments.map;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import project.bsts.semut.LoginActivity;
import project.bsts.semut.R;
import project.bsts.semut.TrackerActivity;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.pojo.trayek.Trayek;
import project.bsts.semut.services.CheckStatusJob;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.CommonAlerts;


public class CheckInFragment extends Fragment {

    Button mCancelBtn, mYesBtn;
    ImageButton mCloseBtn;
    EditText mRemarks;
    Spinner mSpinnerTrayek, mSpinnerArah;
    ProgressDialog mDialog;
    ArrayList<Trayek> trayek = new ArrayList<Trayek>();
    LinearLayout mLayoutSpinner;
    private int trayekState, arahState;
    private PreferencesManager preferencesManager;
    private String arahStr;
    private  int REMINDER_INTERVAL_MINUTES = 15;
    private  int REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_penumpang, container, false);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            REMINDER_INTERVAL_MINUTES = 15;
            REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
        }else {
            REMINDER_INTERVAL_MINUTES = 5;
            REMINDER_INTERVAL_SECONDS = (int) (TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES));
        }

        mCancelBtn = view.findViewById(R.id.cancel_btn);
        mYesBtn = view.findViewById(R.id.submitButton);
        mCloseBtn = view.findViewById(R.id.closeButton);
        mRemarks = view.findViewById(R.id.remarks);
        mSpinnerTrayek = view.findViewById(R.id.spinnerTrayek);
        mSpinnerArah = view.findViewById(R.id.spinnerArah);
        mLayoutSpinner = view.findViewById(R.id.layoutSpinner);
        mLayoutSpinner.setVisibility(View.GONE);
        mSpinnerArah.setVisibility(View.GONE);
        mRemarks.setVisibility(View.GONE);

        mCloseBtn.setOnClickListener(v -> getActivity().finish());
        mCancelBtn.setOnClickListener(v -> toTrackActivity());

        preferencesManager = new PreferencesManager(getActivity());

        mYesBtn.setOnClickListener(v -> {
            if(mYesBtn.getText().toString().equals("SUBMIT")){
                int trayekId = trayek.get(trayekState).getTrayekID();
                int arahID = trayek.get(trayekState).getArah().get(arahState).getFlag();
                String jmlhPenumpang = mRemarks.getText().toString();
                if(mSpinnerTrayek.getSelectedItemPosition() == 0 ||
                        mSpinnerArah.getSelectedItemPosition() == 0 ||
                        jmlhPenumpang.equals("") || jmlhPenumpang.equals("0")){
                    Snackbar.make(v, "Isian kolom belum sesuai", Snackbar.LENGTH_LONG).show();
                }else {
                    submit(trayekId, arahID, Integer.parseInt(jmlhPenumpang));
                }
            }
            if(!mYesBtn.getText().toString().equals("SUBMIT"))
                mYesBtn.setText("SUBMIT");
            if(mCancelBtn.getVisibility() == View.VISIBLE)
                mCancelBtn.setVisibility(View.GONE);
            if(mLayoutSpinner.getVisibility() == View.GONE)
                mLayoutSpinner.setVisibility(View.VISIBLE);
        });

        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Memuat Trayek ...");
        populateData();


        return view;
    }


    private void submit(int trayekID, int arahID, int jumlahPenumpang){
        mDialog.show();
        new RequestRest(getActivity(), (pResult, type) -> {
            mDialog.dismiss();
            Log.i("check in", pResult);
            if(type.contains(Constants.REST_INSERT_PENUMPANG)){
                RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                if(!requestStatus.getSuccess()) CommonAlerts.commonError(getActivity(), requestStatus.getMessage());
                else {
                    Toast.makeText(getActivity(), "Berhasil mengirimkan status penumpang", Toast.LENGTH_LONG).show();
                    try {
                        JSONObject object = new JSONObject(pResult);
                        preferencesManager.save(object.getString("object_id"), Constants.PREFS_WAITING_STATUS_OBJECT_ID);
                        preferencesManager.save(true, Constants.PREFS_IS_WAITING);
                        preferencesManager.save(jumlahPenumpang, Constants.PREFS_WAITING_COUNT);
                        preferencesManager.save(arahStr, Constants.PREFS_WAITING_ROUTE);
                        preferencesManager.apply();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showNotification();
                    toTrackActivity();
                }
            }else
                CommonAlerts.commonError(getActivity(), Constants.MESSAGE_HTTP_ERROR);
        }).insertPenumpang(jumlahPenumpang, trayekID, arahID);

    }


    private void populateData(){
        mDialog.show();
        new RequestRest(getActivity(), (pResult, type) -> {
            mDialog.dismiss();
            if(type.contains(Constants.REST_GET_TRAYEK)){
                RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                if(!requestStatus.getSuccess()) CommonAlerts.commonError(getActivity(), requestStatus.getMessage());
                else {
                    try {
                        JSONObject object = new JSONObject(pResult);
                        JSONArray jsonArray = object.getJSONArray("data");
                        //trayek = new ArrayList<Trayek>();
                        for(int i = 0; i < jsonArray.length(); i++){
                            Trayek model = new Gson().fromJson(jsonArray.get(i).toString(), Trayek.class);
                            trayek.add(model);
                        }

                        String[] trayekStr = new String[trayek.size()+1];
                        trayekStr[0] = "-- Pilih Trayek --";

                        for(int i = 1; i <= trayek.size(); i++){
                            trayekStr[i] = trayek.get(i-1).getTrayekName();
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, trayekStr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        mSpinnerTrayek.setAdapter(adapter);
                        mSpinnerTrayek.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                if(position > 0) {
                                    setArahSpinner(position - 1);
                                    trayekState = position -1;
                                    if(mSpinnerArah.getVisibility() == View.GONE)
                                        mSpinnerArah.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else CommonAlerts.commonError(getActivity(), Constants.MESSAGE_HTTP_ERROR);
        }).getTrayek();
    }

    private void setArahSpinner(int position) {
        String[] trayekStr = new String[trayek.get(position).getArah().size()+1];
        trayekStr[0] = "-- Pilih Arah --";
        for(int i = 1 ; i < trayekStr.length; i ++){
            trayekStr[i] = trayek.get(position).getArah().get(i-1).getNama();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, trayekStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerArah.setAdapter(adapter);
        mSpinnerArah.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position > 0){
                    arahState = position -1;
                    if(mRemarks.getVisibility() == View.GONE) {
                        mRemarks.setVisibility(View.VISIBLE);
                        arahStr = mSpinnerArah.getSelectedItem().toString();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void showNotification(){
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder b = new NotificationCompat.Builder(getActivity());
        b.setOngoing(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_hourglass_empty_black_24dp)
                .setTicker("Semut Angkot")
                .setContentTitle("Semut Angkot")
                .setContentText("Anda sedang dalam status menunggu Angkot, ketuk untuk mengakhiri")
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setContentIntent(contentIntent)
                .setContentInfo("Info");
        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Constants.WAITING_NOTIFICATION_ID, b.build());


        Driver driver = new GooglePlayDriver(getActivity());
        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);
        firebaseJobDispatcher.cancelAll();
        Job constraintReminderJob = firebaseJobDispatcher.newJobBuilder()
                .setService(CheckStatusJob.class)
                .setTag("checkstatus")
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS))
                .setReplaceCurrent(true)
                .build();
       // firebaseJobDispatcher.schedule(constraintReminderJob);
        firebaseJobDispatcher.mustSchedule(constraintReminderJob);
    }


    private void toTrackActivity(){
        startActivity(new Intent(getActivity(), TrackerActivity.class));
        getActivity().finish();
    }
}
