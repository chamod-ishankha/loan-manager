package com.kaluwa.enterprises.loanmanager.adapters;

import static com.kaluwa.enterprises.loanmanager.utils.Utils.applyColorToBackground;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.activities.LoanHistoryActivity;
import com.kaluwa.enterprises.loanmanager.activities.LoanManagementActivity;
import com.kaluwa.enterprises.loanmanager.activities.PayDayRemindersActivity;
import com.kaluwa.enterprises.loanmanager.activities.PendingLoansActivity;
import com.kaluwa.enterprises.loanmanager.adapters.holders.RVDashboardViewHolder;
import com.kaluwa.enterprises.loanmanager.models.Dashboard;

public class RVDashboardAdapter extends FirebaseRecyclerAdapter<Dashboard, RVDashboardViewHolder> {

    private Context context;
    private ProgressBar progressBar;

    public RVDashboardAdapter(@NonNull FirebaseRecyclerOptions<Dashboard> options, ProgressBar progressBar, Context context) {
        super(options);
        this.context = context;
        this.progressBar = progressBar;
    }

    @Override
    protected void onBindViewHolder(@NonNull RVDashboardViewHolder holder, int position, @NonNull Dashboard item) {
        // set color codes to background of card, title, subtitle
        try {
            // Define onClickListener for each card dynamically
            holder.itemCardView.setOnClickListener(v -> {
                Intent intent;
                if (item.getClassName() != null && !TextUtils.isEmpty(item.getClassName())) {
                    try {
                        // find class
                        Class<?> clazz = Class.forName(item.getClassName());
                        // set to intent
                        intent = new Intent(context, clazz);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    intent = null;
                }
                if (intent != null) {
                    context.startActivity(intent);
                }
            });

            // set content to card
            holder.tvTitle.setText(item.getTitle());
            holder.tvSubtitle.setText(item.getSubTitle());
            holder.itemCardView.setBackground(applyColorToBackground(context, item.getBcCode(), R.drawable.card_item_bg_border));
            holder.tvTitle.setTextColor(Color.parseColor(item.getTcCode()));
            holder.tvSubtitle.setTextColor(Color.parseColor(item.getStcCode()));
            // set icon to image view
            holder.ivImageLogo.setImageResource(context.getResources().getIdentifier(item.getDrawable(), "drawable", context.getPackageName()));
        } catch (Exception e) {
            Log.d(String.valueOf(position), e.getMessage());
        }
    }

    @NonNull
    @Override
    public RVDashboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_rv_item, parent, false);
        return new RVDashboardViewHolder(view);
    }

    @Override
    public void onDataChanged() {
        super.onDataChanged();
        // Hide progress bar when data loading is complete
        progressBar.setVisibility(View.GONE);
    }
}
