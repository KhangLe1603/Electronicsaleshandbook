package com.example.electronicsaleshandbook.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.model.Product;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SheetRepository {
    private static SheetRepository instance;
    private static final String SPREADSHEET_ID = "1T0vRbdFnjTUTKkgcpbSuvjNnbG9eD49j_xjlknWtj_A";
    private static final long MIN_REFRESH_INTERVAL = 2000; // 2 giây giữa các refresh

    private final Sheets service;
    private final MutableLiveData<List<Product>> productsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CustomerProductLink>> linksLiveData = new MutableLiveData<>();
    private List<Product> cachedProducts = null;
    private List<CustomerProductLink> cachedLinks = null;
    private long lastRefreshTime = 0;
    private boolean isRefreshingProducts = false;
    private boolean isRefreshingLinks = false;
    private int requestCount = 0;

    //test
    private final MutableLiveData<String> linkResultLiveData = new MutableLiveData<>();

    private SheetRepository(Context context) throws IOException, GeneralSecurityException {
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
        } catch (FileNotFoundException e) {
            throw new IOException("Service account file not found in assets", e);
        }
    }

    public static synchronized SheetRepository getInstance(Context context) throws IOException, GeneralSecurityException {
        if (instance == null) {
            instance = new SheetRepository(context);
        }
        return instance;
    }


    public LiveData<List<Product>> getProducts() {
        synchronized (this) {
            if (isRefreshingProducts) {
                Log.d("SheetRepository", "Skipping product refresh, already refreshing");
                return productsLiveData;
            }
            if (cachedProducts != null) {
                Log.d("SheetRepository", "Using cached products, size: " + cachedProducts.size());
                productsLiveData.postValue(cachedProducts);
                return productsLiveData;
            }
            isRefreshingProducts = true;
        }
        fetchProductsWithBackoff(0, 5);
        return productsLiveData;
    }

    // Trong SheetRepository
    public void refreshLinks() {
        synchronized (this) {
            if (cachedLinks == null || (!isRefreshingLinks && System.currentTimeMillis() - lastRefreshTime >= MIN_REFRESH_INTERVAL)) {
                isRefreshingLinks = true;
                lastRefreshTime = System.currentTimeMillis();
                Log.d("SheetRepository", "Starting fetchLinksWithBackoff for links");
                fetchLinksWithBackoff(0, 5);
            } else {
                Log.d("SheetRepository", "Skipping link refresh, using cache or already refreshing");
                if (cachedLinks != null) {
                    linksLiveData.postValue(cachedLinks);
                }
            }
        }
    }

    public LiveData<List<CustomerProductLink>> getLinks() {
        synchronized (this) {
            if (isRefreshingLinks) {
                Log.d("SheetRepository", "Skipping link refresh, already refreshing");
                return linksLiveData;
            }
            if (cachedLinks != null) {
                Log.d("SheetRepository", "Using cached links, size: " + cachedLinks.size());
                linksLiveData.postValue(cachedLinks);
                return linksLiveData;
            }
            isRefreshingLinks = true;
        }
        fetchLinksWithBackoff(0, 5);
        return linksLiveData;
    }

    public void refreshProducts() {
        synchronized (this) {
            if (System.currentTimeMillis() - lastRefreshTime < MIN_REFRESH_INTERVAL || isRefreshingProducts) {
                Log.d("SheetRepository", "Skipping product refresh, too soon or already refreshing");
                if (cachedProducts != null) {
                    productsLiveData.postValue(cachedProducts);
                }
                return;
            }
            isRefreshingProducts = true;
            lastRefreshTime = System.currentTimeMillis();
        }
        fetchProductsWithBackoff(0, 5);
    }

    public void invalidateCache() {
        synchronized (this) {
            cachedProducts = null;
            cachedLinks = null;
            Log.d("SheetRepository", "Cache invalidated");
        }
    }

    public Sheets getSheetsService() {
        return service;
    }

    //test
    public LiveData<String> getLinkResult() {
        return linkResultLiveData;
    }

    //test
    public void createLink(CustomerProductLink link, String customerFullName, String productName) {
        new Thread(() -> {
            try {
                // Tìm hàng trống tiếp theo dựa trên cột D
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "CustomerProductLink!D:D")
                        .execute();
                List<List<Object>> values = response.getValues();
                int nextRow = 2; // Bắt đầu từ hàng 2
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        if (values.get(i).isEmpty() || values.get(i).get(0).toString().trim().isEmpty()) {
                            nextRow = i + 2;
                            break;
                        }
                    }
                    // Nếu không tìm thấy dòng trống, dùng dòng sau dòng cuối cùng
                    if (nextRow == 2 && values.size() > 0) {
                        nextRow = values.size() + 1; // ✅ Sửa từ +2 thành +1
                        // Loại bỏ các dòng trống ở cuối
                        while (!values.isEmpty() && (values.get(values.size() - 1).isEmpty() ||
                                values.get(values.size() - 1).get(0).toString().trim().isEmpty())) {
                            values.remove(values.size() - 1);
                            nextRow--;
                        }
                    }
                }

                Log.d("SheetRepository", "Values size: " + (values != null ? values.size() : 0) +
                        ", Next row: " + nextRow);

                // Ghi dữ liệu vào cột D và E
                ValueRange body = new ValueRange()
                        .setValues(Arrays.asList(Arrays.asList(
                                customerFullName, // Cột D: Tên đầy đủ
                                productName // Cột E: Tên sản phẩm
                        )));
                String range = "CustomerProductLink!D" + nextRow + ":E" + nextRow;
                Log.d("SheetRepository", "Updating range: " + range + ", Data: " + body.getValues());
                service.spreadsheets().values()
                        .update(SPREADSHEET_ID, range, body)
                        .setValueInputOption("RAW")
                        .execute();
                synchronized (this) {
                    if (cachedLinks != null) {
                        cachedLinks.add(link);
                        linksLiveData.postValue(cachedLinks);
                    }
                }
                linkResultLiveData.postValue("Tạo liên kết thành công");
                linkResultLiveData.postValue(null);
                Log.d("SheetRepository", "Link created: Customer: " + customerFullName + ", Product: " + productName + ", Row: " + nextRow);
            } catch (GoogleJsonResponseException e) {
                linkResultLiveData.postValue("Lỗi: " + e.getDetails().getMessage());
                linkResultLiveData.postValue(null);
                Log.e("SheetRepository", "Error creating link: " + e.getStatusCode(), e);
            } catch (IOException e) {
                linkResultLiveData.postValue("Lỗi: " + e.getMessage());
                linkResultLiveData.postValue(null);
                Log.e("SheetRepository", "IO error creating link", e);
            }
        }).start();
    }

    private void fetchProductsWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                synchronized (this) {
                    requestCount++;
                    Log.d("SheetRepository", "Sending request #" + requestCount + " for products");
                }
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "Sheet1!A2:G")
                        .execute();
                List<Product> products = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        String id = row.size() > 1 ? row.get(1).toString() : "";
                        String name = row.size() > 2 ? row.get(2).toString() : "";
                        String description = row.size() > 3 ? row.get(3).toString() : "";
                        String unitPrice = row.size() > 4 ? row.get(4).toString() : "";
                        String sellingPrice = row.size() > 5 ? row.get(5).toString() : "";
                        String unit = row.size() > 6 ? row.get(6).toString() : "";
                        Product product = new Product(name, description, unitPrice, sellingPrice, unit);
                        product.setId(id);
                        product.setSheetRowIndex(i + 2);
                        products.add(product);
                    }
                }
                synchronized (this) {
                    cachedProducts = products;
                    productsLiveData.postValue(products);
                    isRefreshingProducts = false;
                    Log.d("SheetRepository", "Fetched products, size: " + products.size());
                }
            } catch (GoogleJsonResponseException e) {
                synchronized (this) {
                    isRefreshingProducts = false;
                }
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("SheetRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + ")");
                    try {
                        Thread.sleep(delay);
                        fetchProductsWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("SheetRepository", "Interrupted during backoff", ie);
                    }
                } else {
                    Log.e("SheetRepository", "Error fetching products: " + e.getStatusCode(), e);
                }
            } catch (IOException e) {
                synchronized (this) {
                    isRefreshingProducts = false;
                }
                Log.e("SheetRepository", "IO error fetching products", e);
            }
        }).start();
    }

    private void fetchLinksWithBackoff(int attempt, int maxAttempts) {
        new Thread(() -> {
            try {
                synchronized (this) {
                    requestCount++;
                    Log.d("SheetRepository", "Sending request #" + requestCount + " for links");
                }
                // Changed range to B2:C to fetch MÃ KH (customerId) and MÃ SP (productId)
                ValueRange response = service.spreadsheets().values()
                        .get(SPREADSHEET_ID, "CustomerProductLink!B2:C")
                        .execute();
                List<CustomerProductLink> links = new ArrayList<>();
                List<List<Object>> values = response.getValues();
                if (values != null) {
                    for (int i = 0; i < values.size(); i++) {
                        List<Object> row = values.get(i);
                        // Column B: MÃ KH (customerId)
                        String customerId = row.size() > 0 ? row.get(0).toString() : "";
                        // Column C: MÃ SP (productId)
                        String productId = row.size() > 1 ? row.get(1).toString() : "";
                        if (customerId.isEmpty() || productId.isEmpty()) {
                            Log.w("SheetRepository", "Skipping invalid row " + (i + 2) + ": customerId=" + customerId + ", productId=" + productId);
                            continue;
                        }
                        CustomerProductLink link = new CustomerProductLink(customerId, productId);
                        link.setSheetRowIndex(i + 2);
                        links.add(link);
                        Log.d("SheetRepository", "Parsed link: customerId=" + customerId + ", productId=" + productId);
                    }
                } else {
                    Log.d("SheetRepository", "No links found in CustomerProductLink!B2:C");
                }
                synchronized (this) {
                    cachedLinks = links;
                    linksLiveData.postValue(links);
                    isRefreshingLinks = false;
                    Log.d("SheetRepository", "Fetched links, size: " + links.size());
                }
            } catch (GoogleJsonResponseException e) {
                synchronized (this) {
                    isRefreshingLinks = false;
                }
                if (e.getStatusCode() == 429 && attempt < maxAttempts) {
                    long delay = (long) Math.pow(2, attempt) * 1000;
                    Log.w("SheetRepository", "Quota exceeded, retrying in " + delay + "ms (attempt " + (attempt + 1) + ")");
                    try {
                        Thread.sleep(delay);
                        fetchLinksWithBackoff(attempt + 1, maxAttempts);
                    } catch (InterruptedException ie) {
                        Log.e("SheetRepository", "Interrupted during backoff", ie);
                    }
                } else {
                    Log.e("SheetRepository", "Error fetching links: " + e.getStatusCode() + " - " + e.getDetails().getMessage(), e);
                    linksLiveData.postValue(new ArrayList<>()); // Return empty list to prevent UI issues
                }
            } catch (IOException e) {
                synchronized (this) {
                    isRefreshingLinks = false;
                }
                Log.e("SheetRepository", "IO error fetching links", e);
                linksLiveData.postValue(new ArrayList<>()); // Return empty list to prevent UI issues
            }
        }).start();
    }
}