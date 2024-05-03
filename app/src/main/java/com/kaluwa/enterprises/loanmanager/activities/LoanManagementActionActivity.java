package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.ADD_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.EDIT_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.getDecimalFormatter;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.setUpDatePicker;

import android.annotation.SuppressLint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.Loan;
import com.kaluwa.enterprises.loanmanager.models.LoanType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class LoanManagementActionActivity extends AppCompatActivity {

    private final static String TAG = "LoanManagementActionActivity";
    private FirebaseAuth authProfile;
    private SwipeRefreshLayout swipeContainer;
    private Loan loan = new Loan();
    private ArrayList<LoanType> loanTypeItemList = new ArrayList<>();
    private ArrayAdapter<LoanType> loanTypeAdapterItem;

    private TextView tvHeadLine, tvSubHeadLine;
    private EditText etLoanAmt, etInterestRate, etTerms, etStDate, etDueDate, etInstallment, etAdditionalCharges, etDesp, etLenderInfo, etContactInfo;
    private AutoCompleteTextView loanTypeTvAutoComplete, fopTvAutoComplete;
    private Button btnFunction;
    private ProgressBar progressBar;
    private View overlay;

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_loan);

        authProfile = FirebaseAuth.getInstance();
        // set title
        getSupportActionBar().setTitle("Loan Management");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        swipeToRefresh();

        tvHeadLine = findViewById(R.id.tvAlRoyalBlueBoxHead);
        tvSubHeadLine = findViewById(R.id.tvAlRoyalBlueBoxSub);
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
        btnFunction = findViewById(R.id.al_btn_add);
        progressBar = findViewById(R.id.al_progressBar);
        overlay = findViewById(R.id.al_overlay);

        try {
            // handle intent view
            String loanKey = (String) getIntent().getSerializableExtra(VIEW_ACTION);
            if (loanKey != null) {
                callViewLoan(loanKey);
            } else {
                loanKey = (String) getIntent().getSerializableExtra(EDIT_ACTION);
                if (loanKey != null) {
                    callEditLoan(loanKey);
                } else if (getIntent().getSerializableExtra(ADD_ACTION).equals(ADD_ACTION)) {
                    loan = new Loan();
                    callAddLoan();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void callAddLoan() {
        // setup head
        tvHeadLine.setText("Get Started with Your Next Loan");
        tvSubHeadLine.setText("Enter the details of your new loan to stay on top of your finances.");

        // run dropdowns
        loadLoanTypes(loanTypeTvAutoComplete);
        loadFrequencies(fopTvAutoComplete);
        loadDatePickers(etStDate, etDueDate);

        btnFunction.setOnClickListener(v -> {

            // validate
            Loan validateLoanItem = validateInputFields();

            if (validateLoanItem != null) {
                // loading enabled
                handleLoading(progressBar, overlay, true);
                // Loan reference
                DatabaseReference loanReference = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE);
                // generate unique key for each loan
                String userId = authProfile.getCurrentUser().getUid();
                validateLoanItem.setLoanId(loanReference.child(userId).push().getKey());

                // save the loan
                loanReference.child(userId).child(validateLoanItem.getLoanId())
                        .setValue(validateLoanItem)
                        .addOnSuccessListener(unused -> {
                            // loading disabled
                            handleLoading(progressBar, overlay, false);
                            Toast.makeText(this, "Loan successfully added.", Toast.LENGTH_SHORT).show();
                            // refresh activity
                            startActivity(getIntent());
                            finish();
                            overridePendingTransition(0, 0);
                        }).addOnFailureListener(e -> {
                            // loading disabled
                            handleLoading(progressBar, overlay, false);
                            Toast.makeText(this, "Error while adding your loan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
    }

    @SuppressLint("LongLogTag")
    private void callEditLoan(String loanKey) {
        // setup head
        tvHeadLine.setText("Edit Your Loan");
        tvSubHeadLine.setText("Modify your loan details below");

        // load data
        handleLoading(progressBar, overlay, true);
        DatabaseReference loanRef = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(authProfile.getCurrentUser().getUid()).child(loanKey);
        try {
            loanRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        loan = snapshot.getValue(Loan.class);
                        FirebaseDatabase.getInstance().getReference(LOAN_TYPE_REFERENCE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot childSnap : snapshot.getChildren()) {
                                        LoanType lt = childSnap.getValue(LoanType.class);
                                        if (lt.getId() == loan.getLoanTypeId()) {
                                            loan.setLoanTypeName(lt.getName());
                                            // fill inputs
                                            fillInputFields(loan);
                                            loadLoanTypes(loanTypeTvAutoComplete);
                                            break;
                                        }
                                    }
                                } else {
                                    System.out.println("Loan Types not found");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                handleLoading(progressBar, overlay, false);
                                Log.e(TAG, error.getDetails());
                                throw new RuntimeException(error.getMessage());
                            }
                        });
                    } else {
                        Log.e(TAG, "Loan data not exists.");
                        Toast.makeText(LoanManagementActionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                    handleLoading(progressBar, overlay, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleLoading(progressBar, overlay, false);
                    Log.e(TAG, error.getDetails());
                    throw new RuntimeException(error.getMessage());
                }
            });
        } catch (Exception e) {
            handleLoading(progressBar, overlay, false);
            Toast.makeText(LoanManagementActionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // run dropdowns
        loadFrequencies(fopTvAutoComplete);
        loadDatePickers(etStDate, etDueDate);

        // update button
        btnFunction.setText("Update Your Loan");
        btnFunction.setBackgroundColor(getResources().getColor(R.color.green));
        btnFunction.setOnClickListener(v -> {
            handleLoading(progressBar, overlay, true);
            // validate
            Loan validateLoanItem = validateInputFields();
            if (validateLoanItem != null) {
                validateLoanItem.setLoanId(loanKey);
                // Loan reference
                DatabaseReference loanReference = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE);
                // generate unique key for each loan
                String userId = authProfile.getCurrentUser().getUid();
                loanReference.child(userId).child(loanKey).setValue(validateLoanItem)
                        .addOnSuccessListener(unused -> {
                            handleLoading(progressBar, overlay, false);
                            Toast.makeText(this, "Loan details update completed", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            handleLoading(progressBar, overlay, false);
                            Toast.makeText(this, "Loan details update failed!", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "error: " + e.getMessage());
                        });
            }
        });
    }

    @SuppressLint("LongLogTag")
    private void callViewLoan(String loanKey) {
        handleLoading(progressBar, overlay, true);
        // load data
        DatabaseReference loanRef = FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(authProfile.getCurrentUser().getUid()).child(loanKey);
        try {
            loanRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        loan = snapshot.getValue(Loan.class);
                        FirebaseDatabase.getInstance().getReference(LOAN_TYPE_REFERENCE).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot childSnap : snapshot.getChildren()) {
                                        LoanType lt = childSnap.getValue(LoanType.class);
                                        if (lt.getId() == loan.getLoanTypeId()) {
                                            loan.setLoanTypeName(lt.getName());
                                            // fill inputs
                                            fillInputFields(loan);
                                            break;
                                        }
                                    }
                                } else {
                                    System.out.println("Loan Types not found");
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                handleLoading(progressBar, overlay, false);
                                Log.e(TAG, error.getDetails());
                                throw new RuntimeException(error.getMessage());
                            }
                        });
                    } else {
                        Log.e(TAG, "Loan data not exists.");
                        Toast.makeText(LoanManagementActionActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                    handleLoading(progressBar, overlay, false);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleLoading(progressBar, overlay, false);
                    Log.e(TAG, error.getDetails());
                    throw new RuntimeException(error.getMessage());
                }
            });
        } catch (Exception e) {
            handleLoading(progressBar, overlay, false);
            Toast.makeText(LoanManagementActionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // setup head
        tvHeadLine.setText("View Your Loan Details");
        tvSubHeadLine.setText("Review your loan information below");

        // disable all editable fields
        disableInputFields();

        // cancel button
        btnFunction.setText("Close");
        btnFunction.setBackgroundColor(getResources().getColor(R.color.formTextColor));
        btnFunction.setOnClickListener(v -> {
            setResult(RESULT_OK);
            this.finish();
        });
    }


    private Loan validateInputFields() {
        Loan validatedLoanItem = new Loan();
        boolean isError = false;

        String loanAmt = null, rate = null, installment = null, additionalCharges = null, terms = null, stDate = null, dueDate = null, desp = null, lenderInfo = null, contactInfo = null, loanTypeId = null, fop = null;
        double loanAmtDbl = 0.00, rateDbl = 0.00, installmentDbl = 0.00, additionalChargesDbl = 0.00;

        loanTypeId = String.valueOf(loan.getLoanTypeId());
        fop = loan.getFop();
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

        if (loanTypeId.equals("0") || TextUtils.isEmpty(loanTypeId)) {
            loanTypeTvAutoComplete.setError("Loan type cannot be empty");
            loanTypeTvAutoComplete.requestFocus();
            Toast.makeText(this, "Loan type cannot be empty", Toast.LENGTH_SHORT).show();
            isError = true;
        } else if (TextUtils.isEmpty(loanAmt)) {
            loanTypeTvAutoComplete.setError(null);
            etLoanAmt.setError("Loan amount cannot be empty");
            etLoanAmt.requestFocus();
            isError = true;
        } else if (TextUtils.isEmpty(rate)) {
            etInterestRate.setError("Interest rate cannot be empty");
            etInterestRate.requestFocus();
            isError = true;
        } else if (TextUtils.isEmpty(terms)) {
            etTerms.setError("Duration/term cannot be empty");
            etTerms.requestFocus();
            isError = true;
        } else if (TextUtils.isEmpty(stDate)) {
            etStDate.setError("Start date cannot be empty");
            etStDate.requestFocus();
            Toast.makeText(this, "Start date cannot be empty", Toast.LENGTH_SHORT).show();
            isError = true;
        } else if (TextUtils.isEmpty(dueDate)) {
            etStDate.setError(null);
            etDueDate.setError("Due date cannot be empty");
            etDueDate.requestFocus();
            Toast.makeText(this, "Due date cannot be empty", Toast.LENGTH_SHORT).show();
            isError = true;
        } else if (TextUtils.isEmpty(lenderInfo)) {
            etDueDate.setError(null);
            etLenderInfo.setError("Lender information cannot be empty");
            etLenderInfo.requestFocus();
            isError = true;
        } else if (TextUtils.isEmpty(contactInfo)) {
            etContactInfo.setError("Contact number cannot be empty");
            etContactInfo.requestFocus();
            isError = true;
        } else if (contactInfo.length() != 10) {
            etContactInfo.setError("Contact number should have 10 numbers.");
            etContactInfo.requestFocus();
            isError = true;
        } else {
            isError = false;
            loanAmtDbl = Double.parseDouble(loanAmt);
            rateDbl = Double.parseDouble(rate);

            if (!TextUtils.isEmpty(additionalCharges)) {
                additionalChargesDbl = Double.parseDouble(additionalCharges);
            }

            if (loanAmtDbl < 1000) {
                etLoanAmt.setError("Loan amount must be greater than Rs. 1000.00");
                etLoanAmt.requestFocus();
                isError = true;
            } else if (rateDbl < 0.50) {
                etInterestRate.setError("Interest rate must be greater than 0.50%");
                etInterestRate.requestFocus();
                isError = true;
            } else if (!TextUtils.isEmpty(installment)) {
                installmentDbl = Double.parseDouble(installment);
                isError = false;
                if (installmentDbl != 0) {
                    if (installmentDbl < 100) {
                        etInstallment.setError("Installment amount must be greater than Rs. 100.00");
                        etInstallment.requestFocus();
                        isError = true;
                    } else {
                        isError = false;
                    }
                } else {
                    isError = false;
                }
            } else {
                isError = false;
            }
        }

        if (!isError) {
            // set loan model
            validatedLoanItem.setLoanTypeId(Integer.parseInt(loanTypeId));
            validatedLoanItem.setLoanAmount(loanAmtDbl);
            validatedLoanItem.setInterestRate(rateDbl);
            validatedLoanItem.setTerms(Integer.parseInt(terms));
            validatedLoanItem.setStartDate(stDate);
            validatedLoanItem.setDueDate(dueDate);
            validatedLoanItem.setInstallmentAmount(installmentDbl);
            validatedLoanItem.setFop(fop);
            validatedLoanItem.setAdditionalCharges(additionalChargesDbl);
            validatedLoanItem.setDescription(desp);
            validatedLoanItem.setLenderInfo(lenderInfo);
            validatedLoanItem.setContactInfo(contactInfo);

            return validatedLoanItem;
        } else {
            return null;
        }
    }

    private void fillInputFields(Loan loanItem) {
        DecimalFormat df = getDecimalFormatter();

        etLoanAmt.setText(df.format(loanItem.getLoanAmount()));
        etInterestRate.setText(df.format(loanItem.getInterestRate()));
        etTerms.setText(String.valueOf(loanItem.getTerms()));
        etStDate.setText(loanItem.getStartDate());
        etDueDate.setText(loanItem.getDueDate());
        etInstallment.setText(df.format(loanItem.getInstallmentAmount()));
        etAdditionalCharges.setText(df.format(loanItem.getAdditionalCharges()));
        if (!TextUtils.isEmpty(loanItem.getDescription())) {
            etDesp.setText(loanItem.getDescription());
        } else {
            etDesp.setText("Not Available");
        }
        etLenderInfo.setText(loanItem.getLenderInfo());
        etContactInfo.setText(loanItem.getContactInfo());
        // set loan type value
        loanTypeTvAutoComplete.setText(loanItem.getLoanTypeName());
        TextInputLayout tiLayout = findViewById(R.id.al_text_input_layout);
        tiLayout.setHint("");
        // set fop value
        if (!TextUtils.isEmpty(loanItem.getFop())) {
            fopTvAutoComplete.setText(loanItem.getFop());
        } else {
            fopTvAutoComplete.setText("Not Selected");
        }
        TextInputLayout tiLayoutFop = findViewById(R.id.al_text_input_layout_fop);
        tiLayoutFop.setHint("");
    }

    private void disableInputFields() {
        etLoanAmt.setEnabled(false);
        etInterestRate.setEnabled(false);
        etTerms.setEnabled(false);
        etStDate.setEnabled(false);
        etDueDate.setEnabled(false);
        etInstallment.setEnabled(false);
        etAdditionalCharges.setEnabled(false);
        etDesp.setEnabled(false);
        etLenderInfo.setEnabled(false);
        etContactInfo.setEnabled(false);
        loanTypeTvAutoComplete.setEnabled(false);
        fopTvAutoComplete.setEnabled(false);
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
                Toast.makeText(LoanManagementActionActivity.this, "error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
        this.finish();
        super.onBackPressed();
    }
}