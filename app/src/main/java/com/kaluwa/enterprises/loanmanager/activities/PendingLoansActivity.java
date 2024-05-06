package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.RVLoanManagementAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.RVPendingLoanAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.util.Objects;

public class PendingLoansActivity extends AppCompatActivity {

    private final static String TAG = "PendingLoansActivity";
    private SwipeRefreshLayout swipeContainer;
    private FirebaseAuth authProfile;
    private FirebaseRecyclerAdapter<Loan, RVLoanManagementViewHolder> rvPLAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_loans);

        authProfile = FirebaseAuth.getInstance();

        // set title
        getSupportActionBar().setTitle("Pending Loans");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        // assign
        ProgressBar progressBar = findViewById(R.id.pl_progressBar);
        RecyclerView plRecyler = findViewById(R.id.pl_rv);
        plRecyler.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        plRecyler.setLayoutManager(llManager);

        try {
            rvPLAdapter = loadData(progressBar);
            // set adapter to recycler view
            plRecyler.setAdapter(rvPLAdapter);
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
                .equalTo(false);
        FirebaseRecyclerOptions<Loan> options = new FirebaseRecyclerOptions.Builder<Loan>()
                .setQuery(query, Loan.class)
                .build();

        return new RVPendingLoanAdapter(options, progressBar, this);
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
    protected void onStart() {
        super.onStart();
        if (rvPLAdapter != null) {
            rvPLAdapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (rvPLAdapter != null) {
            rvPLAdapter.stopListening();
        }
        super.onDestroy();
    }
}