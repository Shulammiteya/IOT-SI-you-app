package com.harrysoft.androidbluetoothserial.demoapp;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HerokuService {
    @FormUrlEncoded
    @POST("test")
    Call<ResponseBody> test(
            @Field("data") ArrayList<String> data
    );
}
