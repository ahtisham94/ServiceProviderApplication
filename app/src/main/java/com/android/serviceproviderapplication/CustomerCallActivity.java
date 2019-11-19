package com.android.serviceproviderapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Model.FCMResponse;
import com.android.serviceproviderapplication.Model.Notification;
import com.android.serviceproviderapplication.Model.Sender;
import com.android.serviceproviderapplication.Model.Token;
import com.android.serviceproviderapplication.Remote.IFCMServices;
import com.android.serviceproviderapplication.Remote.IGoogleAPI;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCallActivity extends AppCompatActivity {

    TextView txtTime, txtAddress, txtDistance;
    MediaPlayer mediaPlayer;
    IGoogleAPI mServices;
    IFCMServices ifcmServices;
    Button btnAccept, btnCancel;
    String customer_id;
    double lat,lng;
    private String TAG="User Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);
        mServices = Common.getGoogleApi();
        ifcmServices = Common.getFCMServic();
        initViews();

        btnAccept();
        btnCancel();
    }

    private void btnAccept() {
        btnAccept = findViewById(R.id.button_accept);
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!TextUtils.isEmpty(customer_id)) {
                    acceptRequest(customer_id);
                }

                Intent intent=new Intent(CustomerCallActivity.this,SPTrackingActivity.class);
                intent.putExtra("lat",lat);
                intent.putExtra("lng",lng);
                intent.putExtra("customerID",customer_id);
                startActivity(intent);
                finish();

            }
        });
    }

    private void btnCancel() {
        btnCancel = findViewById(R.id.button_decline);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(customer_id))
                    cancelRequest(customer_id);
            }
        });
    }

    private void acceptRequest(String customer_id) {
        Token token = new Token(customer_id);
//        Notification notification = new Notification("Notice!","Driver has cencelled your request");
//        Sender sender = new Sender(token.getToken(), notification);
//        ifcmServices.sendMessage(sender)
//                .enqueue(new Callback<FCMResponse>() {
//                    @Override
//                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                        if (response.body().success == 1) {
//                            Toast.makeText(CustomerCallActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<FCMResponse> call,Throwable t) {
//                        Log.d("Come Here",t.getMessage());
//                    }
//                });

        JSONArray recipient=new JSONArray();
        recipient.put(token.getToken());
        String serviceProviderToken= FirebaseInstanceId.getInstance().getToken();
        sendMessage(recipient,"Accepted","Service Provider has Accepted your Request","Test Icon",serviceProviderToken);
        Toast.makeText(this, "Accepted", Toast.LENGTH_SHORT).show();
        finish();

    }
    private void cancelRequest(String customer_id) {
        Token token = new Token(customer_id);
//        Notification notification = new Notification("Notice!","Driver has cencelled your request");
//        Sender sender = new Sender(token.getToken(), notification);
//        ifcmServices.sendMessage(sender)
//                .enqueue(new Callback<FCMResponse>() {
//                    @Override
//                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
//                        if (response.body().success == 1) {
//                            Toast.makeText(CustomerCallActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
//                            finish();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<FCMResponse> call,Throwable t) {
//                        Log.d("Come Here",t.getMessage());
//                    }
//                });

        JSONArray recipient=new JSONArray();
        recipient.put(token.getToken());
        String serviceProviderToken= FirebaseInstanceId.getInstance().getToken();
        sendMessage(recipient,"Cancelled","Service Provider has cancelled your Request","Test Icon",serviceProviderToken);
        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
        finish();

    }

    public static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    OkHttpClient mClient = new OkHttpClient();

    public void sendMessage(final JSONArray recipients,final String title,
                            final String body,final String icon,final String message) {

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
//                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
//                    data.put("cid", message);
//                    data.put("lat",mLaatLocation.getLatitude());
//                    data.put("lng",mLaatLocation.getLongitude());
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Toast.makeText(CustomerCallActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Toast.makeText(CustomerCallActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }


    String postToFCM(String bodyString) throws IOException {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "AAAABCuXlRI:APA91bE2O6y2iHML-xEmEyO3wmIjcmxIFmby-W-cqt7qm5p7FntUuLlXdoKcOJjugcjI5v_jI57kBMYT_OfUx3Hy-MqK2uwSsPbDMX7TE4-cVePPP1GyVG_0nrCIlcSApCYUs9XMlxLo")
                .build();
        okhttp3.Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    private void initViews() {
        txtTime = findViewById(R.id.textView_Time);
        txtDistance = findViewById(R.id.textView_Distance);
        txtAddress = findViewById(R.id.textView_Address);
        mediaPlayer = MediaPlayer.create(this,R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
        if (getIntent() != null) {
            lat = getIntent().getDoubleExtra("lat",-1.0);
            lng = getIntent().getDoubleExtra("lng",-1.0);
            customer_id = getIntent().getStringExtra("customer");

            getDirection(lat,lng);

        }
    }

    private void getDirection(double latitude,double longitude) {

        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + Common.mLastLocation.getLatitude() + "," + Common.mLastLocation.getLongitude() +
                    "&" + "destination=" + latitude + "," + longitude + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("Ahsan",requestApi);
            mServices.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call,Response<String> response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONArray routes = jsonObject.getJSONArray("routes");
                                JSONObject object = routes.getJSONObject(0);
                                JSONArray legs = object.getJSONArray("legs");
                                JSONObject legsObject = legs.getJSONObject(0);
                                JSONObject distance = legsObject.getJSONObject("distance");
                                txtDistance.setText(distance.getString("text"));
                                JSONObject Time = legsObject.getJSONObject("duration");
                                txtTime.setText(Time.getString("text"));
                                String add = legsObject.getString("end_address");
                                txtAddress.setText(add);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call,Throwable t) {
                            Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        if (mediaPlayer.isPlaying())
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if (mediaPlayer.isPlaying())
        mediaPlayer.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying())
        mediaPlayer.start();
    }
}
