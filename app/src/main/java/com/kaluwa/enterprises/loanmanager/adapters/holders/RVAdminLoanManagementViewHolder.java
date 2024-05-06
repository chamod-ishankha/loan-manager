package com.kaluwa.enterprises.loanmanager.adapters.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.R;

public class RVAdminLoanManagementViewHolder extends RecyclerView.ViewHolder {

    public CardView cardView;
    public TextView userName, email, totCount, pendingCount, approvedCount, rejectedCount;

    public RVAdminLoanManagementViewHolder(@NonNull View itemView) {
        super(itemView);

        cardView = itemView.findViewById(R.id.rv_admin_l_cv);
        userName = itemView.findViewById(R.id.tv_admin_l_name);
        email = itemView.findViewById(R.id.tv_admin_l_email);
        totCount = itemView.findViewById(R.id.tv_admin_l_tot_count);
        pendingCount = itemView.findViewById(R.id.tv_admin_l_pending_count);
        approvedCount = itemView.findViewById(R.id.tv_admin_l_appr_count);
        rejectedCount = itemView.findViewById(R.id.tv_admin_l_rjt_count);
    }
}
