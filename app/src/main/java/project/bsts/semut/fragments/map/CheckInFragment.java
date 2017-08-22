package project.bsts.semut.fragments.map;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import project.bsts.semut.R;
import project.bsts.semut.TrackerActivity;


public class CheckInFragment extends Fragment {

    Button mCancelBtn, mYesBtn;
    ImageButton mCloseBtn;
    EditText mRemarks;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_penumpang, container, false);
        mCancelBtn = (Button)view.findViewById(R.id.cancel_btn);
        mYesBtn = (Button)view.findViewById(R.id.submitButton);
        mCloseBtn = (ImageButton)view.findViewById(R.id.closeButton);
        mRemarks = (EditText)view.findViewById(R.id.remarks);

        mRemarks.setVisibility(View.GONE);

        mCloseBtn.setOnClickListener(v -> toTrackActivity());
        mCancelBtn.setOnClickListener(v -> toTrackActivity());

        mYesBtn.setOnClickListener(v -> {
            mYesBtn.setText("SUBMIT");
            mCancelBtn.setVisibility(View.INVISIBLE);
            mRemarks.setVisibility(View.VISIBLE);
        });
        return view;
    }


    private void toTrackActivity(){
        startActivity(new Intent(getActivity(), TrackerActivity.class));
        getActivity().finish();
    }
}
