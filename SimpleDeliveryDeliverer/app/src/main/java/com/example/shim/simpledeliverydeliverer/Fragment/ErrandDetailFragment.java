package com.example.shim.simpledeliverydeliverer.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shim.simpledeliverydeliverer.ChatActivity;
import com.example.shim.simpledeliverydeliverer.Model.Errand;
import com.example.shim.simpledeliverydeliverer.R;

public class ErrandDetailFragment extends Fragment {

    private Errand errand;
    private ImageView iv_img;
    private ImageView iv_chat;
    private TextView tv_errandContent;
    private TextView tv_address;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_errand_detail, container, false);
        init(view);
        tv_errandContent.setText(errand.getContents());
        iv_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("errand", errand);
                startActivity(intent);
            }
        });
        return view;
    }

    public void init(View view){
        errand = (Errand) getArguments().getSerializable("errand");
        iv_img = (ImageView) view.findViewById(R.id.orderDetail_iv_img);
        iv_chat = (ImageView) view.findViewById(R.id.orderDetail_iv_chat);
        tv_errandContent = (TextView) view.findViewById(R.id.orderDetail_tv_errandContent);
        tv_address = (TextView) view.findViewById(R.id.orderDetail_tv_address);
    }

}
