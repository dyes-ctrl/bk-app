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
import java.util.Set;

/**
 * Activite principale
 */
public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DEFAULT_APP_REQUEST_CODE = 200;
    
    private ConfigManager configManager;
    private Switch mainToggle;
    private Button toggleVisibilityButton;
    private Button addButton;
    private EditText numberInput;
    private RecyclerView recyclerView;
    private NumberAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        configManager = new ConfigManager(this);
        
        initViews();
        setupListeners();
        checkPermissions();
        
        // Configurer RecyclerView
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
        recyclerView = findViewById(R.id.numbersRecyclerView);
        
        // Etat initial du toggle
        mainToggle.setChecked(configManager.isServiceActive());
    }
    
    private void setupListeners() {
        mainToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            configManager.setServiceActive(isChecked);
            if (isChecked) {
                startService(new Intent(this, CalculationService.class));
                Toast.makeText(this, "Service active", Toast.LENGTH_SHORT).show();
            } else {
                stopService(new Intent(this, CalculationService.class));
                Toast.makeText(this, "Service desactive", Toast.LENGTH_SHORT).show();
            }
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
                Toast.makeText(this, "Numero ajoute", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.invalid_number, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateVisibilityButton() {
        if (configManager.isMasked()) {
            toggleVisibilityButton.setText(R.string.show_numbers);
        } else {
            toggleVisibilityButton.setText(R.string.hide_numbers);
        }
    }
    
    private void refreshList() {
        adapter.updateData(configManager.getNumbers());
    }
    
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) 
                != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_CALL_LOG);
        }
        
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, 
                permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            checkDefaultApp();
        }
    }
    
    private void checkDefaultApp() {
        TelecomManager telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (telecomManager != null) {
            String defaultDialer = telecomManager.getDefaultDialerPackage();
            if (!getPackageName().equals(defaultDialer)) {
                showDefaultAppDialog();
            }
        }
    }
    
    private void showDefaultAppDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Configuration requise")
            .setMessage("Cette application doit etre definie comme application d'appel par defaut pour fonctionner correctement.")
            .setPositiveButton("Configurer", (dialog, which) -> {
                Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
                intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, getPackageName());
                startActivityForResult(intent, DEFAULT_APP_REQUEST_CODE);
            })
            .setNegativeButton("Annuler", null)
            .show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            checkDefaultApp();
        }
    }
}
