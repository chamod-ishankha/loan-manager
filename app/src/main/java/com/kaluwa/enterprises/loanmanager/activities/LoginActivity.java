package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.USER_TYPE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.User;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivityException";
    private EditText etLogEmail, etLogPassword;
    private ProgressBar logProgressBar;
    private FirebaseAuth authProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // set title
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // make default toast
        Toast.makeText(this, "You can login now", Toast.LENGTH_LONG).show();

        // assign viewsByIds
        assignViewsById();

        authProfile = FirebaseAuth.getInstance();

        // show hide password using eye ico
        ImageView logShowHidePwdIco = findViewById(R.id.log_show_hide_pwd);
        logShowHidePwdIco.setImageResource(R.drawable.hide_pwd_30);
        logShowHidePwdIco.setOnClickListener(v -> {
            if (etLogPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                // if password is visible then Hide it
                etLogPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                // Change Icon
                logShowHidePwdIco.setImageResource(R.drawable.hide_pwd_30);
            } else {
                // if password is visible then Hide it
                etLogPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                // Change Icon
                logShowHidePwdIco.setImageResource(R.drawable.show_pwd_30);
            }
        });

        // login user
        Button btnLogin = findViewById(R.id.log_btn_login);
        btnLogin.setOnClickListener(v -> {
            String textEmail = etLogEmail.getText().toString();
            String textPassword = etLogPassword.getText().toString();

            if (TextUtils.isEmpty(textEmail)) {
                Toast.makeText(this, "Please enter your email address!", Toast.LENGTH_LONG).show();
                etLogEmail.setError("Email address is required.");
                etLogEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address!", Toast.LENGTH_LONG).show();
                etLogEmail.setError("Invalid email address.");
                etLogEmail.requestFocus();
            } else if (TextUtils.isEmpty(textPassword)) {
                Toast.makeText(this, "Please enter new password!", Toast.LENGTH_LONG).show();
                etLogPassword.setError("Password is required.");
                etLogPassword.requestFocus();
            } else {
                logProgressBar.setVisibility(View.VISIBLE);
                loginUser(textEmail, textPassword);
            }
        });

        // password reset button
        TextView btnPwdReset = findViewById(R.id.log_reset_button);
        btnPwdReset.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // register button
        TextView btnRegister = findViewById(R.id.log_reg_button);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

    }

    private void loginUser(String textEmail, String textPassword) {
        authProfile.signInWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Get instance of the current User
                FirebaseUser firebaseUser = authProfile.getCurrentUser();

                // Check if email is verified before user can access their profile
                if (firebaseUser.isEmailVerified()) {
                    FirebaseDatabase.getInstance().getReference(USER_REFERENCE).child(firebaseUser.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    Toast.makeText(LoginActivity.this, "Logged in successful.", Toast.LENGTH_LONG).show();
                                    // Open Dashboard
                                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                                    // here handle to load which data
                                    intent.putExtra(USER_TYPE, user.getUserType());
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, error.getMessage());
                                    Toast.makeText(LoginActivity.this, "Something went wrong, Please register again.", Toast.LENGTH_LONG).show();
                                }
                            });
                } else {
                    firebaseUser.sendEmailVerification();
                    authProfile.signOut(); // Sign out user
                    showAlertDialog();
                }
            } else {
                try {
                    throw task.getException();
                } catch (FirebaseAuthInvalidUserException e) {
                    etLogEmail.setError("User does not exists or is no longer valid. Please register again.");
                    etLogEmail.requestFocus();
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    etLogEmail.setError("Invalid credentials. Kindly check and re-enter.");
                    etLogEmail.requestFocus();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, "User login failed, " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            logProgressBar.setVisibility(View.GONE);
        });
    }

    private void showAlertDialog() {
        // Setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email now. You can not login without email verification.");

        // Open email apps if user clicks/taps Continue button.
        builder.setPositiveButton("Continue", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_APP_EMAIL);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // To email app in new window and not our app
            startActivity(intent);
        });

        // Create the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Show Alert
        alertDialog.show();
    }

    private void assignViewsById() {
        etLogEmail = findViewById(R.id.et_log_email);
        etLogPassword = findViewById(R.id.et_log_password);
        logProgressBar = findViewById(R.id.log_progressBar);
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