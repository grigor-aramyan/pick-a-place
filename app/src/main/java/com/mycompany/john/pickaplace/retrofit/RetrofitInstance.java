package com.mycompany.john.pickaplace.retrofit;

import android.content.Context;

import com.mycompany.john.pickaplace.interceptors.AddCookiesInterceptor;
import com.mycompany.john.pickaplace.interceptors.ReceivedCookiesInterceptor;
import com.mycompany.john.pickaplace.utils.Statics;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitInstance {

    private Retrofit mRetrofit;
    private static BackendService mBackend;

    private RetrofitInstance() {
    }

    public static BackendService getBackendService(Context context) {
        if (mBackend != null) {
            return mBackend;
        } else {
            OkHttpClient client = new OkHttpClient();
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder.addInterceptor(new AddCookiesInterceptor(context)); // VERY VERY IMPORTANT
            builder.addInterceptor(new ReceivedCookiesInterceptor(context)); // VERY VERY IMPORTANT
            client = builder.build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://" + Statics.LOCALHOST_IP + "/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mBackend = retrofit.create(BackendService.class);
            return mBackend;
        }
    }
}
