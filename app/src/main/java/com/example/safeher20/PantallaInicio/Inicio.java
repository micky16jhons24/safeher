package com.example.safeher20.PantallaInicio;



import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safeher20.ChatActivity;
import com.example.safeher20.MainActivity;
import com.example.safeher20.R;
import com.example.safeher20.Viaje;
import com.example.safeher20.util.Conductor;
import com.example.safeher20.MenuLateralActivity;
import com.example.safeher20.util.DirectionsApiHelper;
import com.example.safeher20.util.PolylineDecoder;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Inicio extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private static final String PREFS_NAME = "prefsSafeher";
    private static final String SALDO_KEY = "saldo_guardado";
    private double saldoActual ;
    private TextView resumenSaldo;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;

    // Constantes de ubicación inicial
    private final LatLng madridCentro = new LatLng(40.4168, -3.7038);
    private final LatLng defaultDestino = new LatLng(40.4180, -3.7065);
    private List<Conductor> listaConductores = new ArrayList<>();
    private final Map<Conductor, Marker> mapaConductores = new HashMap<>();

    // Variables para almacenar origen y destino seleccionados
    private LatLng origenSeleccionado;
    private LatLng destinoSeleccionado;

    // Nueva variable para guardar la urgencia seleccionada
    private String urgenciaSeleccionada;
    private static final int ID_MENOS_3_MIN = 1;
    private static final int ID_ENTRE_5_7_MIN = 2;
    private static final int ID_PUEDO_ESPERAR = 3;

    // Variables para mover taxis
    private final Handler handlerMovimiento = new Handler();
    private Runnable runnableMovimiento;
    private boolean moviendoTaxis = false;


// En onCreate o método de inicialización

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
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Autocomplete origen
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
                    comprobarYMostrarRuta();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error al seleccionar partida: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Autocomplete destino
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
                    comprobarYMostrarRuta();
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error al seleccionar destino: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        inicializarConductoresMadrid();

        // Botones de acción
        findViewById(R.id.btnViajeSeguro).setOnClickListener(v -> mostrarDialogoUrgencia());
        findViewById(R.id.btnVerMapa).setOnClickListener(v -> centrarEnMiUbicacion());
        findViewById(R.id.btnSafeCall).setOnClickListener(v -> llamarEmergencia());
        findViewById(R.id.btnChat).setOnClickListener(v -> startActivity(new Intent(Inicio.this, ChatActivity.class)));
        findViewById(R.id.btnMenu).setOnClickListener(v -> startActivity(new Intent(Inicio.this, MenuLateralActivity.class)));
        findViewById(R.id.btnSalirRapido).setOnClickListener(v -> {
            getSharedPreferences("user_session", Context.MODE_PRIVATE).edit().remove("email").apply();
            Intent intent = new Intent(Inicio.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finishAffinity();
        });

        resumenSaldo = findViewById(R.id.resumenSaldo);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        saldoActual = Double.longBitsToDouble(prefs.getLong(SALDO_KEY, Double.doubleToLongBits(0.0)));

        resumenSaldo.setText(String.format("Saldo disponible: %.2f €", saldoActual));
    }

    private void mostrarDialogoUrgencia() {
        if (origenSeleccionado == null) {
            Toast.makeText(this, "Por favor, selecciona el origen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destinoSeleccionado == null) {
            destinoSeleccionado = defaultDestino;
            Toast.makeText(this, "Destino por defecto seleccionado", Toast.LENGTH_SHORT).show();
            comprobarYMostrarRuta();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¿Con qué urgencia necesitas el taxi?");

        RadioGroup radioGroup = new RadioGroup(this);

        RadioButton rb1 = new RadioButton(this);
        rb1.setText("Menos de 3 minutos");
        rb1.setId(ID_MENOS_3_MIN);

        RadioButton rb2 = new RadioButton(this);
        rb2.setText("Entre 5 y 7 minutos");
        rb2.setId(ID_ENTRE_5_7_MIN);

        RadioButton rb3 = new RadioButton(this);
        rb3.setText("Puedo esperar");
        rb3.setId(ID_PUEDO_ESPERAR);

        radioGroup.addView(rb1);
        radioGroup.addView(rb2);
        radioGroup.addView(rb3);

        builder.setView(radioGroup);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(this, "Debes seleccionar una opción", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (selectedId) {
                case ID_MENOS_3_MIN:
                    urgenciaSeleccionada = "menos3";
                    break;
                case ID_ENTRE_5_7_MIN:
                    urgenciaSeleccionada = "entre5y7";
                    break;
                case ID_PUEDO_ESPERAR:
                    urgenciaSeleccionada = "puedoEsperar";
                    break;
                default:
                    urgenciaSeleccionada = "puedoEsperar";
            }
            mostrarPresupuestoConUrgencia();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void mostrarPresupuestoConUrgencia() {
        if (origenSeleccionado == null || destinoSeleccionado == null) {
            Toast.makeText(this, "Por favor, selecciona tanto el origen como el destino", Toast.LENGTH_SHORT).show();
            return;
        }

        if (googleMap != null) googleMap.clear();
        inicializarConductoresMadrid();
        mostrarTaxisCercanos(null);

        dibujarRutaEnMapa(origenSeleccionado, destinoSeleccionado);

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                origenSeleccionado.latitude, origenSeleccionado.longitude,
                destinoSeleccionado.latitude, destinoSeleccionado.longitude,
                results);
        float distanciaMetros = results[0];
        double distanciaKm = distanciaMetros / 1000.0;

        boolean esNocturnoOFestivo = false;
        double tarifaPorKm = esNocturnoOFestivo ? 1.5 : 1.2;
        double tarifaBase = 3.0;
        double presupuesto = tarifaBase + (tarifaPorKm * distanciaKm);

        int descuento = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getInt("descuento", 0);

        if (descuento > 0) {
            presupuesto = presupuesto * (1 - descuento / 100.0);
        }

        // Hacer 'presupuesto' final para usar en el lambda:
        final double presupuestoFinal = presupuesto;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Presupuesto");

        String mensaje = String.format("Distancia: %.2f km\nTarifa: €%.2f\nDescuento: %d%%\nUrgencia: %s\n¿Aceptar presupuesto?",
                distanciaKm, presupuestoFinal, descuento, textoUrgencia(urgenciaSeleccionada));
        builder.setMessage(mensaje);

        builder.setPositiveButton("Aceptar Presupuesto", (dialog, which) -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

            double saldoActual = Double.longBitsToDouble(prefs.getLong(SALDO_KEY, Double.doubleToLongBits(0.0)));

            if (saldoActual >= presupuestoFinal) {
                double nuevoSaldo = saldoActual - presupuestoFinal;
                guardarSaldo(nuevoSaldo);
                this.saldoActual = nuevoSaldo;
                actualizarResumen();

                Toast.makeText(this, "Pago realizado. Saldo actualizado.", Toast.LENGTH_LONG).show();

                buscarConductorSimulado();
            } else {
                Toast.makeText(this, "Saldo insuficiente para aceptar el presupuesto.", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void guardarSaldo(double nuevoSaldo) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SALDO_KEY, Double.doubleToLongBits(nuevoSaldo));
        editor.apply();
    }

    private void actualizarResumen() {
        resumenSaldo.setText(String.format("Saldo disponible: %.2f €", saldoActual));
    }

    private String textoUrgencia(String clave) {
        switch (clave) {
            case "menos3": return "Menos de 3 minutos";
            case "entre5y7": return "Entre 5 y 7 minutos";
            case "puedoEsperar": return "Puedo esperar";
            default: return "";
        }
    }

    private void buscarConductorSimulado() {
        AlertDialog buscandoDialog = new AlertDialog.Builder(this)
                .setTitle("Buscando conductor")
                .setMessage("Por favor, espera...")
                .setCancelable(false)
                .create();
        buscandoDialog.show();

        int delay = new Random().nextInt(5000) + 5000; // entre 5000 y 10000 ms

        new Handler().postDelayed(() -> {
            buscandoDialog.dismiss();
            asignarConductor();
        }, delay);
    }

    private void asignarConductor() {
        if (listaConductores.isEmpty()) {
            Toast.makeText(this, "No hay conductores disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        Conductor conductor = listaConductores.get(new Random().nextInt(listaConductores.size()));

        int tiempoEstimadoMinutos;
        switch (urgenciaSeleccionada) {
            case "menos3": tiempoEstimadoMinutos = 3; break;
            case "entre5y7": tiempoEstimadoMinutos = 6; break;
            case "puedoEsperar": tiempoEstimadoMinutos = 10; break;
            default: tiempoEstimadoMinutos = 10;
        }

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                origenSeleccionado.latitude, origenSeleccionado.longitude,
                destinoSeleccionado.latitude, destinoSeleccionado.longitude,
                results);
        float distanciaMetros = results[0];
        double distanciaKm = distanciaMetros / 1000.0;

        boolean esNocturnoOFestivo = false;
        double tarifaPorKm = esNocturnoOFestivo ? 1.5 : 1.2;
        double tarifaBase = 3.0;
        double presupuesto = tarifaBase + (tarifaPorKm * distanciaKm);

        int descuento = getSharedPreferences("prefsSafeher", MODE_PRIVATE).getInt("descuento", 0);
        if (descuento > 0) {
            presupuesto = presupuesto * (1 - descuento / 100.0);
        }

        long fechaActual = System.currentTimeMillis();
        Viaje viaje = new Viaje( urgenciaSeleccionada, conductor.getNombre(), fechaActual, presupuesto);
        List<Viaje> viajes = cargarViajesGuardados();
        viajes.add(viaje);
        guardarViajes(viajes);

        String mensaje = String.format("Conductor %s asignado.\nTiempo estimado de llegada: %d minutos\nPrecio: %.2f €",
                conductor.getNombre(), tiempoEstimadoMinutos, presupuesto);

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();

        if (googleMap != null) {
            googleMap.clear();
            inicializarConductoresMadrid();
            mostrarTaxisCercanos(null);
            dibujarRutaEnMapa(origenSeleccionado, destinoSeleccionado);
        }
    }

    private void centrarEnMiUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && googleMap != null) {
                    LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(miUbicacion, 15));
                    googleMap.addMarker(new MarkerOptions().position(miUbicacion).title("Mi ubicación"));
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                centrarEnMiUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void llamarEmergencia() {
        String numeroEmergencia = "tel:112";
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(numeroEmergencia));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void mostrarTaxisCercanos(LatLng unused) {
        // Limpia el mapa de conductores y markers previos
        if (googleMap == null) return;
        mapaConductores.clear();

        for (Conductor c : listaConductores) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(c.getUbicacion())
                    .title(c.getNombre())
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_car_vector)));
            mapaConductores.put(c, marker);
        }
        // Arranca movimiento tras mostrar taxis
        iniciarMovimientoTaxis();
    }

    private void inicializarConductoresMadrid() {
        listaConductores = new ArrayList<>();
        String[] nombres = {
                "Sara", "María", "Lucía", "Isabel", "Sofía",
                "Patricia", "Teresa", "Natalia", "Daniela",
                "Noemí", "Irene", "Patricia R.", "Clara", "Laura M.", "Rosa",
                "Ana", "Elena", "Camila", "Valeria", "Marta",
                "Emma", "Julia", "Paula", "Alicia", "Carla",
                "Vera", "Adriana", "Lola", "Bianca", "Carmen",
                "Andrea", "Jimena", "Lidia", "Nuria", "Silvia",
                "Lorena", "Pilar", "Rebeca", "Esther", "Belén"
        };
        Random random = new Random();
        double latMin = 40.35, latMax = 40.50;
        double lngMin = -3.75, lngMax = -3.68;

        for (int i = 0; i < nombres.length; i++) {
            double lat = latMin + (latMax - latMin) * random.nextDouble();
            double lng = lngMin + (lngMax - lngMin) * random.nextDouble();
            listaConductores.add(new Conductor(nombres[i], new LatLng(lat, lng)));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridCentro, 14));
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setAllGesturesEnabled(true);

        inicializarConductoresMadrid();
        mostrarTaxisCercanos(null);
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) return BitmapDescriptorFactory.defaultMarker();
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    private void guardarViajes(List<Viaje> viajes) {
        SharedPreferences prefs = getSharedPreferences("viajes_safeher", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(viajes);
        editor.putString("lista_viajes", json);
        editor.apply();
    }
    private List<Viaje> cargarViajesGuardados() {
        SharedPreferences prefs = getSharedPreferences("viajes_safeher", MODE_PRIVATE);
        String json = prefs.getString("lista_viajes", null);
        if (json == null) {
            return new ArrayList<>();
        }
        Gson gson = new Gson();
        Type type = new TypeToken<List<Viaje>>(){}.getType();
        List<Viaje> viajes = gson.fromJson(json, type);
        return viajes != null ? viajes : new ArrayList<>();
    }

    private void comprobarYMostrarRuta() {
        if (origenSeleccionado != null && destinoSeleccionado != null) {
            if (googleMap != null) googleMap.clear();
            inicializarConductoresMadrid();
            mostrarTaxisCercanos(null);
            dibujarRutaEnMapa(origenSeleccionado, destinoSeleccionado);
        }
    }

    public void dibujarRutaEnMapa(LatLng origen, LatLng destino) {
        new Thread(() -> {
            try {
                String strOrigen = origen.latitude + "," + origen.longitude;
                String strDestino = destino.latitude + "," + destino.longitude;
                String apiKey = getString(R.string.google_maps_key);

                String json = DirectionsApiHelper.getDirections(strOrigen, strDestino, apiKey);

                List<LatLng> puntosRuta = PolylineDecoder.getPolylinePoints(json);

                runOnUiThread(() -> {
                    if (googleMap != null && puntosRuta.size() > 0) {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(puntosRuta)
                                .width(10)
                                .color(Color.BLUE);
                        googleMap.addPolyline(polylineOptions);
                    } else {
                        Toast.makeText(this, "No se pudo obtener la ruta", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "Error al obtener ruta: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    private void iniciarMovimientoTaxis() {
        detenerMovimientoTaxis(); // Detén cualquier movimiento anterior antes de arrancar uno nuevo
        moviendoTaxis = true;
        runnableMovimiento = new Runnable() {
            @Override
            public void run() {
                moverTaxisPorRuta();
                if (moviendoTaxis) {
                    handlerMovimiento.postDelayed(this, 1000); // mueve cada 1 segundo
                }
            }
        };
        handlerMovimiento.post(runnableMovimiento);
    }
    private void detenerMovimientoTaxis() {
        moviendoTaxis = false;
        if (handlerMovimiento != null && runnableMovimiento != null) {
            handlerMovimiento.removeCallbacks(runnableMovimiento);
        }
    }
    private void moverTaxisPorRuta() {
        for (Conductor c : mapaConductores.keySet()) {
            // Si no tiene ruta o llegó a destino, asigna nuevo destino y ruta
            if (c.getRuta() == null || c.haLlegadoADestino()) {
                asignarNuevoDestino(c);
                obtenerYRellenarRuta(c);
                continue;
            }
            // Avanza al siguiente punto de la ruta
            if (c.avanzarEnRuta()) {
                Marker marker = mapaConductores.get(c);
                if (marker != null) marker.setPosition(c.getUbicacion());
            }
        }
    }
    private void asignarNuevoDestino(Conductor conductor) {
        Random random = new Random();
        double latMin = 40.35, latMax = 40.50;
        double lngMin = -3.75, lngMax = -3.68;

        LatLng destino;
        do {
            double lat = latMin + (latMax - latMin) * random.nextDouble();
            double lng = lngMin + (lngMax - lngMin) * random.nextDouble();
            destino = new LatLng(lat, lng);
        } while (distance(conductor.getUbicacion(), destino) < 0.005);

        conductor.setDestino(destino);
    }
    private void obtenerYRellenarRuta(Conductor conductor) {
        new Thread(() -> {
            try {
                String strOrigen = conductor.getUbicacion().latitude + "," + conductor.getUbicacion().longitude;
                String strDestino = conductor.getDestino().latitude + "," + conductor.getDestino().longitude;
                String apiKey = getString(R.string.google_maps_key);

                String json = DirectionsApiHelper.getDirections(strOrigen, strDestino, apiKey);
                List<LatLng> puntosRuta = PolylineDecoder.getPolylinePoints(json);

                runOnUiThread(() -> {
                    conductor.setRuta(puntosRuta);
                    conductor.setIndiceRutaActual(0);
                });
            } catch (Exception e) {
                // Maneja error si lo deseas
            }
        }).start();
    }
    private double distance(LatLng a, LatLng b) {
        float[] results = new float[1];
        android.location.Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, results);
        return results[0] / 1000.0; // en km
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerMovimientoTaxis();
    }
    @Override
    protected void onPause() {
        super.onPause();
        detenerMovimientoTaxis();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null && !moviendoTaxis) {
            iniciarMovimientoTaxis();
        }
    }
}