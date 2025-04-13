package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.AuthResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class AuthRepository {
    private static AuthRepository instance;
    private final FirebaseAuth firebaseAuth;
    private final GoogleSignInClient googleSignInClient;
    private final MutableLiveData<AuthResult> authResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<FirebaseUser> currentUserLiveData = new MutableLiveData<>();

    private AuthRepository(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("492706936531-qppvdsgr5p6jhhe2439cit6kmb8r1deg.apps.googleusercontent.com") // Get from Firebase Console > Authentication > Google > Web client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(context, gso);
        // Observe current user
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        currentUserLiveData.postValue(currentUser);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    public LiveData<AuthResult> getAuthResult() {
        return authResultLiveData;
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return currentUserLiveData;
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void loginWithEmail(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            authResultLiveData.postValue(new AuthResult(true, "Đăng nhập thành công", user.getUid()));
                            currentUserLiveData.postValue(user);
                            Log.d("AuthRepository", "Email login successful: " + user.getEmail());
                        } else {
                            authResultLiveData.postValue(new AuthResult(false, "Không tìm thấy người dùng", null));
                            Log.w("AuthRepository", "Email login failed: user is null");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng nhập";
                        authResultLiveData.postValue(new AuthResult(false, error, null));
                        Log.e("AuthRepository", "Email login failed: " + error);
                    }
                });
    }

    public void registerWithEmail(String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            authResultLiveData.postValue(new AuthResult(true, "Đăng ký thành công", user.getUid()));
                            currentUserLiveData.postValue(user);
                            Log.d("AuthRepository", "Email registration successful: " + user.getEmail());
                        } else {
                            authResultLiveData.postValue(new AuthResult(false, "Không tạo được người dùng", null));
                            Log.w("AuthRepository", "Email registration failed: user is null");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng ký";
                        authResultLiveData.postValue(new AuthResult(false, error, null));
                        Log.e("AuthRepository", "Email registration failed: " + error);
                    }
                });
    }

    public void signInWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            authResultLiveData.postValue(new AuthResult(true, "Đăng nhập Google thành công", user.getUid()));
                            currentUserLiveData.postValue(user);
                            Log.d("AuthRepository", "Google sign-in successful: " + user.getEmail());
                        } else {
                            authResultLiveData.postValue(new AuthResult(false, "Không tìm thấy người dùng Google", null));
                            Log.w("AuthRepository", "Google sign-in failed: user is null");
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Lỗi đăng nhập Google";
                        authResultLiveData.postValue(new AuthResult(false, error, null));
                        Log.e("AuthRepository", "Google sign-in failed: " + error);
                    }
                });
    }

    public void signOut() {
        firebaseAuth.signOut();
        googleSignInClient.signOut();
        currentUserLiveData.postValue(null);
        Log.d("AuthRepository", "User signed out");
    }
}