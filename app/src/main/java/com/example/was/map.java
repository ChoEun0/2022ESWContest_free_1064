package com.example.was;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.List;

public class map extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap; 
    private MapView mapView;

    private static final String TAG = "map";
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    List<LatLng> lstLatLng = new ArrayList<>();

    private Marker marker1 = new Marker();
    private Marker marker2 = new Marker();
    private Marker marker3 = new Marker();
    private Marker marker4 = new Marker();
    private Marker marker5 = new Marker();
    private Marker marker6 = new Marker();
    private Marker marker7 = new Marker();
    private Marker marker8 = new Marker();

    long delay = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        ImageButton home = (ImageButton)findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast.makeText(getApplicationContext(),"홈으로 이동", Toast.LENGTH_SHORT).show();
                Intent intent_home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent_home);
            }
        });




        Button btnMark1 = (Button) findViewById(R.id.btnmark1);
        Button btnMark2 = (Button) findViewById(R.id.btnmark2);
        Button btnMark3 = (Button) findViewById(R.id.btnmark3);

        btnMark1.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(System.currentTimeMillis() > delay){
                    setMarker(marker1, 37.455336, 127.00129, R.drawable.ic_red_place_24, 0);
                    setMarker(marker4, 37.449309, 126.999768, R.drawable.ic_red_place_24, 0);
                    marker1.setCaptionText("선바위 경로당");
                    marker4.setCaptionText("한내 경로당");



                    delay = System.currentTimeMillis() + 2000;
                    return;
                }
                if(System.currentTimeMillis() <= delay){
                    marker1.setMap(null);
                    marker4.setMap(null);

                }
                marker1.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        Toast.makeText(getApplication(), "마커 1 클릭", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        btnMark2.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(System.currentTimeMillis() > delay) {
                    setMarker(marker2, 37.442077, 126.996254, R.drawable.ic_green_place_24, 0);
                    setMarker(marker5, 37.461338, 127.015792, R.drawable.ic_green_place_24, 0);
                    marker2.setCaptionText("관문체육공원");
                    marker5.setCaptionText("송동 어린이공원");
                    delay = System.currentTimeMillis() + 2000;
                    return;
                }
                if(System.currentTimeMillis() <= delay){
                    marker2.setMap(null);
                    marker5.setMap(null);
                    return;
                }
                marker1.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        Toast.makeText(getApplication(), "마커 2 클릭", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        btnMark3.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                if(System.currentTimeMillis() > delay) {
                    setMarker(marker3, 37.440544, 126.995286, R.drawable.ic_blue_place_24, 0);
                    setMarker(marker6, 37.452067, 127.002001, R.drawable.ic_blue_place_24, 0);
                    marker3.setCaptionText("관문체육공원 화장실");
                    marker6.setCaptionText("선바위역 화장실");
                    delay = System.currentTimeMillis() + 2000 ;
                    return;
                }
                if(System.currentTimeMillis() <= delay){
                    marker3.setMap(null);
                    marker6.setMap(null);
                    return;
                }
                marker1.setOnClickListener(new Overlay.OnClickListener() {
                    @Override
                    public boolean onClick(@NonNull Overlay overlay) {
                        Toast.makeText(getApplication(), "마커 3 클릭", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });
        mapView = (MapView) findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void setSupportActionBar(Toolbar toolbar) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
                return;
            }else{
                naverMap.setLocationTrackingMode(LocationTrackingMode.Face);
            }

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        ActivityCompat.requestPermissions(this, PERMISSIONS, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void setMarker(Marker marker, double lat, double lng, int resourceID, int zIndex){
        //원근감 표시
        marker.setIconPerspectiveEnabled(true);
        //아이콘 지정
        marker.setIcon(OverlayImage.fromResource(resourceID));
        //마커의 투명도
        marker.setAlpha(0.8f);
        //마커 위치
        marker.setPosition(new LatLng(lat, lng));
        //마커 우선순위
        marker.setZIndex(zIndex);
        //마커 표시
        marker.setMap(naverMap);
        marker.setCaptionTextSize(16);
    }


}