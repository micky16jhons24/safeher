package com.example.safeher20.PantallaInicio;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.List;
import java.util.Random;

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

    // Nueva variable para guardar la urgencia seleccionada
    private String urgenciaSeleccionada;
    private static final int ID_MENOS_3_MIN = 1;
    private static final int ID_ENTRE_5_7_MIN = 2;
    private static final int ID_PUEDO_ESPERAR = 3;

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
                }

                @Override
                public void onError(@NonNull Status status) {
                    Toast.makeText(Inicio.this, "Error al seleccionar destino: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        inicializarConductores(origenSeleccionado, true);


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

    }

    // Nuevo metodo para mostrar diálogo de urgencia con radio buttons


    private void mostrarDialogoUrgencia() {
        if (origenSeleccionado == null) {
            Toast.makeText(this, "Por favor, selecciona el origen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        if (destinoSeleccionado == null) {
            destinoSeleccionado = defaultDestino;
            Toast.makeText(this, "Destino por defecto seleccionado", Toast.LENGTH_SHORT).show();
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
        mostrarTaxisCercanos(origenSeleccionado);

        PolylineOptions ruta = new PolylineOptions()
                .add(origenSeleccionado)
                .add(destinoSeleccionado)
                .width(8f)
                .color(ContextCompat.getColor(this, android.R.color.black));
        if (googleMap != null) googleMap.addPolyline(ruta);

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

        // Leer descuento guardado (en porcentaje)
        int descuento = getSharedPreferences("prefsSafeher", MODE_PRIVATE).getInt("descuento", 0);

        // Aplicar descuento si hay
        if (descuento > 0) {
            presupuesto = presupuesto * (1 - descuento / 100.0);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Presupuesto");

        String mensaje = String.format("Distancia: %.2f km\nTarifa: €%.2f\nDescuento: %d%%\nUrgencia: %s\n¿Aceptar presupuesto?",
                distanciaKm, presupuesto, descuento, textoUrgencia(urgenciaSeleccionada));
        builder.setMessage(mensaje);

        builder.setPositiveButton("Aceptar Presupuesto", (dialog, which) -> {
            buscarConductorSimulado();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());



        builder.show();
    }


    private String textoUrgencia(String clave) {
        switch (clave) {
            case "menos3": return "Menos de 3 minutos";
            case "entre5y7": return "Entre 5 y 7 minutos";
            case "puedoEsperar": return "Puedo esperar";
            default: return "";
        }
    }

    // Simular búsqueda de conductor con Handler y delay random entre 5 y 10 segundos
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

    // Asignar conductor aleatorio y mostrar mensaje con tiempo estimado
    private void asignarConductor() {
        if (listaConductores.isEmpty()) {
            Toast.makeText(this, "No hay conductores disponibles", Toast.LENGTH_SHORT).show();
            return;
        }
        Conductor conductor = listaConductores.get(new Random().nextInt(listaConductores.size()));

        // Calcular tiempo estimado según urgencia
        int tiempoEstimadoMinutos;
        switch (urgenciaSeleccionada) {
            case "menos3": tiempoEstimadoMinutos = 3; break;
            case "entre5y7": tiempoEstimadoMinutos = 6; break;
            case "puedoEsperar": tiempoEstimadoMinutos = 10; break;
            default: tiempoEstimadoMinutos = 10;
        }

        // --- Aquí calculas la distancia y precio ---
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                origenSeleccionado.latitude, origenSeleccionado.longitude,
                destinoSeleccionado.latitude, destinoSeleccionado.longitude,
                results);
        float distanciaMetros = results[0];
        double distanciaKm = distanciaMetros / 1000.0;

        boolean esNocturnoOFestivo = false; // Puedes calcularlo o pasar por parámetro
        double tarifaPorKm = esNocturnoOFestivo ? 1.5 : 1.2;
        double tarifaBase = 3.0;
        double presupuesto = tarifaBase + (tarifaPorKm * distanciaKm);

        // Aplicar descuento
        int descuento = getSharedPreferences("prefsSafeher", MODE_PRIVATE).getInt("descuento", 0);
        if (descuento > 0) {
            presupuesto = presupuesto * (1 - descuento / 100.0);
        }
        // Crear viaje
        long fechaActual = System.currentTimeMillis();
        Viaje viaje = new Viaje( urgenciaSeleccionada, conductor.getNombre(), fechaActual, presupuesto);
        List<Viaje> viajes = cargarViajesGuardados();
        viajes.add(viaje);
        guardarViajes(viajes);
        // Mostrar mensaje al usuario
        String mensaje = String.format("Conductor %s asignado.\nTiempo estimado de llegada: %d minutos\nPrecio: %.2f €",
                conductor.getNombre(), tiempoEstimadoMinutos, presupuesto);

        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        // Opcional: mostrar ruta y conductor en mapa
        if (googleMap != null) {
            googleMap.clear();
            mostrarTaxisCercanos(origenSeleccionado);
            PolylineOptions ruta = new PolylineOptions()
                    .add(origenSeleccionado)
                    .add(destinoSeleccionado)
                    .width(8f)
                    .color(ContextCompat.getColor(this, android.R.color.black));
            googleMap.addPolyline(ruta);
        }
    }


    // Método para centrar en ubicación actual si permiso concedido
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

    // Manejo resultado permiso
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

    // Simular llamada emergencia
    private void llamarEmergencia() {
        String numeroEmergencia = "tel:112";
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse(numeroEmergencia));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // Mostrar taxis cercanos (simulados)
    private void mostrarTaxisCercanos(LatLng ubicacion) {
        // Inicializamos conductores con base en la ubicación actual
        inicializarConductores(ubicacion, true);

        for (Conductor c : listaConductores) {
            googleMap.addMarker(new MarkerOptions()
                    .position(c.getUbicacion())
                    .title(c.getNombre())
                    .icon(bitmapDescriptorFromVector(this, R.drawable.ic_car_vector)));
        }
    }

    // Inicializar lista conductores con offsets para simular ubicación
    private void inicializarConductores(LatLng baseUbicacion, boolean aleatorio) {
        if (baseUbicacion == null) {
            Log.e("InicializarConductores", "baseUbicacion es null");
            return;
        }

        if (listaConductores == null) {
            listaConductores = new ArrayList<>();
        }
        listaConductores.clear();

        String[] nombres = {"Sara", "Carlos", "María", "David", "Lucía", "Javier", "Isabel", "Andrés", "Sofía", "Miguel",
                "Patricia", "Manuel", "Teresa", "Alberto", "Natalia", "Fernando", "César", "Daniela", "Gonzalo", "Noemí",
                "Irene", "Mario", "Leo", "Patricia R.", "Hugo", "Clara", "Santiago", "Laura M.", "Iván", "Rosa"};

        if (aleatorio) {
            Random random = new Random();
            for (int i = 0; i < nombres.length; i++) {
                double latOffset = (random.nextDouble() - 0.5) * 0.02;
                double lngOffset = (random.nextDouble() - 0.5) * 0.02;

                LatLng ubicacionAleatoria = new LatLng(
                        baseUbicacion.latitude + latOffset,
                        baseUbicacion.longitude + lngOffset
                );

                listaConductores.add(new Conductor(nombres[i], ubicacionAleatoria));
            }
        } else {

            listaConductores.add(new Conductor("Cristina", new LatLng(40.3550, -3.8700)));
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madridCentro, 14));
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Mostrar conductores iniciales en centro
        mostrarTaxisCercanos(madridCentro);
    }

    // Para ícono vectorial personalizado en el mapa
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


}