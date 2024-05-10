package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.EDIT_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_APPROVED;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_REJECTED;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.getDecimalFormatter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.LoanManagementActionActivity;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RVLoanManagementAdapter extends FirebaseRecyclerAdapter<Loan, RVLoanManagementViewHolder> {

    private final static String TAG = "RVLoanManagementAdapter";
    private Context context;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;

    public RVLoanManagementAdapter(FirebaseRecyclerOptions<Loan> options, ProgressBar progressBar, Context context) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
        this.authProfile = FirebaseAuth.getInstance();
    }

    protected void onBindViewHolder(@NonNull RVLoanManagementViewHolder viewHolder, int position,@NonNull Loan loanItem) {
        try {
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
                        if (id.toString().equals(String.valueOf(loanItem.getLoanTypeId()))) {
                            loanItem.setLoanTypeIcon(loanTypeIcon);
                            loanItem.setLoanTypeName(loanTypeName);

                            // After setting the loan type name and icon, update the views
                            updateViews(viewHolder, loanItem);
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error: " + error.getMessage());
                }
            });

            // decimal format
            DecimalFormat df = getDecimalFormatter();
            // set content to the card
            String loanId = String.format("LI@%06d", position + 1);
            viewHolder.tvLoanIdValue.setText(loanId);
            viewHolder.tvLoanAmountValue.setText("Rs. " + df.format(loanItem.getLoanAmount()));
            viewHolder.tvLoanTermValue.setText(String.valueOf(loanItem.getTerms()));
            if (loanItem.getStatus().equals(STATUS_APPROVED)) {
                viewHolder.tvLoanStatus.setText("APPROVED");
                viewHolder.tvLoanStatus.setTextColor(context.getResources().getColor(R.color.green));
                viewHolder.tvLoanStatus.setVisibility(View.VISIBLE);
            } else if (loanItem.getStatus().equals(STATUS_REJECTED)) {
                viewHolder.tvLoanStatus.setText("REJECTED");
                viewHolder.tvLoanStatus.setTextColor(context.getResources().getColor(R.color.red));
                viewHolder.tvLoanStatus.setVisibility(View.VISIBLE);
            } else {
                viewHolder.tvLoanStatus.setText("NEW");
                viewHolder.tvLoanStatus.setTextColor(context.getResources().getColor(R.color.royalBlue));
                viewHolder.tvLoanStatus.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
        }
    }

    private void updateViews(@NonNull RVLoanManagementViewHolder viewHolder, @NonNull Loan loanItem) {
        // Set loan type name and icon
        viewHolder.tvLoanTypeValue.setText(loanItem.getLoanTypeName());

        // Set icon to image view
        if (!TextUtils.isEmpty(loanItem.getLoanTypeIcon())) {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier(loanItem.getLoanTypeIcon(), "drawable", context.getPackageName()));
        } else {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier("icon_unavailable", "drawable", context.getPackageName()));
        }

        // Set long click listener
        viewHolder.cvLoanItemCard.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.cvLoanItemCard);
            popupMenu.inflate(R.menu.option_lm_item_menu);
            // make can't remove after loan is approved.
            popupMenu.getMenu().findItem(R.id.lm_menu_remove).setVisible(false);
            // make can't redit after loan is approved.
            popupMenu.getMenu().findItem(R.id.lm_menu_edit).setVisible(false);
            // setup item onclick listener
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.lm_menu_view) {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(VIEW_ACTION, loanItem.getLoanId());
                    context.startActivity(intent);
                } else if (item.getItemId() == R.id.lm_menu_edit) {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(EDIT_ACTION, loanItem.getLoanId());
                    context.startActivity(intent);
                } else if (item.getItemId() == R.id.lm_menu_remove) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle("Delete Loan!");
                    dialogBuilder.setMessage("Are you sure you want to delete this loan?");
                    // disable outside click cancel
                    dialogBuilder.setCancelable(false);
                    // delete click
                    dialogBuilder.setPositiveButton("Delete", (DialogInterface.OnClickListener) (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        FirebaseDatabase.getInstance()
                                .getReference(LOAN_REFERENCE)
                                .child(authProfile.getCurrentUser().getUid())
                                .child(loanItem.getLoanId())
                                .removeValue()
                                .addOnSuccessListener(unused -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(context, "Loan item removed.", Toast.LENGTH_LONG).show();
                                }).addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                                    Toast.makeText(context, "Loan item remove failed.", Toast.LENGTH_LONG).show();
                                });
                    });
                    // cancel click
                    dialogBuilder.setNegativeButton("Cancel", (DialogInterface.OnClickListener) (dialog, which) -> {
                        dialog.cancel();
                    });
                    // Create the Alert dialog
                    AlertDialog alertDialog = dialogBuilder.create();
                    // Change the continue button color
                    alertDialog.setOnShowListener(dialog -> {
                        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.red));
                    });
                    // Show the Alert Dialog box
                    alertDialog.show();
                }
                return false;
            });
            popupMenu.show();
        });
    }


    @NonNull
    @Override
    public RVLoanManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_loan_item, parent, false);
        return new RVLoanManagementViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        // Hide progress bar when data loading is complete
        progressBar.setVisibility(View.GONE);
    }
}
