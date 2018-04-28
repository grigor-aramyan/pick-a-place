package com.mycompany.john.pickaplace.retrofit;

import com.mycompany.john.pickaplace.models.MyCustomLocation;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface BackendService {

    @Headers("Content-Type: application/json")
    @POST("api/locations")
    Call<ResponseBody> createAnonymousLocation(@Body MyCustomLocation myCustomLocation);
}
