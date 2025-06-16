package com.example.safeher20;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.example.safeher20.db.AppDatabase;
import com.example.safeher20.model.Usuaria;

public class CuentaActivity extends AppCompatActivity {

    private TextView txtNombre, txtEmail, txtTelefono, txtDni, txtGenero, txtDireccion, txtCiudad;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cuenta);

        txtNombre = findViewById(R.id.textNombre);
        txtEmail = findViewById(R.id.textEmail);
        txtTelefono = findViewById(R.id.textTelefono);
        txtDni = findViewById(R.id.textDni);
        txtGenero = findViewById(R.id.textGenero);
        txtDireccion = findViewById(R.id.textDireccion);
        txtCiudad = findViewById(R.id.textCiudad);

        db = AppDatabase.getInstance(getApplicationContext());

        cargarDatosUsuaria();

        Button btnCerrar = findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> finish());
    }

    private void cargarDatosUsuaria() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String emailGuardado = prefs.getString("email_logueado", null);
        if (emailGuardado == null) {
            Toast.makeText(this, "No hay usuaria logueada", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Usuaria usuaria = db.usuariaDao().buscarPorEmail(emailGuardado); // método DAO para buscar por email
            runOnUiThread(() -> {
                if (usuaria != null) {
                    txtNombre.setText("Nombre: " + usuaria.getNombre() + " " + usuaria.getApellido());
                    txtEmail.setText("Correo: " + usuaria.getEmail());
                    txtTelefono.setText("Teléfono: " + usuaria.getTelefono());
                    txtDni.setText("DNI: " + usuaria.getDni());
                    txtGenero.setText("Género: " + usuaria.getGenero());
                    txtDireccion.setText("Dirección: " + usuaria.getDireccion() + ", " + usuaria.getPortal() +
                            ", Piso: " + usuaria.getPiso() + ", Puerta: " + usuaria.getPuerta());
                    txtCiudad.setText("Ciudad: " + usuaria.getCiudad() + ", País: " + usuaria.getPais());
                } else {
                    Toast.makeText(this, "Usuaria no encontrada", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}
