package com.example.shim.simpledeliverybuyer.Fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shim.simpledeliverybuyer.Model.ReverseGeoResponse;
import com.example.shim.simpledeliverybuyer.Network.NaverApiService;
import com.example.shim.simpledeliverybuyer.R;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PickAddressFragment extends Fragment implements OnMapReadyCallback {

    private LocationButtonView locationButtonView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private final String NAVER_MAPS_API_CLIENT_ID = "anmnho90ij";
    private CameraPosition cameraPosition;
    private Button btn_position;
    private TextView tv_addr;
    private TextView tv_addrToggle;

    private String addr;
    private String roadAddr;
    private boolean addrFlag; //도로명주소를 보여줄지 지번주소를 보여줄지 결정하는 플래그

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pick_address, container, false);

        locationButtonView = view.findViewById(R.id.pickAddress_locationBtnView);
        tv_addr = (TextView) view.findViewById(R.id.pickAddress_tv_addr);
        tv_addrToggle = (TextView) view.findViewById(R.id.pickAddress_tv_addrToggle);

        //클릭시 도로명 or 지번 주소 토글해서 보여줌
        tv_addrToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_addr.setText(addrFlag ? roadAddr : addr);
                tv_addrToggle.setText(addrFlag ? "지번 주소로 보기" : "도로명 주소로 보기");
                addrFlag = !addrFlag;
            }
        });

        btn_position = (Button) view.findViewById(R.id.pickAddress_btn_camPosition);
        btn_position.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderFragment orderFragment = new OrderFragment();
                Bundle args = new Bundle();

                double latitude = cameraPosition.target.latitude;
                double longitude = cameraPosition.target.longitude;

                //Bundle에 위도, 경도, 지번주소, 도로명주소를 넘겨줌.
                args.putDouble("latitude", latitude);
                args.putDouble("longitude", longitude);
                args.putString("addr", addr);
                args.putString("roadAddr", roadAddr);

                orderFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.index_frameLayout, orderFragment).commit();

            }
        });

        //내위치 퍼미션을 획득했는지 확인
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        //네이버 지도 API 클라이언트 ID를 등록함
        NaverMapSdk.getInstance(getActivity()).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(NAVER_MAPS_API_CLIENT_ID));

//        MapFragment mapFragment = (MapFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.pickAddress_frag_map);
        MapFragment mapFragment = null;
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.pickAddress_frag_map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        locationSource = new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

        return view;
    }

    @Override
    public void onMapReady(@NonNull final NaverMap naverMap) {
        //지도에 현위치 버튼 표시
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        locationButtonView.setMap(naverMap);

        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        //지도 카메라 이동시 호출되는 콜백 메서드
        naverMap.addOnCameraChangeListener(new NaverMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(int i, boolean b) {
                cameraPosition = naverMap.getCameraPosition();
            }
        });

        naverMap.addOnCameraIdleListener(new NaverMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                cameraPosition = naverMap.getCameraPosition();
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://naveropenapi.apigw.ntruss.com/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                NaverApiService naverApiService = retrofit.create(NaverApiService.class);

                String coords = String.valueOf(cameraPosition.target.longitude) + "," + String.valueOf(cameraPosition.target.latitude); //longitude, latitude
                String output = "json"; //json or xml
                String order = "roadaddr,addr";
                Call<ReverseGeoResponse> call = naverApiService.getAddrWithLatLng(coords, output, order);

                call.enqueue(new Callback<ReverseGeoResponse>() {
                    @Override
                    public void onResponse(Call<ReverseGeoResponse> call, Response<ReverseGeoResponse> response) {
                        Log.d("Call Request =" , call.request().toString());
                        if(response.isSuccessful()){
                            if(response.body().getResults().size() == 0){
                                Toast.makeText(getActivity(), "지정한 위치의 주소가 없습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            ReverseGeoResponse reverseGeoResponse = response.body();
                            String area1 = reverseGeoResponse.getResults().get(0).getRegion().getArea1().getName();
                            String area2 = reverseGeoResponse.getResults().get(0).getRegion().getArea2().getName();
                            String area3 = reverseGeoResponse.getResults().get(0).getRegion().getArea3().getName();
                            String area4 = reverseGeoResponse.getResults().get(0).getRegion().getArea4().getName();
                            String roadName = reverseGeoResponse.getResults().get(0).getLand().getName();
                            String roadNumber = reverseGeoResponse.getResults().get(0).getLand().getNumber1();
                            roadAddr = area1 + " " + area2 + " " + area3 + " " + area4 + " " + roadName + " " + roadNumber;

                            if(reverseGeoResponse.getResults().size() != 1) {
                                String road_area1 = reverseGeoResponse.getResults().get(1).getRegion().getArea1().getName();
                                String road_area2 = reverseGeoResponse.getResults().get(1).getRegion().getArea2().getName();
                                String road_area3 = reverseGeoResponse.getResults().get(1).getRegion().getArea3().getName();
                                String road_area4 = reverseGeoResponse.getResults().get(1).getRegion().getArea4().getName();
                                String road_roadNumber = reverseGeoResponse.getResults().get(1).getLand().getNumber1();
                                String road_roadNumber2 = reverseGeoResponse.getResults().get(1).getLand().getNumber2();
                                addr = road_area1 + " " + road_area2 + " " + road_area3 + " " + road_area4 + " " + road_roadNumber + "-" + road_roadNumber2;
                            }
                            else {
                                Log.d("testing", String.valueOf(cameraPosition.target.longitude) + ", "+ String.valueOf(cameraPosition.target.latitude));
                            }

                            tv_addr.setText(addrFlag ? addr : roadAddr);
                        }
                    }
                    @Override
                    public void onFailure(Call<ReverseGeoResponse> call, Throwable t) {
                        Log.d("onFailure", t.getMessage());
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE){
            if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {


                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return ;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
