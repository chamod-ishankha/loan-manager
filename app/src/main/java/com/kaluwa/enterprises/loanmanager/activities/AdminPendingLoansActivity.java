package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.APPROVE_ACTIVITY;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.PENDING_ACTIVITY;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.UserTypes.NORMAL_USER;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.firebase.ui.database.SnapshotParser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.RVAdminLoanManagementAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.RVAdminPendingLoanAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVAdminLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVAdminPendingLoanViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;
import com.kaluwa.enterprises.loanmanager.models.User;

import java.util.Objects;

public class AdminPendingLoansActivity extends AppCompatActivity {
    private static final String TAG = "AdminPendingLoansActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;
    private FirebaseRecyclerAdapter<Loan, RVAdminPendingLoanViewHolder> adapter;
    private String userId;
    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_pending_loans);

        authProfile = FirebaseAuth.getInstance();

        // get action bundle
        String action = getString();

        // set title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();
        if (action.equals(PENDING_ACTIVITY)) {
            getSupportActionBar().setTitle("Pending Loans");
        } else {
            getSupportActionBar().setTitle("Approved Loans");
        }

        progressBar = findViewById(R.id.admin_pl_progressBar);
        RecyclerView recyclerView = findViewById(R.id.admin_pl_rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(llManager);

        try {
            loadData(action, query -> {
                // make option using query
                FirebaseRecyclerOptions<Loan> options = new FirebaseRecyclerOptions.Builder<Loan>()
                        .setQuery(query, new SnapshotParser<Loan>() {
                            @NonNull
                            @Override
                            public Loan parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return Objects.requireNonNull(snapshot.getValue(Loan.class));
                            }
                        })
                        .build();
                adapter = new RVAdminPendingLoanAdapter(progressBar, options, action, userId, AdminPendingLoansActivity.this);
                adapter.startListening();
                recyclerView.setAdapter(adapter);
            });
        } catch (Exception e) {
            Log.e(TAG, Objects.requireNonNull(e.getMessage()));
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Something went wrong: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData(String action, OnQueryCompleteListener listener) {
        progressBar.setVisibility(View.VISIBLE);
        Query query;
        if (action.equals(PENDING_ACTIVITY)) {
            query = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(userId).orderByChild("approved").equalTo(false);
        } else {
            query = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(userId).orderByChild("approved").equalTo(true);
        }
        listener.OnQueryComplete(query);
    }

    @NonNull
    private String getString() {
        String action = "";
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String[] data = bundle.getStringArray("Object");
            if (data != null) {
                if (data[0] != null && data[1].equals(PENDING_ACTIVITY)) {
                    action = PENDING_ACTIVITY;
                    userId = data[0];
                } else if (data[0] != null && data[1].equals(APPROVE_ACTIVITY)) {
                    action = APPROVE_ACTIVITY;
                    userId = data[0];
                }
            }
        }
        return action;
    }

    private interface OnQueryCompleteListener {
        void OnQueryComplete(Query query);
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

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (adapter != null) {
//            adapter.stopListening();
//        }
//    }
}