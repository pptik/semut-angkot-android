package project.bsts.semut.map.osm;


import android.content.Context;
import android.view.View;
import android.view.animation.Animation;

import org.osmdroid.views.overlay.Marker;

import project.bsts.semut.R;
import project.bsts.semut.fragments.map.AngkotReportFragment;
import project.bsts.semut.fragments.map.MapAngkotFragment;
import project.bsts.semut.fragments.map.MapTrackerFragment;
import project.bsts.semut.pojo.angkot.Angkot;
import project.bsts.semut.pojo.angkot.AngkotPost;
import project.bsts.semut.pojo.mapview.Tracker;
import project.bsts.semut.ui.AnimationView;
import project.bsts.semut.utilities.FragmentTransUtility;

public class MarkerClick {
    private Context context;
    private View frameView;
    private FragmentTransUtility fragmentTransUtility;
    private Animation fromRight;
    private AnimationView animationView;

    public MarkerClick(Context context, View frameView){
        this.context = context;
        this.frameView = frameView;
        fragmentTransUtility = new FragmentTransUtility(context);
        animationView = new AnimationView(context);
        fromRight = animationView.getAnimation(R.anim.slide_up, null);
    }

    public void checkMarker(Marker marker){
        if(marker.getRelatedObject() instanceof Tracker){
            MapTrackerFragment mapTrackerFragment = new MapTrackerFragment();
            mapTrackerFragment.setData((Tracker) marker.getRelatedObject());
            fragmentTransUtility.setTrackerMapFragment(mapTrackerFragment, frameView.getId());
            frameView.setVisibility(View.VISIBLE);
            frameView.startAnimation(fromRight);
        }else if(marker.getRelatedObject() instanceof Angkot){
            MapAngkotFragment mapAngkotFragment = new MapAngkotFragment();
            mapAngkotFragment.setData((Angkot) marker.getRelatedObject());
            fragmentTransUtility.setAngkotMapFragment(mapAngkotFragment, frameView.getId());
            frameView.setVisibility(View.VISIBLE);
            frameView.startAnimation(fromRight);
        }else if(marker.getRelatedObject() instanceof AngkotPost){
            AngkotReportFragment mapAngkotFragment = new AngkotReportFragment();
            mapAngkotFragment.setData((AngkotPost) marker.getRelatedObject());
            fragmentTransUtility.setAngkotReportFragment(mapAngkotFragment, frameView.getId());
            frameView.setVisibility(View.VISIBLE);
            frameView.startAnimation(fromRight);
        }
    }
}
