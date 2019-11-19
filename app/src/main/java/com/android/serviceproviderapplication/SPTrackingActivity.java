package com.android.serviceproviderapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Helper.DirectionJSONParser;
import com.android.serviceproviderapplication.Model.ServiceProviderInformation;
import com.android.serviceproviderapplication.Model.Token;
import com.android.serviceproviderapplication.Permissions.PermissionUtils;
import com.android.serviceproviderapplication.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SPTrackingActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    double riderLat, riderLng;
    private static final int PLAY_SERVICES_RES_REQUEST = 7001;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 3000;
    private static final int DISPLACEMENT = 10;
    private Circle riderMarker;
    private Marker serviceProviderMarker;
    private Polyline direction;
    IGoogleAPI mServices;
    public ServiceProviderInformation information;

    public String customer_id;
    GeoFire geoFire;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(PermissionUtils.hasLocationPermissionGranted(this))
        {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            setContentView(R.layout.activity_sptracking);
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);

        }

        else
            PermissionUtils.requestLocationPermissions(this,113);

        if (getIntent() != null)
        {
            riderLat = getIntent().getDoubleExtra("lat",-1.0);
            riderLng = getIntent().getDoubleExtra("lng",-1.0);
            customer_id=getIntent().getStringExtra("customerID");

        }
        mServices= Common.getGoogleApi();
        information=new ServiceProviderInformation();

        FirebaseDatabase.getInstance().getReference(Common.service_providers_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        information =dataSnapshot.getValue(ServiceProviderInformation.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }




    private void setUpLocation() {

//        connectMap();
        createLocationRequest();
        displayLocation();

    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        if(PermissionUtils.hasLocationPermissionGranted(this))
        {   connectMap();
            riderMarker = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(riderLat,riderLng))
                    .radius(10)
                    .strokeColor(Color.BLUE)
                    .fillColor(0x220000FF)
                    .strokeWidth(5.0f));
            mMap.setMyLocationEnabled(true);

            //Create geo Fencing with radius 50m
            geoFire=new GeoFire(FirebaseDatabase.getInstance().getReference(Common.service_provider_LatLng_tbl).child(Common.currentServiceProvider.getSpProfession()));
            GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderLat,riderLng),0.1f);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    sendArrivedNotification(customer_id);


                }

                @Override
                public void onKeyExited(String key) {

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            });

        }else
        {
            connectMap();
            mMap.setMyLocationEnabled(true);
        }



    }

    private void sendArrivedNotification(String customer_id) {

        Token token = new Token(customer_id);

        JSONArray recipient=new JSONArray();
        recipient.put(token.getToken());
        String serviceProviderToken= FirebaseInstanceId.getInstance().getToken();
        String name = information.getSpName();
        sendMessage(recipient,"Arrived",name+" has Arrived","Test Icon",serviceProviderToken);
        Toast.makeText(this, "Notification Sent", Toast.LENGTH_SHORT).show();

        Intent intent= new Intent(SPTrackingActivity.this,TimerActivity.class);
        intent.putExtra("customerID",customer_id);
        startActivity(intent);
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
//                    data.put("lat",mLastLocation.getLatitude());
//                    data.put("lng",mLastLocation.getLongitude());
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());

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
                    Toast.makeText(SPTrackingActivity.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
//                    e.printStackTrace();
                    Toast.makeText(SPTrackingActivity.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
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


    private synchronized void connectMap() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint({"RestrictedApi","MissingPermission"})
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(PermissionUtils.hasLocationPermissionGranted(this))
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }

//        createLocationRequest();
//        connectMap();
//        displayLocation();
//        startLocationUpdates();

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!PermissionUtils.hasLocationPermissionGranted(this)) {

            return;
        }

        connectMap();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
    }

    @SuppressLint("MissingPermission")
    private void displayLocation() {
        if (!PermissionUtils.hasLocationPermissionGranted(this)) {
            return;
        }

//        mLaatLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Common.mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (Common.mLastLocation != null) {

            final double lat = Common.mLastLocation.getLatitude();
            final double lng = Common.mLastLocation.getLongitude();
            if (serviceProviderMarker != null)
                serviceProviderMarker.remove();
            serviceProviderMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lat,lng))
                    .title("YOU")
                    .icon(BitmapDescriptorFactory.defaultMarker()));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                    lat,lng
            ),15.f));
            if (direction != null)
                direction.remove();//remove Old Direction
            getDirection();

        } else
            Toast.makeText(this,"Can't get your location",Toast.LENGTH_SHORT).show();
    }

    private void getDirection() {
        LatLng currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());
        String requestApi = null;
        try {
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" + currentPosition.latitude + "," + currentPosition.longitude +
                    "&" + "destination=" + riderLat + "," + riderLng + "&" +
                    "key=" + getResources().getString(R.string.google_direction_api);
            Log.d("EMDTDEV",requestApi);
            mServices.getPath(requestApi)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            try {
                                new ParserTask().execute(response.body());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call,Throwable t) {
                            Toast.makeText(SPTrackingActivity.this,t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Common.mLastLocation = location;
        displayLocation();

    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        ProgressDialog mDialog = new ProgressDialog(SPTrackingActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.setMessage("Please Waiting....");
            mDialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionJSONParser parser = new DirectionJSONParser();
                routes = parser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            mDialog.dismiss();
            ArrayList points = null;
            PolylineOptions polylineOptions = null;
            for (int i = 0; i < lists.size(); i++) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = lists.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat,lng);
                    points.add(position);


                }
                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.RED);
                polylineOptions.geodesic(true);
            }
            direction = mMap.addPolyline(polylineOptions);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*FirebaseDatabase.getInstance().getReference(Common.service_providers_tbl)
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Common.currentServiceProvider =dataSnapshot.getValue(ServiceProviderInformation.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        mGoogleApiClient.disconnect();
    }

    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        switch (requestCode)
        {
            case 113:
            {
                if(grantResults.length>0 &&grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    if(mGoogleApiClient==null)
                    {
                        connectMap();
                    }
                    mMap.setMyLocationEnabled(true);
                }else
                    Toast.makeText(this,"Permissin Denied",Toast.LENGTH_SHORT).show();

                return;
            }
        }
    }



}
