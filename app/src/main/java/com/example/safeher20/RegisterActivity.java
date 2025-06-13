package com.example.safeher20;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.safeher20.db.AppDatabase;
import com.example.safeher20.model.Usuaria;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class RegisterActivity extends AppCompatActivity{

    private static final int REQUEST_DNI_IMAGE = 101;
    private Uri dniImageUri = null;

    private EditText dni, nombre, apellido, phone, cp, direccion, portal, piso, puerta;
    private RadioGroup genero;
    private Spinner spinnerPais, spinnerCiudad;
    private Map<String, List<String>> paisCiudadMap = new HashMap<>();
    private EditText editTextEmail, editTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inicializarComponentes();
        configurarSpinners();
        configurarBotones();
    }

    private void inicializarComponentes() {
        dni = findViewById(R.id.editTextDni);
        nombre = findViewById(R.id.editTextNombre);
        apellido = findViewById(R.id.editTextApellido);
        phone = findViewById(R.id.editTextPhone);
        cp = findViewById(R.id.editTextCodigoPostal);
        direccion = findViewById(R.id.editTextDireccion);
        portal = findViewById(R.id.editTextPortal);
        piso = findViewById(R.id.editTextPiso);
        puerta = findViewById(R.id.editTextPuerta);
        genero = findViewById(R.id.radioGroupGenero);
        spinnerPais = findViewById(R.id.spinnerPais);
        spinnerCiudad = findViewById(R.id.spinnerCiudad);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

    }

    private void configurarSpinners() {
        paisCiudadMap.put("España", Arrays.asList("Madrid", "Barcelona", "Sevilla"));
        paisCiudadMap.put("México", Arrays.asList("CDMX", "Guadalajara", "Monterrey"));
        paisCiudadMap.put("Argentina", Arrays.asList("Buenos Aires", "Córdoba", "Rosario"));
        paisCiudadMap.put("Chile", Arrays.asList("Santiago", "Valparaíso", "Concepción"));

        ArrayAdapter<String> paisAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(paisCiudadMap.keySet()));
        paisAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(paisAdapter);

        spinnerPais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String paisSeleccionado = (String) parent.getItemAtPosition(position);
                List<String> ciudades = paisCiudadMap.getOrDefault(paisSeleccionado, Collections.emptyList());
                ArrayAdapter<String> ciudadAdapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, ciudades);
                ciudadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCiudad.setAdapter(ciudadAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void configurarBotones() {
        findViewById(R.id.btnUploadDni).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_DNI_IMAGE);
        });

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> registrarUsuaria());
    }
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    private void registrarUsuaria() {
        // Obtener los textos una vez y validarlos
        String emailTexto = editTextEmail.getText().toString().trim();
        String passwordTexto = editTextPassword.getText().toString().trim();

        if (!validarCampos()) return;

        Usuaria usuaria = new Usuaria();
        usuaria.setDni(dni.getText().toString().trim());
        usuaria.setNombre(nombre.getText().toString().trim());
        usuaria.setApellido(apellido.getText().toString().trim());
        usuaria.setTelefono(phone.getText().toString().trim());
        usuaria.setCodigoPostal(cp.getText().toString().trim());
        usuaria.setEmail(editTextEmail.getText().toString().trim());

        // Aplica SHA-256 a la contraseña antes de guardarla
        String contrasenaHasheada = hashPassword(passwordTexto);
        Log.d("RegisterActivity", "Contraseña hasheada: " + contrasenaHasheada); // AGREGADO EL LOG
        usuaria.setContrasena(contrasenaHasheada); // GUARDA CONTRASEÑA HASHEADA

        usuaria.setDireccion(direccion.getText().toString().trim());
        usuaria.setPortal(portal.getText().toString().trim());
        usuaria.setPiso(piso.getText().toString().trim());
        usuaria.setPuerta(puerta.getText().toString().trim());
        usuaria.setGenero(((RadioButton) findViewById(genero.getCheckedRadioButtonId())).getText().toString());
        usuaria.setPais(spinnerPais.getSelectedItem().toString());
        usuaria.setCiudad(spinnerCiudad.getSelectedItem().toString());
        usuaria.setFotoDniPath(dniImageUri != null ? dniImageUri.toString() : "");

        new Thread(() -> {
            AppDatabase.getInstance(getApplicationContext()).usuariaDao().insertar(usuaria);

            runOnUiThread(() -> {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        }).start();

    }

    private boolean validarCampos() {
        String dniTexto = dni.getText().toString().trim();
        String nombreTexto = nombre.getText().toString().trim();
        String apellidoTexto = apellido.getText().toString().trim();
        String phoneTexto = phone.getText().toString().trim();
        String cpTexto = cp.getText().toString().trim();
        String direccionTexto = direccion.getText().toString().trim();
        int selectedGenderId = genero.getCheckedRadioButtonId();

        // Validar campos obligatorios
        if (dniTexto.isEmpty() || nombreTexto.isEmpty() || apellidoTexto.isEmpty() ||
                phoneTexto.isEmpty() || cpTexto.isEmpty() || direccionTexto.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!dniTexto.matches("^[0-9]{8}[A-HJ-NP-TV-Z]$")) {
            Toast.makeText(this, "DNI inválido (formato: 8 números + letra).", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!phoneTexto.matches("^\\d{9,15}$")) {
            Toast.makeText(this, "Número de teléfono inválido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (selectedGenderId != R.id.radioMujer) {
            Toast.makeText(this, "Solo se permiten registros de mujeres.", Toast.LENGTH_LONG).show();
            return false;
        }

        String emailTexto = editTextEmail.getText().toString().trim();
        if (emailTexto.isEmpty()) {
            editTextEmail.setError("El correo electrónico es obligatorio");
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailTexto).matches()) {
            editTextEmail.setError("Introduce un correo electrónico válido");
            return false;
        }

        String passwordTexto = editTextPassword.getText().toString().trim();
        if (passwordTexto.isEmpty()) {
            editTextPassword.setError("La contraseña es obligatoria");
            return false;
        }
        if (passwordTexto.length() < 6) {
            editTextPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DNI_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            dniImageUri = data.getData();
            Toast.makeText(this, "Foto de DNI cargada correctamente", Toast.LENGTH_SHORT).show();
        }
    }
}
