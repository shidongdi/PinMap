/*
Copyright 2014 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.robert.pin.subscaleview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.robert.pin.R;
import com.robert.pin.bean.Coordinate;
import com.robert.pin.bean.ScenicBean;
import com.robert.pin.util.CalUtils;

import java.util.List;


/**
 * 带位置标记视图
 */
public class PinView extends SubsamplingScaleImageView {

    private List<ScenicBean> points;
    public Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;
    View viewMark;
    //地图左上坐标
    private Coordinate lt;
    //地图右下坐标
    private Coordinate rb;
    double wGeoScale, hGeoScale;
    /**
     * 是否显示用户坐标点
     */
    boolean isShowUserMark;
    //当前定位到的纬度
    double currentLat = 0;
    //当前定位到的经度
    double currentLng = 0;

    public PinView(Context context) {
        this(context, null);
        this.mContext = context;
    }

    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        this.mContext = context;
    }

    public void init(Coordinate lt, Coordinate rb) {
        if (null == lt || null == rb)
            return;
        this.lt = lt;
        this.rb = rb;
        // 横向坐标差值(右下lng-左上lng)
        double hGeoLngDistance = Math.abs(CalUtils.sub(rb.getLongitude(), lt.getLongitude()));
        // 竖向坐标差值(右下lat-左上lat)
        double vGeoLatDistance = Math.abs(CalUtils.sub(rb.getLatitude(), lt.getLatitude()));
        // 计算坐标与图片宽高比例
        wGeoScale = CalUtils.div(Double.valueOf(getSWidth()),
                hGeoLngDistance);
        hGeoScale = CalUtils.div(Double.valueOf(getSHeight()),
                vGeoLatDistance);
    }

    /**
     * 定位到位置后更新视图
     */
    public void onLocation() {
        if (!isReady()) {
            return;
        }
        //设置当前经纬度
        currentLat = 0;
        currentLng = 0;
        PointF pointF = getViewCoordByGPSCoord(currentLat, currentLng);
        PointF pointTopLeft = getViewCoordByGPSCoord(lt.getLongitude(), lt.getLatitude());
        PointF pointButtomRight = getViewCoordByGPSCoord(rb.getLongitude(), rb.getLatitude());
        if (pointF.x > pointTopLeft.x && pointF.x < pointButtomRight.x && pointF.y > pointTopLeft.y && pointF.y < pointButtomRight.y) {
            //当前定位坐标在图片坐标内
            isShowUserMark = true;
            //以当前位置为中心点移动视图
            animateCenter(viewToSourceCoord(pointF)).start();
        } else {
            hideUserMark();
        }
    }

    public void hideUserMark() {
        if (isShowUserMark) {
            isShowUserMark = false;
            invalidate();
        }
    }

    /**
     * 设置讲解点
     *
     * @param points
     */
    public void setPoints(List<ScenicBean> points) {
        this.points = points;
        // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        viewMark = LayoutInflater.from(mContext).inflate(R.layout.view_scenic_marker, null);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReady()) {
            return;
        }
        if (null != points) {
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            for (int i = 0; i < points.size(); i++) {
                ScenicBean missionaryPoint = points.get(i);
                PointF viewCoord = getViewCoordByGPSCoord(missionaryPoint.getLatitude(), missionaryPoint.getLongitude());
                if (viewCoord.x > getLeft() && viewCoord.x < getRight() && viewCoord.y > getTop() && viewCoord.y < getBottom()) {
                    //坐标在视图内的，绘制讲解点
                    Bitmap pinBitmap = mMemoryCache.get(missionaryPoint.getScenic_name());
                    if (null == pinBitmap) {
                        viewMark.destroyDrawingCache();
                        TextView tvMark = (TextView) viewMark.findViewById(R.id.tvMark);
                        tvMark.setBackgroundResource(R.mipmap.ic_point_yellow);
                        ((TextView) viewMark.findViewById(R.id.tvMarkName)).setText(missionaryPoint.getScenic_name());
                        viewMark.setDrawingCacheEnabled(true);
                        // 显示序号
                        if (missionaryPoint.getSeq() != 0) {
                            tvMark.setText(missionaryPoint.getSeq() + "");
                        }
                        viewMark.measure(MeasureSpec.makeMeasureSpec(0,
                                MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
                                MeasureSpec.UNSPECIFIED));
                        viewMark.layout(0, 0, viewMark.getMeasuredWidth(),
                                viewMark.getMeasuredHeight());
                        pinBitmap = Bitmap.createBitmap(viewMark.getDrawingCache());
                        viewMark.setDrawingCacheEnabled(false);
                        mMemoryCache.put(missionaryPoint.getScenic_name(), pinBitmap);
                    }
                    canvas.drawBitmap(pinBitmap, viewCoord.x - CalUtils.dp2px(mContext, 13) / 2, viewCoord.y - pinBitmap.getHeight() / 2, paint);
                }
            }
            //显示人物坐标
            if (isShowUserMark) {
                Bitmap pinBitmap = mMemoryCache.get("userMark");
                if (null == pinBitmap) {
                    pinBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_point_green);
                    mMemoryCache.put("userMark", pinBitmap);
                }
                PointF pointF = getViewCoordByGPSCoord(currentLat, currentLng);
                canvas.drawBitmap(pinBitmap, pointF.x - pinBitmap.getWidth() / 2, pointF.y - pinBitmap.getHeight() / 2, paint);
            }
        }
    }

    /**
     * 根据GPS坐标获取坐标在视图的实际坐标
     *
     * @param lat
     * @param lng
     * @return
     */
    public PointF getViewCoordByGPSCoord(double lat, double lng) {
        double geoX = CalUtils.sub(
                Double.valueOf(lng), lt.getLongitude()); // 点与左上角点的GPS坐标lng差值
        double geoY = CalUtils.sub(
                Double.valueOf(lat), lt.getLatitude()); // 点与左上角点的GPS坐标lat差值
        double photoX = CalUtils.format(CalUtils.mul(Math.abs(geoX),
                wGeoScale)); //点的source横向坐标
        double photoY = CalUtils.format(CalUtils.mul(Math.abs(geoY),
                hGeoScale)); //点的source纵向坐标
        return sourceToViewCoord((int) photoX, (int) photoY);
    }

    /**
     * 获取讲解点的宽度
     *
     * @param scenicId
     * @return
     */
    public int getPinWidth(int scenicId) {
        Bitmap pinBitmap = mMemoryCache.get(scenicId + "_" + 0);
        if (null != pinBitmap) {
            return pinBitmap.getWidth();
        }
        return 88;
    }

    /**
     * 获取讲解点高度
     *
     * @param scenicId
     * @return
     */
    public int getPinHeight(int scenicId) {
        Bitmap pinBitmap = mMemoryCache.get(scenicId + "_" + 0);
        if (null != pinBitmap) {
            return pinBitmap.getHeight();
        }
        return 30;
    }

    public double[] getCenterGPS() {
        return new double[]{lt.getLatitude() + (rb.getLatitude() - lt.getLatitude()) / 2, lt.getLongitude() + (rb.getLongitude() - lt.getLongitude()) / 2};
    }

    public double getLocationRadio() {
        //获取触发距离
        return CalUtils.getDistance(lt.getLatitude(), lt.getLongitude(), getCenterGPS()[1], getCenterGPS()[0]) + 500;
    }

}
