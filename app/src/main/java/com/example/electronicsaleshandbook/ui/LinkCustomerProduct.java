package com.example.electronicsaleshandbook.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.electronicsaleshandbook.R;
import com.example.electronicsaleshandbook.model.Customer;
import com.example.electronicsaleshandbook.model.CustomerProductLink;
import com.example.electronicsaleshandbook.model.Product;
import com.example.electronicsaleshandbook.viewmodel.LinkViewModel;
import com.example.electronicsaleshandbook.util.LinkViewModelFactory;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class LinkCustomerProduct extends AppCompatActivity {
    private Spinner customerSpinner, productSpinner;
    private Button linkButton;
    private TextView linkStatusText;
    private LinkViewModel viewModel;
    private FirebaseAuth firebaseAuth;
    private List<CustomerProductLink> currentLinks;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link_customer_product);

        customerSpinner = findViewById(R.id.customerSpinner);
        productSpinner = findViewById(R.id.productSpinner);
        linkButton = findViewById(R.id.linkButton);
        linkStatusText = findViewById(R.id.linkStatusText);
        firebaseAuth = FirebaseAuth.getInstance();
        btnBack = findViewById(R.id.btnBack);

        View Layout = findViewById(R.id.addLinkCustomer);

        ViewCompat.setOnApplyWindowInsetsListener(Layout, (v, insets) -> {
            Insets statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(
                    v.getPaddingLeft(),
                    statusBarInsets.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        // Kiểm tra đăng nhập
        if (firebaseAuth.getCurrentUser() == null) {
            Log.d("LinkCustomerProductActivity", "User not logged in, redirecting to LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());

        // Khởi tạo ViewModel với Factory
        try {
            LinkViewModelFactory factory = new LinkViewModelFactory(this);
            viewModel = new ViewModelProvider(this, factory).get(LinkViewModel.class);
            Log.d("LinkCustomerProductActivity", "LinkViewModel initialized successfully");
        } catch (RuntimeException e) {
            Log.e("LinkCustomerProductActivity", "Failed to initialize LinkViewModel: " + e.getMessage(), e);
            Toast.makeText(this, "Lỗi khởi tạo: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Quan sát danh sách khách hàng
        viewModel.getCustomers().observe(this, customers -> {
            ArrayAdapter<Customer> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, customers != null ? customers : new ArrayList<>()) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Customer customer = getItem(position);
                    if (customer != null) {
                        String displayText = (customer.getSurname() + " " + customer.getFirstName()).trim() +
                                " (" + customer.getPhone() + ")";
                        view.setText(displayText);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Customer customer = getItem(position);
                    if (customer != null) {
                        String displayText = (customer.getSurname() + " " + customer.getFirstName()).trim() +
                                " (" + customer.getPhone() + ")";
                        view.setText(displayText);
                    }
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            customerSpinner.setAdapter(adapter);
            Log.d("LinkCustomerProductActivity", "Customer spinner updated, size: " + (customers != null ? customers.size() : 0));
            updateLinkStatus();
        });

        // Quan sát danh sách sản phẩm
        viewModel.getProducts().observe(this, products -> {
            ArrayAdapter<Product> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, products != null ? products : new ArrayList<>()) {
                @Override
                public View getView(int position, View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getView(position, convertView, parent);
                    Product product = getItem(position);
                    if (product != null) {
                        view.setText(product.getName());
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                    TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                    Product product = getItem(position);
                    if (product != null) {
                        view.setText(product.getName());
                    }
                    return view;
                }
            };
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            productSpinner.setAdapter(adapter);
            Log.d("LinkCustomerProductActivity", "Product spinner updated, size: " + (products != null ? products.size() : 0));
            updateLinkStatus();
        });

        // Quan sát danh sách liên kết
        viewModel.getLinks().observe(this, links -> {
            currentLinks = links != null ? links : new ArrayList<>();
            Log.d("LinkCustomerProductActivity", "Links updated, size: " + currentLinks.size());
            updateLinkStatus();
        });

        // Quan sát kết quả tạo liên kết
        viewModel.getLinkResult().observe(this, result -> {
            if (result != null) { // Chỉ xử lý nếu result không null
                Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
                Log.d("LinkCustomerProductActivity", "Link creation result: " + result);
                if (result.startsWith("Tạo liên kết thành công")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("REFRESH", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        // Xử lý sự kiện chọn Spinner
        AdapterView.OnItemSelectedListener selectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLinkStatus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                updateLinkStatus();
            }
        };
        customerSpinner.setOnItemSelectedListener(selectionListener);
        productSpinner.setOnItemSelectedListener(selectionListener);

        // Xử lý nút tạo liên kết
        linkButton.setOnClickListener(v -> {
            Customer customer = (Customer) customerSpinner.getSelectedItem();
            Product product = (Product) productSpinner.getSelectedItem();
            if (customer == null || product == null) {
                Toast.makeText(this, "Vui lòng chọn khách hàng và sản phẩm", Toast.LENGTH_SHORT).show();
                Log.d("LinkCustomerProductActivity", "Invalid selection: customer=" + customer + ", product=" + product);
                return;
            }
            CustomerProductLink link = new CustomerProductLink(customer.getId(), product.getId());
            String customerFullName = (customer.getSurname() + " " + customer.getFirstName()).trim();
            String productName = product.getName();
            Log.d("LinkCustomerProductActivity", "Creating link: " + customer.getId() + " - " + product.getId() +
                    ", Customer: " + customerFullName + ", Product: " + productName);
            viewModel.createLink(link, customerFullName, productName);
        });
    }

    private void updateLinkStatus() {
        Customer customer = (Customer) customerSpinner.getSelectedItem();
        Product product = (Product) productSpinner.getSelectedItem();
        boolean hasSelection = customer != null && product != null;

        if (!hasSelection) {
            linkButton.setEnabled(false);
            linkStatusText.setVisibility(View.GONE);
            Log.d("LinkCustomerProductActivity", "No selection, disabling link button");
            return;
        }

        boolean linkExists = currentLinks != null && currentLinks.stream()
                .anyMatch(link -> link.getCustomerId().equals(customer.getId()) &&
                        link.getProductId().equals(product.getId()));

        if (linkExists) {
            linkStatusText.setVisibility(View.VISIBLE);
            linkButton.setEnabled(false);
            Log.d("LinkCustomerProductActivity", "Link exists: " + customer.getId() + " - " + product.getId());
        } else {
            linkStatusText.setVisibility(View.GONE);
            linkButton.setEnabled(true);
            Log.d("LinkCustomerProductActivity", "Link does not exist, enabling link button");
        }
    }
}