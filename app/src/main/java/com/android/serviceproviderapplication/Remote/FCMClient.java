package com.android.serviceproviderapplication.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Ahtisham Alam on 4/23/2018.
 */

public class FCMClient {
    private static Retrofit retrofit=null;
    public static Retrofit getClient(String baseURL){
        if(retrofit==null){
            retrofit=new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return  retrofit;
    }
}
