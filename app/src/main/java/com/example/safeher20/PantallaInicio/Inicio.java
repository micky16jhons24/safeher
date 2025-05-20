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
    private GoogleMap mMap;
    private final LatLng destino = new LatLng(40.4180, -3.7065);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Pedir permiso si no está concedido
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }

        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> salirApp());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

            FusedLocationProviderClient clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);
            clienteUbicacion.getLastLocation()
                    .addOnSuccessListener(ubicacion -> {
                        if (ubicacion != null) {
                            LatLng posicionActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicionActual, 16));
                        } else {
                            Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void centrarEnMiUbicacion() {
        if (mMap != null && mMap.isMyLocationEnabled() && mMap.getMyLocation() != null) {
            LatLng ubicacion = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 16));
            Toast.makeText(this, "Centrado en tu ubicación", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void iniciarViajeSeguro() {
        if (mMap == null || !mMap.isMyLocationEnabled() || mMap.getMyLocation() == null) {
            Toast.makeText(this, "Ubicación no disponible aún", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origen = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());

        // Añadir taxis simulados
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(origen.latitude + 0.001, origen.longitude + 0.001))
                .title("Taxi cercano 1")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(origen.latitude - 0.001, origen.longitude - 0.001))
                .title("Taxi cercano 2")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

        // Dibujar ruta
        mMap.addPolyline(new PolylineOptions()
                .add(origen, destino)
                .width(8)
                .color(getResources().getColor(R.color.black, getTheme())));

        // Distancia y tiempo
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                origen.latitude, origen.longitude,
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
        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                mMap != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);

                // Obtener ubicación actual al conceder el permiso
                FusedLocationProviderClient clienteUbicacion = LocationServices.getFusedLocationProviderClient(this);
                clienteUbicacion.getLastLocation()
                        .addOnSuccessListener(ubicacion -> {
                            if (ubicacion != null) {
                                LatLng posicionActual = new LatLng(ubicacion.getLatitude(), ubicacion.getLongitude());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posicionActual, 16));
                            } else {
                                Toast.makeText(this, "Ubicación aún no disponible", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        } else {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
