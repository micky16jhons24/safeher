package com.example.safeher20.util;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.safeher20.MainActivity;  // Ajusta el paquete si es necesario
import com.example.safeher20.R;

public class MenuLateralActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> {
            // Cerrar esta actividad y volver a la pantalla de inicio
            Intent intent = new Intent(MenuLateralActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        // Otros listeners de opciones aquí...
        findViewById(R.id.opcionCuenta).setOnClickListener(v -> {
            // Acción Mi Cuenta
        });

        findViewById(R.id.opcionHistorial).setOnClickListener(v -> {
            // Acción Historial
        });

        findViewById(R.id.opcionCodigos).setOnClickListener(v -> {
            // Acción Códigos
        });

        findViewById(R.id.opcionAyuda).setOnClickListener(v -> {
            // Acción Ayuda
        });

        findViewById(R.id.opcionSeguridad).setOnClickListener(v -> {
            // Acción Seguridad
        });

        findViewById(R.id.opcionSobre).setOnClickListener(v -> {
            // Acción Sobre
        });
    }
}
