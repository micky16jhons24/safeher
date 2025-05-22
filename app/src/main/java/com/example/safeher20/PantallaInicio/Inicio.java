package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.ChatActivity;
import com.example.safeher20.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final LatLng destino = new LatLng(40.4180, -3.7065);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Botones funcionales
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))));
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> finishAffinity());
        findViewById(R.id.btnChat).setOnClickListener(v ->
                startActivity(new Intent(Inicio.this, ChatActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            activarUbicacionUsuario();
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 14f));
    }

    private void activarUbicacionUsuario() {
        if (googleMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
    }

    private void centrarEnMiUbicacion() {
        if (googleMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                Toast.makeText(this, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarViajeSeguro() {
        if (googleMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación no concedido", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location == null) {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
                return;
            }

            LatLng origen = new LatLng(location.getLatitude(), location.getLongitude());

            // Cálculo de distancia
            float[] results = new float[1];
            Location.distanceBetween(
                    origen.latitude, origen.longitude,
                    destino.latitude, destino.longitude,
                    results);
            double distanciaKm = results[0] / 1000.0;
            double tiempoMin = (distanciaKm / 30.0) * 60.0;

            // Marcadores ficticios
            LatLng taxi1 = new LatLng(origen.latitude + 0.001, origen.longitude + 0.001);
            LatLng taxi2 = new LatLng(origen.latitude - 0.001, origen.longitude - 0.001);

            googleMap.addMarker(new MarkerOptions().position(taxi1).title("Taxi cercano"));
            googleMap.addMarker(new MarkerOptions().position(taxi2).title("Taxi cercano"));

            PolylineOptions polyline = new PolylineOptions()
                    .add(origen)
                    .add(destino)
                    .color(ContextCompat.getColor(this, R.color.black))
                    .width(10f);
            googleMap.addPolyline(polyline);

            new AlertDialog.Builder(this)
                    .setTitle("Viaje Seguro")
                    .setMessage(String.format(
                            "Distancia: %.1f km\nTiempo estimado: %.0f min\n\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                            distanciaKm, tiempoMin))
                    .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            activarUbicacionUsuario();
        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
    }
}
