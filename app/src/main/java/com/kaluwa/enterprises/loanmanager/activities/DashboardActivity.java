package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.DASHBOARD_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.applyColorToBackground;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.RVDashboardAdapter;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVDashboardViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Dashboard;

import java.util.Objects;

public class DashboardActivity extends AppCompatActivity {

    private final static String TAG = "DashboardActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private FirebaseRecyclerAdapter<Dashboard, RVDashboardViewHolder> rvDBAdapter;
    private ProgressBar progressBar;
    private String key = null;
    private int initialDashboardLimit = 5;

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
        RecyclerView dbRecycler = findViewById(R.id.db_rv);
        dbRecycler.setHasFixedSize(true);
        LinearLayoutManager llManager = new LinearLayoutManager(this);
        dbRecycler.setLayoutManager(llManager);

        try {
            rvDBAdapter = loadData();
            // set adapter to recycler view
            dbRecycler.setAdapter(rvDBAdapter);
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private FirebaseRecyclerAdapter<Dashboard, RVDashboardViewHolder> loadData() {
        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);

        Query query = FirebaseDatabase.getInstance().getReference(DASHBOARD_REFERENCE).orderByKey();
        FirebaseRecyclerOptions<Dashboard> options = new FirebaseRecyclerOptions.Builder<Dashboard>()
                .setQuery(query, new SnapshotParser<Dashboard>() {
                    @NonNull
                    @Override
                    public Dashboard parseSnapshot(@NonNull DataSnapshot snapshot) {
                        key = snapshot.getKey();
                        System.out.println("key: "+key);
                        return Objects.requireNonNull(snapshot.getValue(Dashboard.class));
                    }
                }).build();
        return new RVDashboardAdapter(options, progressBar, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (rvDBAdapter != null) {
            rvDBAdapter.startListening();
        }
    }

    @Override
    protected void onDestroy() {
        if (rvDBAdapter != null) {
            rvDBAdapter.stopListening();
        }
        super.onDestroy();
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