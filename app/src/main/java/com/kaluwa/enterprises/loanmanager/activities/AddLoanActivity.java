package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.LoanType;

import java.util.ArrayList;

public class AddLoanActivity extends AppCompatActivity {

    private final static String TAG = "AddLoanActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private String[] item = {};
    private AutoCompleteTextView tvAutoComplete;
    private ArrayAdapter<String> adapterItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);

        authProfile = FirebaseAuth.getInstance();
        // set title
        getSupportActionBar().setTitle("Loan Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        try {
            // assigners
            assign();
            loadLoanTypes();
            // Add click listener to the dropdown icon
            onClickers();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, "error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadLoanTypes() {
        Query loanTypesQuery = FirebaseDatabase.getInstance().getReference(LOAN_TYPE_REFERENCE).orderByKey();
        loanTypesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> loanTypeNames = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    LoanType loanType = data.getValue(LoanType.class);
                    if (loanType != null) {
                        loanTypeNames.add(loanType.getName());
                    }
                }

                item = loanTypeNames.toArray(new String[0]);
                // dropdown adapter setting
                adapterItem = new ArrayAdapter<String>(AddLoanActivity.this, R.layout.list_item_dd, item);
                tvAutoComplete.setAdapter(adapterItem);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getMessage());
                Toast.makeText(AddLoanActivity.this, "error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onClickers() {
        tvAutoComplete.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                tvAutoComplete.showDropDown();
                return true;
            }
            return false;
        });
        tvAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            Toast.makeText(this, "Item: " + selectedItem, Toast.LENGTH_SHORT).show();
            // Clear hint
            TextInputLayout tiLayout = findViewById(R.id.al_text_input_layout);
            tiLayout.setHint("");
        });
    }

    private void assign() {
        tvAutoComplete = findViewById(R.id.al_atc_loan_type);
        adapterItem = new ArrayAdapter<String>(this, R.layout.list_item_dd, item);
    }

    // Enable back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
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