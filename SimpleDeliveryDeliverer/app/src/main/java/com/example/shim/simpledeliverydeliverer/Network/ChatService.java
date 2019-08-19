package com.example.shim.simpledeliverydeliverer.Network;

import com.example.shim.simpledeliverydeliverer.Model.Message;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ChatService {
    //진행 전인 모든 심부름을 불러옴
    @GET("/message")
    Call<List<Message>> getMessage(@Header("x-access-token") String token, @Query("destinationId") int destinationId, @Query("errandId") int errandId);
}
