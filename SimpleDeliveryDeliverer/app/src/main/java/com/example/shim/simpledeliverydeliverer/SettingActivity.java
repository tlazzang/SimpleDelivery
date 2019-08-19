package com.example.shim.simpledeliverydeliverer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shim.simpledeliverydeliverer.Network.ErrandService;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SettingActivity extends AppCompatActivity {

    private EditText et_km;
    private Button btn_ok;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
    }

    public void init(){
        et_km = findViewById(R.id.setting_et_km);
        btn_ok = findViewById(R.id.setting_btn_ok);
        sharedPreferences = getSharedPreferences("pref", MODE_PRIVATE);
        if(sharedPreferences.contains("pushDistance")){
            et_km.setText(String.valueOf(sharedPreferences.getInt("pushDistance", 20)));
        }
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retrofit retrofit = RetrofitInstance.getInstance();
                ErrandService service = retrofit.create(ErrandService.class);
                final int km = Integer.parseInt(et_km.getText().toString());
                Call<ResponseBody> call = service.updateSettingDistance(MainActivity.token, km);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            sharedPreferences.edit().putInt("pushDistance", km).commit();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });

            }
        });
    }
}
