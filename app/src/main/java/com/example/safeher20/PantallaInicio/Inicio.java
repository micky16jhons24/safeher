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

    // Constantes de ubicación inicial
    private final LatLng madridCentro = new LatLng(40.4168, -3.7038);
    private final LatLng defaultDestino = new LatLng(40.4180, -3.7065);

    // Variables para almacenar origen y destino seleccionados
    private LatLng origenSeleccionado;
    private LatLng destinoSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inicio);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Cargar mapa
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Inicializar Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDWbbbV3AFWfHMc1CB_Xd2Vhr31TCWwMLw");
        }

// Inicialización de los fragmentos de autocompletado
        AutocompleteSupportFragment autocompleteFragmentPartida = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_partida);
        if (autocompleteFragmentPartida != null) {
            autocompleteFragmentPartida.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG));
            autocompleteFragmentPartida.setHint("Elige punto de partida");
            autocompleteFragmentPartida.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    origenSeleccionado = place.getLatLng();
                    Toast.makeText(Inicio.this, "Origen: " + place.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error al seleccionar partida: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        AutocompleteSupportFragment autocompleteFragmentDestino = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_destino);
        if (autocompleteFragmentDestino != null) {
            autocompleteFragmentDestino.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.LAT_LNG));
            autocompleteFragmentDestino.setHint("¿A dónde vas?");
            autocompleteFragmentDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    destinoSeleccionado = place.getLatLng();
                    Toast.makeText(Inicio.this, "Destino: " + place.getName(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error al seleccionar destino: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Botones de acción
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> salirApp());
        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(Inicio.this, ChatActivity.class)));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        // Configurar mapa
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Comprobar permiso de ubicación
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

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));

                // Establecer en el campo de partida la ubicación actual
                if (origenSeleccionado == null) {
                    origenSeleccionado = current;
                }
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
            Toast.makeText(this, "Permiso denegado", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                LatLng current = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15f));
                origenSeleccionado = current;
                Toast.makeText(this, "Ubicación actual establecida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void iniciarViajeSeguro() {
        if (googleMap == null) return;

        if (origenSeleccionado == null || destinoSeleccionado == null) {
            Toast.makeText(this, "Por favor, selecciona tanto el origen como el destino", Toast.LENGTH_SHORT).show();
            return;
        }

        // Limpiar mapa
        googleMap.clear();

        // Mostrar taxis cercanos (simulados)
        mostrarTaxisCercanos(origenSeleccionado);

        // Dibujar ruta entre puntos
        PolylineOptions ruta = new PolylineOptions()
                .add(origenSeleccionado)
                .add(destinoSeleccionado)
                .width(8f)
                .color(ContextCompat.getColor(this, android.R.color.black));
        googleMap.addPolyline(ruta);

        // Calcular distancia
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                origenSeleccionado.latitude, origenSeleccionado.longitude,
                destinoSeleccionado.latitude, destinoSeleccionado.longitude,
                results);
        float distanciaMetros = results[0];
        double distanciaKm = distanciaMetros / 1000.0;
        double tiempoMin = (distanciaKm / 30.0) * 60.0;

        // Mostrar diálogo con info
        new AlertDialog.Builder(this)
                .setTitle("Viaje Seguro")
                .setMessage(String.format("Distancia: %.1f km\nTiempo estimado: %.0f minutos\n• Corto: 5€\n• Medio: 10€\n• Largo: 15€",
                        distanciaKm, tiempoMin))
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();

        // Centrar cámara en ambos puntos
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origenSeleccionado)
                .include(destinoSeleccionado)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
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
                obtenerUbicacionYCentrar();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}