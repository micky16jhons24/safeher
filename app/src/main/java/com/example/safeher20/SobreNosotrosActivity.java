package com.example.safeher20;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SobreNosotrosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre_nosotros);

        TextView btnCerrar = findViewById(R.id.btnCerrar);
        if (btnCerrar != null) {
            btnCerrar.setOnClickListener(v -> finish());
        }

        Button btnAyuda = findViewById(R.id.btnAyuda);
        btnAyuda.setOnClickListener(v -> {
            Intent intent = new Intent(SobreNosotrosActivity.this, AyudaActivity.class);
            startActivity(intent);
        });


    }
}
