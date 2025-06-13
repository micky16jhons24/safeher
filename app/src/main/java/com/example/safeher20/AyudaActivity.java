package com.example.safeher20;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AyudaActivity extends AppCompatActivity {

    private Spinner spinnerTipoProblema;
    private EditText editMensaje;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayuda);

        spinnerTipoProblema = findViewById(R.id.spinnerTipoProblema);
        editMensaje = findViewById(R.id.editMensaje);
        btnEnviar = findViewById(R.id.btnEnviar);
        Button btnCerrar = findViewById(R.id.btnCerrar);

        btnCerrar.setOnClickListener(v -> finish());

        // Rellenar spinnerTipoProblema con opciones
        ArrayAdapter<String> adapterProblemas = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item,
                new String[] {
                        "Reportar un problema con un viaje",
                        "Objeto perdido",
                        "Problema con un conductor",
                        "Dudas generales",
                        "Otros"
                }
        );
        adapterProblemas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoProblema.setAdapter(adapterProblemas);

        btnEnviar.setOnClickListener(v -> {
            String problema = (String) spinnerTipoProblema.getSelectedItem();
            String mensaje = editMensaje.getText().toString().trim();

            if (mensaje.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe un mensaje.", Toast.LENGTH_SHORT).show();
                return;
            }


            Toast.makeText(this, "Formulario enviado:\nTipo: " + problema + "\nMensaje: " + mensaje, Toast.LENGTH_LONG).show();
        });
    }
}
