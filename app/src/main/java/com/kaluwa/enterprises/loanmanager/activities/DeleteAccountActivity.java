package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;

public class DeleteAccountActivity extends AppCompatActivity {

    private static final String TAG = "DeleteAccountActivity";
    private FirebaseAuth authProfile;
    private EditText etCurrentPwd;
    private TextView tvAuthenticateStatus;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // set title
        getSupportActionBar().setTitle("Account Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        etCurrentPwd = findViewById(R.id.et_da_pwd);
        tvAuthenticateStatus = findViewById(R.id.da_tv_authenticate_status);
        progressBar = findViewById(R.id.da_progressBar);

        // authenticate
        Button btnAuthenticate = findViewById(R.id.da_btn_authenticate);
        Button btnDelProfile = findViewById(R.id.da_btn_del_profile);
        btnAuthenticate.setOnClickListener(v -> {
            // validate
            String textCurrentPwd = etCurrentPwd.getText().toString();
            if (TextUtils.isEmpty(textCurrentPwd)) {
                etCurrentPwd.setError("Current password is required.");
                etCurrentPwd.requestFocus();
                Toast.makeText(this, "Please enter current password.", Toast.LENGTH_LONG).show();
            } else {
                // authenticate && change status
                progressBar.setVisibility(View.VISIBLE);

                // ReAuthenticate User now
                AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), textCurrentPwd);

                firebaseUser.reauthenticate(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // disable buttons and edits
                        etCurrentPwd.setEnabled(false);
                        btnAuthenticate.setEnabled(false);

                        // setting tv
                        tvAuthenticateStatus.setText("Your profile is authenticated/verified now.");
                        tvAuthenticateStatus.setTextColor(getResources().getColor(R.color.green));

                        // enable change password section
                        btnDelProfile.setEnabled(true);
                        btnDelProfile.setBackgroundColor(getResources().getColor(R.color.red));

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Password has been verified. You can continue now.", Toast.LENGTH_LONG).show();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            Toast.makeText(this, "Please enter your current password!", Toast.LENGTH_LONG).show();
                            etCurrentPwd.setError("Invalid credentials. Please check and try again.");
                            etCurrentPwd.requestFocus();
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        
        btnDelProfile.setOnClickListener(v -> {
            showAlertDialog(firebaseUser);
        });
    }

    private void showAlertDialog(FirebaseUser firebaseUser) {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete User and Related Data?");
        builder.setMessage("Do you really want to delete your profile and related data? This action is irreversible!");

        // Delete user if user clicks/taps Continue button.
        builder.setPositiveButton("Continue", (dialog, which) -> {
            deleteUser(firebaseUser);
        });

        // Return to User Profile Activity if User clicks/taps Cancel
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();
        });

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Change the continue button color
        alertDialog.setOnShowListener(dialog -> {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
        });

        // Show Alert
        alertDialog.show();
    }

    private void deleteUser(FirebaseUser firebaseUser) {
        progressBar.setVisibility(View.VISIBLE);
        // delete user related data including profile picture
        deleteUserData(firebaseUser);
        firebaseUser.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                authProfile.signOut();
                Toast.makeText(this, "User has been deleted!", Toast.LENGTH_LONG).show();
                // Open main activity after successful deletation
                Intent intent = new Intent(this, MainActivity.class);
                // To prevent user from returning back to delete profile activity on pressing back button.
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                try {
                    throw task.getException();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void deleteUserData(FirebaseUser firebaseUser) {
        // Delete Profile Picture
        if (firebaseUser.getPhotoUrl() != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
            storageReference.delete().addOnSuccessListener(unused -> {
                Log.d(TAG, "OnSuccess: "+firebaseUser.getUid()+" user's photo deleted.");
            }).addOnFailureListener(e -> {
                Log.d(TAG, "OnFailure: "+e.getMessage());
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            });
        }

        // Delete data from Realtime Database
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference(USER_REFERENCE);
        databaseReference.child(firebaseUser.getUid()).removeValue().addOnSuccessListener(unused -> {
            Log.d(TAG, "OnSuccess: "+firebaseUser.getUid()+" user's data deleted.");
        }).addOnFailureListener(e -> {
            Log.d(TAG, "OnFailure: "+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // Creating ActionBar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.menu_dashboard) {
            // open dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_my_profile) {
            // open profile
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_change_pwd) {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_account) {
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
        } else {
            // logout
            authProfile.signOut();
            Toast.makeText(this, "User successfully logout.", Toast.LENGTH_LONG).show();
            // Open main activity after successful registration
            Intent intent = new Intent(this, MainActivity.class);
            // To prevent user from returning back to register on pressing back button.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void swipeToRefresh() {
        // Look up for the swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup Refresh Listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            // Code to refresh goes here. Make sure to call swipeContainer.setRefresh(false) once the refreshed.
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }
}