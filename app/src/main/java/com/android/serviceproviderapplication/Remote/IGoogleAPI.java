package com.android.serviceproviderapplication.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by AhSaN BaiG on 4/17/2018.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);

}
