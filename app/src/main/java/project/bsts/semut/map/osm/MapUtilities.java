package project.bsts.semut.map.osm;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import project.bsts.semut.map.MapViewComponent;
import project.bsts.semut.pojo.mapview.Tracker;
import project.bsts.semut.pojo.mapview.TranspostMap;
import project.bsts.semut.setup.Constants;

public class MapUtilities {

    MapView mapView;
    private IMapController mapController;

    OsmMarker osmMarker;

    private Marker[] userMarkers;

    public Marker[] getOtherMarkers() {
        return otherMarkers;
    }

    public Marker[] getUserMarkers() {
        return userMarkers;
    }

    public Marker[] getCctvMarkers() {
        return cctvMarkers;
    }

    public Marker[] getPoliceMarkers() {
        return policeMarkers;
    }

    public Marker[] getAccidentMarkers() {
        return accidentMarkers;
    }

    public Marker[] getTrafficMarkers() {
        return trafficMarkers;
    }

    public Marker[] getDisasterMarkers() {
        return disasterMarkers;
    }

    public Marker[] getClosureMarkers() {
        return closureMarkers;
    }

    private Marker[] cctvMarkers;
    private Marker[] policeMarkers;
    private Marker[] accidentMarkers;
    private Marker[] trafficMarkers;
    private Marker[] disasterMarkers;
    private Marker[] closureMarkers;
    private Marker[] otherMarkers;
    private Marker[] trackerMarkers;
    private Marker[] transpostMarkers;



    private void generateMarker(Object[] objects, Marker[] markers){
        for (int i = 0; i < objects.length; i ++){
            markers[i] = osmMarker.add(objects[i]);

            //markers[i].setIcon(descriptor);
        }
    }

    private Tracker[] trackers;
    private TranspostMap[] transpostMaps;

    public boolean isReady() {
        return isReady;
    }

    private boolean isReady = false;

    public GeoPoint getMyLocationGeo() {
        return myLocationGeo;
    }

    public void setMyLocationGeo(String jsonStr) {
        try {
            JSONObject object = new JSONObject(jsonStr);
            myLocationGeo = new GeoPoint(object.getDouble(Constants.ENTITY_LATITUDE), object.getDouble(Constants.ENTITY_LONGITUDE));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private GeoPoint myLocationGeo;

    public MapUtilities(MapView mapView){
        this.mapView = mapView;
        osmMarker = new OsmMarker(mapView);
    }

    public IMapController init(){
        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setMultiTouchControls(true);
        mapController = mapView.getController();
        mapController.setZoom(25);
        GeoPoint geoPoint = myLocationGeo;
        mapController.animateTo(geoPoint);
        isReady = true;
        return mapController;
    }


    public static BoundingBox computeArea(ArrayList<GeoPoint> points) {
        double nord = 0, sud = 0, ovest = 0, est = 0;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i) == null) continue;
            double lat = points.get(i).getLatitude();
            double lon = points.get(i).getLongitude();
            if ((i == 0) || (lat > nord)) nord = lat;
            if ((i == 0) || (lat < sud)) sud = lat;
            if ((i == 0) || (lon < ovest)) ovest = lon;
            if ((i == 0) || (lon > est)) est = lon;
        }

        return new BoundingBox(nord, est, sud, ovest);

    }

}
