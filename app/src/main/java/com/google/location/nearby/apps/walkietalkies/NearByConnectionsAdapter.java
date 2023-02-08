package com.google.location.nearby.apps.walkietalkies;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.location.nearby.apps.walkietalkies.R;

import java.util.List;

public class NearByConnectionsAdapter extends RecyclerView.Adapter<NearByConnectionsAdapter.ViewHolder> {
    private List<ConnectionsActivity.Endpoint> countryList;
    private OnItemClick onItemClick;

    public NearByConnectionsAdapter(List<ConnectionsActivity.Endpoint> countryList,OnItemClick onItemClick) {
        this.countryList = countryList;
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_nearby, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        viewHolder.textViewName.setText(countryList.get(i).getName());
        viewHolder.textViewCode.setText(countryList.get(i).getId());
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClick.onItemClick(i);
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    public void updateList(List<ConnectionsActivity.Endpoint> temp) {
        this.countryList = temp;
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        TextView textViewCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewCode = itemView.findViewById(R.id.textViewCode);
        }
    }

    public interface OnItemClick{
        void onItemClick(int i);
    }
}