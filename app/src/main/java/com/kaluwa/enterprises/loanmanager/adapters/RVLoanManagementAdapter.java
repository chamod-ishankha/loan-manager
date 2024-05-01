package com.kaluwa.enterprises.loanmanager.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVLoanManagementViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Loan;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RVLoanManagementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Loan> loanList = new ArrayList<>();

    public RVLoanManagementAdapter(Context context) {
        this.context = context;
    }

    public void setLoanList(ArrayList<Loan> loanList) {
        this.loanList.addAll(loanList);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_loan_item, parent, false);
        return new RVLoanManagementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RVLoanManagementViewHolder viewHolder = (RVLoanManagementViewHolder) holder;
        Loan item = loanList.get(position);

        // decimal format
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        // set content to the card
        String loanId = String.format("LI@%06d", position + 1);
        viewHolder.tvLoanIdValue.setText(loanId);
        viewHolder.tvLoanAmountValue.setText("Rs. " + df.format(item.getLoanAmount()));
        viewHolder.tvLoanTermValue.setText(String.valueOf(item.getTerms()));
        viewHolder.tvLoanTypeValue.setText(item.getLoanTypeName());

        // set icon to image view
        if (!TextUtils.isEmpty(item.getLoanTypeIcon())) {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier(item.getLoanTypeIcon(), "drawable", context.getPackageName()));
        } else {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier("icon_unavailable", "drawable", context.getPackageName()));
        }
    }

    @Override
    public int getItemCount() {
        return loanList.size();
    }


//    private void onClickers(RVDashboardViewHolder viewHolder, Dashboard item, int position, CardView.OnClickListener[] onClickListeners) {
//
//        // card oncliker
//        // Set onClickListener for the card based on position
//        viewHolder.itemCardView.setOnClickListener(onClickListeners[position]);
//
//    }
}
