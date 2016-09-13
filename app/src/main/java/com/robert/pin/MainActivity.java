package com.robert.pin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.robert.pin.bean.Coordinate;
import com.robert.pin.bean.ScenicBean;
import com.robert.pin.subscaleview.ImageSource;
import com.robert.pin.subscaleview.PinView;
import com.robert.pin.subscaleview.SubsamplingScaleImageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    PinView scenicSpotImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
    }

    private void initData() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    getAssets().open("scenics_data.json")));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        final List<ScenicBean> listScenics = new Gson().fromJson(stringBuilder.toString(), new TypeToken<List<ScenicBean>>() {
        }.getType());

        //地图左上角的坐标
        final Coordinate topLeft = new Coordinate(118.841529, 32.066003);
        //地图右下角的坐标
        final Coordinate bottomRight = new Coordinate(118.855075, 32.049417);

        scenicSpotImage = (PinView) findViewById(R.id.scenicSpotImage);
        scenicSpotImage.setMinimumScaleType(SubsamplingScaleImageView.SCALE_TYPE_CENTER_CROP);
        scenicSpotImage.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
                scenicSpotImage.init(topLeft, bottomRight);
                scenicSpotImage.setPoints(listScenics);
            }

            @Override
            public void onImageLoaded() {
            }

            @Override
            public void onPreviewLoadError(Exception e) {
                e.printStackTrace();
            }

            @Override
            public void onImageLoadError(Exception e) {
                finish();
            }

            @Override
            public void onTileLoadError(Exception e) {
                e.printStackTrace();
            }
        });
        scenicSpotImage.setImage(ImageSource.asset("scenic_map.png"));
    }
}
