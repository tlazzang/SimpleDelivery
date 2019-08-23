package com.example.shim.simpledeliverydeliverer.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shim.simpledeliverydeliverer.Adapter.MyErrandAdapter;
import com.example.shim.simpledeliverydeliverer.MainActivity;
import com.example.shim.simpledeliverydeliverer.R;

public class MyErrandFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyErrandAdapter myErrandAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_errand, container, false);
        init(view);
        return view;
    }

    public void init(View view){
        recyclerView = view.findViewById(R.id.myErrandFrag_recyclerView);
        myErrandAdapter = new MyErrandAdapter(getActivity(), MainActivity.myId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myErrandAdapter);
    }
}
