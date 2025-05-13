package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.Arrays;

public class Inicio extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private MapView map;
    private MyLocationNewOverlay locationOverlay;

    // Coordenadas de destino ejemplo
    private final GeoPoint destino = new GeoPoint(40.4180, -3.7065);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.inicio);

        // Configurar mapa
        map = findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        // Pedir permiso de ubicación si no está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activarUbicacionUsuario();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }

        // Configurar botones
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> salirApp());
    }

    private void activarUbicacionUsuario() {
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        map.getOverlays().add(locationOverlay);
        map.invalidate();

        // Centrar mapa cuando se obtenga ubicación
        locationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint current = locationOverlay.getMyLocation();
            if (current != null) {
                map.getController().setCenter(current);
            }
        }));
    }

    private void centrarEnMiUbicacion() {
        if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
            map.getController().setCenter(locationOverlay.getMyLocation());
            Toast.makeText(this, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarViajeSeguro() {
        GeoPoint ubicacion = locationOverlay.getMyLocation();
        if (ubicacion == null) {
            Toast.makeText(this, "Ubicación no disponible aún", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar taxis cercanos (simulados)
        mostrarTaxisCercanos(ubicacion);

        // Mostrar ruta al destino
        Polyline ruta = new Polyline();
        ruta.setPoints(Arrays.asList(ubicacion, destino));
        ruta.setWidth(8f);
        ruta.setColor(getResources().getColor(R.color.black, getTheme()));
        map.getOverlays().add(ruta);

        // Calcular distancia y tiempo estimado
        double distanciaMetros = ubicacion.distanceToAsDouble(destino);
        double distanciaKm = distanciaMetros / 1000.0;
        double tiempoMin = (distanciaKm / 30.0) * 60.0; // 30 km/h promedio

        // Mostrar información
        new AlertDialog.Builder(this)
                .setTitle("Viaje Seguro")
                .setMessage(String.format(
                        "Distancia: %.1f km\nTiempo estimado: %.0f minutos\n\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                        distanciaKm, tiempoMin))
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();

        map.invalidate();
    }

    private void mostrarTaxisCercanos(GeoPoint origen) {
        Marker taxi1 = new Marker(map);
        taxi1.setPosition(new GeoPoint(origen.getLatitude() + 0.001, origen.getLongitude() + 0.001));
        taxi1.setTitle("Taxi cercano 1");
        taxi1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(taxi1);

        Marker taxi2 = new Marker(map);
        taxi2.setPosition(new GeoPoint(origen.getLatitude() - 0.001, origen.getLongitude() - 0.001));
        taxi2.setTitle("Taxi cercano 2");
        taxi2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(taxi2);
    }

    private void llamarEmergencia() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"));
        startActivity(intent);
    }

    private void salirApp() {
        Toast.makeText(this, "Saliendo...", Toast.LENGTH_SHORT).show();
        finishAffinity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarUbicacionUsuario();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
