package com.example.shim.simpledeliverydeliverer.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.shim.simpledeliverydeliverer.Adapter.ErrandAdapter;
import com.example.shim.simpledeliverydeliverer.R;
import com.example.shim.simpledeliverydeliverer.SettingActivity;


public class ErrandFragment extends Fragment {
    private RecyclerView recyclerView;
    private ErrandAdapter errandAdapter;
    private Spinner filterSpinner;
    private FloatingActionButton fab_setting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_errand, container, false);
        init(view);
        return view;
    }

    private void init(View view){
        recyclerView = view.findViewById(R.id.errand_recyclerView);
        filterSpinner = view.findViewById(R.id.errand_spinner);
        errandAdapter = new ErrandAdapter(getActivity());
        fab_setting = view.findViewById(R.id.errand_fab);
        fab_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(errandAdapter);

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.spinner_array, android.R.layout.simple_spinner_item);
        filterSpinner.setAdapter(arrayAdapter);
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int pos = parent.getSelectedItemPosition();
                // 0: 최신 순, 1: 거리 순, 2: 가격 높은 순
                switch(pos){
                    case 0 : {
                        errandAdapter.sortByTime();
                        break;
                    }
                    case 1 : {
                        errandAdapter.sortByDistance();
                        break;
                    }
                    case 2 : {
                        errandAdapter.sortByPrice();
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
