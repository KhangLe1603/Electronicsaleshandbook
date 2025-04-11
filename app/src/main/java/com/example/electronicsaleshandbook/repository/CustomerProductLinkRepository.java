package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.example.electronicsaleshandbook.model.CustomerProductLink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomerProductLinkRepository {
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final String RANGE = "CustomerProductLink!A2:C";
    private static final long MIN_REFRESH_INTERVAL = 2000;

    private final Sheets service;
    private final MutableLiveData<List<CustomerProductLink>> linksLiveData = new MutableLiveData<>();
    private List<CustomerProductLink> cachedLinks = null; // Cache dữ liệu liên kết
    private long lastRefreshTime = 0;
    private boolean isRefreshing = false;
    private int requestCount = 0; // Đếm số request

    public CustomerProductLinkRepository(Context context) throws IOException, GeneralSecurityException {
        try {
            GoogleCredential credential = GoogleCredential.fromStream(
                            context.getAssets().open("service_account.json"))
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

            service = new Sheets.Builder(
                    new NetHttpTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Your App Name")
                    .build();
            if (cachedLinks == null) {
                fetchLinksWithBackoff(0, 5); // Lấy dữ liệu lần đầu nếu cache trống
            } else {
                linksLiveData.postValue(cachedLinks); // Dùng cache nếu đã có
            }
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public LiveData<List<CustomerProductLink>> getLinks() {
        return linksLiveData;
    }

    public void refreshLinks() {
        synchronized (this) {
            if (isRefreshing) {
                Log.d("CustomerProductLinkRepository", "Skipping refresh, already refreshing");
                if (cachedLinks != null) {
                    linksLiveData.postValue(cachedLinks);
                }
                return;
            }
            // Chỉ làm mới nếu cache đã bị vô hiệu hóa
            if (cachedLinks != null) {
                Log.d("CustomerProductLinkRepository", "Using cached links, size: " + cachedLinks.size());
                linksLiveData.postValue(cachedLinks);
                return;
            }
            isRefreshing = true;
            lastRefreshTime = System.currentTimeMillis();
        }
        fetchLinksWithBackoff(0, 5);
        synchronized (this) {
            isRefreshing = false;
        }
    }

    public Sheets getSheetsService() {
        return service;
    }

    private void fetchLinksWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                requestCount++;
                Log.d("CustomerProductLinkRepository", "Sending request #" + requestCount);
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, RANGE)
                        .execute();
                List<CustomerProductLink> links = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String customerId = row.size() > 1 ? row.get(1).toString() : "";
                        String productId = row.size() > 2 ? row.get(2).toString() : "";
                        CustomerProductLink link = new CustomerProductLink(customerId, productId);
                        link.setSheetRowIndex(i + 2);
                        links.add(link);
                    }
                }
                cachedLinks = links;
                linksLiveData.postValue(links);
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("CustomerProductLinkRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + "/" + maxAttempts + ")");
                    try {
                        Thread.sleep(delay);
                        fetchLinksWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("CustomerProductLinkRepository", "Interrupted during backoff", ie);
                        if (cachedLinks != null) linksLiveData.postValue(cachedLinks);
                    }
                } else {
                    Log.e("CustomerProductLinkRepository", "Error fetching links: " + e.getStatusCode(), e);
                    if (cachedLinks != null) linksLiveData.postValue(cachedLinks);
                }
            } catch (IOException e) {
                Log.e("CustomerProductLinkRepository", "IO error fetching links", e);
                if (cachedLinks != null) linksLiveData.postValue(cachedLinks);
            }
        }).start();
    }

    // Phương thức để vô hiệu hóa cache khi có thay đổi
    public void invalidateCache() {
        cachedLinks = null;
        Log.d("CustomerProductLinkRepository", "Link cache invalidated");
    }
}