package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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

import com.example.safeher20.util.Conductor;
import com.example.safeher20.util.MenuLateralActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    // Constantes de ubicación inicial
    private final LatLng madridCentro = new LatLng(40.4168, -3.7038);
    private final LatLng defaultDestino = new LatLng(40.4180, -3.7065);
    private List<Conductor> listaConductores = new ArrayList<>();


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

        inicializarConductores();


        // Botones de acción
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> iniciarViajeSeguro());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> salirApp());
        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(Inicio.this, ChatActivity.class)));
        findViewById(R.id.btnMenu).setOnClickListener(v -> {
            startActivity(new Intent(Inicio.this, MenuLateralActivity.class));
        });

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

        if (destinoSeleccionado == null) {
            destinoSeleccionado = defaultDestino;
            Toast.makeText(this, "Destino por defecto seleccionado", Toast.LENGTH_SHORT).show();
        }

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

        // Asignar conductor aleatorio
        Conductor conductorAsignado = listaConductores.get(
                new java.util.Random().nextInt(listaConductores.size())
        );

        // Mostrar conductor en el mapa
        googleMap.addMarker(new MarkerOptions()
                .position(conductorAsignado.ubicacion)
                .title("Conductor asignado: " + conductorAsignado.nombre)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        // Mostrar diálogo con info del viaje y conductor
        boolean esNocturnoOFestivo = false; // cambia esto según la condición real
        double tarifaPorKm = esNocturnoOFestivo ? 1.50 : 1.30;
        double bajadaBandera = 3.00;
        double precio = bajadaBandera + (distanciaKm * tarifaPorKm);

        new AlertDialog.Builder(this)
                .setTitle("Viaje Seguro")
                .setMessage(String.format(
                        "Conductor: %s\n\nDistancia: %.1f km\nTiempo estimado: %.0f minutos\n\nTarifa: %.2f €/km\nBajada de bandera: %.2f €\n\nPrecio estimado: %.2f €",
                        conductorAsignado.nombre, distanciaKm, tiempoMin, tarifaPorKm, bajadaBandera, precio))
                .setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss())
                .show();

        // Centrar cámara en ambos puntos
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(origenSeleccionado)
                .include(destinoSeleccionado)
                .include(conductorAsignado.ubicacion)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

        googleMap.clear();
        mostrarConductoresEnMapa();
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

    private void inicializarConductores() {
        listaConductores.clear();

        listaConductores.add(new Conductor("Laura", new LatLng(40.4800, -3.7000)));  // Norte
        listaConductores.add(new Conductor("María", new LatLng(40.4750, -3.6900)));
        listaConductores.add(new Conductor("Carmen", new LatLng(40.4700, -3.6800)));
        listaConductores.add(new Conductor("Ana", new LatLng(40.4650, -3.6700)));
        listaConductores.add(new Conductor("Sara", new LatLng(40.4600, -3.6600)));

        listaConductores.add(new Conductor("David", new LatLng(40.4400, -3.7200)));  // Noroeste
        listaConductores.add(new Conductor("Lucía", new LatLng(40.4350, -3.7150)));
        listaConductores.add(new Conductor("Carlos", new LatLng(40.4300, -3.7100)));
        listaConductores.add(new Conductor("Isabel", new LatLng(40.4250, -3.7050)));
        listaConductores.add(new Conductor("Javier", new LatLng(40.4200, -3.7000)));

        listaConductores.add(new Conductor("Elena", new LatLng(40.4000, -3.7400)));  // Oeste
        listaConductores.add(new Conductor("Andrés", new LatLng(40.3950, -3.7350)));
        listaConductores.add(new Conductor("Sofía", new LatLng(40.3900, -3.7300)));
        listaConductores.add(new Conductor("Miguel", new LatLng(40.3850, -3.7250)));
        listaConductores.add(new Conductor("Patricia", new LatLng(40.3800, -3.7200)));

        listaConductores.add(new Conductor("Manuel", new LatLng(40.3500, -3.7000)));  // Sur (cerca A-42)
        listaConductores.add(new Conductor("Teresa", new LatLng(40.3450, -3.6950)));
        listaConductores.add(new Conductor("Alberto", new LatLng(40.3400, -3.6900)));
        listaConductores.add(new Conductor("Natalia", new LatLng(40.3350, -3.6850)));
        listaConductores.add(new Conductor("Fernando", new LatLng(40.3300, -3.6800)));

        listaConductores.add(new Conductor("César", new LatLng(40.3100, -3.6900)));  // Sur más alejado
        listaConductores.add(new Conductor("Daniela", new LatLng(40.3050, -3.6850)));
        listaConductores.add(new Conductor("Gonzalo", new LatLng(40.3000, -3.6800)));
        listaConductores.add(new Conductor("Noemí", new LatLng(40.2950, -3.6750)));
        listaConductores.add(new Conductor("Irene", new LatLng(40.2900, -3.6700)));

        listaConductores.add(new Conductor("Mario", new LatLng(40.4200, -3.6500)));  // Este
        listaConductores.add(new Conductor("Leo", new LatLng(40.4150, -3.6450)));
        listaConductores.add(new Conductor("Patricia R.", new LatLng(40.4100, -3.6400)));
        listaConductores.add(new Conductor("Hugo", new LatLng(40.4050, -3.6350)));
        listaConductores.add(new Conductor("Clara", new LatLng(40.4000, -3.6300)));

        listaConductores.add(new Conductor("Santiago", new LatLng(40.3850, -3.6200)));  // Sureste
        listaConductores.add(new Conductor("Laura M.", new LatLng(40.3800, -3.6150)));
        listaConductores.add(new Conductor("Iván", new LatLng(40.3750, -3.6100)));
        listaConductores.add(new Conductor("Rosa", new LatLng(40.3700, -3.6050)));
        listaConductores.add(new Conductor("Pablo", new LatLng(40.3650, -3.6000)));

        listaConductores.add(new Conductor("Marta", new LatLng(40.4500, -3.5800)));  // Noreste
        listaConductores.add(new Conductor("Andrés V.", new LatLng(40.4550, -3.5850)));
        listaConductores.add(new Conductor("Sandra", new LatLng(40.4600, -3.6900)));
        listaConductores.add(new Conductor("David P.", new LatLng(40.4650, -3.5950)));
        listaConductores.add(new Conductor("Elisa", new LatLng(40.4700, -3.6400)));

        listaConductores.add(new Conductor("Juan", new LatLng(40.5000, -3.7000)));  // Carretera M-30 norte
        listaConductores.add(new Conductor("Beatriz", new LatLng(40.4950, -3.7050)));
        listaConductores.add(new Conductor("Carlos B.", new LatLng(40.4900, -3.6900)));
        listaConductores.add(new Conductor("Julia", new LatLng(40.4850, -3.7850)));
        listaConductores.add(new Conductor("Felipe", new LatLng(40.4800, -3.7200)));

        listaConductores.add(new Conductor("Natalia G.", new LatLng(40.4600, -3.7500)));  // Carretera M-40 oeste
        listaConductores.add(new Conductor("Ricardo", new LatLng(40.4550, -3.7550)));
        listaConductores.add(new Conductor("Claudia", new LatLng(40.4500, -3.7600)));
        listaConductores.add(new Conductor("Óscar", new LatLng(40.4450, -3.7650)));
        listaConductores.add(new Conductor("Silvia", new LatLng(40.4400, -3.7700)));

        listaConductores.add(new Conductor("Enrique", new LatLng(40.4200, -3.8000)));  // Carretera A-2 sur
        listaConductores.add(new Conductor("Natalia T.", new LatLng(40.4150, -3.8050)));
        listaConductores.add(new Conductor("Mónica", new LatLng(40.4100, -3.8100)));
        listaConductores.add(new Conductor("Jorge", new LatLng(40.4050, -3.8150)));
        listaConductores.add(new Conductor("Fernando R.", new LatLng(40.4000, -3.8200)));

        listaConductores.add(new Conductor("Sonia", new LatLng(40.3750, -3.8500)));  // Carretera A-42 sur
        listaConductores.add(new Conductor("Raúl", new LatLng(40.3700, -3.8550)));
        listaConductores.add(new Conductor("Paula", new LatLng(40.3650, -3.8600)));
        listaConductores.add(new Conductor("Victor", new LatLng(40.3600, -3.8650)));
        listaConductores.add(new Conductor("Cristina", new LatLng(40.3550, -3.8700)));


    }

    private void mostrarConductoresEnMapa() {
        BitmapDescriptor carIcon = getBitmapFromVector(R.drawable.ic_car_vector);

        for (Conductor conductor : listaConductores) {
            googleMap.addMarker(new MarkerOptions()
                    .position(conductor.getUbicacion())
                    .title("Conductor: " + conductor.getNombre())
                    .icon(carIcon));
        }
    }

    private BitmapDescriptor getBitmapFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) return null;

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

}