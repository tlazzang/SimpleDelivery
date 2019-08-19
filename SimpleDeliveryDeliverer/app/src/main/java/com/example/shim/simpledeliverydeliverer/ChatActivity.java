package com.example.shim.simpledeliverydeliverer;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.shim.simpledeliverydeliverer.Adapter.MessageAdapter;
import com.example.shim.simpledeliverydeliverer.Model.Errand;
import com.example.shim.simpledeliverydeliverer.Model.Message;
import com.example.shim.simpledeliverydeliverer.Network.ChatService;
import com.example.shim.simpledeliverydeliverer.Network.ErrandService;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;

import java.net.URISyntaxException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private EditText et_input;
    private Button btn_send;
    private Socket socket;
    private Errand errand;
    private String userId;
    private String destinationId;
    private String token;

    {
        try {
            socket = IO.socket("http://13.209.21.97:6060/");

        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        socket.connect();
        socket.on("new message", onNewMessage);
        socket.emit("update socket_id", userId);

        Retrofit retrofit = RetrofitInstance.getInstance();

        ChatService service = retrofit.create(ChatService.class);
        token = sharedPreferences.getString("token","");
        Call<List<Message>> call = service.getMessage(token, Integer.valueOf(destinationId), errand.getId());

        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                List<Message> messageList = response.body();
                messageAdapter = new MessageAdapter(messageList, ChatActivity.this);
                LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(llm);
                recyclerView.setAdapter(messageAdapter);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {

            }
        });
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Gson gson = new Gson();
                    Message message = gson.fromJson(args[0].toString(), Message.class);

                    //나에게 메시지가 왔을 때 현재 채팅방에 전송된 메시지일 경우에만 어댑터에 메시지 추가
                    if((message.getSender_id() == Integer.valueOf(destinationId) && message.getReceiver_id() == MainActivity.myId) ||
                            (message.getSender_id() == MainActivity.myId && message.getReceiver_id() == Integer.valueOf(destinationId))){
                        messageAdapter.addItem(message);
                        recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            });
        }
    };

    private void init() {
        errand = (Errand) getIntent().getSerializableExtra("errand");
        if(errand.getBuyer_id() == MainActivity.myId){
            userId = String.valueOf(errand.getBuyer_id());
            destinationId = String.valueOf(errand.getPorter_id());
        }
        else{
            userId = String.valueOf(errand.getPorter_id());
            destinationId = String.valueOf(errand.getBuyer_id());
        }
        sharedPreferences = getSharedPreferences("pref", 0);
        recyclerView = findViewById(R.id.chat_recyclerView);
        et_input = findViewById(R.id.chat_et_input);
        btn_send = findViewById(R.id.chat_btn_send);
    }

    private void sendMessage() {
        Message message = new Message();
        message.setSender_id(MainActivity.myId);
        message.setReceiver_id(Integer.valueOf(destinationId));
        message.setContents(et_input.getText().toString().trim());
        message.setErrand_id(errand.getId());
        Gson gson = new Gson();
        if (TextUtils.isEmpty(message.getContents())) {
            return;
        }
        socket.emit("sendToSomeone", gson.toJson(message), destinationId);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://13.209.21.97:5050/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ErrandService service = retrofit.create(ErrandService.class);
        Call<ResponseBody> call = service.sendFcm(token, Integer.parseInt(destinationId));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("ChatActivity: ", "FCM SENDING IS FAIL");
            }
        });
        et_input.setText("");
//        messageAdapter.addItem(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off("new message", onNewMessage);
    }
}
