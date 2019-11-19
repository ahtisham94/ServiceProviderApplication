package com.android.serviceproviderapplication.Common;

import android.location.Location;

import com.android.serviceproviderapplication.Model.ServiceProviderInformation;
import com.android.serviceproviderapplication.Remote.FCMClient;
import com.android.serviceproviderapplication.Remote.IFCMServices;
import com.android.serviceproviderapplication.Remote.IGoogleAPI;
import com.android.serviceproviderapplication.Remote.RetrofitClient;

/**
 * Created by AhSaN BaiG on 4/17/2018.
 */

public class Common {

    public static final String tokens_tbl = "Tokens";
    public static final String service_provider_LatLng_tbl = "Service Providers LatLng";
    public static final String service_providers_tbl = "Service Providers";
    public static final String users_tbl = "Users";
    public static final String service_request_tbl = "Service Request";
    public static final String baseURL = "https://maps.googleapis.com";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static Location mLastLocation = null;
    public static ServiceProviderInformation currentServiceProvider;

    public static double minutess=3;
    public static double seconds=0.05;
    public static double bookingCharges=70;

    public static double getPrice(int mins,int sec)
    {
        return (bookingCharges+minutess*mins+seconds*sec);
    }

    public static IGoogleAPI getGoogleApi() {
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMServices getFCMServic() {
        return FCMClient.getClient(fcmURL).create(IFCMServices.class);

    }
}
