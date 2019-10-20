package com.example.shim.simpledeliverybuyer.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shim.simpledeliverybuyer.Model.Errand;
import com.example.shim.simpledeliverybuyer.Network.ErrandService;
import com.example.shim.simpledeliverybuyer.R;
import com.example.shim.simpledeliverybuyer.RetrofitInstance;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class OrderFragment extends Fragment {

    EditText et_detailAddr;
    EditText et_errandPrice;
    EditText et_productPrice;
    EditText et_contents;

    Button btn_complete;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        init(view);

        btn_complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_errandPrice.getText())){
                    Toast.makeText(getActivity(), "심부름값을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isNumeric(et_errandPrice.getText().toString()) || !isNumeric(et_productPrice.getText().toString())){
                    Toast.makeText(getActivity(), "심부름값에 숫자만 입력해주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Retrofit retrofit = RetrofitInstance.getInstance();

                String destination = getArguments().getString("roadAddr");
                double latitude = getArguments().getDouble("latitude", 0.0);
                double longitude = getArguments().getDouble("longitude", 0.0);
                int price = Integer.valueOf(et_errandPrice.getText().toString()) + Integer.valueOf(et_productPrice.getText().toString());
                String contents = et_contents.getText().toString();

                Errand errand = new Errand(destination, latitude, longitude, price, contents);

                ErrandService service = retrofit.create(ErrandService.class);
                String token = getActivity().getSharedPreferences("pref",0).getString("token", "");
                Call<ResponseBody> call = service.createErrand(token, errand);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            Toast.makeText(getActivity(), "의뢰가 완료되었습니다.", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
        });

        return view;
    }


    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private void init(View view){
        et_detailAddr = view.findViewById(R.id.order_et_detailAddr);
        et_errandPrice = view.findViewById(R.id.order_et_errandPrice);
        et_productPrice = view.findViewById(R.id.order_et_productPrice);
        et_contents = view.findViewById(R.id.order_et_contents);
        btn_complete = view.findViewById(R.id.order_btn_complete);
    }

}
