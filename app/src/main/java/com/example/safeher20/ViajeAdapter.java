package com.example.safeher20;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class ViajeAdapter extends RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder> {

    private List<Viaje> listaViajes;

    public ViajeAdapter(List<Viaje> listaViajes) {
        this.listaViajes = listaViajes;
    }

    @NonNull
    @Override
    public ViajeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_viaje, parent, false);
        return new ViajeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViajeViewHolder holder, int position) {
        Viaje viaje = listaViajes.get(position);

        holder.textConductor.setText("Conductor: " + viaje.getConductor());
        holder.textUrgencia.setText("Urgencia: " + viaje.getUrgencia());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaFormateada = sdf.format(new Date(viaje.getFecha()));
        holder.textFecha.setText("Fecha: " + fechaFormateada);
        holder.textPrecio.setText("Precio: $" + String.format(Locale.getDefault(), "%.2f", viaje.getPresupuesto()));

    }


    @Override
    public int getItemCount() {
        return listaViajes.size();
    }

    static class ViajeViewHolder extends RecyclerView.ViewHolder {
        TextView textConductor, textUrgencia, textFecha, textPrecio;

        public ViajeViewHolder(@NonNull View itemView) {
            super(itemView);
            textConductor = itemView.findViewById(R.id.textConductor);
            textFecha = itemView.findViewById(R.id.textFecha);
            textUrgencia = itemView.findViewById(R.id.textUrgencia);
            textPrecio = itemView.findViewById(R.id.textPrecio);
        }
    }

    // Método para convertir LatLng en un String legible
    private String formatoLatLng(LatLng latLng) {
        if (latLng == null) return "Desconocido";
        return String.format(Locale.getDefault(), "%.5f, %.5f", latLng.latitude, latLng.longitude);
    }
}
