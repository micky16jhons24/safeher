package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.ChatActivity;
import com.example.safeher20.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final LatLng destino = new LatLng(40.4180, -3.7065); // Destino fijo
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // Hilo secundario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        // Verificar servicios de Google
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            googleApiAvailability.getErrorDialog(this, resultCode, 0).show();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        // Botones
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))));
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> finishAffinity());
        findViewById(R.id.btnChat).setOnClickListener(v ->
                startActivity(new Intent(Inicio.this, ChatActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            verificarGPSActivo();
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 14f));
    }

    private void verificarGPSActivo() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsActivo = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!gpsActivo) {
            new AlertDialog.Builder(this)
                    .setTitle("Ubicación desactivada")
                    .setMessage("Por favor, activa la ubicación para usar el mapa correctamente.")
                    .setCancelable(false)
                    .setPositiveButton("Activar", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    private void centrarEnMiUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null && mMap != null) {
                        LatLng myLoc = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 15));
                        Toast.makeText(this, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void iniciarViajeSeguro() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            executor.execute(() -> {
                // Operaciones pesadas fuera del hilo principal
                LatLng origen = new LatLng(location.getLatitude(), location.getLongitude());

                // Calcular distancia
                float[] results = new float[1];
                Location.distanceBetween(
                        origen.latitude, origen.longitude,
                        destino.latitude, destino.longitude,
                        results);
                double distanciaKm = results[0] / 1000.0;
                double tiempoMin = (distanciaKm / 30.0) * 60.0;

                // Posiciones de los taxis ficticios
                LatLng taxi1 = new LatLng(origen.latitude + 0.001, origen.longitude + 0.001);
                LatLng taxi2 = new LatLng(origen.latitude - 0.001, origen.longitude - 0.001);

                // Actualizar la interfaz de usuario en el hilo principal
                runOnUiThread(() -> {
                    // Añadir marcadores de taxis
                    mMap.addMarker(new MarkerOptions().position(taxi1).title("Taxi cercano"));
                    mMap.addMarker(new MarkerOptions().position(taxi2).title("Taxi cercano"));

                    // Dibujar la ruta desde origen a destino
                    PolylineOptions polyline = new PolylineOptions()
                            .add(origen)
                            .add(destino)
                            .color(ContextCompat.getColor(this, R.color.black))
                            .width(10f);
                    mMap.addPolyline(polyline);

                    // Mostrar alerta con detalles
                    new AlertDialog.Builder(this)
                            .setTitle("Viaje Seguro")
                            .setMessage(String.format(
                                    "Distancia: %.1f km\nTiempo estimado: %.0f min\n\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                                    distanciaKm, tiempoMin))
                            .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            });
        });
    }
    }