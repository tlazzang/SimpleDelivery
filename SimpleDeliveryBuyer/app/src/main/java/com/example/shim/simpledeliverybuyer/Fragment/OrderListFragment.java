package com.example.shim.simpledeliverybuyer.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shim.simpledeliverybuyer.Adapter.MyOrderAdapter;
import com.example.shim.simpledeliverybuyer.MainActivity;
import com.example.shim.simpledeliverybuyer.R;

public class OrderListFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyOrderAdapter myOrderAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);
        init(view);
        return view;
    }

    public void init(View view){
        recyclerView = view.findViewById(R.id.orderList_recyclerView);
        myOrderAdapter = new MyOrderAdapter(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myOrderAdapter);
    }
}
