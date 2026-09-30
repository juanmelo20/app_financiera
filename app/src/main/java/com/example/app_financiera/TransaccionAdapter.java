package com.example.app_financiera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TransaccionAdapter extends RecyclerView.Adapter<TransaccionAdapter.TransaccionViewHolder> {

    private final List<Transaccion> transacciones;

    public TransaccionAdapter(List<Transaccion> transacciones) {
        this.transacciones = transacciones;
    }

    public void actualizar(List<Transaccion> nuevas) {
        transacciones.clear();
        transacciones.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransaccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaccion, parent, false);
        return new TransaccionViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TransaccionViewHolder holder, int position) {
        Transaccion t = transacciones.get(position);

        // Agrupar por fecha mostrando separadores "... Fecha ..."
        boolean mostrarSeparador = (position == 0) ||
                !transacciones.get(position - 1).getFecha().equals(t.getFecha());

        if (mostrarSeparador) {
            holder.tvFechaSeparador.setVisibility(View.VISIBLE);
            holder.tvFechaSeparador.setText("... " + t.getFecha() + " ...");
        } else {
            holder.tvFechaSeparador.setVisibility(View.GONE);
        }

        if (t.isSalida()) {
            holder.ivTipo.setImageResource(R.drawable.ic_salida_rojo);
            holder.tvEtiqueta.setText("Para");
            holder.tvNombreTarjeta.setText(t.getNombreDestinoCompleto());
        } else {
            holder.ivTipo.setImageResource(R.drawable.ic_entrada_verde);
            holder.tvEtiqueta.setText("De");
            holder.tvNombreTarjeta.setText(t.getNombreOrigenCompleto());
        }

        holder.tvMonto.setText(Formato.dinero(t.getMonto()));
    }

    @Override
    public int getItemCount() {
        return transacciones.size();
    }

    static class TransaccionViewHolder extends RecyclerView.ViewHolder {

        final TextView tvFechaSeparador, tvEtiqueta, tvNombreTarjeta, tvMonto;
        final ImageView ivTipo;

        TransaccionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFechaSeparador = itemView.findViewById(R.id.tv_fecha_separador);
            ivTipo = itemView.findViewById(R.id.iv_tipo_transaccion);
            tvEtiqueta = itemView.findViewById(R.id.tv_etiqueta_tipo);
            tvNombreTarjeta = itemView.findViewById(R.id.tv_nombre_tarjeta_transaccion);
            tvMonto = itemView.findViewById(R.id.tv_monto_transaccion);
        }
    }
}
