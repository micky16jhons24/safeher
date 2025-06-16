package com.example.safeher20;


import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.safeher20.PantallaInicio.Inicio;

public class MenuLateralActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_lateral);

        findViewById(R.id.btnCerrar).setOnClickListener(v -> {
            // Cerrar esta actividad y volver a la pantalla de inicio
            Intent intent = new Intent(MenuLateralActivity.this, Inicio.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });


        findViewById(R.id.opcionCuenta).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, CuentaActivity.class);
            startActivity(intent);
        });
        findViewById(R.id.opcionSaldo).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, SaldoActivity.class);
            startActivity(intent);
        });


        findViewById(R.id.opcionHistorial).setOnClickListener(v -> {
            Intent intent = new Intent(this, HistorialActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.opcionCodigos).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, CodigosActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.opcionAyuda).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, AyudaActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.opcionSeguridad).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, SeguridadActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.opcionSobreNosotros).setOnClickListener(v -> {
            Intent intent = new Intent(MenuLateralActivity.this, SobreNosotrosActivity.class);
            startActivity(intent);
        });
    }
}
