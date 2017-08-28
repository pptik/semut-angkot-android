package project.bsts.semut.fragments.map;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import project.bsts.semut.R;
import project.bsts.semut.TrackerActivity;
import project.bsts.semut.connections.rest.IConnectionResponseHandler;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.pojo.trayek.Trayek;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_penumpang, container, false);
        mCancelBtn = (Button)view.findViewById(R.id.cancel_btn);
        mYesBtn = (Button)view.findViewById(R.id.submitButton);
        mCloseBtn = (ImageButton)view.findViewById(R.id.closeButton);
        mRemarks = (EditText)view.findViewById(R.id.remarks);
        mSpinnerTrayek = (Spinner)view.findViewById(R.id.spinnerTrayek);
        mSpinnerArah = (Spinner)view.findViewById(R.id.spinnerArah);
        mLayoutSpinner = (LinearLayout)view.findViewById(R.id.layoutSpinner);
        mLayoutSpinner.setVisibility(View.GONE);
        mSpinnerArah.setVisibility(View.GONE);
        mRemarks.setVisibility(View.GONE);

        mCloseBtn.setOnClickListener(v -> getActivity().finish());
        mCancelBtn.setOnClickListener(v -> toTrackActivity());

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
                    if(mRemarks.getVisibility() == View.GONE)
                        mRemarks.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private void toTrackActivity(){
        startActivity(new Intent(getActivity(), TrackerActivity.class));
        getActivity().finish();
    }
}
