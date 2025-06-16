package com.example.safeher20;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SaldoActivity extends AppCompatActivity {

    private EditText inputCantidad;
    private ImageButton btnPaypal;
    private TextView resumenSaldo;
    private Button btnCerrar;

    private static final String PREFS_NAME = "prefsSafeher";
    private static final String SALDO_KEY = "saldo_guardado";

    private double saldoActual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saldo);

        inputCantidad = findViewById(R.id.inputCantidad);
        btnPaypal = findViewById(R.id.btnPaypal);
        resumenSaldo = findViewById(R.id.resumenSaldo);
        btnCerrar = findViewById(R.id.btnCerrar);
        cargarSaldo();

        btnPaypal.setOnClickListener(v -> mostrarDialogoPayPal());

        btnCerrar.setOnClickListener(v -> finish());
    }

    private void mostrarDialogoPayPal() {
        LayoutInflater inflater = getLayoutInflater();
        final android.view.View dialogView = inflater.inflate(R.layout.dialog_paypal_login, null);

        EditText inputEmail = dialogView.findViewById(R.id.inputEmail);
        EditText inputPassword = dialogView.findViewById(R.id.inputPassword);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmar);
        CheckBox checkRemember = dialogView.findViewById(R.id.checkRemember);
        Button btnCerrar = dialogView.findViewById(R.id.btnCerrar); // Nuevo botón de cierre

        // Cargar el correo guardado si existe
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedEmail = prefs.getString("paypal_email", "");
        boolean rememberMe = prefs.getBoolean("remember_paypal_email", false);

        if (rememberMe && !savedEmail.isEmpty()) {
            inputEmail.setText(savedEmail);
            checkRemember.setChecked(true);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar el botón de cierre (✕)
        btnCerrar.setOnClickListener(v -> {
            dialog.dismiss(); // Cierra el diálogo al hacer clic
        });

        btnConfirmar.setOnClickListener(v -> {
            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (!esEmailValido(email)) {
                inputEmail.setError("Introduce un correo válido");
                return;
            }

            if (password.isEmpty()) {
                inputPassword.setError("Introduce la contraseña");
                return;
            }

            // Guardar el correo si "Remember Me" está activado
            SharedPreferences.Editor editor = prefs.edit();
            if (checkRemember.isChecked()) {
                editor.putString("paypal_email", email);
                editor.putBoolean("remember_paypal_email", true);
            } else {
                editor.remove("paypal_email");
                editor.putBoolean("remember_paypal_email", false);
            }
            editor.apply();

            // Resto del código para procesar el pago...
            String cantidadTexto = inputCantidad.getText().toString().trim();

            if (cantidadTexto.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce una cantidad válida", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            try {
                double cantidad = Double.parseDouble(cantidadTexto);
                if (cantidad <= 0) {
                    Toast.makeText(this, "La cantidad debe ser mayor que 0", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }

                saldoActual += cantidad;
                guardarSaldo(saldoActual);
                actualizarResumen();

                Toast.makeText(this, "Saldo añadido con éxito. Pago realizado por PayPal", Toast.LENGTH_LONG).show();
                inputCantidad.setText("");
                dialog.dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad no válida", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private boolean esEmailValido(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void cargarSaldo() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        saldoActual = Double.longBitsToDouble(prefs.getLong(SALDO_KEY, Double.doubleToLongBits(0.0)));
        actualizarResumen();
    }

    private void guardarSaldo(double nuevoSaldo) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SALDO_KEY, Double.doubleToLongBits(nuevoSaldo));
        editor.apply();
    }


    private void actualizarResumen() {
        resumenSaldo.setText(String.format("Saldo disponible: %.2f €", saldoActual));
    }
}
