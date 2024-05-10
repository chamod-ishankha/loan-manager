package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.PENDING_ACTIVITY;
import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.LOAN_REFERENCE;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_NEW;
import static com.kaluwa.enterprises.loanmanager.constants.StatusConstant.STATUS_REJECTED;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.AdminPendingLoansActivity;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVAdminLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Count;
import com.kaluwa.enterprises.loanmanager.models.Loan;
import com.kaluwa.enterprises.loanmanager.models.User;

import java.util.List;

public class RVAdminLoanManagementAdapter extends FirebaseRecyclerAdapter<User, RVAdminLoanManagementViewHolder> {
    private static final String TAG = "RVAdminLoanManagementAdapter";
    private Context context;
    private ProgressBar progressBar;
    private String action;
    public RVAdminLoanManagementAdapter(ProgressBar progressBar, FirebaseRecyclerOptions<User> options, String action, Context context) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
        this.action = action;
    }

    @Override
    protected void onBindViewHolder(@NonNull RVAdminLoanManagementViewHolder holder, int position, @NonNull User user) {
        // set counts
        getLoanCounts(user.getUid(), count -> {
            holder.userName.setText(user.getTitle()+ " " + user.getFirstName() + " " + user.getLastName());
            holder.email.setText(user.getEmail());
            holder.totCount.setText(String.valueOf(count.getTotal()));
            holder.pendingCount.setText(String.valueOf(count.getPending()));
            holder.approvedCount.setText(String.valueOf(count.getApproved()));
            holder.rejectedCount.setText(String.valueOf(count.getRejected()));

            holder.cardView.setOnClickListener(v -> {
                if (action != null) {
                    Intent intent = new Intent(context, AdminPendingLoansActivity.class);
                    String[] data = {user.getUid(), action};
                    intent.putExtra("Object", data);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No action!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void getLoanCounts(String uid, OnQueryCompleteListener listener) {
        Count count = new Count( 0, 0, 0, 0);
        FirebaseDatabase.getInstance().getReference(LOAN_REFERENCE).child(uid).orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int approvedCount = 0;
                int pendingCount = 0;
                int rejectCount = 0;

                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Loan loan = childSnapshot.getValue(Loan.class);
                    if (loan != null && loan.isApproved()) {
                        approvedCount++;
                    }
                }
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Loan loan = childSnapshot.getValue(Loan.class);
                    if (loan != null && !loan.isApproved() && loan.getStatus().equals(STATUS_NEW)) {
                        pendingCount++;
                    }
                }
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    Loan loan = childSnapshot.getValue(Loan.class);
                    if (loan != null && !loan.isApproved() && loan.getStatus().equals(STATUS_REJECTED)) {
                        rejectCount++;
                    }
                }
                count.setTotal((int) snapshot.getChildrenCount());
                count.setApproved(approvedCount);
                count.setPending(pendingCount);
                count.setRejected(rejectCount);
                listener.onQueryComplete(count);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, error.getMessage());
                listener.onQueryComplete(count);
            }
        });
    }

    // Define the interface for the listener
    interface OnQueryCompleteListener {
        void onQueryComplete(Count count);
    }

    @NonNull
    @Override
    public RVAdminLoanManagementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_rv_admin_user_item, parent, false);
        return new RVAdminLoanManagementViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        progressBar.setVisibility(View.GONE);
    }
}
