package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.EDIT_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.REMOVE_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.Loan;

public class LoanManagementActionActivity extends AppCompatActivity {
    private final static String TAG = "LoanManagementActionActivity";
    private FirebaseAuth authProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_management_action);

        authProfile = FirebaseAuth.getInstance();
        // set title
        getSupportActionBar().setTitle("Loan Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Loan loanItem = null;
        loanItem = (Loan) getIntent().getSerializableExtra(VIEW_ACTION);
        if (loanItem != null) {
            viewLoanItem(loanItem);
        } else {
            loanItem = (Loan) getIntent().getSerializableExtra(EDIT_ACTION);
            if (loanItem != null) {
                editLoanItem(loanItem);
            }
        }

    }

    private void editLoanItem(Loan loanItem) {
        Toast.makeText(this, "Editable Loan Item: "+loanItem.getLoanId(), Toast.LENGTH_SHORT).show();
    }

    private void viewLoanItem(Loan loanItem) {
        Toast.makeText(this, "View only Loan Item: "+loanItem.getLoanId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem loanTypeItemList) {
        int id = loanTypeItemList.getItemId();

        if (id == android.R.id.home) {
            setResult(RESULT_OK);
            this.finish();
        }

        return super.onOptionsItemSelected(loanTypeItemList);
    }
}