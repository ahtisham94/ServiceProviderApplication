package com.android.serviceproviderapplication;

import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Model.Rate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ApplicationRatingActivity extends AppCompatActivity {
    Button btn_Submit;
    MaterialRatingBar ratingBar;
    MaterialEditText editText_Comment;

    FirebaseDatabase database;
    DatabaseReference ratingDetailRef;
    DatabaseReference serviceProviderInfoRef;

    double ratingStars=0.0;
    String serviceProviderId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_rating);

        //Init Firebase
        database=FirebaseDatabase.getInstance();
        ratingDetailRef=database.getReference("Service Provider Application Rating Details");

        //Init View
        btn_Submit=(Button)findViewById(R.id.btn_Submit);
        ratingBar=(MaterialRatingBar) findViewById(R.id.ratingBar);
        editText_Comment=(MaterialEditText) findViewById(R.id.editText_Comment);

        //Event
        ratingBar.setOnRatingChangeListener(new MaterialRatingBar.OnRatingChangeListener() {
            @Override
            public void onRatingChanged(MaterialRatingBar ratingBar, float rating) {
                ratingStars=rating;
            }
        });

        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitRatingDetails(serviceProviderId);
            }
        });

    }

    private void submitRatingDetails(final String serviceProviderId) {
        final AlertDialog alertDialog=new SpotsDialog(this);
        alertDialog.show();

        Rate rate = new Rate();
        rate.setRates(String.valueOf(ratingStars));
        rate.setComments(editText_Comment.getText().toString());

        //Update new value to Firebase
        ratingDetailRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) //UserId of User who rate this Service Provider
                .push()
                .setValue(rate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        alertDialog.dismiss();
                        Toast.makeText(ApplicationRatingActivity.this, "Thankyou for your Feedback", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        alertDialog.dismiss();
                        Toast.makeText(ApplicationRatingActivity.this, "Feedback Failed to Submit", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
