package project.bsts.semut.map.osm;


import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import project.bsts.semut.R;
import project.bsts.semut.pojo.mapview.MyLocation;
import project.bsts.semut.pojo.mapview.Tracker;
import project.bsts.semut.pojo.mapview.TranspostMap;
import project.bsts.semut.utilities.CustomDrawable;

public class OsmMarker {

    private MapView mapView;

    public OsmMarker(MapView mapView){
        this.mapView = mapView;
    }

    public Marker add(Object objectMap){
        Marker marker = null;
        if(objectMap instanceof Tracker){
            GeoPoint point = new GeoPoint(((Tracker) objectMap).getData().get(0), ((Tracker) objectMap).getData().get(1));
            marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setIcon(mapView.getContext().getResources().getDrawable(R.drawable.angkot_icon));
            marker.setRelatedObject(objectMap);
            mapView.getOverlays().add(marker);
        }else if(objectMap instanceof MyLocation){
            GeoPoint point = new GeoPoint(((MyLocation) objectMap).getMyLatitude(), ((MyLocation) objectMap).getMyLongitude());
            marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setIcon(CustomDrawable.create(mapView.getContext(), GoogleMaterial.Icon.gmd_navigation, 24, R.color.primary_dark));
            marker.setRelatedObject(objectMap);
            mapView.getOverlays().add(marker);
        }else if(objectMap instanceof TranspostMap){
            GeoPoint point = new GeoPoint(((TranspostMap) objectMap).getLatitude(), ((TranspostMap) objectMap).getLongitude());
            marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setIcon(mapView.getContext().getResources().getDrawable(R.drawable.tranpost_icon));
            marker.setRelatedObject(objectMap);
            mapView.getOverlays().add(marker);
        }

        return marker;
    }
}
