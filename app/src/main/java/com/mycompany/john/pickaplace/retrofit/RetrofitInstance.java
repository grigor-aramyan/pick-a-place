package com.mycompany.john.pickaplace.retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private Retrofit mRetrofit;
    private static BackendService mBackend;

    private RetrofitInstance() {
    }

    public static BackendService getBackendService() {
        if (mBackend != null) {
            return mBackend;
        } else {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.8:4000/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mBackend = retrofit.create(BackendService.class);
            return mBackend;
        }
    }
}
