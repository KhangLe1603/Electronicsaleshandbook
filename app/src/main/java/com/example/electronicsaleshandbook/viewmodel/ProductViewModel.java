package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.SheetRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ProductViewModel extends ViewModel {
    private final SheetRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public ProductViewModel(Context context) {
        try {
            repository = new SheetRepository(context);
            allProducts = repository.getProducts();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize repository", e);
        }
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<List<Product>> getFilteredProducts() {
        return Transformations.switchMap(searchQuery, query -> {
            MutableLiveData<List<Product>> filteredData = new MutableLiveData<>();
            allProducts.observeForever(products -> {
                if (query == null || query.isEmpty()) {
                    filteredData.setValue(products);
                } else {
                    List<Product> filteredList = products.stream()
                            .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                            .collect(Collectors.toList());
                    filteredData.setValue(filteredList);
                }
            });
            return filteredData;
        });
    }

    public void refreshProducts() {
        searchQuery.setValue("");
        repository.refreshProducts();
    }

//    public LiveData<List<Product>> getAllProducts() {
//        return allProducts;
//    }
}