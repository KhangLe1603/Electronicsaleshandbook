package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Customer;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.CustomerRepository;
import com.example.electronicsaleshandbook.repository.SheetRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class LinkViewModel extends ViewModel {
    private final SheetRepository sheetRepository;
    private final CustomerRepository customerRepository;

    public LinkViewModel(Context context) throws IOException, GeneralSecurityException {
        sheetRepository = SheetRepository.getInstance(context);
        customerRepository = CustomerRepository.getInstance(context);
    }

    public LiveData<List<Customer>> getCustomers() {
        return customerRepository.getCustomers();
    }

    public LiveData<List<Product>> getProducts() {
        return sheetRepository.getProducts();
    }

    public LiveData<List<CustomerProductLink>> getLinks() {
        return sheetRepository.getLinks();
    }

    public LiveData<String> getLinkResult() {
        return sheetRepository.getLinkResult();
    }

    public void createLink(CustomerProductLink link) {
        sheetRepository.createLink(link);
    }
}