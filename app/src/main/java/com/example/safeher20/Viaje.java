package com.example.safeher20;


public class Viaje {
    private String urgencia;
    private String conductor;
    private long fecha;
    private double presupuesto;

    public Viaje( String urgencia, String conductor, long fecha, double presupuesto) {

        this.urgencia = urgencia;
        this.conductor = conductor;
        this.fecha = fecha;
        this.presupuesto = presupuesto;
    }



    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getConductor() {
        return conductor;
    }

    public void setConductor(String conductor) {
        this.conductor = conductor;
    }

    public long getFecha() {
        return fecha;
    }

    public void setFecha(long fecha) {
        this.fecha = fecha;
    }

    public double getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(double presupuesto) {
        this.presupuesto = presupuesto;
    }

    @Override
    public String toString() {
        return "Viaje{" +
                ", urgencia='" + urgencia + '\'' +
                ", conductor='" + conductor + '\'' +
                ", fecha=" + fecha +
                ", presupuesto=" + presupuesto +
                '}';
    }
}
