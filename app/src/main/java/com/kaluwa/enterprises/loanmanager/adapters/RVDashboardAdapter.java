package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.utils.Utils.applyColorToBackground;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.InstallmentManagementActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoanHistoryActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoanManagementActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoginActivity;
import com.kaluwa.enterprises.loanmanager.activities.PayDayRemindersActivity;
import com.kaluwa.enterprises.loanmanager.activities.PendingLoansActivity;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVDashboardViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Dashboard;

import java.util.ArrayList;
import java.util.List;

public class RVDashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Dashboard> dashboardList = new ArrayList<>();

    public RVDashboardAdapter(Context context) {
        this.context = context;
    }

    public void setItems(ArrayList<Dashboard> item) {
        dashboardList.addAll(item);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_rv_item, parent, false);
        return new RVDashboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RVDashboardViewHolder viewHolder = (RVDashboardViewHolder) holder;
        Dashboard item = dashboardList.get(position);

        // Define onClickListener for each card
        CardView.OnClickListener[] onClickListeners = new View.OnClickListener[dashboardList.size()];
        onClickListeners[position] = v -> {
            Intent intent;
            switch (item.getId()) {
                case 1:
                    intent = new Intent(context, LoanManagementActivity.class);
                    break;
                case 2:
                    intent = new Intent(context, InstallmentManagementActivity.class);
                    break;
                case 3:
                    intent = new Intent(context, PendingLoansActivity.class);
                    break;
                case 4:
                    intent = new Intent(context, LoanHistoryActivity.class);
                    break;
                case 5:
                    intent = new Intent(context, PayDayRemindersActivity.class);
                    break;
                default:
                    intent = null;
                    break;
            }
            if (intent != null) {
                context.startActivity(intent);
            }
        };

        // set color codes to background of card, title, subtitle
        try {
            // set content to card
            viewHolder.tvTitle.setText(item.getTitle());
            viewHolder.tvSubtitle.setText(item.getSubTitle());
            viewHolder.itemCardView.setBackground(applyColorToBackground(context, item.getBcCode(), R.drawable.card_item_bg_border));
            viewHolder.tvTitle.setTextColor(Color.parseColor(item.getTcCode()));
            viewHolder.tvSubtitle.setTextColor(Color.parseColor(item.getStcCode()));
            // set icon to image view
            if (item.getDrawable() != null) {
                viewHolder.ivImageLogo.setImageResource(context.getResources().getIdentifier(item.getDrawable(), "drawable", context.getPackageName()));
            }
        } catch (Exception e) {
            Log.d(String.valueOf(position), e.getMessage());
        }

        // onclick listeners
        onClickers(viewHolder, item, position, onClickListeners);
    }

    private void onClickers(RVDashboardViewHolder viewHolder, Dashboard item, int position, CardView.OnClickListener[] onClickListeners) {

        // card oncliker
        // Set onClickListener for the card based on position
        viewHolder.itemCardView.setOnClickListener(onClickListeners[position]);

    }

    @Override
    public int getItemCount() {
        return dashboardList.size();
    }
}
