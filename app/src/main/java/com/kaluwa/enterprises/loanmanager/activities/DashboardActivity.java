package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.DASHBOARD_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.kaluwa.enterprises.loanmanager.models.Dashboard;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private final static String TAG = "DashboardActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView dbRecyler;
    private RVDashboardAdapter rvDBAdapter;
    private ProgressBar progressBar;
    private boolean isLoading = false;
    private String key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        authProfile = FirebaseAuth.getInstance();

        // set title
        getSupportActionBar().setTitle("Dashboard");
        swipeToRefresh();

        // assign
        progressBar = findViewById(R.id.db_progressBar);
        dbRecyler = findViewById(R.id.db_rv);
        dbRecyler.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        dbRecyler.setLayoutManager(llManager);

        // recycler adapter
        rvDBAdapter = new RVDashboardAdapter(this);

        // set adapter to recycler view
        dbRecyler.setAdapter(rvDBAdapter);

        // load data from database
        loadData();

        // scroll loading
        dbRecyler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItem = linearLayoutManager.getItemCount();
                int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (totalItem < lastVisible + 2) {
                    if (!isLoading) {
                        isLoading = true;
                        loadData();
                    }
                }
            }
        });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        // Extracting dashboard reference from database
        Query dashboardQuery = null;
        if (key == null) {
            dashboardQuery = FirebaseDatabase.getInstance().getReference(DASHBOARD_REFERENCE).orderByKey().limitToFirst(2);
        } else {
            dashboardQuery = FirebaseDatabase.getInstance().getReference(DASHBOARD_REFERENCE).orderByKey().startAfter(key).limitToFirst(2);
        }

        dashboardQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Dashboard> dbDataList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Dashboard dbData = data.getValue(Dashboard.class);
                    dbDataList.add(dbData);
                    key = data.getKey();
                }

                rvDBAdapter.setItems(dbDataList);
                rvDBAdapter.notifyDataSetChanged();
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getDetails());
                Toast.makeText(DashboardActivity.this, "Data retriving error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }
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

        if (id == R.id.menu_dashboard) {
            // open dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
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