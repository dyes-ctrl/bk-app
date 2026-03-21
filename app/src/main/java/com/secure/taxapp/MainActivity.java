package com.secure.taxapp;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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
import com.secure.taxapp.utils.NumberValidator;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DND_REQUEST_CODE = 300;

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
        checkAndRequestPermissions();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NumberAdapter(configManager.getNumbers(), configManager.isMasked());
        recyclerView.setAdapter(adapter);

        updateVisibilityButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rafraichir au retour dans l'app
        if (adapter != null) refreshList();
        if (mainToggle != null) mainToggle.setChecked(configManager.isServiceActive());

        // Si le service est actif mais que le DND n'est pas autorise,
        // afficher le dialog d'avertissement
        if (configManager.isServiceActive() && !hasDndPermission()) {
            showDndPermissionWarning();
        }
    }

    private void initViews() {
        mainToggle          = findViewById(R.id.mainToggle);
        toggleVisibilityButton = findViewById(R.id.toggleVisibilityButton);
        addButton           = findViewById(R.id.addButton);
        numberInput         = findViewById(R.id.numberInput);
        dialNumber          = findViewById(R.id.dialNumber);
        recyclerView        = findViewById(R.id.numbersRecyclerView);
        dialerLayout        = findViewById(R.id.dialerLayout);
        numbersLayout       = findViewById(R.id.numbersLayout);

        mainToggle.setChecked(configManager.isServiceActive());
    }

    private void setupListeners() {

        // Toggle principal ON/OFF
        mainToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !hasDndPermission()) {
                // Pas de permission DND -> revenir en arriere + avertir
                mainToggle.setChecked(false);
                showDndPermissionWarning();
                return;
            }
            configManager.setServiceActive(isChecked);

            if (isChecked) {
                startForegroundServiceCompat();
                Toast.makeText(this,
                    "🔒 Filtrage actif — appels inconnus → messagerie",
                    Toast.LENGTH_LONG).show();
            } else {
                stopService(new Intent(this, CalculationService.class));
                Toast.makeText(this,
                    "🔓 Filtrage inactif — tous les appels passent",
                    Toast.LENGTH_SHORT).show();
            }
        });

        // Onglets
        findViewById(R.id.tabDialer).setOnClickListener(v -> {
            dialerLayout.setVisibility(View.VISIBLE);
            numbersLayout.setVisibility(View.GONE);
        });

        findViewById(R.id.tabNumbers).setOnClickListener(v -> {
            dialerLayout.setVisibility(View.GONE);
            numbersLayout.setVisibility(View.VISIBLE);
        });

        // Masquer/Afficher numeros
        toggleVisibilityButton.setOnClickListener(v -> {
            boolean newMasked = !configManager.isMasked();
            configManager.setMasked(newMasked);
            adapter.setMasked(newMasked);
            updateVisibilityButton();
        });

        // Ajouter un numero
        addButton.setOnClickListener(v -> {
            String number = numberInput.getText().toString().trim();
            if (NumberValidator.isValid(number)) {
                boolean added = configManager.addNumber(number);
                if (added) {
                    numberInput.setText("");
                    refreshList();
                    Toast.makeText(this,
                        "✅ Numéro autorisé — il passera en DND",
                        Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Ce numéro est déjà dans la liste",
                        Toast.LENGTH_SHORT).show();
                }
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
        String[] values = {"0","1","2","3","4","5","6","7","8","9","*","#"};

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
            if (!number.isEmpty()) makeCall(number);
        });
    }

    private void makeCall(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permission d'appel requise", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateVisibilityButton() {
        toggleVisibilityButton.setText(
            configManager.isMasked() ? "Afficher les numéros" : "Masquer les numéros"
        );
    }

    private void refreshList() {
        adapter.updateData(configManager.getNumbers());
    }

    // =========================================================
    // PERMISSIONS
    // =========================================================

    private void checkAndRequestPermissions() {
        List<String> permissions = new ArrayList<>();

        String[] needed = {
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }

        for (String perm : needed) {
            if (ContextCompat.checkSelfPermission(this, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                permissions.add(perm);
            }
        }

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                this, permissions.toArray(new String[0]), PERMISSION_REQUEST_CODE
            );
        } else {
            onPermissionsReady();
        }
    }

    private void onPermissionsReady() {
        // 1. Verifier la permission DND (necessite une action utilisateur speciale)
        if (!hasDndPermission()) {
            showDndPermissionDialog();
            return;
        }

        // 2. Verifier l'exemption batterie (crucial pour Huawei)
        checkBatteryOptimization();

        // 3. Demarrer le service si le toggle est ON
        if (configManager.isServiceActive()) {
            startForegroundServiceCompat();
        }
    }

    private boolean hasDndPermission() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        return nm != null && nm.isNotificationPolicyAccessGranted();
    }

    /**
     * Dialog pour demander la permission DND.
     * SANS cette permission, l'application ne peut PAS fonctionner.
     */
    private void showDndPermissionDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Permission indispensable")
            .setMessage(
                "BK a besoin d'accéder au mode « Ne Pas Déranger » pour fonctionner.\n\n" +
                "C'est ESSENTIEL : c'est ce mode qui fait que les appels bloqués " +
                "tombent directement en messagerie, sans aucune sonnerie.\n\n" +
                "Dans l'écran suivant :\n" +
                "→ Trouvez « BK » dans la liste\n" +
                "→ Activez l'interrupteur"
            )
            .setCancelable(false)
            .setPositiveButton("Configurer maintenant", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, DND_REQUEST_CODE);
            })
            .show();
    }

    /**
     * Avertissement si DND non autorise et service actif
     */
    private void showDndPermissionWarning() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Permission manquante")
            .setMessage(
                "Le filtrage ne peut pas fonctionner sans la permission " +
                "« Ne Pas Déranger ».\n\n" +
                "Sans cette permission, les appels bloqués sonneront quand même " +
                "au lieu d'aller directement en messagerie."
            )
            .setPositiveButton("Configurer", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, DND_REQUEST_CODE);
            })
            .setNegativeButton("Ignorer", null)
            .show();
    }

    private void checkBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                showBatteryOptimizationDialog();
            }
        }
    }

    private void showBatteryOptimizationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🔋 Configuration Huawei requise")
            .setMessage(
                "Sur Huawei/EMUI, le système va tuer le service en arrière-plan " +
                "ce qui désactivera le filtrage.\n\n" +
                "Pour éviter ça :\n\n" +
                "1️⃣  Paramètres > Batterie\n" +
                "    > Lancement d'applications > BK\n" +
                "    > Mode Manuel > Tout activer\n\n" +
                "2️⃣  Dans le gestionnaire de tâches\n" +
                "    > Appuyer longuement sur BK > Verrouiller 🔒\n\n" +
                "3️⃣  Bouton ci-dessous pour désactiver l'optimisation batterie"
            )
            .setPositiveButton("Désactiver optimisation", (dialog, which) -> {
                try {
                    Intent intent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    );
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    try {
                        startActivity(
                            new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        );
                    } catch (Exception e2) {
                        Toast.makeText(this,
                            "Paramètres > Batterie > BK > Aucune restriction",
                            Toast.LENGTH_LONG).show();
                    }
                }
            })
            .setNegativeButton("Plus tard", null)
            .show();
    }

    private void startForegroundServiceCompat() {
        Intent serviceIntent = new Intent(this, CalculationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            onPermissionsReady();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DND_REQUEST_CODE) {
            if (hasDndPermission()) {
                Toast.makeText(this,
                    "✅ Permission DND accordée - BK peut fonctionner",
                    Toast.LENGTH_LONG).show();
                checkBatteryOptimization();
                if (configManager.isServiceActive()) {
                    startForegroundServiceCompat();
                }
            } else {
                Toast.makeText(this,
                    "⚠️ Sans cette permission, les appels bloqués peuvent sonner",
                    Toast.LENGTH_LONG).show();
            }
        }
    }
}
