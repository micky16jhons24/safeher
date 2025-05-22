package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private final LatLng destino = new LatLng(40.4180, -3.7065); // Ejemplo destino (Madrid centro)

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

        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> salirApp());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            activarUbicacionUsuario();
        }
    }

    private void activarUbicacionUsuario() {
        if (googleMap == null) return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);
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
                Toast.makeText(this, "Ubicación no disponible aún", Toast.LENGTH_SHORT).show();
                return;
            }

            LatLng ubicacion = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.clear();

            mostrarTaxisCercanos(ubicacion);

            googleMap.addPolyline(new PolylineOptions()
                    .add(ubicacion, destino)
                    .width(8f)
                    .color(getResources().getColor(R.color.black, getTheme())));

            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    ubicacion.latitude, ubicacion.longitude,
                    destino.latitude, destino.longitude,
                    results);

            double distanciaKm = results[0] / 1000.0;
            double tiempoMin = (distanciaKm / 30.0) * 60.0;

            new AlertDialog.Builder(this)
                    .setTitle("Viaje Seguro")
                    .setMessage(String.format(
                            "Distancia: %.1f km\nTiempo estimado: %.0f minutos\n\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                            distanciaKm, tiempoMin))
                    .setPositiveButton("Aceptar", null)
                    .show();
        });
    }

    private void mostrarTaxisCercanos(LatLng origen) {
        LatLng taxi1 = new LatLng(origen.latitude + 0.001, origen.longitude + 0.001);
        googleMap.addMarker(new MarkerOptions()
                .position(taxi1)
                .title("Taxi cercano 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        LatLng taxi2 = new LatLng(origen.latitude - 0.001, origen.longitude - 0.001);
        googleMap.addMarker(new MarkerOptions()
                .position(taxi2)
                .title("Taxi cercano 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
