package com.example.shim.simpledeliverydeliverer.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shim.simpledeliverydeliverer.IndexActivity;
import com.example.shim.simpledeliverydeliverer.Model.Errand;
import com.example.shim.simpledeliverydeliverer.Network.ErrandService;
import com.example.shim.simpledeliverydeliverer.R;
import com.example.shim.simpledeliverydeliverer.RetrofitInstance;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ErrandAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Errand> errandList = new ArrayList<>();
    private Context context;
    private Retrofit retrofit;

    public ErrandAdapter(Context context) {
        this.context = context;

        retrofit = RetrofitInstance.getInstance();

        ErrandService service = retrofit.create(ErrandService.class);
        String token = context.getSharedPreferences("pref", 0).getString("token", "");

        Call<List<Errand>> call = service.getErrand(token);
        call.enqueue(new Callback<List<Errand>>() {
            @Override
            public void onResponse(Call<List<Errand>> call, Response<List<Errand>> response) {
                if(response.isSuccessful()){
                    errandList = response.body();
                    sortByTime();
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
        return new ErrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
        final Errand errand = errandList.get(i);

        Date date = new Date(errand.getTimestamp());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = format.format(date);

        ((ErrandViewHolder)viewHolder).tv_timestamp.setText(now);
        ((ErrandViewHolder)viewHolder).tv_address.setText(errand.getDestination());
        ((ErrandViewHolder)viewHolder).tv_price.setText(String.valueOf(errand.getPrice()));
        ((ErrandViewHolder)viewHolder).tv_contents.setText(errand.getContents());
        ((ErrandViewHolder)viewHolder).btn_accpet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ErrandService errandService = retrofit.create(ErrandService.class);
                final String token = context.getSharedPreferences("pref", 0).getString("token", "");
                Call<ResponseBody> call = errandService.updateErrand(token, errand.getId());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.isSuccessful()){
                            Toast.makeText(context, "수락 완료", Toast.LENGTH_SHORT).show();
                            //해당 심부름의 porter_id와 상태 업데이트 완료 후에 주문자한테 푸시 알람 전송
                            call = errandService.sendFcm(token, errand.getBuyer_id());
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                    if(response.isSuccessful()){
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

    public void sortByTime() {
        Collections.sort(errandList, new Comparator<Errand>() {
            @Override
            public int compare(Errand o1, Errand o2) {
                if (o1.getTimestamp() > o2.getTimestamp()) {
                    return -1;
                }
                else if (o1.getTimestamp() < o2.getTimestamp()) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    public void sortByPrice() {
        Collections.sort(errandList, new Comparator<Errand>() {
            @Override
            public int compare(Errand o1, Errand o2) {
                if (o1.getPrice() > o2.getPrice()) {
                    return -1;
                }
                else if (o1.getPrice() < o2.getPrice()) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    public void sortByDistance() {
        final double myLatitude = IndexActivity.latitude;
        final double myLongitude = IndexActivity.longitude;
        Collections.sort(errandList, new Comparator<Errand>() {
            @Override
            public int compare(Errand o1, Errand o2) {
                double o1_distance = getDistance(o1.getLatitude(), myLatitude, o1.getLongitude(), myLongitude, 0.0, 0.0);
                double o2_distance = getDistance(o2.getLatitude(), myLatitude, o2.getLongitude(), myLongitude, 0.0, 0.0);
                if (o1_distance > o2_distance) {
                    return 1;
                }
                else if (o1_distance < o2_distance) {
                    return -1;
                }
                else {
                    return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    public static double getDistance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

    private class ErrandViewHolder extends RecyclerView.ViewHolder {
        TextView tv_timestamp;
        TextView tv_address;
        TextView tv_price;
        TextView tv_contents;
        Button btn_accpet;
        public ErrandViewHolder(View view) {
            super(view);
            tv_timestamp = view.findViewById(R.id.errandList_tv_timestamp);
            tv_address = view.findViewById(R.id.errandList_tv_address);
            tv_price = view.findViewById(R.id.errandList_tv_price);
            tv_contents = view.findViewById(R.id.errandList_tv_contents);
            btn_accpet = view.findViewById(R.id.errandList_btn_accept);
        }
    }
}
