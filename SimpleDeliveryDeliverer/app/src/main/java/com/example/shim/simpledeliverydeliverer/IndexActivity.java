package com.example.shim.simpledeliverydeliverer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.example.shim.simpledeliverydeliverer.Fragment.ErrandFragment;
import com.example.shim.simpledeliverydeliverer.Fragment.MyErrandFragment;
import com.example.shim.simpledeliverydeliverer.Network.ErrandService;
import com.example.shim.simpledeliverydeliverer.Util.GPSTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IndexActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 4개의 메뉴에 들어갈 Fragment들
    private ErrandFragment errandFragment = new ErrandFragment();
    private MyErrandFragment myErrandFragment = new MyErrandFragment();
    private MyProfileFragment myProfileFragment = new MyProfileFragment();

    public FusedLocationProviderClient fusedLocationProviderClient;
    public GPSTracker gpsTracker;
    public static double latitude;
    public static double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        updateFcmToken();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else{

        }

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
        else{

        }

        gpsTracker = new GPSTracker(this);
        latitude = gpsTracker.getLatitude();
        longitude = gpsTracker.getLongitude();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        else{
            gpsTracker = new GPSTracker(this);
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
            updateMyLocation(latitude, longitude);
        }

        bottomNavigationView = findViewById(R.id.index_bottomNavigationView);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.index_frameLayout, errandFragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navigation_errand: {
                        fragmentTransaction.replace(R.id.index_frameLayout, errandFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navigation_myErrand: {
                        fragmentTransaction.replace(R.id.index_frameLayout, myErrandFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navigation_profile: {
                        fragmentTransaction.replace(R.id.index_frameLayout, myProfileFragment).commitAllowingStateLoss();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED))  {
//                gpsTracker = new GPSTracker(this);
//                latitude = gpsTracker.getLatitude();
//                longitude = gpsTracker.getLongitude();
//                updateMyLocation(latitude, longitude);

            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void updateMyLocation(double latitude, double longitude){
        Retrofit retrofit = RetrofitInstance.getInstance();
        ErrandService service = retrofit.create(ErrandService.class);

        Log.d("updateMyLocation", String.valueOf(latitude)+", "+String.valueOf(longitude));
        //locationManager에서 얻어오는 lat, lng이 소수점 8자리가 넘어가는 경우가 있는데 db의 lat,lng이 소수점 8자리까지만 허용하므로 잘라줌
        Call<ResponseBody> call = service.updateLocation(MainActivity.token, Double.parseDouble(String.format("%.8f", latitude)),
                Double.parseDouble(String.format("%.8f", longitude)));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){

                }
                else{
                    Log.d("IndexActivity", "updateLocation fail");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("IndexActivity", t.getMessage());
            }
        });
    }

    private void updateFcmToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TEST", "getInstanceId failed", task.getException());
                            return;
                        }

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl("http://13.209.21.97:5050/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();

                        ErrandService service = retrofit.create(ErrandService.class);
                        String jwtToken = getSharedPreferences("pref", 0).getString("token","");

                        String token = task.getResult().getToken();
                        Call<ResponseBody> call = service.updateFcmToken(jwtToken, token);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                if (response.isSuccessful()){
                                    Log.d("FCM SERVICE : ", "FCM Token is updated");
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d("FCM SERVICE : ", t.getMessage());
                            }
                        });
                        // Get new Instance ID token

                        Log.d("TEST", token);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        gpsTracker.stopUsingGPS();
        super.onDestroy();
    }


}
