package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.REQUEST_CODE_ADD;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.RVDashboardAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.RVLoanManagementAdapter;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.util.ArrayList;

public class LoanManagementActivity extends AppCompatActivity {

    private final static String TAG = "LoanManagementActivity";
    private SwipeRefreshLayout swipeContainer;
    private FirebaseAuth authProfile;

    // buttons
    private FloatingActionButton fabAdd;

    // items
    private boolean isLoading = false;
    private String key = null;

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

        // recycler adapter
        RVLoanManagementAdapter rvLMAdapter = new RVLoanManagementAdapter(this);

        // set adapter to recycler view
        lmRecyler.setAdapter(rvLMAdapter);

        // load data from database
        loadData(progressBar, rvLMAdapter);

        lmRecyler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItems = linearLayoutManager.getItemCount();
                int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (totalItems < lastVisible + 2) {
                    if (!isLoading) {
                        isLoading = true;
                        loadData(progressBar, rvLMAdapter);
                    }
                }
            }
        });

        // clickers
        onClickers();
    }

    private void loadData(ProgressBar progressBar, RVLoanManagementAdapter rvLMAdapter) {
        progressBar.setVisibility(View.VISIBLE);
        String userId = authProfile.getCurrentUser().getUid();
        // extract loan reference from database
        Query loanQuery = null;
        if (key == null) {
            loanQuery = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(userId).orderByKey().limitToFirst(8);
        } else {
            loanQuery = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(userId).orderByKey().startAfter(key).limitToFirst(8);
        }

        loanQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Loan> loanList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Loan loanData = data.getValue(Loan.class);

                    // get loan type icon name
                    DatabaseReference loanTypeRef = FirebaseDatabase.getInstance().getReference().child(LOAN_TYPE_REFERENCE);
                    loanTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                Long id = childSnap.child("id").getValue(Long.class);
                                String loanTypeIcon = childSnap.child("ico").getValue(String.class);
                                String loanTypeName = childSnap.child("name").getValue(String.class);

                                assert id != null;
                                assert loanData != null;
                                if (id.toString().equals(String.valueOf(loanData.getLoanTypeId()))) {
                                    loanData.setLoanTypeIcon(loanTypeIcon);
                                    loanData.setLoanTypeName(loanTypeName);
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "LOAN_TYPE_REFERENCE_icon_name_getting_error: " + error.getMessage());
                        }
                    });

                    loanList.add(loanData);
                    key = data.getKey();
                }

                rvLMAdapter.setLoanList(loanList);
                rvLMAdapter.notifyDataSetChanged();
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getDetails());
                Toast.makeText(LoanManagementActivity.this, "Data retriving error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void onClickers() {
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddLoanActivity.class);
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
}