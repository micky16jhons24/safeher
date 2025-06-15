package com.example.safeher20.util;

import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class Conductor {
    private String nombre;
    private LatLng ubicacion;
    private LatLng destino;
    private List<LatLng> ruta;
    private int indiceRutaActual;
    public Conductor(String nombre, LatLng ubicacion) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.destino = null;
        this.ruta = null;
        this.indiceRutaActual = 0;
    }

    // Getters y setters estándar
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


    public LatLng getDestino() {
        return destino;
    }

    public void setDestino(LatLng destino) {
        this.destino = destino;
    }

    public List<LatLng> getRuta() {
        return ruta;
    }

    public void setRuta(List<LatLng> ruta) {
        this.ruta = ruta;
        this.indiceRutaActual = 0;
    }

    public int getIndiceRutaActual() {
        return indiceRutaActual;
    }

    public void setIndiceRutaActual(int indiceRutaActual) {
        this.indiceRutaActual = indiceRutaActual;
    }


    public boolean avanzarEnRuta() {
        if (ruta != null && indiceRutaActual < ruta.size() - 1) {
            indiceRutaActual++;
            ubicacion = ruta.get(indiceRutaActual);
            return true;
        }
        return false;
    }

    public boolean haLlegadoADestino() {
        return ruta != null && indiceRutaActual >= ruta.size() - 1;
    }
}