package com.example.shim.simpledeliverybuyer;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.shim.simpledeliverybuyer.Fragment.OrderListFragment;
import com.example.shim.simpledeliverybuyer.Fragment.PickAddressFragment;
import com.example.shim.simpledeliverybuyer.Network.ErrandService;
import com.google.android.gms.tasks.OnCompleteListener;
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
    private BottomNavigationView bottomNavigationView;

    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 4개의 메뉴에 들어갈 Fragment들
    private PickAddressFragment pickAddressFragment = new PickAddressFragment();
    private OrderListFragment orderListFragment = new OrderListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        updateFcmToken();

        bottomNavigationView = findViewById(R.id.index_bottomNavigationView);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.index_frameLayout, pickAddressFragment).commitAllowingStateLoss();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.navigation_home : {
                        fragmentTransaction.replace(R.id.index_frameLayout, pickAddressFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navigation_orderList : {
                        fragmentTransaction.replace(R.id.index_frameLayout, orderListFragment).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.navigation_profile : {
                        return true;
                    }
                }
                return false;
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
                        Retrofit retrofit = RetrofitInstance.getInstance();

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
    public void onBackPressed() {
        AlertDialog.Builder builder = new android.app.AlertDialog.Builder(IndexActivity.this);
        builder.setMessage("앱을 종료하시겠습니까?")
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
