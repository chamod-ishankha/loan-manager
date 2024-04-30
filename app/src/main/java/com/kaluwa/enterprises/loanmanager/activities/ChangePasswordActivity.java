package com.kaluwa.enterprises.loanmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseAuth authProfile;
    private EditText etCurrentPwd, etNewPwd, etConfPassword;
    private TextView tvAuthenticateStatus;
    private ProgressBar progressBar;
    private String userPwdCurr;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        // set title
        getSupportActionBar().setTitle("Password Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        assignViewByIds();

        // authenticate
        Button btnAuthenticate = findViewById(R.id.cp_btn_authenticate);
        Button btnChangePwd = findViewById(R.id.cp_btn_change_pwd);
        btnAuthenticate.setOnClickListener(v -> {
            // validate
            String textCurrentPwd = etCurrentPwd.getText().toString();
            userPwdCurr = textCurrentPwd;
            if (TextUtils.isEmpty(textCurrentPwd)) {
                etCurrentPwd.setError("Current password is required.");
                etCurrentPwd.requestFocus();
                Toast.makeText(this, "Please enter current password.", Toast.LENGTH_SHORT).show();
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
                        tvAuthenticateStatus.setText("You are authenticated/verified.\nChange your password now!");
                        tvAuthenticateStatus.setTextColor(getResources().getColor(R.color.green));

                        // enable change password section
                        etNewPwd.setEnabled(true);
                        etConfPassword.setEnabled(true);
                        btnChangePwd.setEnabled(true);
                        btnChangePwd.setBackgroundColor(getResources().getColor(R.color.green));

                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Password has been verified. Change password now.", Toast.LENGTH_LONG).show();
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

        // change password
        btnChangePwd.setOnClickListener(v -> {
            changePassword(firebaseUser);
        });

    }

    private void changePassword(FirebaseUser firebaseUser) {
        // validate
        String textNewPwd = etNewPwd.getText().toString();
        String textConfPwd = etConfPassword.getText().toString();
        if (TextUtils.isEmpty(textNewPwd)) {
            Toast.makeText(this, "Please enter new password.", Toast.LENGTH_LONG).show();
            etNewPwd.setError("New password is required.");
            etNewPwd.requestFocus();
        } else if (textNewPwd.length() < 6) {
            Toast.makeText(this, "Password should be at least 6 digits!", Toast.LENGTH_LONG).show();
            etNewPwd.setError("Password too weak.");
            etNewPwd.requestFocus();
        } else if (textNewPwd.equals(userPwdCurr)) {
            Toast.makeText(this, "Please enter new password!", Toast.LENGTH_LONG).show();
            etNewPwd.setError("Password is same as previous password.");
            etNewPwd.requestFocus();
        } else if (TextUtils.isEmpty(textConfPwd)) {
            Toast.makeText(this, "Please confirm your new password.", Toast.LENGTH_LONG).show();
            etConfPassword.setError("Password confirmation is required.");
            etConfPassword.requestFocus();
        } else if (!textConfPwd.equals(textNewPwd)) {
            Toast.makeText(this, "Passwords are not matched. Confirm your password!", Toast.LENGTH_LONG).show();
            etConfPassword.setError("Confirmation of password is required.");
            etConfPassword.requestFocus();
        } else {
            // change password
            progressBar.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(textNewPwd).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Password has been changed.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, UserProfileActivity.class);
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
    }

    private void assignViewByIds() {
        etCurrentPwd = findViewById(R.id.et_cp_pwd);
        etNewPwd = findViewById(R.id.et_cp_new_pwd);
        etConfPassword = findViewById(R.id.et_cp_conf_pwd);
        tvAuthenticateStatus = findViewById(R.id.cp_tv_authenticate_status);
        progressBar = findViewById(R.id.cp_progressBar);
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
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_delete_account) {
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            startActivity(intent);
        } else {
            // logout
            authProfile.signOut();
            Toast.makeText(this, "User successfully logout.", Toast.LENGTH_SHORT).show();
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