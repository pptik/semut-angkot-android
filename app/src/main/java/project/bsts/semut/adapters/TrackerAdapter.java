package project.bsts.semut.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import project.bsts.semut.R;
import project.bsts.semut.pojo.angkot.Angkot;
import project.bsts.semut.pojo.mapview.Tracker;
import project.bsts.semut.utilities.CompareDate;

public class TrackerAdapter extends BaseAdapter {

    private Context mContext = null;
    private Angkot[] angkots;
    private LayoutInflater mInflater = null;
    private TextView gpsNameText, gpsLocText, gpsDetail;
    private RadioButton stateRadio;
    private int checkedState;
    private View sortView;

    public interface MarkerPositionListener{
        public void onMarkerSelected(int position);
    }


    MarkerPositionListener listener;
    public TrackerAdapter(Context mContext, Angkot[] angkots, int checkedState, View sortView, MarkerPositionListener listener){
        this.mContext = mContext;
        this.angkots = angkots;
        this.mInflater = (LayoutInflater)this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = listener;
        this.checkedState = checkedState;
        this.sortView = sortView;
    }



    @Override
    public int getCount() {
        if (angkots != null) {
            return angkots.length;
        }
        else {
            return 0;
        }
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "InflateParams", "SimpleDateFormat"})
    @Override
    public View getView(final int i, View view, final ViewGroup viewGroup) {

      //  View identityView = getView(i, view, viewGroup);
      //  identityView.setTag(i);

        view = mInflater.inflate(R.layout.layout_list_tracker_filter, null);
        gpsNameText = (TextView)view.findViewById(R.id.gps_name);
        stateRadio = (RadioButton)view.findViewById(R.id.state);
        gpsLocText = (TextView)view.findViewById(R.id.gps_location);
        gpsDetail = (TextView)view.findViewById(R.id.gps_detail);

        boolean state = (i == checkedState);
        String detail = "detail";

       /* SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date(System.currentTimeMillis() - 3600 * 10);
        String dateNow = df.format(date);
        String dateToCompare = angkots[i].getDate()+" "+ angkots[i].getTime();
        boolean isExpired = CompareDate.compare(dateToCompare, dateNow);
        if(isExpired) detail += " <br> <b><font color='red'>LOKASI TIDAK UPDATE</font></b>";
        else detail += " <br> <b><font color='blue'>LOKASI UPDATE</font></b>"; */

        gpsNameText.setText(angkots[i].getName());
        gpsLocText.setText(angkots[i].getAngkot().getJumlahPenumpang().toString());
        gpsDetail.setText(Html.fromHtml(detail), TextView.BufferType.SPANNABLE);
        stateRadio.setChecked(state);

        view.setOnClickListener(view1 -> {
            listener.onMarkerSelected(i);
            sortView.setVisibility(View.GONE);

        });

    //    stateRadio.setOnCheckedChangeListener((compoundButton, b) -> {
    //        listener.onMarkerSelected(i);
    //        sortView.setVisibility(View.GONE);
    //    });

        stateRadio.setOnClickListener(view1 -> {
            listener.onMarkerSelected(i);
            sortView.setVisibility(View.GONE);

        });

        view.setTag(i);
        return view;
    }



}