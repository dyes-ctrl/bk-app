package com.secure.taxapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.secure.taxapp.services.CalculationService;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int ROLE_REQUEST_CODE = 200;
    
    private ConfigManager configManager;
    private Switch mainToggle;
    private Button toggleVisibilityButton;
    private Button addButton;
    private EditText numberInput;
    private EditText dialNumber;
    private RecyclerView recyclerView;
    private NumberAdapter adapter;
    private LinearLayout dialerLayout;
    private LinearLayout numbersLayout;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        configManager = new ConfigManager(this);
        
        initViews();
        setupListeners();
        checkPermissions();
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NumberAdapter(configManager.getNumbers(), configManager.isMasked());
        recyclerView.setAdapter(adapter);
        
        updateVisibilityButton();
    }
    
    private void initViews() {
        mainToggle = findViewById(R.id.mainToggle);
        toggleVisibilityButton = findViewById(R.id.toggleVisibilityButton);
        addButton = findViewById(R.id.addButton);
        numberInput = findViewById(R.id.numberInput);
        dialNumber = findViewById(R.id.dialNumber);
        recyclerView = findViewById(R.id.numbersRecyclerView);
        dialerLayout = findViewById(R.id.dialerLayout);
        numbersLayout = findViewById(R.id.numbersLayout);
        
        mainToggle.setChecked(configManager.isServiceActive());
    }
    
    private void setupListeners() {
        mainToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.setServiceActive(isChecked);
            if (isChecked) {
                startService(new Intent(this, CalculationService.class));
                Toast.makeText(this, "Blocking Active", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, CalculationService.class));
                Toast.makeText(this, "Blocking Inactive", Toast.LENGTH_SHORT).show();
            }
        });
        
        findViewById(R.id.tabDialer).setOnClickListener(v -> {
            dialerLayout.setVisibility(View.VISIBLE);
            numbersLayout.setVisibility(View.GONE);
        });
        
        findViewById(R.id.tabNumbers).setOnClickListener(v -> {
            dialerLayout.setVisibility(View.GONE);
            numbersLayout.setVisibility(View.VISIBLE);
        });
        
        toggleVisibilityButton.setOnClickListener(v -> {
            boolean newMasked = !configManager.isMasked();
            configManager.setMasked(newMasked);
            adapter.setMasked(newMasked);
            updateVisibilityButton();
        });
        
        addButton.setOnClickListener(v -> {
            String number = numberInput.getText().toString().trim();
            if (com.secure.taxapp.utils.NumberValidator.isValid(number)) {
                configManager.addNumber(number);
                numberInput.setText("");
                refreshList();
                Toast.makeText(this, "Number Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            }
        });
        
        setupDialerButtons();
    }
    
    private void setupDialerButtons() {
        int[] buttonIds = {R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                          R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                          R.id.btnStar, R.id.btnHash};
        
        String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "#"};
        
        for (int i = 0; i < buttonIds.length; i++) {
            Button btn = findViewById(buttonIds[i]);
            final String value = values[i];
            btn.setOnClickListener(v -> {
                String current = dialNumber.getText().toString();
                dialNumber.setText(current + value);
            });
        }
        
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            String current = dialNumber.getText().toString();
            if (current.length() > 0) {
                dialNumber.setText(current.substring(0, current.length() - 1));
            }
        });
        
        findViewById(R.id.btnCall).setOnClickListener(v -> {
            String number = dialNumber.getText().toString();
            if (!number.isEmpty()) {
                makeCall(number);
            }
        });
    }
    
    private void makeCall(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Call permission required", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateVisibilityButton() {
        toggleVisibilityButton.setText(configManager.isMasked() ? "Show Numbers" : "Hide Numbers");
    }
    
    private void refreshList() {
        adapter.updateData(configManager.getNumbers());
    }
    
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CALL_PHONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }
        
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            checkPhoneRole();
        }
    }
    
    private void checkPhoneRole() {
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null) {
            String defaultDialer = telecomManager.getDefaultDialerPackage();
            if (!getPackageName().equals(defaultDialer)) {
                requestPhoneRole();
            }
        }
    }
    
    private void requestPhoneRole() {
        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
        startActivityForResult(intent, ROLE_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            checkPhoneRole();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ROLE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                showManualConfigDialog();
            }
        }
    }
    
    private void showManualConfigDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Required Configuration")
            .setMessage("BK must be set as your default Phone app to block calls instantly. Open settings?")
            .setPositiveButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
