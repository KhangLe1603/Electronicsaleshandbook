package com.example.electronicsaleshandbook.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.electronicsaleshandbook.model.AuthResult;
import com.example.electronicsaleshandbook.repository.AuthRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class AuthViewModel extends ViewModel {
    private final AuthRepository repository;

    public AuthViewModel(Context context) {
        try {
            repository = AuthRepository.getInstance(context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize AuthRepository", e);
        }
    }

    public LiveData<AuthResult> getAuthResult() {
        return repository.getAuthResult();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return repository.getCurrentUser();
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return repository.getGoogleSignInClient();
    }

    public void loginWithEmail(String email, String password) {
        repository.loginWithEmail(email, password);
    }

    public void registerWithEmail(String email, String password) {
        repository.registerWithEmail(email, password);
    }

    public void signInWithGoogle(GoogleSignInAccount account) {
        repository.signInWithGoogle(account);
    }

    public void signOut() {
        repository.signOut();
    }
}