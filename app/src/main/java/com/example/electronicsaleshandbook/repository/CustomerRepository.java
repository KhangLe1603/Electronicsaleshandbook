package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.Customer;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerRepository {
    private static CustomerRepository instance;
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "KhachHang!B2:I"; // B: MÃ KH, C: Họ, D: Tên, ..., I: Giới tính
    private static final long MIN_REFRESH_INTERVAL = 2000;

    private final Sheets service;
    private final MutableLiveData<List<Customer>> customersLiveData = new MutableLiveData<>();
    private List<Customer> cachedCustomers = null;
    private long lastRefreshTime = 0;
    private boolean isRefreshing = false;
    private int requestCount = 0;

    private CustomerRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            GoogleCredentials credentials = GoogleCredentials.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
            HttpCredentialsAdapter requestInitializer = new HttpCredentialsAdapter(credentials);

            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    jsonFactory,
                    requestInitializer)
                    .setApplicationName("Customer List App")
                    .build();

            fetchCustomersWithBackoff(0, 5); // Initial fetch
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public static synchronized CustomerRepository getInstance(Context context) throws IOException, GeneralSecurityException {
        if (instance == null) {
            instance = new CustomerRepository(context);
        }
        return instance;
    }

    public LiveData<List<Customer>> getCustomers() {
        synchronized (this) {
            if (isRefreshing) {
                Log.d("CustomerRepository", "Skipping refresh, already refreshing");
                return customersLiveData;
            }
            if (cachedCustomers != null) {
                Log.d("CustomerRepository", "Using cached customers, size: " + cachedCustomers.size());
                customersLiveData.postValue(cachedCustomers);
                return customersLiveData;
            }
            isRefreshing = true;
        }
        fetchCustomersWithBackoff(0, 5);
        return customersLiveData;
    }

    public void refreshCustomers() {
        synchronized (this) {
            if (System.currentTimeMillis() - lastRefreshTime < MIN_REFRESH_INTERVAL || isRefreshing) {
                Log.d("CustomerRepository", "Skipping refresh, too soon or already refreshing");
                if (cachedCustomers != null) {
                    customersLiveData.postValue(cachedCustomers);
                }
                return;
            }
            isRefreshing = true;
            lastRefreshTime = System.currentTimeMillis();
        }
        fetchCustomersWithBackoff(0, 5);
    }

    public Sheets getSheetsService() {
        return service;
    }

    private void fetchCustomersWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                synchronized (this) {
                    requestCount++;
                    Log.d("CustomerRepository", "Sending request #" + requestCount + " for customers");
                }
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();
                List<Customer> customers = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String id = row.size() > 0 ? row.get(0).toString() : ""; // B: MÃ KH
                        String surname = row.size() > 1 ? row.get(1).toString() : ""; // C: Họ
                        String firstName = row.size() > 2 ? row.get(2).toString() : ""; // D: Tên
                        String address = row.size() > 3 ? row.get(3).toString() : ""; // E: Địa chỉ
                        String phone = row.size() > 4 ? row.get(4).toString() : ""; // F: Phone
                        String email = row.size() > 5 ? row.get(5).toString() : ""; // G: Email
                        String birthday = row.size() > 6 ? row.get(6).toString() : ""; // H: Ngày sinh
                        String gender = row.size() > 7 ? row.get(7).toString() : ""; // I: Giới tính
                        if (id.isEmpty()) {
                            Log.w("CustomerRepository", "Skipping invalid row " + (i + 2) + ": id is empty");
                            continue;
                        }
                        Customer customer = new Customer(surname, firstName, address, phone, email, birthday, gender);
                        customer.setId(id);
                        customer.setSheetRowIndex(i + 2);
                        customers.add(customer);
                        Log.d("CustomerRepository", "Parsed customer: id=" + id + ", name=" + firstName);
                    }
                } else {
                    Log.d("CustomerRepository", "No customers found in " + RANGE);
                }
                synchronized (this) {
                    cachedCustomers = customers;
                    customersLiveData.postValue(customers);
                    isRefreshing = false;
                    Log.d("CustomerRepository", "Fetched customers, size: " + customers.size());
                }
            } catch (GoogleJsonResponseException e) {
                synchronized (this) {
                    isRefreshing = false;
                }
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("CustomerRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + ")");
                    try {
                        Thread.sleep(delay);
                        fetchCustomersWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("CustomerRepository", "Interrupted during backoff", ie);
                    }
                } else {
                    Log.e("CustomerRepository", "Error fetching customers: " + e.getStatusCode() + " - " + e.getDetails().getMessage(), e);
                    customersLiveData.postValue(new ArrayList<>());
                }
            } catch (IOException e) {
                synchronized (this) {
                    isRefreshing = false;
                }
                Log.e("CustomerRepository", "IO error fetching customers", e);
                customersLiveData.postValue(new ArrayList<>());
            }
        }).start();
    }

    public void invalidateCache() {
        synchronized (this) {
            cachedCustomers = null;
            Log.d("CustomerRepository", "Customer cache invalidated");
        }
    }
}