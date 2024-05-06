package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.ADD_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.REQUEST_CODE_ADD;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.RVLoanManagementAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.util.Objects;

public class LoanManagementActivity extends AppCompatActivity {

    private final static String TAG = "LoanManagementActivity";
    private SwipeRefreshLayout swipeContainer;
    private FirebaseAuth authProfile;
    private FirebaseRecyclerAdapter<Loan, RVLoanManagementViewHolder> rvLMAdapter;
    // buttons
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_management);

        authProfile = FirebaseAuth.getInstance();

        // set title
        getSupportActionBar().setTitle("Loan Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        // assign
        assign();
        ProgressBar progressBar = findViewById(R.id.lm_progressBar);
        RecyclerView lmRecyler = findViewById(R.id.lm_rv);
        lmRecyler.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        lmRecyler.setLayoutManager(llManager);

        try {
            rvLMAdapter = loadData(progressBar);
            // set adapter to recycler view
            lmRecyler.setAdapter(rvLMAdapter);
            // clickers
            onClickers();
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private FirebaseRecyclerAdapter<Loan, RVLoanManagementViewHolder> loadData(ProgressBar progressBar) {
        String userId = authProfile.getCurrentUser().getUid();
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        Query query = FirebaseDatabase.getInstance()
                .getReference(LOAN_REFERENCE)
                .child(userId)
                .orderByChild("approved")
                .equalTo(true);
        FirebaseRecyclerOptions<Loan> options = new FirebaseRecyclerOptions.Builder<Loan>()
                .setQuery(query, Loan.class)
                .build();

        return new RVLoanManagementAdapter(options, progressBar, this);
    }

    private void onClickers() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoanManagementActionActivity.class);
            intent.putExtra(ADD_ACTION, ADD_ACTION);
            startActivityForResult(intent, REQUEST_CODE_ADD);
        });
    }

    private void assign() {
        fabAdd = findViewById(R.id.lm_fab_add);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_ADD && resultCode == RESULT_OK) {
            // Refresh the list here
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (rvLMAdapter != null) {
            rvLMAdapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (rvLMAdapter != null) {
            rvLMAdapter.stopListening();
        }
        super.onDestroy();
    }
}