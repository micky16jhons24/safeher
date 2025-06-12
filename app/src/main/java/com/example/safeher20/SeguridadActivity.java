package com.example.safeher20;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SeguridadActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 1;
    private final String telefonoContacto = "112";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguridad);

        Button btnCompartirUbicacion = findViewById(R.id.btnCompartirUbicacion);
        Button btnLlamarEmergencia = findViewById(R.id.btnLlamarEmergencia);
        Button btnCerrar = findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> finish());

        btnCompartirUbicacion.setOnClickListener(v -> {
            String ubicacion = "https://maps.google.com/?q=mi+ubicación"; // esto se puede mejorar con GPS real
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, "¡Necesito ayuda! Esta es mi ubicación: " + ubicacion);
            startActivity(Intent.createChooser(intent, "Compartir con"));
        });

        btnLlamarEmergencia.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        REQUEST_CALL_PERMISSION);
            } else {
                llamarEmergencia();
            }
        });
    }

    private void llamarEmergencia() {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + telefonoContacto));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                llamarEmergencia();
            } else {
                Toast.makeText(this, "Permiso para llamadas no concedido", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
