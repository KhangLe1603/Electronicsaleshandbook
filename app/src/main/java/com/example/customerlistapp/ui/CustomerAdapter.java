package com.example.customerlistapp.ui;
;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customerlistapp.models.Customer;
import com.example.electronicsaleshandbook.R;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private List<Customer> customers = new ArrayList<>();

    public void setCustomers(List<Customer> customers) {
        this.customers = customers != null ? customers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_item, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        String fullName = customer.getFullName().trim().isEmpty() ? "N/A" : customer.getFullName().trim();
        holder.nameTextView.setText(fullName);
        holder.addressTextView.setText(customer.getAddress());
        holder.phoneTextView.setText(customer.getPhone());
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, addressTextView, phoneTextView;

        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.tvCustomerName);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            phoneTextView = itemView.findViewById(R.id.tvPhoneNumber);
        }
    }
}