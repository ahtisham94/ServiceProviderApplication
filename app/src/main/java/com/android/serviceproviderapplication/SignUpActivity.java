package com.android.serviceproviderapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.serviceproviderapplication.Model.ServiceProviderInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressBar progressBar;
    EditText editTextUserName,editTextPhone,editTextEmail, editTextPassword,editTextAddress;
    ProgressDialog progress;
    Spinner spinnerProfession,spinnerExperience;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        editTextUserName=(EditText) findViewById(R.id.editText_username);

        editTextPhone = (EditText) findViewById(R.id.editText_phoneno);

        editTextEmail = (EditText) findViewById(R.id.editText_email);

        editTextPassword = (EditText) findViewById(R.id.editText_password);

        editTextAddress = (EditText) findViewById(R.id.editText_address);

        spinnerProfession = (Spinner) findViewById(R.id.spinner_profession);

        spinnerExperience = (Spinner) findViewById(R.id.spinner_experience);

        progressBar = (ProgressBar) findViewById(R.id.progressbar);



        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.button_signup).setOnClickListener(this);
        findViewById(R.id.textView_clickhere).setOnClickListener(this);
        progress=new ProgressDialog(this);

        databaseReference= FirebaseDatabase.getInstance().getReference("Service Providers");
    }


    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String userName = editTextUserName.getText().toString().trim();
        final String phoneNo = editTextPhone.getText().toString().trim();
        final String address = editTextAddress.getText().toString().trim();
        final String profession=spinnerProfession.getSelectedItem().toString().trim();
        final String experience=spinnerExperience.getSelectedItem().toString().trim();


        if (userName.isEmpty()) {
            editTextUserName.setError("User Name is required");
            editTextUserName.requestFocus();
            return;
        }

        if (phoneNo.isEmpty()) {
            editTextPhone.setError("Phone No. is required");
            editTextPhone.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Minimum lenght of password should be 6");
            editTextPassword.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Phone No. is required");
            editTextAddress.requestFocus();
            return;
        }

        if (spinnerProfession.getSelectedItem().toString().trim().equals("Profession"))
        {
            TextView errorText = (TextView)spinnerProfession.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Please Select a Profession");
            return;

        }

        if (spinnerExperience.getSelectedItem().toString().trim().equals("Experience"))
        {
            TextView errorText = (TextView)spinnerExperience.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Please Select your Experience");
            return;

        }




        progress.setMessage("Signing Up User...");
        progress.show();
        //    progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //             progressBar.setVisibility(View.GONE);
                progress.dismiss();
                if (task.isSuccessful()) {
//
                    ServiceProviderInformation user=new ServiceProviderInformation();
                    user.setSpName(userName);
                    user.setSpPhoneNo(phoneNo);
                    user.setSpEmail(email);
                    user.setSpPassword(password);
                    user.setSpProfession(profession);
                    user.setSpAddress(address);
                    user.setSpExperience(experience);
                    user.setSpPhotoUrl("");
                    user.setSpRatings("");




                    //User email to Key
                    databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(SignUpActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(SignUpActivity.this,SPLocationActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    });

                }
                else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });



    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.button_signup:
                registerUser();
                break;

            case R.id.textView_clickhere:
                finish();
                startActivity(new Intent(this, SignInActivity.class));
                break;
        }
    }
}
