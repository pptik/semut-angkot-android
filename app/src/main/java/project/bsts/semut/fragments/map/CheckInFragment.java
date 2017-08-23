package project.bsts.semut.fragments.map;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import project.bsts.semut.R;
import project.bsts.semut.TrackerActivity;
import project.bsts.semut.connections.rest.RequestRest;
import project.bsts.semut.pojo.RequestStatus;
import project.bsts.semut.pojo.trayek.Trayek;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.CommonAlerts;


public class CheckInFragment extends Fragment {

    Button mCancelBtn, mYesBtn;
    ImageButton mCloseBtn;
    EditText mRemarks;
    Spinner mSpinnerTrayek;
    ProgressDialog mDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_penumpang, container, false);
        mCancelBtn = (Button)view.findViewById(R.id.cancel_btn);
        mYesBtn = (Button)view.findViewById(R.id.submitButton);
        mCloseBtn = (ImageButton)view.findViewById(R.id.closeButton);
        mRemarks = (EditText)view.findViewById(R.id.remarks);
        mSpinnerTrayek = (Spinner)view.findViewById(R.id.spinnerTrayek);
        mRemarks.setVisibility(View.GONE);

        mCloseBtn.setOnClickListener(v -> toTrackActivity());
        mCancelBtn.setOnClickListener(v -> toTrackActivity());

        mYesBtn.setOnClickListener(v -> {
            mYesBtn.setText("SUBMIT");
            mCancelBtn.setVisibility(View.INVISIBLE);
            mRemarks.setVisibility(View.VISIBLE);
        });

        mDialog = new ProgressDialog(getActivity());
        mDialog.setCancelable(false);
        mDialog.setMessage("Memuat Trayek ...");
        populateData();
        return view;
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
                        ArrayList<Trayek> trayek = new ArrayList<Trayek>();
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


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }else CommonAlerts.commonError(getActivity(), Constants.MESSAGE_HTTP_ERROR);
        }).getTrayek();
    }




    private void toTrackActivity(){
        startActivity(new Intent(getActivity(), TrackerActivity.class));
        getActivity().finish();
    }
}
