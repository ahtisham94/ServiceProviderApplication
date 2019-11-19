package com.android.serviceproviderapplication;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Helper.BottomSheetBillCalculate;
import com.android.serviceproviderapplication.Model.Token;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TimerActivity extends AppCompatActivity {
    Button mStartTimer, mStopTimer;
    private Chronometer mChronometer;
    boolean running;
    private long pauseOffSet;
    String customerId;
    private int mints,secc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        mStartTimer = findViewById(R.id.starttime);
        mStopTimer = findViewById(R.id.reset);
        mChronometer = findViewById(R.id.chronometer);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.setText("00:00:00");
        mStopTimer.setVisibility(View.INVISIBLE);
        if (getIntent() != null) {
            customerId = getIntent().getStringExtra("customerID");
        }
        mChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                showTime();
            }
        });
        mStartTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!running) {
                    mChronometer.setBase(SystemClock.elapsedRealtime() - pauseOffSet);
                    mChronometer.start();
                    running = true;
                    mStartTimer.setText("Pause");
                    mStopTimer.setVisibility(View.VISIBLE);

                } else {
                    mChronometer.stop();
                    pauseOffSet = SystemClock.elapsedRealtime() - mChronometer.getBase();
                    running = false;
                    mStartTimer.setText("Start");
                    mStopTimer.setVisibility(View.VISIBLE);
                }
            }
        });
        mStopTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mChronometer.stop();
                mChronometer.setText("00:00:00");
                int elecMillis = (int) (SystemClock.elapsedRealtime() - mChronometer.getBase());
                int sec = elecMillis / 1000;
                 mints = (elecMillis / 1000) / 60;
                 secc = (elecMillis / 1000) % 60;
                int gSec = 60;
                if (secc < gSec) {
                    Toast.makeText(TimerActivity.this,"Time is" + mints + "m" + secc + "s",Toast.LENGTH_SHORT).show();
                    BottomSheetBillCalculate b = BottomSheetBillCalculate.newInstance(mints,secc);
                    b.show(getSupportFragmentManager(),b.getTag());

                } else {
                    Toast.makeText(TimerActivity.this,"Time is" + mints + "m" + secc + "s",Toast.LENGTH_SHORT).show();
                    BottomSheetBillCalculate b = BottomSheetBillCalculate.newInstance(mints,secc);
                    b.show(getSupportFragmentManager(),b.getTag());
                }
                sendBillReceipt(customerId);

            }
        });
    }

    private void sendBillReceipt(String customerId) {
        Token token=new Token(customerId);
        JSONArray recipients=new JSONArray();
        recipients.put(token.getToken());
        String serviceProviderToken= FirebaseInstanceId.getInstance().getToken();
        sendMessage(recipients,"bill","","",serviceProviderToken);
    }

    private void showTime() {
        long time = SystemClock.elapsedRealtime() - mChronometer.getBase();
        int h = (int) (time / 3600000);
        int m = (int) (time - h * 3600000) / 60000;
        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
        String t = (h < 10 ? "0" + h : h) + ":" + (m < 10 ? "0" + m : m) + ":" + (s < 10 ? "0" + s : s);
        mChronometer.setText(t);

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
                    notification.put("body",body);
                    notification.put("title",title);
//                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
//                    data.put("ctmr", message);
//                    data.put("lat",mLaatLocation.getLatitude());
//                    data.put("lng",mLaatLocation.getLongitude());
                    String ans=Double.toString(Common.getPrice(mints,secc));

                    root.put("notification",notification);
                    data.put("min",mints);
                    data.put("sec",secc);
                    data.put("price",ans);
                    root.put("data", data);
                    root.put("registration_ids",recipients);

                    String result = postToFCM(root.toString());
//                    Log.d(TAG, "Result: " + result);
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
                    Toast.makeText(TimerActivity.this,"Message Success: " + success + "Message Failed: " + failure,Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Toast.makeText(TimerActivity.this,"Message Failed, Unknown error occurred.",Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON,bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization","key=" + "AAAABCuXlRI:APA91bE2O6y2iHML-xEmEyO3wmIjcmxIFmby-W-cqt7qm5p7FntUuLlXdoKcOJjugcjI5v_jI57kBMYT_OfUx3Hy-MqK2uwSsPbDMX7TE4-cVePPP1GyVG_0nrCIlcSApCYUs9XMlxLo")
                .build();
        okhttp3.Response response = mClient.newCall(request).execute();
        return response.body().string();
    }
}
