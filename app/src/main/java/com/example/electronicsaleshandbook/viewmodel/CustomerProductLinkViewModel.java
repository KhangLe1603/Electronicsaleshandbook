package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.repository.CustomerRepository;
import com.example.electronicsaleshandbook.repository.SheetRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class CustomerProductLinkViewModel extends ViewModel {
    private final SheetRepository repository;

    public CustomerProductLinkViewModel(Context context) throws IOException, GeneralSecurityException {
        repository = SheetRepository.getInstance(context);
    }

    public LiveData<List<CustomerProductLink>> getLinks() {
        return repository.getLinks();
    }

    public void refreshLinks() {
        repository.invalidateCache();
        repository.refreshLinks();
    }

}