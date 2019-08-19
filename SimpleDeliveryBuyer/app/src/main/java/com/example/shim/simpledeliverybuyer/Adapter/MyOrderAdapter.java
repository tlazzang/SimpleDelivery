package com.example.shim.simpledeliverybuyer.Adapter;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shim.simpledeliverybuyer.Fragment.OrderDetailFragment;
import com.example.shim.simpledeliverybuyer.Model.Errand;
import com.example.shim.simpledeliverybuyer.Network.ErrandService;
import com.example.shim.simpledeliverybuyer.R;


import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Errand> errandList = new ArrayList<>();
    private Context context;
    private Retrofit retrofit;
    private int myId;

    public MyOrderAdapter(Context context) {
        this.context = context;
        myId = context.getSharedPreferences("pref", 0).getInt("myId", 0);
        retrofit = new Retrofit.Builder()
                .baseUrl("http://13.209.21.97:5050/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ErrandService service = retrofit.create(ErrandService.class);
        String token = context.getSharedPreferences("pref", 0).getString("token", "");
        Log.d("MyOrderAdapter", token);
        Call<List<Errand>> call = service.getMyOrder(token);
        call.enqueue(new Callback<List<Errand>>() {
            @Override
            public void onResponse(Call<List<Errand>> call, Response<List<Errand>> response) {
                if(response.isSuccessful()){
                    errandList = response.body();
                    Iterator<Errand> iterator = errandList.iterator();
                    while(iterator.hasNext()){
                        if(iterator.next().getBuyer_id() != myId){
                            iterator.remove();
                        }
                    }
                    notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Errand>> call, Throwable t) {

            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.errand_list_item, viewGroup, false);
        Button button = view.findViewById(R.id.errandList_btn_accept);
        button.setVisibility(View.GONE);
        return new ErrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        final Errand errand = errandList.get(i);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Bundle에 선택된 errand를 넘겨줌
                Bundle args = new Bundle();
                args.putSerializable("errand", errandList.get(i));

                OrderDetailFragment orderDetailFragment = new OrderDetailFragment();
                orderDetailFragment.setArguments(args);
                ((AppCompatActivity)context).getSupportFragmentManager().
                        beginTransaction().replace(R.id.index_frameLayout, orderDetailFragment).commit();
            }
        });
        ((ErrandViewHolder) viewHolder).tv_address.setText(errand.getDestination());
        ((ErrandViewHolder) viewHolder).tv_price.setText(String.valueOf(errand.getPrice()));
        ((ErrandViewHolder) viewHolder).tv_contents.setText(errand.getContents());
        ((ErrandViewHolder) viewHolder).btn_accpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ErrandService errandService = retrofit.create(ErrandService.class);
                final String token = context.getSharedPreferences("pref", 0).getString("token", "");
                Call<ResponseBody> call = errandService.updateErrand(token, errand.getId());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "수락 완료", Toast.LENGTH_SHORT).show();
                            //해당 심부름의 porter_id와 상태 업데이트 완료 후에 주문자한테 푸시 알람 전송
                            call = errandService.sendFcm(token, errand.getBuyer_id());
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        Toast.makeText(context, "fcm 전송 완료", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
                Toast.makeText(context, String.valueOf(i), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return errandList.size();
    }

    private class ErrandViewHolder extends RecyclerView.ViewHolder {
        TextView tv_address;
        TextView tv_price;
        TextView tv_contents;
        Button btn_accpet;
        public ErrandViewHolder(View view) {
            super(view);
            tv_address = view.findViewById(R.id.errandList_tv_address);
            tv_price = view.findViewById(R.id.errandList_tv_price);
            tv_contents = view.findViewById(R.id.errandList_tv_contents);
            btn_accpet = view.findViewById(R.id.errandList_btn_accept);
        }
    }
}
