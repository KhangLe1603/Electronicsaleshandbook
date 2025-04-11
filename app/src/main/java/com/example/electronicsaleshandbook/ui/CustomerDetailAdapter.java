package com.example.electronicsaleshandbook.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Customer;
import java.util.ArrayList;
import java.util.List;

public class CustomerDetailAdapter extends RecyclerView.Adapter<CustomerDetailAdapter.CustomerViewHolder> {
    private List<Customer> customers = new ArrayList<>();

    public void setCustomers(List<Customer> customers) {
        this.customers = customers != null ? customers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.customer_in_product, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        String fullName = (customer.getSurname() + " " + customer.getFirstName()).trim();
        holder.nameTextView.setText(fullName.isEmpty() ? "N/A" : fullName);
        holder.addressTextView.setText(customer.getAddress() != null ? customer.getAddress() : "N/A");
        holder.phoneTextView.setText(customer.getPhone() != null ? customer.getPhone() : "N/A");
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView nameTextView, addressTextView, phoneTextView;

        CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            nameTextView = itemView.findViewById(R.id.tvCustomerName);
            addressTextView = itemView.findViewById(R.id.tvAddress);
            phoneTextView = itemView.findViewById(R.id.tvPhoneNumber);
        }
    }
}