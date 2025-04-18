package com.example.electronicsaleshandbook.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.viewmodel.LinkViewModel;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class LinkViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    public LinkViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LinkViewModel.class)) {
            try {
                return (T) new LinkViewModel(context);
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException("Cannot create LinkViewModel", e);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}