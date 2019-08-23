package com.example.shim.simpledeliverydeliverer.Adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.shim.simpledeliverydeliverer.Fragment.ErrandDetailFragment;
import com.example.shim.simpledeliverydeliverer.Model.Errand;
import com.example.shim.simpledeliverydeliverer.Network.ErrandService;
import com.example.shim.simpledeliverydeliverer.R;
import com.example.shim.simpledeliverydeliverer.RetrofitInstance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyErrandAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Errand> errandList;
    private Context context;
    private Retrofit retrofit;
    private int myId;

    public MyErrandAdapter(Context context, final int myId) {
        this.errandList = new ArrayList<>();
        this.context = context;
        this.myId = myId;

        retrofit = RetrofitInstance.getInstance();

        ErrandService service = retrofit.create(ErrandService.class);
        String token = context.getSharedPreferences("pref", 0).getString("token", "");

        Call<List<Errand>> call = service.getMyOrder(token);
        call.enqueue(new Callback<List<Errand>>() {
            @Override
            public void onResponse(Call<List<Errand>> call, Response<List<Errand>> response) {
                if(response.isSuccessful()){
                    errandList = response.body();
                    Iterator<Errand> iterator = errandList.iterator();
                    while(iterator.hasNext()){
                        if(iterator.next().getPorter_id() != myId){
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
        //ErrandAdapter와 똑같은 레이아웃 파일을 사용하는데 내 심부름에서는 심부름 수락버튼이 필요 없기때문에 보이지 않게 설정.
        view.findViewById(R.id.errandList_btn_accept).setVisibility(View.GONE);
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

                ErrandDetailFragment errandDetailFragment = new ErrandDetailFragment();
                errandDetailFragment.setArguments(args);
                ((AppCompatActivity)context).getSupportFragmentManager().
                        beginTransaction().replace(R.id.index_frameLayout, errandDetailFragment).commit();
            }
        });
        ((ErrandViewHolder) viewHolder).tv_address.setText(errand.getDestination());
        ((ErrandViewHolder) viewHolder).tv_price.setText(String.valueOf(errand.getPrice()));
        ((ErrandViewHolder) viewHolder).tv_contents.setText(errand.getContents());
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
