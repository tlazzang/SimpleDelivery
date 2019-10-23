package com.example.shim.simpledeliverybuyer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.shim.simpledeliverybuyer.Fragment.OrderFragment;
import com.example.shim.simpledeliverybuyer.Model.Errand;

public class OrderDetailActivity extends AppCompatActivity {
    private Errand errand;
    private ImageView iv_img;
    private ImageView iv_chat;
    private TextView tv_errandContent;
    private TextView tv_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        init();
        tv_errandContent.setText(errand.getContents());
        iv_chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, ChatActivity.class);
                intent.putExtra("errand", errand);
                startActivity(intent);
            }
        });
    }

    private void init() {
        errand = (Errand) getIntent().getSerializableExtra("errand");
        iv_img = (ImageView) findViewById(R.id.orderDetail_iv_img);
        iv_chat = (ImageView) findViewById(R.id.orderDetail_iv_chat);
        tv_errandContent = (TextView) findViewById(R.id.orderDetail_tv_errandContent);
        tv_address = (TextView) findViewById(R.id.orderDetail_tv_address);
    }
}
