package com.kaluwa.enterprises.loanmanager.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;

public class CustomDropDownAdapter extends ArrayAdapter<String> {

    private int visibleItemCount = 5; // Initial number of visible items

    public CustomDropDownAdapter(Context context, int resource, String[] objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        return Math.min(super.getCount(), visibleItemCount);
    }

    // Method to load more items
    public void loadMoreItems() {
        visibleItemCount = super.getCount(); // Set visible item count to total items
        notifyDataSetChanged(); // Notify adapter about the change
    }
}
