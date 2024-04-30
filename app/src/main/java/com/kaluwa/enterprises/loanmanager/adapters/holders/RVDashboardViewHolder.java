package com.kaluwa.enterprises.loanmanager.adapters.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.kaluwa.enterprises.loanmanager.R;

public class RVDashboardViewHolder extends RecyclerView.ViewHolder {

    public LinearLayout linearItemLayout;
    public TextView tvTitle, tvSubtitle;
    public ImageView ivImageLogo;
    public CardView itemCardView;
    public RVDashboardViewHolder(@NonNull View itemView) {
        super(itemView);
        linearItemLayout = itemView.findViewById(R.id.rv_item_l_layout);
        tvTitle = itemView.findViewById(R.id.rv_item_title);
        tvSubtitle = itemView.findViewById(R.id.rv_item_sub_title);
        ivImageLogo = itemView.findViewById(R.id.rv_item_image);
        itemCardView = itemView.findViewById(R.id.rv_card_view_item);
    }
}
