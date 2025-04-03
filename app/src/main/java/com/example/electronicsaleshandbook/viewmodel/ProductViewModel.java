package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.repository.SheetRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class ProductViewModel extends ViewModel {
    private final SheetRepository repository;
    private final LiveData<List<Product>> products;

    public ProductViewModel(Context context) {
        try {
            repository = new SheetRepository(context);
            products = repository.getProducts();
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException("Failed to initialize repository", e);
        }
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }
}