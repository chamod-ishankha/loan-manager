package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.User;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private final String TAG = "RegisterActivityExceptions";
    private EditText etRegFirstName, etRegLastName, etRegEmail, etRegMobile, etRegDob, etRegPassword, etRegConfPassword;
    private RadioGroup rbgRegGender;
    private RadioButton rbRegGenderSelected;
    private ProgressBar regProgressBar;
    private DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // set title
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // make default toast
        Toast.makeText(this, "You can register now", Toast.LENGTH_LONG).show();

        // assign viewsByIds
        assignViewsById();

        Button btnRegister = findViewById(R.id.reg_btn_register);
        btnRegister.setOnClickListener(v -> {

            // find selected gender
            int selectedGenderId = rbgRegGender.getCheckedRadioButtonId();
            rbRegGenderSelected = findViewById(selectedGenderId);

            // obtain the entered data
            String textFirstName = etRegFirstName.getText().toString();
            String textLastName = etRegLastName.getText().toString();
            String textEmail = etRegEmail.getText().toString();
            String textMobile = etRegMobile.getText().toString();
            String textDob = etRegDob.getText().toString();
            String textPassword = etRegPassword.getText().toString();
            String textConfPassword = etRegConfPassword.getText().toString();
            String textGender;
            TextView tvGender = findViewById(R.id.tv_reg_gender);

            // validation
            if (TextUtils.isEmpty(textFirstName)) {
                Toast.makeText(this, "Please enter your first name!", Toast.LENGTH_LONG).show();
                etRegFirstName.setError("First name is required.");
                etRegFirstName.requestFocus();
            } else if (TextUtils.isEmpty(textLastName)) {
                Toast.makeText(this, "Please enter your last name!", Toast.LENGTH_LONG).show();
                etRegLastName.setError("Last name is required.");
                etRegLastName.requestFocus();
            } else if (TextUtils.isEmpty(textEmail)) {
                Toast.makeText(this, "Please enter your email address!", Toast.LENGTH_LONG).show();
                etRegEmail.setError("Email address is required.");
                etRegEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                Toast.makeText(this, "Please enter a valid email address!", Toast.LENGTH_LONG).show();
                etRegEmail.setError("Invalid email address.");
                etRegEmail.requestFocus();
            } else if (TextUtils.isEmpty(textMobile)) {
                Toast.makeText(this, "Please enter your mobile number!", Toast.LENGTH_LONG).show();
                etRegMobile.setError("Mobile number is required.");
                etRegMobile.requestFocus();
            } else if (textMobile.length() != 10) {
                Toast.makeText(this, "Please enter a valid mobile number!", Toast.LENGTH_LONG).show();
                etRegMobile.setError("Mobile No. should be 10 digits.");
                etRegMobile.requestFocus();
            } else if (TextUtils.isEmpty(textDob)) {
                Toast.makeText(this, "Please enter your date of birth!", Toast.LENGTH_LONG).show();
                etRegDob.setError("Date of birth is required.");
                etRegDob.requestFocus();
            } else if (rbgRegGender.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select your gender!", Toast.LENGTH_LONG).show();
                tvGender.setError("Gender is required.");
                tvGender.requestFocus();
            } else if (TextUtils.isEmpty(textPassword)) {
                Toast.makeText(this, "Please enter new password!", Toast.LENGTH_LONG).show();
                etRegPassword.setError("Password is required.");
                etRegPassword.requestFocus();
            } else if (textPassword.length() < 6) {
                Toast.makeText(this, "Password should be at least 6 digits!", Toast.LENGTH_LONG).show();
                etRegPassword.setError("Password too weak.");
                etRegPassword.requestFocus();
            } else if (TextUtils.isEmpty(textConfPassword)) {
                Toast.makeText(this, "Please confirm your password!", Toast.LENGTH_LONG).show();
                etRegConfPassword.setError("Password confirm is required.");
                etRegConfPassword.requestFocus();
            } else if (!textPassword.equals(textConfPassword)) {
                Toast.makeText(this, "Passwords are not matched. Confirm your password!", Toast.LENGTH_LONG).show();
                etRegConfPassword.setError("Confirmation of password is required.");
                etRegConfPassword.requestFocus();
                // clear entered confirmation password
                etRegConfPassword.clearComposingText();
            } else {
                textGender = rbRegGenderSelected.getText().toString();
                String titile = textGender.equalsIgnoreCase("male") ? "Mr. " : "Mrs. ";
                regProgressBar.setVisibility(View.VISIBLE);
                registerUser(textFirstName, textLastName, textEmail, textMobile, textDob, textGender, textPassword, titile, regProgressBar);
            }

        });

    }

    @SuppressLint("LongLogTag")
    private void registerUser(String textFirstName, String textLastName, String textEmail, String textMobile, String textDob, String textGender, String textPassword, String titile, ProgressBar progressBar) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        // create a user profile
        auth.createUserWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = auth.getCurrentUser();

                // update display name of User
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFirstName + " " + textLastName).build();
                firebaseUser.updateProfile(profileChangeRequest);

                // Save User Data into the firebase Realtime Database.
                User user = new User(textFirstName, textLastName, textEmail, textMobile, textDob, textGender, titile);

                // Extracting user reference from database for "Registered Users"
                DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference(USER_REFERENCE);
                // save user details in the database
                referenceProfile.child(firebaseUser.getUid()).setValue(user).addOnCompleteListener(detailsTask -> {
                    if (detailsTask.isSuccessful()) {
                        // success
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "User registered successfully. Please verify your email address and Login to the Loan Manager.", Toast.LENGTH_LONG).show();

                        // send verification email
                        firebaseUser.sendEmailVerification();

                        // signout
                        auth.signOut();

                        // Open main activity after successful registration
                        Intent intent = new Intent(this, MainActivity.class);
                        // To prevent user from returning back to register on pressing back button.
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw detailsTask.getException();
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "User registered failed, " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                try {
                    throw task.getException();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "User registered failed, " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void assignViewsById() {
        etRegFirstName = findViewById(R.id.et_reg_first_name);
        etRegLastName = findViewById(R.id.et_reg_last_name);
        etRegEmail = findViewById(R.id.et_reg_email);
        etRegMobile = findViewById(R.id.et_reg_mobile);
        etRegDob = findViewById(R.id.et_reg_dob);
        etRegPassword = findViewById(R.id.et_reg_password);
        etRegConfPassword = findViewById(R.id.et_reg_conf_password);
        regProgressBar = findViewById(R.id.reg_progressBar);

        // radio buttons for gender
        rbgRegGender = findViewById(R.id.rbg_reg_gender);
        rbgRegGender.clearCheck();

        // setting up date picker on EditText
        etRegDob.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            // Date Picker Dialog
            datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    etRegDob.setText(dayOfMonth + "/" + (month + 1) + "/" + year);
                }
            } , year, month, day);
            datePicker.show();
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