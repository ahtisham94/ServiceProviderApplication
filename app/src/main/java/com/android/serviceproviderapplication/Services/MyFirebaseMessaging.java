package com.android.serviceproviderapplication.Services;

import android.content.Intent;
import android.util.Log;

import com.android.serviceproviderapplication.CustomerCallActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;


public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getNotification() == null)
            return;
        sentNotification(remoteMessage);
    }

    private void sentNotification(RemoteMessage remoteMessage) {
        Log.d("debug","here it is :"+remoteMessage.getNotification().toString());
        Log.d("debug","here it is :"+remoteMessage.getData().toString());
        double lat= Double.parseDouble( remoteMessage.getData().get("lat"));
        double lng= Double.parseDouble( remoteMessage.getData().get("lng"));

//        LatLng customer_locations=new Gson().fromJson(remoteMessage.getNotification().getBody(),LatLng.class);

        String userToken=remoteMessage.getData().get("cid");
        Intent intent=new Intent(getBaseContext(),CustomerCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("customer",userToken);
        startActivity(intent);
}

}
