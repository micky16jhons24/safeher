package com.example.safeher20;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.safeher20.PantallaInicio.Inicio;
import com.example.safeher20.db.AppDatabase;
import com.example.safeher20.model.Usuaria;
import com.example.safeher20.R;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<Usuaria> todas = AppDatabase.getInstance(getApplicationContext()).usuariaDao().getTodas();
        for (Usuaria u : todas) {
            Log.d("DEBUG_USUARIAS", "Email: " + u.getEmail() + ", Pass: " + u.getContrasena());
        }


        // Referencias a vistas
        Button buttonContinuar = findViewById(R.id.button); // ID correcto para el botón "Continuar"
        Button buttonRegistrarse = findViewById(R.id.button9); // Botón "Registrarse"
        Button selectCountryButton = findViewById(R.id.selectCountryButton); // Botón para selección de país
        ImageView imageView = findViewById(R.id.myImageView); // Imagen de bienvenida
        editTextEmail = findViewById(R.id.editTextPhone2); // Campo de correo electrónico
        editTextPassword = findViewById(R.id.editTextPhone); // Campo de contraseña

        // Botón CONTINUAR → Validación e ir a Inicio
        buttonContinuar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = hashPassword(editTextPassword.getText().toString().trim());

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce el correo electrónico y la contraseña.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Introduce un correo electrónico válido.", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                Usuaria usuaria = AppDatabase.getInstance(getApplicationContext())
                        .usuariaDao()
                        .login(email, password);

                runOnUiThread(() -> {
                    if (usuaria == null) {
                        Toast.makeText(MainActivity.this, "Correo o contraseña incorrectos.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Bienvenida, " + usuaria.getNombre(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Inicio.class));
                        finish();
                    }
                });
            }).start();
        });

        // Botón REGISTRARSE → ir a la actividad de registro
        buttonRegistrarse.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Ajuste de bordes (barra de estado)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Imagen de bienvenida
        imageView.setImageResource(R.drawable.icono);

        // Lista de países desde strings.xml
        String[] countries = getResources().getStringArray(R.array.countries_array);

        // Botón de selección de país
        selectCountryButton.setOnClickListener(v -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Selecciona un país")
                    .setAdapter(adapter, (dialog, which) -> {
                        String selectedCountry = countries[which];
                        selectCountryButton.setText(selectedCountry);
                        Toast.makeText(MainActivity.this, "Seleccionaste: " + selectedCountry, Toast.LENGTH_SHORT).show();
                    });

            builder.create().show();
        });
    }
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
