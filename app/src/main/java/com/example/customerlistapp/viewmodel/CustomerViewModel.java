package com.example.customerlistapp.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.customerlistapp.models.Customer;
import com.example.customerlistapp.repository.CustomerRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerViewModel extends ViewModel {
    private final CustomerRepository repository;
    private final LiveData<List<Customer>> allCustomers;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Integer> sortOption = new MutableLiveData<>(0);

    public CustomerViewModel(Context context) {
        try {
            repository = new CustomerRepository(context);
            allCustomers = repository.getCustomers();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize repository", e);
        }
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setSortOption(int option) {
        sortOption.setValue(option);
    }

    public LiveData<List<Customer>> getFilteredCustomers() {
        return Transformations.switchMap(searchQuery, query ->
                Transformations.switchMap(sortOption, sort ->
                        Transformations.map(allCustomers, customers -> {
                            if (customers == null) {
                                return null; // Trả về null nếu không có dữ liệu
                            }
                            // Khai báo rõ ràng filteredList là List<Customer>
                            List<Customer> filteredList = customers;
                            // Lọc theo từ khóa tìm kiếm (nếu có)
                            if (query != null && !query.isEmpty()) {
                                filteredList = filteredList.stream()
                                        .filter(customer ->
                                                (customer.getFirstName() != null && customer.getFirstName().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getSurname() != null && customer.getSurname().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getAddress() != null && customer.getAddress().toLowerCase().contains(query.toLowerCase())) ||
                                                        (customer.getPhone() != null && customer.getPhone().toLowerCase().contains(query.toLowerCase())))
                                        .collect(Collectors.toList());
                            }

                            // Sắp xếp theo tiêu chí
                            switch (sort) {
                                case 0: // Tên A-Z (dùng firstName)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getFirstName, Comparator.nullsLast(String::compareTo)))
                                            .collect(Collectors.toList());
                                case 1: // Tên Z-A (dùng firstName)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getFirstName, Comparator.nullsLast(String::compareTo).reversed()))
                                            .collect(Collectors.toList());
                                case 2: // Theo địa chỉ (A-Z)
                                    return filteredList.stream()
                                            .sorted(Comparator.comparing(Customer::getAddress, Comparator.nullsLast(String::compareTo)))
                                            .collect(Collectors.toList());
                                default:
                                    return filteredList; // Không sắp xếp
                            }
                        })));
    }

    public void refreshCustomers() {
        Log.d("CustomerViewModel", "Refreshing customers...");
        searchQuery.postValue(""); // Reset tìm kiếm
        sortOption.postValue(0);   // Reset sắp xếp về mặc định (A-Z)
        repository.refreshCustomers();
    }

    public LiveData<List<Customer>> getAllCustomers() {
        return allCustomers;
    }
}