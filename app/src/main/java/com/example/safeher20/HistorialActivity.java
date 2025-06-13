package com.example.safeher20;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ViajeAdapter adapter;
    private List<Viaje> listaViajes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        recyclerView = findViewById(R.id.recyclerHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaViajes = cargarViajesGuardados();
        adapter = new ViajeAdapter(listaViajes);
        recyclerView.setAdapter(adapter);

        Button btnCerrar = findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> finish());
    }

    private List<Viaje> cargarViajesGuardados() {
        SharedPreferences prefs = getSharedPreferences("viajes_safeher", MODE_PRIVATE);
        String json = prefs.getString("lista_viajes", null);
        Type type = new TypeToken<List<Viaje>>(){}.getType();
        return json == null ? new ArrayList<>() : new Gson().fromJson(json, type);
    }
}
