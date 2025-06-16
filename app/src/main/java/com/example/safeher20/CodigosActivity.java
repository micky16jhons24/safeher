package com.example.safeher20;


import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


public class CodigosActivity extends AppCompatActivity {

    private EditText inputCodigo;
    private TextView textResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_codigos);

        inputCodigo = findViewById(R.id.inputCodigo);
        textResultado = findViewById(R.id.textResultado);
        findViewById(R.id.btnCerrar).setOnClickListener(v -> finish());

        Button btnAplicar = findViewById(R.id.btnAplicarCodigo);
        btnAplicar.setOnClickListener(v -> {
            String codigo = inputCodigo.getText().toString().trim();

            if (codigo.isEmpty()) {
                Toast.makeText(this, "Por favor, introduce un código.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (codigo.equalsIgnoreCase("safe10")) {
                // Guardar el descuento para futuros viajes (puede ser SharedPreferences)
                saveDiscount(10);
                textResultado.setText("¡Código válido! 10% de descuento aplicado a futuros viajes.");
                textResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));

            } else {
                textResultado.setText("Código no válido. Intenta de nuevo.");
                textResultado.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            }
        });
    }

    private void saveDiscount(int porcentaje) {
        // Guardamos el descuento en SharedPreferences para usarlo después
        getSharedPreferences("prefsSafeher", MODE_PRIVATE)
                .edit()
                .putInt("descuento", porcentaje)
                .apply();
    }
}
