package com.bk.callblocker;

import android.Manifest;
import android.app.role.RoleManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private WhitelistManager whitelist;
    private NumberAdapter adapter;

    private TextView statusText;
    private Button roleButton;
    private Switch filterSwitch;
    private Switch privateSwitch;
    private EditText numberInput;

    private final ActivityResultLauncher<Intent> roleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> updateUI());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        whitelist = new WhitelistManager(this);

        statusText  = findViewById(R.id.statusText);
        roleButton  = findViewById(R.id.roleButton);
        filterSwitch = findViewById(R.id.filterSwitch);
        privateSwitch = findViewById(R.id.privateSwitch);
        numberInput = findViewById(R.id.numberInput);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        Button addButton = findViewById(R.id.addButton);

        // RecyclerView
        adapter = new NumberAdapter(whitelist.getNumbers(), number -> {
            whitelist.removeNumber(number);
            adapter.updateList(whitelist.getNumbers());
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Request call screening role
        roleButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                RoleManager rm = getSystemService(RoleManager.class);
                Intent intent = rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                roleLauncher.launch(intent);
            }
        });

        // Enable/disable filter
        filterSwitch.setOnCheckedChangeListener((btn, checked) -> {
            whitelist.setEnabled(checked);
            updateUI();
        });

        // Block private numbers toggle
        privateSwitch.setOnCheckedChangeListener((btn, checked) ->
                whitelist.setBlockPrivateNumbers(checked));

        // Add number to whitelist
        addButton.setOnClickListener(v -> {
            String raw = numberInput.getText().toString().trim();
            if (raw.isEmpty()) return;

            String normalized = WhitelistManager.normalize(raw);
            if (normalized == null || normalized.replaceAll("[^\\d]", "").length() < 7) {
                Toast.makeText(this, "Numéro invalide", Toast.LENGTH_SHORT).show();
                return;
            }

            whitelist.addNumber(raw);
            numberInput.setText("");
            adapter.updateList(whitelist.getNumbers());
            Toast.makeText(this, normalized + " ajouté", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        adapter.updateList(whitelist.getNumbers());
        requestAnswerPhoneCallsPermission();
    }

    private void requestAnswerPhoneCallsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ANSWER_PHONE_CALLS}, 100);
            }
        }
    }

    private void updateUI() {
        boolean hasRole = hasScreeningRole();
        boolean isOn = whitelist.isEnabled();

        // Status banner
        if (hasRole && isOn) {
            statusText.setText("BK ACTIF — Filtre en place");
            statusText.setBackgroundColor(0xFF388E3C);
        } else if (hasRole) {
            statusText.setText("BK en pause — Filtrage désactivé");
            statusText.setBackgroundColor(0xFFF57C00);
        } else {
            statusText.setText("BK INACTIF — Permission requise !");
            statusText.setBackgroundColor(0xFFD32F2F);
        }

        // Role button
        if (hasRole) {
            roleButton.setText("Permission accordée ✓");
            roleButton.setEnabled(false);
        } else {
            roleButton.setText("Accorder la permission de filtrage");
            roleButton.setEnabled(true);
        }

        // Switches
        filterSwitch.setEnabled(hasRole);
        filterSwitch.setChecked(isOn);
        privateSwitch.setChecked(whitelist.isPrivateNumberBlocked());
    }

    private boolean hasScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager rm = getSystemService(RoleManager.class);
            return rm != null && rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
        }
        return false;
    }
}
