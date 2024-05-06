package com.kaluwa.enterprises.loanmanager.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.R;

public class RVLoanManagementViewHolder extends RecyclerView.ViewHolder {

    public TextView tvLoanIdValue, tvLoanAmountValue, tvLoanTermValue, tvLoanTypeValue;
    public ImageView ivLoanTypeIcon;
    public CardView cvLoanItemCard;

    public RVLoanManagementViewHolder(@NonNull View view) {
        super(view);
        tvLoanIdValue = view.findViewById(R.id.loan_rv_item_id_d_text_view);
        tvLoanAmountValue = view.findViewById(R.id.loan_rv_item_amount_d_text_view);
        tvLoanTermValue = view.findViewById(R.id.loan_rv_item_terms_d_text_view);
        tvLoanTypeValue = view.findViewById(R.id.loan_rv_item_loan_type_d_text_view);
        ivLoanTypeIcon = view.findViewById(R.id.loan_rv_item_image_view);
        cvLoanItemCard = view.findViewById(R.id.loan_rv_item_card_view);
    }
}
