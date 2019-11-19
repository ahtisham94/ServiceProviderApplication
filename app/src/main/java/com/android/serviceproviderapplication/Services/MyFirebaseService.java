package com.android.serviceproviderapplication.Services;

import android.util.Log;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Model.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;



public class MyFirebaseService extends FirebaseInstanceIdService {


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        Log.d("Token is",refreshedToken.toString());
        updateTokenService(refreshedToken);
    }

    private void updateTokenService(String refreshedToken) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference(Common.tokens_tbl);
        Token token=new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null)//if already login then just refresh tokens
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }
}
