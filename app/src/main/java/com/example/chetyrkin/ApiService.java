package com.example.chetyrkin;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ApiService {
    @GET("/api/v7/convert")
    Call<ResponseBody> getMyJSON(@QueryMap Map<String,String> options);

}
