package com.example.safeher20;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.safeher20.PantallaInicio.Inicio;
import com.example.safeher20.db.AppDatabase;
import com.example.safeher20.model.Usuaria;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si ya hay una sesión iniciada
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String savedEmail = prefs.getString("email_logueado", null);
        if (savedEmail != null) {
            startActivity(new Intent(this, Inicio.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Referencias a vistas
        Button buttonContinuar = findViewById(R.id.button);
        Button buttonRegistrarse = findViewById(R.id.button9);
        Button selectCountryButton = findViewById(R.id.selectCountryButton);
        ImageView imageView = findViewById(R.id.myImageView);
        editTextEmail = findViewById(R.id.editTextPhone2);
        editTextPassword = findViewById(R.id.editTextPhone);

        // Imagen de bienvenida
        imageView.setImageResource(R.drawable.icono);

        // Ajuste de padding para barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cargar países desde recursos y preparar el selector
        String[] countries = getResources().getStringArray(R.array.countries_array);
        selectCountryButton.setOnClickListener(v -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);

            new AlertDialog.Builder(this)
                    .setTitle("Selecciona un país")
                    .setAdapter(adapter, (dialog, which) -> {
                        String selectedCountry = countries[which];
                        selectCountryButton.setText(selectedCountry);
                        Toast.makeText(this, "Seleccionaste: " + selectedCountry, Toast.LENGTH_SHORT).show();
                    })
                    .create()
                    .show();
        });

        // Acción para botón de registro
        buttonRegistrarse.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        // Acción para botón continuar (login)
        buttonContinuar.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = hashPassword(editTextPassword.getText().toString().trim());

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce el correo electrónico y la contraseña.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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
                        // Guardar sesión con email_logueado
                        getSharedPreferences("user_session", MODE_PRIVATE)
                                .edit()
                                .putString("email_logueado", usuaria.getEmail())
                                .apply();

                        Toast.makeText(MainActivity.this, "Bienvenida, " + usuaria.getNombre(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Inicio.class));
                        finish();
                    }
                });
            }).start();
        });

        // Log de todas las usuarias (debug)
        new Thread(() -> {
            List<Usuaria> todas = AppDatabase.getInstance(getApplicationContext()).usuariaDao().getTodas();
            for (Usuaria u : todas) {
                Log.d("DEBUG_USUARIAS", "Email: " + u.getEmail() + ", Pass: " + u.getContrasena());
            }
        }).start();
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
