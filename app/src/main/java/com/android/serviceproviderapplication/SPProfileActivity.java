package com.android.serviceproviderapplication;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.serviceproviderapplication.Model.ServiceProviderInformation;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SPProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CHOOSE_IMAGE = 101;

    ImageView profileImage, editIcon, drawerImage;
    TextView drawerUserName, drawerUserEmail ,drawerRatings,textViewChangePassword;
    EditText editTextName, editTextEmail, editTextPhone, editTextAddress, editTextProfession, editTextExperience;
    Spinner spinnerProfession, spinnerExperience;
    Button buttonSave;
    Uri uriProfileImage;
    String profileImageUrl;

    FirebaseAuth mAuth;
    ProgressDialog progress;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Toolbar mToolbar;

    private DatabaseReference databaseReference;
    private FirebaseUser user;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spprofile);

        mAuth = FirebaseAuth.getInstance();

        profileImage = (ImageView) findViewById(R.id.profile_image);
        editTextName = (EditText) findViewById(R.id.editText_name);
        editTextEmail = (EditText) findViewById(R.id.editText_email);
        editTextPhone = (EditText) findViewById(R.id.editText_phone);
        editTextAddress = (EditText) findViewById(R.id.editText_address);
        editTextProfession = (EditText) findViewById(R.id.editText_profession);
        editTextExperience = (EditText) findViewById(R.id.editText_experience);
        spinnerProfession = (Spinner) findViewById(R.id.spinner_profession);
        spinnerExperience = (Spinner) findViewById(R.id.spinner_experience);
        editIcon = (ImageView) findViewById(R.id.editIcon);
        buttonSave = (Button) findViewById(R.id.button_save);
        textViewChangePassword = (TextView) findViewById(R.id.textView_change_password);

        buttonSave.setEnabled(false);
        profileImage.setEnabled(false);
        editTextName.setEnabled(false);
        editTextEmail.setEnabled(false);
        editTextPhone.setEnabled(false);
        editTextAddress.setEnabled(false);
        editTextProfession.setEnabled(false);
        editTextExperience.setEnabled(false);
//        spinnerProfession.setEnabled(false);
//        spinnerExperience.setEnabled(false);
        spinnerProfession.setVisibility(View.GONE);
        spinnerExperience.setVisibility(View.GONE);


        mToolbar = (Toolbar) findViewById(R.id.navigation_actionbar_layover);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Profile");

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View navHeaderView = navigationView.getHeaderView(0);
        drawerImage = (ImageView) navHeaderView.findViewById(R.id.imageView);
        drawerUserName = (TextView) navHeaderView.findViewById(R.id.textView_name);
        drawerUserEmail = (TextView) navHeaderView.findViewById(R.id.textView_email);
        drawerRatings = (TextView) navHeaderView.findViewById(R.id.textView_Ratings);


        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSave.setEnabled(true);
                profileImage.setEnabled(true);
                editTextName.setEnabled(true);
                editTextEmail.setEnabled(true);
                editTextPhone.setEnabled(true);
                editTextAddress.setEnabled(true);
                editTextProfession.setVisibility(View.GONE);
                editTextExperience.setVisibility(View.GONE);
                spinnerProfession.setVisibility(View.VISIBLE);
                spinnerExperience.setVisibility(View.VISIBLE);


            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });

        textViewChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChangePasswordDialog();
            }
        });

        progress = new ProgressDialog(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadUserInformation();
        firebaseStorage=FirebaseStorage.getInstance();
        storageReference=firebaseStorage.getReference();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void loadUserInformation() {

        user = mAuth.getCurrentUser();

//        if (user.getPhotoUrl() != null) {
//            Glide.with(this)
//                    .load(user.getPhotoUrl().toString())
//                    .into(profileImage);
//        }
//        if (user.getDisplayName() != null) {
//            editTextName.setText(user.getDisplayName());
//        }
//
//        if (user.getPhotoUrl() != null) {
//            Glide.with(this)
//                    .load(user.getPhotoUrl().toString())
//                    .into(drawerImage);
//
//        }
//        if (user.getDisplayName() != null) {
//            drawerUserName.setText(user.getDisplayName());
//        }
//        if (user.getEmail() != null) {
//            drawerUserEmail.setText(user.getEmail());
//        }
//
//
//        databaseReference=FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid());
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/userName");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                ServiceProviderInformation spInfo = dataSnapshot.getValue(ServiceProviderInformation.class);
//                drawerUserName.setText(spInfo.getSpName());
//                drawerRatings.setText(spInfo.getSpRatings());
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spName");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/userName");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextName.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
////
////
////
////
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spEmail");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/email");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextEmail.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
////
////
////
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spPhoneNo");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/phoneNo");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextPhone.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spAddress");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/phoneNo");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextAddress.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spProfession");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/phoneNo");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextProfession.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid() + "/spExperience");
////            databaseReference = FirebaseDatabase.getInstance().getReference("Users"+ user.getUid() + "/phoneNo");
////
//        databaseReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                String value = dataSnapshot.getValue().toString();
//                editTextExperience.setText(value);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Service Providers").child(user.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ServiceProviderInformation spInformation = dataSnapshot.getValue(ServiceProviderInformation.class);

                if (spInformation.getSpName() != null) {
                    editTextName.setText(spInformation.getSpName());
                    drawerUserName.setText(spInformation.getSpName());
                }

                if (spInformation.getSpEmail() != null) {
                    editTextEmail.setText(spInformation.getSpEmail());
                    drawerUserEmail.setText(spInformation.getSpEmail());
                }

                if (spInformation.getSpRatings()!=null) {
                    drawerRatings.setText(spInformation.getSpRatings());
                }

                if (spInformation.getSpPhoneNo() != null) {
                    editTextPhone.setText(spInformation.getSpPhoneNo());
                }


                if (spInformation.getSpAddress()!=null) {
                    editTextAddress.setText(spInformation.getSpAddress());
                }

                if (spInformation.getSpProfession()!=null) {
                    editTextProfession.setText(spInformation.getSpProfession());
                }

                if (spInformation.getSpExperience()!=null) {
                    editTextExperience.setText(spInformation.getSpExperience());
                }

                if (spInformation.getSpPhotoUrl()!=null&&!TextUtils.isEmpty(spInformation.getSpPhotoUrl())) {

                    Picasso.with(SPProfileActivity.this)
                            .load(spInformation.getSpPhotoUrl().toString())
                            .into(profileImage);
                }

                if (spInformation.getSpPhotoUrl()!=null&&!TextUtils.isEmpty(spInformation.getSpPhotoUrl())) {
                    Picasso.with(SPProfileActivity.this)
                            .load(spInformation.getSpPhotoUrl().toString())
                            .into(drawerImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                profileImage.setImageBitmap(bitmap);

                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {
        if (uriProfileImage != null) {
            progress.setMessage("Uploading Image...");
            progress.show();

            String imageName = UUID.randomUUID().toString();

            final StorageReference imageFolder = storageReference.child("ProfileImages/" + imageName);
            imageFolder.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progress.dismiss();
                            Toast.makeText(SPProfileActivity.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Update this Uri to photoUrl of User
                                    Map<String, Object> profilePhotoUpdate = new HashMap<>();
                                    profilePhotoUpdate.put("photoUrl", uri.toString());
                                    DatabaseReference userInformation = FirebaseDatabase.getInstance().getReference("Service Providers");
                                    userInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .updateChildren(profilePhotoUpdate)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful())
                                                        Toast.makeText(SPProfileActivity.this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                                                    else
                                                        Toast.makeText(SPProfileActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                            });
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double uploadProgress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progress.setMessage("Uploaded" + uploadProgress + "%");

                }
            });
        }
    }


    private void saveUserInformation() {


        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String address = editTextAddress.getText().toString().trim();
        String profession = spinnerProfession.getSelectedItem().toString().trim();
        String experience = spinnerExperience.getSelectedItem().toString().trim();

        if (name.isEmpty()) {
            editTextName.setError("Name required");
            editTextName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email required");
            editTextEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            editTextPhone.setError("Phone# required");
            editTextPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            editTextAddress.setError("Address required");
            editTextAddress.requestFocus();
            return;
        }

//        if (profession.isEmpty()) {
//            editTextProfession.setError("Profession required");
//            editTextProfession.requestFocus();
//            return;
//        }
//
//        if (experience.isEmpty()) {
//            editTextExperience.setError("Phone No. is required");
//            editTextExperience.requestFocus();
//            return;
//        }

        if (spinnerProfession.getSelectedItem().toString().trim().equals("Profession")) {
            TextView errorText = (TextView) spinnerProfession.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Please Select a Profession");
            return;

        }

        if (spinnerExperience.getSelectedItem().toString().trim().equals("Experience")) {
            TextView errorText = (TextView) spinnerExperience.getSelectedView();
            errorText.setError("");
            errorText.setTextColor(Color.RED);
            errorText.setText("Please Select your Experience");
            return;

        }

        progress.setMessage("Updating Information...");
        progress.show();
        FirebaseUser user = mAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Service Providers");

        Map<String,Object> spInfoUpdate= new HashMap<>();
        spInfoUpdate.put("spName",name);
        spInfoUpdate.put("spEmail",email);
        spInfoUpdate.put("spPhoneNo",phone);
        spInfoUpdate.put("spAddress",address);
        spInfoUpdate.put("spProfession",profession);
        spInfoUpdate.put("spExperience",experience);

//        ServiceProviderInformation spInformation = new ServiceProviderInformation();
//        spInformation.setSpName(name);
//        spInformation.setSpEmail(email);
//        spInformation.setSpPhoneNo(phone);
//        spInformation.setSpAddress(address);
//        spInformation.setSpProfession(profession);
//        spInformation.setSpExperience(experience);

        databaseReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(spInfoUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progress.dismiss();
                        Toast.makeText(SPProfileActivity.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progress.dismiss();
                Toast.makeText(SPProfileActivity.this, "Unsuccessful", Toast.LENGTH_SHORT).show();
                return;
            }
        });


//        if (user != null && profileImageUrl != null) {
//            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
//                    .setDisplayName(displayName)
//                    .setPhotoUri(Uri.parse(profileImageUrl))
//                    .build();

//        if (user != null) {
//            if (user.getPhotoUrl() == null) {
//                if (profileImageUrl != null) {
//                    UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
//                            .setDisplayName(name)
//                            .setPhotoUri(Uri.parse(profileImageUrl))
//                            .build();
//
//
//                    user.updateProfile(profile)
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(SPProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
//
//                                    }
//                                }
//                            });
//                }
//            }
//        }


        finish();
        startActivity(getIntent());


    }

    private void showChangePasswordDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Change Password");
        alertDialog.setMessage("Please change your Password here");
        LayoutInflater inflater = LayoutInflater.from(this);
        View change_password_layout = inflater.inflate(R.layout.layout_change_password,null);
        final EditText editTextPassword =(EditText) change_password_layout.findViewById(R.id.editText_password);
        final EditText editTextNewPassword = (EditText) change_password_layout.findViewById(R.id.editText_new_password);
        final EditText editTextConfirmPassword = (EditText) change_password_layout.findViewById(R.id.editText_confirm_password);

        alertDialog.setView(change_password_layout);
        alertDialog.setPositiveButton("CHANGE PASSWORD",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int i) {


                if (editTextPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SPProfileActivity.this, "Old Password field cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editTextNewPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SPProfileActivity.this, "New Password field cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editTextNewPassword.getText().toString().length() < 6) {
                    Toast.makeText(SPProfileActivity.this, "Minimum length of password should be 6", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (editTextConfirmPassword.getText().toString().isEmpty()) {
                    Toast.makeText(SPProfileActivity.this, "Confirm Password field cannot be Empty", Toast.LENGTH_SHORT).show();
                    return;
                }


                progress.setMessage("Changing Password...");
                progress.show();
                if (editTextNewPassword.getText().toString().equals(editTextConfirmPassword.getText().toString()))
                {
                    String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    //Get Auth credential from the user for Re-Authentication
                    AuthCredential credential = EmailAuthProvider.getCredential(email,editTextPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(editTextConfirmPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful())
                                                        {
                                                            //Update Service Provider Information
                                                            Map<String,Object> updatePassword=new HashMap<>();
                                                            updatePassword.put("password",editTextConfirmPassword.getText().toString());
                                                            DatabaseReference serviceProviderInformation=FirebaseDatabase.getInstance().getReference("Service Providers");
                                                                    serviceProviderInformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                                    .updateChildren(updatePassword)
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful())
                                                                            {
                                                                                progress.dismiss();
                                                                                Toast.makeText(SPProfileActivity.this, "Password changed Successfully!", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            else
                                                                            {
                                                                                Toast.makeText(SPProfileActivity.this, "Password change Unsuccessful", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                        }
                                                        else
                                                        {
                                                            Toast.makeText(SPProfileActivity.this, "Password Doesn't Change", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                    }

                                    else
                                    {
                                        progress.dismiss();
                                        Toast.makeText(SPProfileActivity.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
                else
                {
                    progress.dismiss();
                    Toast.makeText(SPProfileActivity.this, "Password Doesn't Match", Toast.LENGTH_SHORT).show();
                }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(SPProfileActivity.this, SPLocationActivity.class);
            startActivity(intent);


        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(SPProfileActivity.this, SPProfileActivity.class);
            startActivity(intent);


        }  else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            Intent intent=new Intent(this,SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


        }  else if (id == R.id.nav_rate_us) {
            Intent intent=new Intent(SPProfileActivity.this,ApplicationRatingActivity.class);
            startActivity(intent);

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}