package com.android.serviceproviderapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.serviceproviderapplication.Common.Common;
import com.android.serviceproviderapplication.Model.ServiceProviderInformation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {


    FirebaseAuth mAuth;
    EditText editTextEmail, editTextPassword;

    ProgressBar progressBar;
    ProgressDialog progress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPassword = (EditText) findViewById(R.id.editText_password);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.textView_clickhere).setOnClickListener(this);
        findViewById(R.id.button_login).setOnClickListener(this);
        findViewById(R.id.textView_forgotPassword).setOnClickListener(this);

        progress = new ProgressDialog(this);

        }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            progress.setMessage("Loading data...");
            progress.show();
            FirebaseDatabase.getInstance().getReference(Common.service_providers_tbl)
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Common.currentServiceProvider = dataSnapshot.getValue(ServiceProviderInformation.class);
                            progress.dismiss();
                            finish();
                            startActivity(new Intent(getApplicationContext(), SPLocationActivity.class));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.textView_clickhere:
                finish();
                startActivity(new Intent(this, SignUpActivity.class));
                break;

            case R.id.button_login:
                userLogin();
                break;

            case R.id.textView_forgotPassword:
                dialogForgotPassword();
                break;
        }

    }

    private void dialogForgotPassword() {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle("Forgot Password");
            alertDialog.setMessage("Please enter your Email Address");
            LayoutInflater inflater = LayoutInflater.from(this);
            View forgot_password_layout = inflater.inflate(R.layout.layout_forgot_password,null);
            final MaterialEditText editTextEmail = forgot_password_layout.findViewById(R.id.editText_Email);

            alertDialog.setView(forgot_password_layout);
            alertDialog.setPositiveButton("RESET",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    progress.setMessage("Sending Email...");
                    progress.show();
                    mAuth.sendPasswordResetEmail(editTextEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                progress.dismiss();
                                    Toast.makeText(SignInActivity.this, "Reset Password Link has Sent", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            alertDialog.setNegativeButton("CANCEL",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface,int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();


    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

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

        progress.setMessage("Signing In User...");
        progress.show();

        //   progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                //            progressBar.setVisibility(View.GONE);
                progress.dismiss();
                if (task.isSuccessful()) {

                        FirebaseDatabase.getInstance().getReference(Common.service_providers_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Common.currentServiceProvider =dataSnapshot.getValue(ServiceProviderInformation.class);
                                        finish();
                                        Intent intent = new Intent(SignInActivity.this,SPLocationActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                }
                else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}
