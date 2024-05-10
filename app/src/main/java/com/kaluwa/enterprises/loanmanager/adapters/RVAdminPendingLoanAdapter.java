package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.EDIT_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.USER_ID_KEY;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_TYPE_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_APPROVED;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_REJECTED;
import static com.kaluwa.enterprises.loanmanager.utils.Utils.getDecimalFormatter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.AdminPendingLoansActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoanManagementActionActivity;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVAdminPendingLoanViewHolder;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RVAdminPendingLoanAdapter extends FirebaseRecyclerAdapter<Loan, RVAdminPendingLoanViewHolder> {

    private final static String TAG = "RVAdminPendingLoanAdapter";
    private ProgressBar progressBar;
    private Context context;
    private String userId;
    private String action;

    public RVAdminPendingLoanAdapter(ProgressBar progressBar, FirebaseRecyclerOptions<Loan> options, String action, String userId, Context context) {
        super(options);
        this.progressBar = progressBar;
        this.context = context;
        this.action = action;
        this.userId = userId;
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onBindViewHolder(@NonNull RVAdminPendingLoanViewHolder viewHolder, int position, @NonNull Loan loanItem) {
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

    @SuppressLint("LongLogTag")
    private void updateViews(RVAdminPendingLoanViewHolder viewHolder, Loan loanItem) {
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
            popupMenu.inflate(R.menu.option_admin_lm_item_menu);
            if (loanItem.getStatus().equals(STATUS_APPROVED)) {
                popupMenu.getMenu().findItem(R.id.lm_menu_approve).setEnabled(false);
            }
            if (loanItem.getStatus().equals(STATUS_REJECTED)) {
                popupMenu.getMenu().findItem(R.id.lm_menu_approve).setEnabled(false);
                popupMenu.getMenu().findItem(R.id.lm_menu_reject).setEnabled(false);
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.lm_menu_view) {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(USER_ID_KEY, userId);
                    intent.putExtra(VIEW_ACTION, loanItem.getLoanId());
                    context.startActivity(intent);
                } else if (item.getItemId() == R.id.lm_menu_edit) {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(USER_ID_KEY, userId);
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
                                .child(userId)
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
                } else if (item.getItemId() == R.id.lm_menu_approve) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle("Loan Approval");
                    dialogBuilder.setMessage("Are you sure you want to approve this loan?");
                    // disable outside click cancel
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.setPositiveButton("Approve", (DialogInterface.OnClickListener) (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        Loan loan = loanItem;
                        loan.setApproved(true);
                        loan.setStatus(STATUS_APPROVED);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("approved", loan.isApproved());
                        updates.put("status", loan.getStatus());
                        FirebaseDatabase.getInstance()
                                .getReference(LOAN_REFERENCE)
                                .child(userId)
                                .child(loan.getLoanId())
                                .updateChildren(updates)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Update successful
                                        Log.d(TAG, "Loan updated successfully");
                                        Toast.makeText(context, "Loan approved successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle errors
                                        Log.e(TAG, "Failed to update loan: " + task.getException().getMessage());
                                        Toast.makeText(context, "Failed to approve loan!", Toast.LENGTH_SHORT).show();
                                    }
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
                        alertDialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.green));
                    });
                    // Show the Alert Dialog box
                    alertDialog.show();
                } else if (item.getItemId() == R.id.lm_menu_reject) {
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                    dialogBuilder.setTitle("Loan Reject");
                    dialogBuilder.setMessage("Are you sure you want to reject this loan?");
                    // disable outside click cancel
                    dialogBuilder.setCancelable(false);
                    dialogBuilder.setPositiveButton("Reject", (DialogInterface.OnClickListener) (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        Loan loan = loanItem;
                        loan.setApproved(false);
                        loan.setStatus(STATUS_REJECTED);

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("approved", loan.isApproved());
                        updates.put("status", loan.getStatus());
                        FirebaseDatabase.getInstance()
                                .getReference(LOAN_REFERENCE)
                                .child(userId)
                                .child(loan.getLoanId())
                                .updateChildren(updates)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        // Update successful
                                        Log.d(TAG, "Loan rejected successfully");
                                        Toast.makeText(context, "Loan rejected successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Handle errors
                                        Log.e(TAG, "Failed to update loan: " + task.getException().getMessage());
                                        Toast.makeText(context, "Failed to rejected loan!", Toast.LENGTH_SHORT).show();
                                    }
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
    public RVAdminPendingLoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_loan_item, parent, false);
        return new RVAdminPendingLoanViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        progressBar.setVisibility(View.GONE);
    }
}
