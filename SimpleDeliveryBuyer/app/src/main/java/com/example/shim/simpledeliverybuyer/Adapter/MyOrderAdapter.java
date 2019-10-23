package com.example.shim.simpledeliverybuyer.Adapter;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.shim.simpledeliverybuyer.Model.Errand;
import com.example.shim.simpledeliverybuyer.Network.ErrandService;
import com.example.shim.simpledeliverybuyer.OrderDetailActivity;
import com.example.shim.simpledeliverybuyer.R;
import com.example.shim.simpledeliverybuyer.RetrofitInstance;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MyOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Errand> errandList = new ArrayList<>();
    private Context context;
    private Retrofit retrofit;
    private int myId;

    public MyOrderAdapter(Context context) {
        this.context = context;
        myId = context.getSharedPreferences("pref", 0).getInt("myId", 0);
        retrofit = RetrofitInstance.getInstance();

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
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("errand", errandList.get(i));
                context.startActivity(intent);
            }
        });

        ((ErrandViewHolder) viewHolder).bind(errand);
    }

    @Override
    public int getItemCount() {
        return errandList.size();
    }

    private class ErrandViewHolder extends RecyclerView.ViewHolder {
        TextView tv_address;
        TextView tv_price;
        TextView tv_contents;
        TextView tv_timestamp;

        public void bind(Errand errand){
            tv_address.setText(errand.getDestination());
            tv_price.setText(String.format("%,d", errand.getPrice()) + "원");
            tv_contents.setText(errand.getContents());
            Date date = new Date(errand.getTimestamp());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String now = format.format(date);
            tv_timestamp.setText(now);
        }
        public ErrandViewHolder(View view) {
            super(view);
            tv_address = view.findViewById(R.id.errandList_tv_address);
            tv_price = view.findViewById(R.id.errandList_tv_price);
            tv_contents = view.findViewById(R.id.errandList_tv_contents);
            tv_timestamp = view.findViewById(R.id.errandList_tv_timestamp);
        }
    }
}
