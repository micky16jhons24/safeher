package com.example.safeher20.util;

import com.google.android.gms.maps.model.LatLng;

public class Conductor {
    public String nombre;
    public LatLng ubicacion;

    public Conductor(String nombre, LatLng ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
    }

    public String getNombre() {
        return nombre;
    }

    public LatLng getUbicacion() {
        return ubicacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setUbicacion(LatLng ubicacion) {
        this.ubicacion = ubicacion;
    }
}