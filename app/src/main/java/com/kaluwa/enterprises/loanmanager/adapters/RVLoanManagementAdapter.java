package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.EDIT_ACTION;
import static com.kaluwa.enterprises.loanmanager.constants.ActivityRequestCodes.VIEW_ACTION;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.LoanManagementActionActivity;
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
        Loan loanItem = loanList.get(position);

        // decimal format
        DecimalFormat df = new DecimalFormat("#.##");
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);
        // set content to the card
        String loanId = String.format("LI@%06d", position + 1);
        viewHolder.tvLoanIdValue.setText(loanId);
        viewHolder.tvLoanAmountValue.setText("Rs. " + df.format(loanItem.getLoanAmount()));
        viewHolder.tvLoanTermValue.setText(String.valueOf(loanItem.getTerms()));
        viewHolder.tvLoanTypeValue.setText(loanItem.getLoanTypeName());

        // set icon to image view
        if (!TextUtils.isEmpty(loanItem.getLoanTypeIcon())) {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier(loanItem.getLoanTypeIcon(), "drawable", context.getPackageName()));
        } else {
            viewHolder.ivLoanTypeIcon.setImageResource(context.getResources().getIdentifier("icon_unavailable", "drawable", context.getPackageName()));
        }

        viewHolder.cvLoanItemCard.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, viewHolder.cvLoanItemCard);
            popupMenu.inflate(R.menu.option_lm_item_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.lm_menu_view) {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(VIEW_ACTION, loanItem);
                    context.startActivity(intent);
                } else if (item.getItemId() == R.id.lm_menu_edit)  {
                    Intent intent = new Intent(context, LoanManagementActionActivity.class);
                    intent.putExtra(EDIT_ACTION, loanItem);
                    context.startActivity(intent);
                } else if (item.getItemId() == R.id.lm_menu_remove) {
                    Toast.makeText(context, "Item remove called", Toast.LENGTH_SHORT).show();
                }
                return false;
            });
            popupMenu.show();
            return true;
        });


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
