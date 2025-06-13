package com.example.safeher20;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
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
    private CheckBox checkboxRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencias a vistas
        Button buttonContinuar = findViewById(R.id.button);
        Button buttonRegistrarse = findViewById(R.id.button9);
        ImageView imageView = findViewById(R.id.myImageView);
        editTextEmail = findViewById(R.id.editTextPhone2);
        editTextPassword = findViewById(R.id.editTextPhone);
        checkboxRemember = findViewById(R.id.checkboxRemember);

        // Imagen bienvenida
        imageView.setImageResource(R.drawable.icono);

        // Cargar email recordado si existe
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String rememberedEmail = prefs.getString("remembered_email", null);
        if (rememberedEmail != null) {
            editTextEmail.setText(rememberedEmail);
            checkboxRemember.setChecked(true);
        }

        // Ajuste de padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ir a pantalla de registro
        buttonRegistrarse.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        });

        // Login
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
                        SharedPreferences.Editor editor = getSharedPreferences("user_session", MODE_PRIVATE).edit();
                        editor.putString("email_logueado", usuaria.getEmail());

                        if (checkboxRemember.isChecked()) {
                            editor.putString("remembered_email", email);
                        } else {
                            editor.remove("remembered_email");
                        }

                        editor.apply();

                        Toast.makeText(MainActivity.this, "Bienvenida, " + usuaria.getNombre(), Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, Inicio.class));
                        finish();
                    }
                });
            }).start();
        });

        // DEBUG: Mostrar todas las usuarias
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
