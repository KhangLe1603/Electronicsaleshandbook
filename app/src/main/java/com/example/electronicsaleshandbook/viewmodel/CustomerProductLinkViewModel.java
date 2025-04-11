package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.repository.CustomerProductLinkRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class CustomerProductLinkViewModel extends ViewModel {
    private final CustomerProductLinkRepository repository;
    private final LiveData<List<CustomerProductLink>> links;

    public CustomerProductLinkViewModel(Context context) throws IOException, GeneralSecurityException {
        repository = new CustomerProductLinkRepository(context);
        links = repository.getLinks();
    }

    public LiveData<List<CustomerProductLink>> getLinks() {
        return links;
    }
}