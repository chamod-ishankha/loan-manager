package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.setUpDatePicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.Loan;
import com.kaluwa.enterprises.loanmanager.models.LoanType;

import java.time.LocalDate;
import java.util.ArrayList;

public class AddLoanActivity extends AppCompatActivity {

    private final static String TAG = "AddLoanActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private Loan loan = new Loan();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);

        authProfile = FirebaseAuth.getInstance();
        // set title
        getSupportActionBar().setTitle("Loan Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        // edittext
        EditText etLoanAmt, etInterestRate, etTerms, etStDate, etDueDate, etInstallment, etAdditionalCharges, etDesp, etLenderInfo, etContactInfo;
        AutoCompleteTextView loanTypeTvAutoComplete, fopTvAutoComplete;
        Button btnAdd;
        ProgressBar progressBar;
        View overlay;

        etLoanAmt = findViewById(R.id.al_et_loan_amount);
        etInterestRate = findViewById(R.id.al_et_interest_rate);
        etTerms = findViewById(R.id.al_et_terms);
        etStDate = findViewById(R.id.al_et_start_date);
        etDueDate = findViewById(R.id.al_et_due_date);
        etInstallment = findViewById(R.id.al_et_installment_amount);
        etAdditionalCharges = findViewById(R.id.al_et_additional_fees);
        etDesp = findViewById(R.id.al_et_description);
        etLenderInfo = findViewById(R.id.al_et_lender_info);
        etContactInfo = findViewById(R.id.al_et_contact_info);
        loanTypeTvAutoComplete = findViewById(R.id.al_atc_loan_type);
        fopTvAutoComplete = findViewById(R.id.al_atc_fop);

        // related to process
        btnAdd = findViewById(R.id.al_btn_add);
        progressBar = findViewById(R.id.al_progressBar);
        overlay = findViewById(R.id.al_overlay);


        try {
            // run dropdowns
            loadLoanTypes(loanTypeTvAutoComplete);
            loadFrequencies(fopTvAutoComplete);
            loadDatePickers(etStDate, etDueDate);
            callProcess(btnAdd, progressBar, overlay, etLoanAmt, etInterestRate, etTerms, etStDate, etDueDate, etInstallment, etAdditionalCharges, etDesp, etLenderInfo, etContactInfo, loanTypeTvAutoComplete);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void callProcess(Button btnAdd, ProgressBar progressBar, View overlay, EditText etLoanAmt, EditText etInterestRate, EditText etTerms, EditText etStDate, EditText etDueDate, EditText etInstallment, EditText etAdditionalCharges, EditText etDesp, EditText etLenderInfo, EditText etContactInfo, AutoCompleteTextView loanTypeTvAutoComplete) {
        btnAdd.setOnClickListener(v -> {
            // validate
            String loanAmt, rate, installment, additionalCharges, terms, stDate, dueDate, desp, lenderInfo, contactInfo, loanTypeId, fop;

            loanAmt = etLoanAmt.getText().toString();
            rate = etInterestRate.getText().toString();
            installment = etInstallment.getText().toString();
            additionalCharges = etAdditionalCharges.getText().toString();
            terms = etTerms.getText().toString();
            stDate = etStDate.getText().toString();
            dueDate = etDueDate.getText().toString();
            desp = etDesp.getText().toString();
            lenderInfo = etLenderInfo.getText().toString();
            contactInfo = etContactInfo.getText().toString();
            loanTypeId = String.valueOf(loan.getLoanTypeId());
            fop = loan.getFop();

            if (loanTypeId.equals("0") || TextUtils.isEmpty(loanTypeId)) {
                loanTypeTvAutoComplete.setError("Loan type cannot be empty");
                loanTypeTvAutoComplete.requestFocus();
                Toast.makeText(this, "Loan type cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(loanAmt)) {
                loanTypeTvAutoComplete.setError(null);
                etLoanAmt.setError("Loan amount cannot be empty");
                etLoanAmt.requestFocus();
            } else if (TextUtils.isEmpty(rate)) {
                etInterestRate.setError("Interest rate cannot be empty");
                etInterestRate.requestFocus();
            } else if (TextUtils.isEmpty(terms)) {
                etTerms.setError("Duration/term cannot be empty");
                etTerms.requestFocus();
            } else if (TextUtils.isEmpty(stDate)) {
                etStDate.setError("Start date cannot be empty");
                etStDate.requestFocus();
                Toast.makeText(this, "Start date cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(dueDate)) {
                etStDate.setError(null);
                etDueDate.setError("Due date cannot be empty");
                etDueDate.requestFocus();
                Toast.makeText(this, "Due date cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(lenderInfo)) {
                etDueDate.setError(null);
                etLenderInfo.setError("Lender information cannot be empty");
                etLenderInfo.requestFocus();
            } else if (TextUtils.isEmpty(contactInfo)) {
                etContactInfo.setError("Contact number cannot be empty");
                etContactInfo.requestFocus();
            } else if (contactInfo.length() != 10) {
                etContactInfo.setError("Contact number should have 10 numbers.");
                etContactInfo.requestFocus();
            } else {
                double loanAmtDbl, rateDbl, installmentDbl = 0.00, additionalChargesDbl = 0.00;
                loanAmtDbl = Double.parseDouble(loanAmt);
                rateDbl = Double.parseDouble(rate);

                if (!TextUtils.isEmpty(additionalCharges)) {
                    additionalChargesDbl = Double.parseDouble(additionalCharges);
                }

                if (loanAmtDbl < 1000) {
                    etLoanAmt.setError("Loan amount must be greater than Rs. 1000.00");
                    etLoanAmt.requestFocus();
                } else if (rateDbl < 0.50) {
                    etInterestRate.setError("Interest rate must be greater than 0.50%");
                    etInterestRate.requestFocus();
                } else if (!TextUtils.isEmpty(installment)) {
                    installmentDbl = Double.parseDouble(installment);
                    if (installmentDbl < 100) {
                        etInstallment.setError("Installment amount must be greater than Rs. 100.00");
                        etInstallment.requestFocus();
                    }
                } else {
                    // set loan model
                    loan.setLoanTypeId(Integer.parseInt(loanTypeId));
                    loan.setLoanAmount(loanAmtDbl);
                    loan.setInterestRate(rateDbl);
                    loan.setTerms(Integer.parseInt(terms));
                    loan.setStartDate(stDate);
                    loan.setDueDate(dueDate);
                    loan.setInstallmentAmount(installmentDbl);
                    loan.setFop(fop);
                    loan.setAdditionalCharges(additionalChargesDbl);
                    loan.setDescription(desp);
                    loan.setLenderInfo(lenderInfo);
                    loan.setContactInfo(contactInfo);

                    // Loan reference
                    DatabaseReference loanReference = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE);
                    // generate unique key for each loan
                    String userId = authProfile.getCurrentUser().getUid();
                    loan.setLoanId(loanReference.child(userId).push().getKey());

                    // save the loan
                    // loading enabled
                    handleLoading(progressBar, overlay, true);
                    loanReference.child(userId).child(loan.getLoanId()).setValue(loan).addOnSuccessListener(unused -> {
                        // loading disabled
                        handleLoading(progressBar, overlay, false);
                        Toast.makeText(this, "Loan successfully added.", Toast.LENGTH_SHORT).show();
                        // refresh activity
                        startActivity(getIntent());
                        finish();
                        overridePendingTransition(0,0);
                    }).addOnFailureListener(e -> {
                        // loading disabled
                        handleLoading(progressBar, overlay, false);
                        Toast.makeText(this, "Error while adding your loan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }

        });
    }

    private void handleLoading(ProgressBar pg, View v, boolean status) {
        if (status) {
            pg.setVisibility(View.VISIBLE);
            v.setVisibility(View.VISIBLE);
            ScrollView scrollView = findViewById(R.id.scroll_view);
            scrollView.setOnTouchListener((v1, event) -> true);
        } else {
            pg.setVisibility(View.GONE);
            v.setVisibility(View.GONE);
            ScrollView scrollView = findViewById(R.id.scroll_view);
            scrollView.setOnTouchListener(null);
        }
    }

    private void loadDatePickers(EditText etStDate, EditText etDueDate) {
        setUpDatePicker(etStDate, this);
        setUpDatePicker(etDueDate, this);
    }

    private void loadFrequencies(AutoCompleteTextView fopTvAutoComplete) {
        String[] fopItemList = {
                "Weekly",
                "Bi-Weekly",
                "Monthly",
                "Quarterly",
                "Semi-Annually",
                "Annually"
        };

        ArrayAdapter<String> fopArrayAdapter = new ArrayAdapter<String>(this, R.layout.list_item_dd, fopItemList);
        fopTvAutoComplete.setAdapter(fopArrayAdapter);
        fopTvAutoComplete.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                fopTvAutoComplete.showDropDown();
                return true;
            }
            return false;
        });
        fopTvAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = parent.getItemAtPosition(position).toString();
            loan.setFop(selectedItem);
            // Clear hint
            TextInputLayout tiLayoutFop = findViewById(R.id.al_text_input_layout_fop);
            tiLayoutFop.setHint("");
        });
    }

    private ArrayList<LoanType> loanTypeItemList = new ArrayList<>();
    private ArrayAdapter<LoanType> loanTypeAdapterItem;

    private void loadLoanTypes(AutoCompleteTextView loanTypeTvAutoComplete) {
        loanTypeAdapterItem = new ArrayAdapter<LoanType>(this, R.layout.list_item_dd, loanTypeItemList);

        Query loanTypesQuery = FirebaseDatabase.getInstance().getReference(LOAN_TYPE_REFERENCE).orderByKey();
        loanTypesQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loanTypeItemList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    LoanType loanType = data.getValue(LoanType.class);
                    if (loanType != null) {
                        LoanType loanTypeItem = new LoanType(loanType.getId(), loanType.getName());
                        loanTypeItemList.add(loanTypeItem);
                    }
                }
                // dropdown adapter setting
                loanTypeAdapterItem.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println(error.getMessage());
                Toast.makeText(AddLoanActivity.this, "error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        loanTypeTvAutoComplete.setAdapter(loanTypeAdapterItem);

        loanTypeTvAutoComplete.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                loanTypeTvAutoComplete.showDropDown();
                return true;
            }
            return false;
        });
        loanTypeTvAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            LoanType selectedItem = (LoanType) parent.getItemAtPosition(position);
            loan.setLoanTypeId(selectedItem.getId());
            // Clear hint
            TextInputLayout tiLayout = findViewById(R.id.al_text_input_layout);
            tiLayout.setHint("");
        });
    }

    // Enable back button
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem loanTypeItemList) {
        int id = loanTypeItemList.getItemId();

        if (id == android.R.id.home) {
            setResult(RESULT_OK);
            this.finish();
        }

        return super.onOptionsItemSelected(loanTypeItemList);
    }

    private void swipeToRefresh() {
        // Look up for the swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup Refresh Listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            // Code to refresh goes here. Make sure to call swipeContainer.setRefresh(false) once the refreshed.
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
            swipeContainer.setRefreshing(false);
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
        super.onBackPressed();
    }
}