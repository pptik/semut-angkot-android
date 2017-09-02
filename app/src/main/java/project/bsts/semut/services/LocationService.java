package project.bsts.semut.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import project.bsts.semut.connections.broker.BrokerCallback;
import project.bsts.semut.connections.broker.Config;
import project.bsts.semut.connections.broker.Consumer;
import project.bsts.semut.connections.broker.Factory;
import project.bsts.semut.connections.broker.Producer;
import project.bsts.semut.helper.BroadcastManager;
import project.bsts.semut.helper.JSONRequest;
import project.bsts.semut.helper.PreferenceManager;
import project.bsts.semut.pojo.Profile;
import project.bsts.semut.pojo.Session;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.utilities.CheckService;
import project.bsts.semut.utilities.GetCurrentDate;
import project.bsts.semut.utilities.MapItem;
import project.bsts.semut.utilities.ScheduleTask;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, BrokerCallback {

    private double latitude, longitude, speed, altitude;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private String TAG = this.getClass().getSimpleName();
    private BroadcastManager broadcastManager;
    private JSONObject object;

    private PreferenceManager preferenceManager;
    Session session;
    Profile profile;

    public LocationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Start Command");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)
                .setFastestInterval(1 * 1000);
        if (mGoogleApiClient.isConnected() == false) {
            mGoogleApiClient.connect();
        }

        broadcastManager = new BroadcastManager(getApplicationContext());
        preferenceManager = new PreferenceManager(getApplicationContext());
        session = new Gson().fromJson(preferenceManager.getString(Constants.PREF_SESSION_ID), Session.class);
        profile = new Gson().fromJson(preferenceManager.getString(Constants.PREF_PROFILE), Profile.class);

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        if(CheckService.isGoogleLocationServiceRunning(getApplication())){
            stopService(new Intent(getApplication(), LocationService.class));
            Log.i(TAG, "Service Stopped");
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission Denied");
        }else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i(TAG, "LOCATION CONNECTED");
         //   Log.i(TAG, ""+location.getLatitude()+" - "+location.getLongitude());
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.i(TAG, "location null");
                broadCastMessage(Constants.BROADCAST_MY_LOCATION_NULL, "");
            } else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                speed = location.getSpeed();
                altitude = location.getAltitude();
                broadCastMessage(Constants.BROADCAST_MY_LOCATION, JSONRequest.myLocation(latitude, longitude));
                try {
                    object = new JSONObject();
                    object.put(Constants.ENTITY_LATITUDE, latitude);
                    object.put(Constants.ENTITY_LONGITUDE, longitude);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult((Activity)getBaseContext(), 100);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location Changed");
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        preferenceManager.save((float)latitude, Constants.ENTITY_LATITUDE);
        preferenceManager.save((float)longitude, Constants.ENTITY_LONGITUDE);
        preferenceManager.apply();
        broadCastMessage(Constants.BROADCAST_MY_LOCATION, JSONRequest.myLocation(latitude, longitude));

    }


    private void broadCastMessage(String type, String message){
        Log.i(TAG, type+" - "+message);
        switch (type){
            case Constants.BROADCAST_MY_LOCATION:
                broadcastManager.sendBroadcastToUI(type, message);
                break;
            case Constants.BROADCAST_MY_LOCATION_NULL:
                broadcastManager.sendBroadcastToUI(type, message);
        }
    }

    //---------- mq
    @Override
    public void onMQConnectionFailure(String message) {
        Log.i(TAG, message);
    }

    @Override
    public void onMQDisconnected() {
        Log.i(TAG, "Disconnected from mq");
    }

    @Override
    public void onMQConnectionClosed(String message) {
        Log.i(TAG, message);
    }
}
