package com.kaluwa.enterprises.loanmanager.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // set title
        getSupportActionBar().setTitle("Password Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etEmail = findViewById(R.id.et_fp_email);
        progressBar = findViewById(R.id.fp_progressBar);

        Button btnResetPwd = findViewById(R.id.fp_btn_reset_pwd);
        btnResetPwd.setOnClickListener(v -> {

            String textEmail = etEmail.getText().toString();

            // validate
            if (TextUtils.isEmpty(textEmail)) {
                etEmail.setError("Email address is required.");
                etEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                etEmail.setError("Invalid email address.");
                etEmail.requestFocus();
            } else {
                // send password reset link
                progressBar.setVisibility(View.VISIBLE);
                resetPassword(textEmail);
            }

        });

    }

    private void resetPassword(String email) {
        authProfile = FirebaseAuth.getInstance();
        authProfile.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Email sent! Please check your inbox.", Toast.LENGTH_SHORT).show();
                // Open main activity after successful registration
                Intent intent = new Intent(this, MainActivity.class);
                // To prevent user from returning back to register on pressing back button.
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                    etEmail.setError("User does not exists or is no longer valid. Please register again.");
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}