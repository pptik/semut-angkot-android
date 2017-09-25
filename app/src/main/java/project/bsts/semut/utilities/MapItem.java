package project.bsts.semut.utilities;


import android.content.Context;

import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.setup.Constants;

public class MapItem {

    public static String get(Context context){
        PreferencesManager preferencesManager = new PreferencesManager(context);
        return String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_USER, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_CCTV,1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_POLICE_POST,1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_ACCIDENT_POST, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_TRAFFIC_POST, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_DISASTER_POST, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_CLOSURE_POST, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_OTHER_POST, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_COMMUTER_TRAIN, 1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_ANGKOT_LOCATION,1))+
                String.valueOf(preferencesManager.getInt(Constants.MAP_FILTER_TRANSPOST,1));
    }

}
