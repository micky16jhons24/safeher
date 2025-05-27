package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.ChatActivity;
import com.example.safeher20.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    private final LatLng madridCentro = new LatLng(40.4168, -3.7038);
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

        // Inicializar Places
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDWbbbV3AFWfHMc1CB_Xd2Vhr31TCWwMLw");
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
            autocompleteFragment.setHint("¿A dónde vas?");
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    obtenerUbicacionYMostrarRuta(place.getLatLng());
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        View autocompleteView = autocompleteFragment != null ? autocompleteFragment.getView() : null;
        if (autocompleteView != null) {
            autocompleteView.setBackgroundResource(R.drawable.search_background);
        }

        // Botones
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))));
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> finishAffinity());
        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(Inicio.this, ChatActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            activarUbicacionUsuario();
            obtenerUbicacionYCentrar();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridCentro, 14f));
        }

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destino, 14f));
    }

    private void activarUbicacionUsuario() {
        if (googleMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        googleMap.setMyLocationEnabled(true);
    }

    private void obtenerUbicacionYCentrar() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
            } else {
                Toast.makeText(this, "No se pudo obtener tu ubicación", Toast.LENGTH_SHORT).show();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(madridCentro, 14f));
            }
        });
    }

    private void centrarEnMiUbicacion() {
        if (googleMap == null) return;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
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

            PolylineOptions ruta = new PolylineOptions()
                    .add(ubicacion)
                    .add(destino)
                    .width(8f)
                    .color(getResources().getColor(R.color.black, getTheme()));
            googleMap.addPolyline(ruta);

            float[] results = new float[1];
            Location.distanceBetween(
                    ubicacion.latitude, ubicacion.longitude,
                    destino.latitude, destino.longitude,
                    results);
            float distanciaMetros = results[0];
            double distanciaKm = distanciaMetros / 1000.0;
            double tiempoMin = (distanciaKm / 30.0) * 60.0;

            new AlertDialog.Builder(this)
                    .setTitle("Viaje Seguro")
                    .setMessage(String.format("Distancia: %.1f km\nTiempo estimado: %.0f minutos\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                            distanciaKm, tiempoMin))
                    .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
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

    private void obtenerUbicacionYMostrarRuta(LatLng destino) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng origen = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.clear();
                mostrarTaxisCercanos(origen);

                PolylineOptions ruta = new PolylineOptions()
                        .add(origen)
                        .add(destino)
                        .width(10f)
                        .color(ContextCompat.getColor(this, R.color.black));
                googleMap.addPolyline(ruta);

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(origen)
                        .include(destino)
                        .build();

                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activarUbicacionUsuario();
                obtenerUbicacionYCentrar();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
