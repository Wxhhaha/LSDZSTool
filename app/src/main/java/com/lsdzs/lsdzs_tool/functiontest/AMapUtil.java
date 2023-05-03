package com.lsdzs.lsdzs_tool.functiontest;

import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.List;


public class AMapUtil {
    /**
     * 重新设置地图中心点
     *
     * @param map
     * @param latLng
     * @param zoom
     */
    public static void setCenterZoomByLatLng(AMap map, LatLng latLng, float zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * 重新设置地图中心点
     *
     * @param map
     * @param latLng
     * @param zoom
     */
    public static void setCenterByLatLng(AMap map, LatLng latLng) {
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    /**
     * 向添加标记点
     *
     * @param map
     * @param latLng
     * @param drawableId
     */
    public static Marker addMarker(AMap map, LatLng latLng, int drawableId) {
        return map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(drawableId)).draggable(false));
    }

    /**
     * 向添加标记点
     *
     * @param map
     * @param latLng
     */
    public static Marker addMarker(AMap map, LatLng latLng, View view) {
        return map.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromView(view)).draggable(false));
    }

    public static void changeMarkerIcon(View view,Marker marker){
        marker.setIcon(BitmapDescriptorFactory.fromView(view));
    }

    /**
     * 清空地图上的组件
     *
     * @param map
     */
    public static void clearMap(AMap map) {
        if (map != null) {
            map.clear();
        }
    }

    /**
     * 在地图上画圆
     *
     * @param map
     * @param latLng
     * @param radius 单位 米
     * @return
     */
    public static Circle addLocationCircle(AMap map, LatLng latLng, double radius) {
        CircleOptions circleOptions =
                new CircleOptions().center(latLng).radius(radius).fillColor(0x205D9CEC).strokeColor(0xff5D9CEC).strokeWidth(2);
        return map.addCircle(circleOptions);
    }

    /**
     * 添加多边形
     * @param map
     * @param points
     * @return
     */
    public static Polygon addPolygon(AMap map, List<LatLng> points) {
        PolygonOptions options = new PolygonOptions();
        options.addAll(points);
//        if (holes != null) {
//            for (List<LatLng> hole : holes) {
//                options.addHole(hole);
//            }
//        }
        return map.addPolygon(options);
    }

    /**
     * 画线
     * @param map
     * @param points
     * @return
     */
    public static Polyline addPolyline(AMap map, List<LatLng> points){
        PolylineOptions options = new PolylineOptions();
        options.addAll(points);
        return map.addPolyline(options);
    }
}
