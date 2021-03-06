package project.bsts.semut;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import net.qiujuer.genius.res.Resource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import project.bsts.semut.adapters.TrackerAdapter;
import project.bsts.semut.connections.broker.BrokerCallback;
import project.bsts.semut.connections.broker.Config;
import project.bsts.semut.connections.broker.Consumer;
import project.bsts.semut.connections.broker.Factory;
import project.bsts.semut.helper.BroadcastManager;
import project.bsts.semut.helper.PermissionHelper;
import project.bsts.semut.helper.PreferencesManager;
import project.bsts.semut.map.MarkerBearing;
import project.bsts.semut.map.osm.MarkerClick;
import project.bsts.semut.map.osm.OSMarkerAnimation;
import project.bsts.semut.map.osm.OsmMarker;
import project.bsts.semut.pojo.angkot.Angkot;
import project.bsts.semut.pojo.angkot.AngkotPost;
import project.bsts.semut.pojo.mapview.MyLocation;
import project.bsts.semut.services.LocationService;
import project.bsts.semut.setup.Constants;
import project.bsts.semut.ui.AnimationView;
import project.bsts.semut.ui.CommonAlerts;
import project.bsts.semut.utilities.CheckService;
import project.bsts.semut.utilities.CustomDrawable;

public class TrackerActivity extends AppCompatActivity implements BrokerCallback, TrackerAdapter.MarkerPositionListener, Marker.OnMarkerClickListener, BroadcastManager.UIBroadcastListener {


    @BindView(R.id.maposm)
    MapView mapset;
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.sortLayout)
    RelativeLayout sortLayout;
    @BindView(R.id.sort_fab)
    FloatingActionButton sortFab;
    @BindView(R.id.markerdetail_layout)
    RelativeLayout markerDetailLayout;
    @BindView(R.id.myLocRadio)
    RadioButton mRadioMyLocation;
    @BindView(R.id.add_post)
    Button mAddPost;
    @BindView(R.id.more)
    ImageView mMore;


    private Switch mSwitchTrack;

    private Factory mqFactory;
    private Consumer mqConsumer;
    private final String TAG = this.getClass().getSimpleName();
    private Context context;
    private boolean isConnected = true, isMessageReceived = false;
    private ProgressDialog mProgressDialog;
    private IMapController mapController;
    private Marker[] markers;
    private Angkot[] angkots;
    private boolean isFirsInit = true, isTracked = true;
    private TrackerAdapter adapter;
    private int checkedState = -1;
    private MarkerClick markerClick;
    private Animation slideDown;
    private AnimationView animationView;
    private final static int FAB_STATE_OPEN = 1;
    private final static int FAB_STATE_CLOSE = 0;
    private int fabState = FAB_STATE_CLOSE;
    private AngkotPost[] angkotPosts;
    private String ROUTING_KEY;
    private Intent locService;
    private BroadcastManager broadcastManager;
    private Marker markerMyLocation;
    private OsmMarker osmMarker;
    private OSMarkerAnimation markerAnimation;
    private TSnackbar snackbar;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracker);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwitchTrack = (Switch)findViewById(R.id.switch_track);
        toolbar.setTitleTextColor(getResources().getColor(R.color.lynchLight));
        ROUTING_KEY = Constants.MQ_BROADCAST_ROUTING_KEY;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
      /*  getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(CustomDrawable.create(
                this, GoogleMaterial.Icon.gmd_more_horiz, 24, R.color.white)); */
        ButterKnife.bind(this);
        mMore.setImageDrawable(CustomDrawable.create(
                this, GoogleMaterial.Icon.gmd_more_horiz, 24, R.color.white));
        mMore.setOnClickListener(v -> showPopup(v));

        setSnackbar();

        mRadioMyLocation.setOnCheckedChangeListener((compoundButton, b) -> {
            if(b) {
                checkedState = -1;
                animateToSelected();
                setListView();
                doFab();
            }
        });


        context = this;
        mAddPost.setCompoundDrawables(
                CustomDrawable.create(context, GoogleMaterial.Icon.gmd_add, 24, R.color.primary_light), null, null, null
        );
        mAddPost.setOnClickListener(v -> startActivity(new Intent(context, TagsActivity.class)));

        mSwitchTrack.setChecked(true);
        mSwitchTrack.setOnCheckedChangeListener((compoundButton, b) -> isTracked = b);

        PermissionHelper permissionHelper = new PermissionHelper(context);
        broadcastManager = new BroadcastManager(context);
        broadcastManager.subscribeToUi(this);


        locService = new Intent(context, LocationService.class);
        locService.putExtra(Constants.INTENT_LOCATION_WITH_STORING, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(locService);
        }else {
            startService(locService);
        }


        mProgressDialog = new ProgressDialog(context);
        markerClick = new MarkerClick(context, markerDetailLayout);
        mProgressDialog.setMessage("Memuat...");
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        connectToRabbit();
        mapset.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapset.setMultiTouchControls(true);
        mapset.setMaxZoomLevel(19);
        mapController = mapset.getController();
        osmMarker = new OsmMarker(mapset);
        mapController.setZoom(17);
        sortFab.setImageDrawable(CustomDrawable.create(context, GoogleMaterial.Icon.gmd_sort, 24, R.color.primary_light));
        sortFab.setOnClickListener(view -> doFab());
        markerAnimation = new OSMarkerAnimation();
        animationView = new AnimationView(context);
        slideDown = animationView.getAnimation(R.anim.slide_down, anim -> {
            if(markerDetailLayout.getVisibility() == View.VISIBLE) markerDetailLayout.setVisibility(View.GONE);
        });
       // markerDetailLayout.setOnClickListener(v-> markerDetailLayout.startAnimation(slideDown));
    }

    private void setSnackbar() {
        snackbar = TSnackbar.make(findViewById(android.R.id.content), "Gagal memuat lokasi perangkat Anda " +
                "", TSnackbar.LENGTH_INDEFINITE);
        snackbar.setActionTextColor(Resource.Color.WHITE);
       // snackbar.setIconLeft(R.drawable.cast_ic_expanded_controller_closed_caption, 24);
        snackbar.setAction("Close", view -> {
            snackbar.getView().setVisibility(View.GONE);
            snackbar.dismiss();
        });
        View snackbarView = snackbar.getView();
        snackbarView.setVisibility(View.GONE);
        snackbarView.setBackgroundColor(getResources().getColor(R.color.primary_dark));
        TextView textView = snackbarView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);

        //snackbar.show();
    }

    private void doFab(){
        if(markerDetailLayout.getVisibility() == View.VISIBLE) fabState = FAB_STATE_OPEN;
        if(fabState == FAB_STATE_CLOSE){
            if(sortLayout.getVisibility() == View.GONE) sortLayout.setVisibility(View.VISIBLE);
            fabState = FAB_STATE_OPEN;
            sortFab.setImageDrawable(CustomDrawable.create(context, GoogleMaterial.Icon.gmd_close, 24, R.color.primary_light));
            mAddPost.setVisibility(View.GONE);
         //   setListView();
        }else {
            if(sortLayout.getVisibility() == View.VISIBLE) sortLayout.setVisibility(View.GONE);
            if(markerDetailLayout.getVisibility() == View.VISIBLE) markerDetailLayout.startAnimation(slideDown);
            fabState = FAB_STATE_CLOSE;
            sortFab.setImageDrawable(CustomDrawable.create(context, GoogleMaterial.Icon.gmd_sort, 24, R.color.primary_light));
            mAddPost.setVisibility(View.VISIBLE);
        }
    }


    private void setListView() {

        adapter = new TrackerAdapter(context, angkots, checkedState, sortLayout, this);
        listView.setAdapter(adapter);
    }

    private void connectToRabbit() {
        mqFactory = new Factory(Config.hostName, Config.virtualHostname, Config.username, Config.password, Config.exchange, Config.rotuingkey, Config.port);
        mqConsumer = this.mqFactory.createConsumer(this);
        consume();
    }

    private void consume(){

        mqConsumer.setQueueName("");
        mqConsumer.setExchange(Constants.MQ_EXCHANGE_NAME_ANGKOT);
        mqConsumer.setRoutingkey(ROUTING_KEY);
        mqConsumer.subsribe();
        mqConsumer.setMessageListner(delivery -> {
            try {
                final String message = new String(delivery.getBody(), "UTF-8");
                Log.i(TAG, "-------------------------------------");
                Log.i(TAG, "incoming message");
                Log.i(TAG, "-------------------------------------");
                Log.i(TAG, message);
                if(!isMessageReceived){
                    isMessageReceived = true;
                    mProgressDialog.dismiss();
                }
               // getMessage(message);
                populateMsg(message);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        });

    }
    private void populateMsg(String msg){
        try {
            JSONObject mainObject = new JSONObject(msg);
            JSONArray angkotArray = mainObject.getJSONArray("angkot");
            JSONArray postArray = mainObject.getJSONArray("laporan");
            // angkot
            if(isFirsInit){
                isFirsInit = false;
                angkots = new Angkot[angkotArray.length()];
                markers = new Marker[angkotArray.length()];
                for (int i = 0; i < angkotArray.length(); i++) {
                    angkots[i] = new Gson().fromJson(angkotArray.get(i).toString(), Angkot.class);
                    markers[i] = new Marker(mapset);
                    markers[i].setPosition(new GeoPoint(angkots[i].getAngkot().getLocation().getCoordinates().get(1),
                            angkots[i].getAngkot().getLocation().getCoordinates().get(0)));
                    markers[i].setIcon(getResources().getDrawable(R.drawable.tracker_angkot));
                    markers[i].setRelatedObject(angkots[i]);
                    markers[i].setOnMarkerClickListener(this);
                    mapset.getOverlays().add(markers[i]);
                    mapset.invalidate();
                }
                setListView();
                animateToSelected();
            }else {
                if (angkotArray.length() == angkots.length) {
                    for (int i = 0; i < angkotArray.length(); i++) {
                        JSONObject entity = null;
                        try {
                            entity = angkotArray.getJSONObject(i);
                            Angkot angkot = new Gson().fromJson(entity.toString(), Angkot.class);
                            if (angkots[i].getAngkot().getPlatNomor().equals(angkot.getAngkot().getPlatNomor())) { // update markers
                                angkots[i] = new Gson().fromJson(entity.toString(), Angkot.class);
                                if (markers[i].getPosition().getLatitude() != angkots[i].getAngkot().getLocation().getCoordinates().get(1) ||
                                        markers[i].getPosition().getLongitude() != angkots[i].getAngkot().getLocation().getCoordinates().get(0)) {
                                    double bearing = MarkerBearing.bearing(markers[i].getPosition().getLatitude(), markers[i].getPosition().getLongitude(),
                                            angkots[i].getAngkot().getLocation().getCoordinates().get(1), angkots[i].getAngkot().getLocation().getCoordinates().get(0));
                                    markers[i].setRelatedObject(angkots[i]);
                                    markers[i].setRotation((float) bearing);
                                    markerAnimation.animate(mapset, markers[i],
                                            new GeoPoint(angkots[i].getAngkot().getLocation().getCoordinates().get(1), angkots[i].getAngkot().getLocation().getCoordinates().get(0)),
                                            1500);
                                    if (checkedState != -1) mapController.setZoom(17);
                                } else {
                                    // same position
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if(listView.getVisibility() == View.GONE) setListView();
                    if(isTracked) animateToSelected();
                    // post
                    angkotPosts = new AngkotPost[postArray.length()];
                    for(int i = 0; i < postArray.length(); i++){
                        angkotPosts[i] = new Gson().fromJson(postArray.get(i).toString(), AngkotPost.class);
                    }
                    for(Overlay overlay : mapset.getOverlays()){
                        if(overlay instanceof Marker){
                            if(((Marker) overlay).getRelatedObject() instanceof  AngkotPost){
                                mapset.getOverlays().remove(overlay);
                                mapset.invalidate();
                            }
                        }
                    }
                    // add markers
                    for(int i = 0; i < angkotPosts.length; i++){
                        Marker marker = new Marker(mapset);
                        marker.setPosition(new GeoPoint(angkotPosts[i].getLocation().getCoordinates().get(1),
                                angkotPosts[i].getLocation().getCoordinates().get(0)));
                        marker.setIcon(getResources().getDrawable(R.drawable.angkot_icon));
                        marker.setRelatedObject(angkotPosts[i]);
                        marker.setOnMarkerClickListener(this);
                        mapset.getOverlays().add(marker);
                        mapset.invalidate();
                    }
                }else {
                    // found new data
                    isFirsInit = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void zoomToNullLocation(){
        // zoom to bandung
        double _lat = -6.90389, _lon = 107.61861;
        GeoPoint _geo = new GeoPoint(_lat, _lon);
        mapController.setCenter(_geo);
        mapController.animateTo(_geo);
        mapController.setZoom(15);
        mapset.invalidate();
    }


    private void animateToSelected(){
        if(checkedState == -1) {
            if(markerMyLocation != null)
                mapController.animateTo(markerMyLocation.getPosition());
            else {
                if(snackbar.getView().getVisibility() != View.VISIBLE){
                    snackbar.getView().setVisibility(View.VISIBLE);
                    snackbar.show();
                    //zoomToNullLocation();
                }
            }
        }
        else mapController.animateTo(markers[checkedState].getPosition());
    }


    @Override
    public void onMQConnectionFailure(String message) {
        Log.i(TAG, message);
        if(isConnected){
            isConnected = false;
            if(mProgressDialog.isShowing()) mProgressDialog.dismiss();
            try {
                CommonAlerts.commonError(context, "Server tidak merespon atau koneksi internet Anda tidak stabil, coba beberapa saat lagi");
            }catch (IllegalStateException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onMQDisconnected() {

    }

    @Override
    public void onMQConnectionClosed(String message) {
        Log.i(TAG, message);
        if(isConnected){
            isConnected = false;
            if(mProgressDialog.isShowing()) mProgressDialog.dismiss();
         //   if(!isFirsInit)
            try {
                CommonAlerts.commonError(context, "Server tidak merespon atau koneksi internet Anda tidak stabil, coba beberapa saat lagi");
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMarkerSelected(int position) {
        Log.i(TAG, "pos "+position);
        checkedState = position;
        mRadioMyLocation.setChecked(false);
        animateToSelected();
        setListView();
        doFab();
        Log.i(TAG, "pos 2"+checkedState);


    }




    @Override
    public void onDestroy(){
        super.onDestroy();
        mqConsumer.stop();
        broadcastManager.unSubscribeToUi();
        if(CheckService.isGoogleLocationServiceRunning(context)){
            stopService(locService);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        markerClick.checkMarker(marker);
        sortFab.setImageDrawable(CustomDrawable.create(context, GoogleMaterial.Icon.gmd_close, 24, R.color.primary_light));
        mAddPost.setVisibility(View.GONE);
        return false;
    }

    private void showPopup(View v){
        PopupMenu popup = new PopupMenu(context, v);
        popup.inflate(R.menu.menu_main);
        popup.setOnMenuItemClickListener(item1 -> {
            switch (item1.getItemId()) {
                case R.id.logout:
                    PreferencesManager preferencesManager = new PreferencesManager(context);
                    preferencesManager.save(false, Constants.IS_LOGGED_IN);
                    preferencesManager.apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                  //  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ComponentName cn = intent.getComponent();
                    Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
                    context.startActivity(mainIntent);
                    finish();
                    break;
            }
            return false;
        });
        //displaying the popup
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMessageReceived(String type, String msg) {
        Log.i(TAG, "-------------------------------------");
        Log.i(TAG, "Receive on UI : Type : "+type);
        Log.i(TAG, msg);
        switch (type) {
            case Constants.BROADCAST_MY_LOCATION:
                MyLocation myLocationObject = new Gson().fromJson(msg, MyLocation.class);
                if (isFirsInit) {

                    markerMyLocation = osmMarker.add(myLocationObject);
                 //   if (isTracked) mapController.animateTo(markerMyLocation.getPosition());


                } else {
                    GeoPoint currPoint = new GeoPoint(myLocationObject.getMyLatitude(), myLocationObject.getMyLongitude());
                    if(markerMyLocation == null) markerMyLocation = osmMarker.add(myLocationObject);
                    markerMyLocation.setRotation((float) MarkerBearing.bearing(markerMyLocation.getPosition().getLatitude(),
                            markerMyLocation.getPosition().getLongitude(), currPoint.getLatitude(), currPoint.getLongitude()));
                  //  if (isTracked) mapController.animateTo(markerMyLocation.getPosition());
                    markerAnimation.animate(mapset, markerMyLocation, currPoint, 1500);


                    mapset.invalidate();
                }

                if(snackbar.getView().getVisibility() == View.VISIBLE){
                    snackbar.dismiss();
                    snackbar.getView().setVisibility(View.GONE);
                }

                break;
            case Constants.BROADCAST_MY_LOCATION_NULL:
                if(snackbar.getView().getVisibility() != View.VISIBLE){
                    snackbar.getView().setVisibility(View.VISIBLE);
                    snackbar.show();
                    zoomToNullLocation();
                }
                break;
        }
    }
}
