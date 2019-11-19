package com.android.serviceproviderapplication.Remote;

import com.android.serviceproviderapplication.Model.FCMResponse;
import com.android.serviceproviderapplication.Model.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Ahtisham Alam on 4/21/2018.
 */

public interface IFCMServices {
    @Headers({
                "Content-Type:application/json",
                "Authorization:key=AAAABCuXlRI:APA91bE2O6y2iHML-xEmEyO3wmIjcmxIFmby-W-cqt7qm5p7FntUuLlXdoKcOJjugcjI5v_jI57kBMYT_OfUx3Hy-MqK2uwSsPbDMX7TE4-cVePPP1GyVG_0nrCIlcSApCYUs9XMlxLo"
    })
    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);
}

