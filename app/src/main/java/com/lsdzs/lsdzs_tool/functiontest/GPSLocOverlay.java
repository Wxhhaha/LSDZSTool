package com.lsdzs.lsdzs_tool.functiontest;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.lsdzs.lsdzs_tool.R;

public class GPSLocOverlay {
    private static GPSLocOverlay mlocoverlay;
    private LatLng point;
//    private float radius;
    private Marker locMarker;
//    private Marker locBearingMarker;
//    private Circle locCircle;
    private AMap aMap;

    public GPSLocOverlay(AMap amap) {
        this.aMap = amap;
    }

    /**
     * 位置变化时调用这个方法，实现marker位置变化
     *
     * @param location 定位返回的位置类
     */
    public void locationChanged(LatLng location) {
        //LatLng location = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

        this.point = location;
      //  this.radius = aMapLocation.getAccuracy();
        if (locMarker == null) {
            AMapUtil.setCenterZoomByLatLng(aMap,point,16);
            addMarker();
        }
//        if (locBearingMarker == null) {
//            addBearingMarker();
//        }
//        if (locCircle == null) {
//            addCircle();
//        }
//        float bearing = aMapLocation.getBearing();
//        locBearingMarker.setRotateAngle(bearing);
        moveLocationMarker();
//        locCircle.setRadius(radius);
    }

    /**
     * 平滑移动动画
     */
    private void moveLocationMarker() {
        final LatLng startPoint = locMarker.getPosition();
        final LatLng endPoint = point;
        float rotate = getRotate(startPoint, endPoint);
//        locBearingMarker.setRotateAngle(360 - rotate + aMap.getCameraPosition().bearing);
        ValueAnimator anim = ValueAnimator.ofObject(new PointEvaluator(), startPoint, endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                LatLng target = (LatLng) valueAnimator.getAnimatedValue();
//                if (locCircle != null) {
//                    locCircle.setCenter(target);
//                }
                AMapUtil.setCenterByLatLng(aMap,target);
                if (locMarker != null) {
                    locMarker.setPosition(target);
                }
//                if (locBearingMarker != null) {
//                    locBearingMarker.setPosition(target);
//                }
            }
        });
        anim.setDuration(1000);
        anim.start();
    }

    /**
     * 添加定位marker
     */
    private void addMarker() {
//        locMarker = AMapUtil.addLocationMarker(aMap, point);
        locMarker = AMapUtil.addMarker(aMap, point, R.mipmap.amap_icon);
    }

    /**
     * 添加指向点marker
     */
//    private void addBearingMarker(){
//        locBearingMarker = AMapUtil.addLocationMarkerPoint(aMap,point);
//    }

    /**
     * 添加定位精度圈
     */
//    private void addCircle() {
//        locCircle = AMapUtil.addLocationCircle(aMap, point, radius);
//    }

    public void remove() {
        if (locMarker != null) {
            locMarker.remove();
            locMarker.destroy();
            locMarker = null;
        }
//        if (locBearingMarker != null) {
//            locBearingMarker.remove();
//            locBearingMarker.destroy();
//            locBearingMarker = null;
//        }
//        if (locCircle != null) {
//            locCircle.remove();
//            locCircle = null;
//        }
    }

    public class PointEvaluator implements TypeEvaluator {
        @Override
        public Object evaluate(float fraction, Object startValue, Object endValue) {
            LatLng startPoint = (LatLng) startValue;
            LatLng endPoint = (LatLng) endValue;
            double x = startPoint.latitude + fraction * (endPoint.latitude - startPoint.latitude);
            double y = startPoint.longitude + fraction * (endPoint.longitude - startPoint.longitude);
            LatLng point = new LatLng(x, y);
            return point;
        }
    }

    /**
     * 根据经纬度计算需要偏转的角度
     *
     * @param curPos
     * @param nextPos
     * @return
     */
    private float getRotate(LatLng curPos, LatLng nextPos) {
        if (curPos == null || nextPos == null) {
            return 0;
        }
        double x1 = curPos.latitude;
        double x2 = nextPos.latitude;
        double y1 = curPos.longitude;
        double y2 = nextPos.longitude;

        float rotate = (float) (Math.atan2(y2 - y1, x2 - x1) / Math.PI * 180);
        return rotate;
    }
}
