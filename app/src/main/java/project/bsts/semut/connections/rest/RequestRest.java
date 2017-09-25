package project.bsts.semut.connections.rest;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.pojo.Profile;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.utilities.GetCurrentDate;

public class RequestRest extends ConnectionHandler {

    protected static AsyncHttpClient mClient = new AsyncHttpClient();
    private String TAG = this.getClass().getSimpleName();
    private PreferencesManager preferencesManager;


    public RequestRest(Context context, IConnectionResponseHandler handler) {
        this.mContext = context;
        this.responseHandler = handler;
        preferencesManager = new PreferencesManager(mContext);
    }


    @Override
    public String getAbsoluteUrl(String relativeUrl) {
        return Constants.REST_BASE_URL + relativeUrl;
    }


    public void login(String uniqueParam, String pass){
        RequestParams params = new RequestParams();
        params.put("entity", uniqueParam);
        params.put("password", pass);
        Log.i(TAG, params.toString());
        post(Constants.REST_USER_LOGIN, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_USER_LOGIN);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }


    public void register(String uniqueParam, String pass, String name, String username){
        RequestParams params = new RequestParams();
        params.put("entity", uniqueParam);
        params.put("password", pass);
        params.put("name", name);
        params.put("username", username);
        Log.i(TAG, params.toString());
        post(Constants.REST_USER_REGISTER, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_USER_REGISTER);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }

    public void insertLaporanAngkot(String detail){
        preferencesManager = new PreferencesManager(mContext);
        Profile profile = new Gson().fromJson(preferencesManager.getString(Constants.PREF_PROFILE), Profile.class);
        String session = profile.getSessionID();
        float latitude = preferencesManager.getFloat(Constants.ENTITY_LATITUDE, 0.0F);
        float longitude = preferencesManager.getFloat(Constants.ENTITY_LONGITUDE, 0.0F);
        RequestParams params = new RequestParams();
        params.put("detail", detail);
        params.put("session_id", session);
        params.put("tanggal", GetCurrentDate.now());
        params.put("latitude", latitude);
        params.put("longitude", longitude);
        Log.i(TAG, params.toString()+" -> "+Constants.REST_INSERT_ANGKOT_POST);
        post(Constants.REST_INSERT_ANGKOT_POST, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_INSERT_ANGKOT_POST);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }



    public void checkStatus(){
        preferencesManager = new PreferencesManager(mContext);
        Profile profile = new Gson().fromJson(preferencesManager.getString(Constants.PREF_PROFILE), Profile.class);
        String session = profile.getSessionID();
        RequestParams params = new RequestParams();
        params.put("session_id", session);
        Log.i(TAG, params.toString()+" -> "+Constants.REST_USER_STATUS);
        post(Constants.REST_USER_STATUS, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_USER_STATUS);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }


    public void updatePenumpang(String objectID){
        preferencesManager = new PreferencesManager(mContext);
        Profile profile = new Gson().fromJson(preferencesManager.getString(Constants.PREF_PROFILE), Profile.class);
        String session = profile.getSessionID();
        RequestParams params = new RequestParams();
        params.put("session_id", session);
        params.put("object_id", objectID);
        Log.i(TAG, params.toString()+" -> "+Constants.REST_UPDATE_PENUMPANG);
        post(Constants.REST_UPDATE_PENUMPANG, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_UPDATE_PENUMPANG);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }




    public void checkStatusPenumpang(String objectID){
        preferencesManager = new PreferencesManager(mContext);
        Profile profile = new Gson().fromJson(preferencesManager.getString(Constants.PREF_PROFILE), Profile.class);
        String session = profile.getSessionID();
        RequestParams params = new RequestParams();
        params.put("session_id", session);
        params.put("object_id", objectID);
        Log.i(TAG, params.toString()+" -> "+Constants.REST_CHECK_PENUMPANG);
        post(Constants.REST_CHECK_PENUMPANG, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_CHECK_PENUMPANG);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }





    public void getTrayek(){
        RequestParams params = new RequestParams();
        post(Constants.REST_GET_TRAYEK, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_GET_TRAYEK);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }



    public void insertPenumpang(int jumlah, int trayekID, int arahID){
        preferencesManager = new PreferencesManager(mContext);
        Profile profile = new Gson().fromJson(preferencesManager.getString(Constants.PREF_PROFILE), Profile.class);
        String session = profile.getSessionID();
        RequestParams params = new RequestParams();
        params.put("session_id", session);
        params.put("jumlah_penunggu", jumlah);
        params.put("trayek_id", trayekID);
        params.put("flag", arahID);
        post(Constants.REST_INSERT_PENUMPANG, params, new JsonHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                Log.i(TAG, "Sending request");
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Log.i(TAG, "Success");
                responseHandler.onSuccessRequest(response.toString(), Constants.REST_INSERT_PENUMPANG);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, e, errorResponse);
                Log.e(TAG, "Failed");
                responseHandler.onSuccessRequest(String.valueOf(statusCode), Constants.REST_ERROR);
            }

            @Override
            public void onFinish() {
                super.onFinish();
                Log.i(TAG, "Disconnected");
            }

        }, mClient);
    }



}
