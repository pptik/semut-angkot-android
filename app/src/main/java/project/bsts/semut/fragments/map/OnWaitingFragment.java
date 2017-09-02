package project.bsts.semut.fragments.map;

import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
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
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import project.bsts.semut.LoginActivity;
import project.bsts.semut.R;
import project.bsts.semut.TrackerActivity;
import project.bsts.semut.connections.rest.IConnectionResponseHandler;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.helper.PreferenceManager;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.pojo.trayek.Trayek;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.CommonAlerts;


public class OnWaitingFragment extends Fragment {

    Button mCancelBtn, mYesBtn;
    ImageButton mCloseBtn;
    TextView mTitle;

    ProgressDialog mDialog;

    private PreferenceManager preferenceManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_on_waiting, container, false);

        mCancelBtn = view.findViewById(R.id.cancel_btn);
        mYesBtn = view.findViewById(R.id.submitButton);
        mCloseBtn = view.findViewById(R.id.closeButton);
        mTitle = view.findViewById(R.id.title);




        mCloseBtn.setOnClickListener(v -> getActivity().finish());
        mCancelBtn.setOnClickListener(v -> toTrackActivity());

        preferenceManager = new PreferenceManager(getActivity());
        String totalWaiting = String.valueOf(preferenceManager.getInt(Constants.PREFS_WAITING_COUNT, 0));
        String route = preferenceManager.getString(Constants.PREFS_WAITING_ROUTE);
        String tmp = "Anda dalam status sedang menunggu Angkot dengan jumlah penumpang <b>"+totalWaiting+" </b>" +
                " dan angkot dengan arah <b>"+route+"</b>. Apakah Anda akan mengakhiri status menunggu ?";
        mTitle.setText(Html.fromHtml(tmp));

        mYesBtn.setOnClickListener(v -> {
            String obejctID = preferenceManager.getString(Constants.PREFS_WAITING_STATUS_OBJECT_ID);
            new RequestRest(getActivity(), (pResult, type) -> {
                if(type.contains(Constants.REST_UPDATE_PENUMPANG)){
                    RequestStatus requestStatus = new Gson().fromJson(pResult, RequestStatus.class);
                    if(requestStatus.getSuccess()){
                        cancelNotification();
                        preferenceManager.save(false, Constants.PREFS_IS_WAITING);
                        preferenceManager.apply();

                        Driver driver = new GooglePlayDriver(getActivity());
                        FirebaseJobDispatcher firebaseJobDispatcher = new FirebaseJobDispatcher(driver);
                        firebaseJobDispatcher.cancelAll();

                        toTrackActivity();
                    }else CommonAlerts.commonError(getActivity(), requestStatus.getMessage());
                }else CommonAlerts.commonError(getActivity(), Constants.MESSAGE_HTTP_ERROR);
            }).updatePenumpang(obejctID);
        });

        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Memuat Permintaan ...");


        return view;
    }

    private void cancelNotification(){
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(Constants.WAITING_NOTIFICATION_ID);
    }


    private void toTrackActivity(){
        startActivity(new Intent(getActivity(), TrackerActivity.class));
        getActivity().finish();
    }
}
