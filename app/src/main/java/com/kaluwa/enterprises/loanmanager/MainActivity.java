package com.kaluwa.enterprises.loanmanager;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.USER_TYPE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.activities.DashboardActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoginActivity;
import com.kaluwa.enterprises.loanmanager.activities.RegisterActivity;
import com.kaluwa.enterprises.loanmanager.models.User;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authProfile = FirebaseAuth.getInstance();
        progressBar = findViewById(R.id.main_progressbar);
        // set the title
        getSupportActionBar().setTitle("Loan Manager");

        // open login activity
        Button btnLogin = findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // open register activity
        TextView btnRegister = findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Check if User is already logged in. In such case, Redirect user to dashboard
    @Override
    protected void onStart() {
        super.onStart();
        if (authProfile.getCurrentUser() != null) {
            progressBar.setVisibility(View.VISIBLE);
            FirebaseUser firebaseUser = authProfile.getCurrentUser();
            FirebaseDatabase.getInstance().getReference(USER_REFERENCE).child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            Toast.makeText(MainActivity.this, "Already Logged In!", Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                            // Open Dashboard
                            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                            // here handle to load which data
                            intent.putExtra(USER_TYPE, user.getUserType());
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressBar.setVisibility(View.GONE);
                            Log.e(TAG, error.getMessage());
                            Toast.makeText(MainActivity.this, "Something went wrong, Please register again.", Toast.LENGTH_LONG).show();
                        }
                    });


        }
    }
}