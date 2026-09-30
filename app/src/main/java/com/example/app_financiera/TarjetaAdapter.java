package com.example.app_financiera;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class TarjetaAdapter extends RecyclerView.Adapter<TarjetaAdapter.TarjetaViewHolder> {

    public interface OnTarjetaClickListener {
        void onTarjetaClick(Tarjeta tarjeta);
    }

    private static final int[] COLORES = {
            R.color.azul, R.color.naranja, R.color.morado, R.color.verde
    };

    private final List<Tarjeta> tarjetas;
    private final OnTarjetaClickListener listener;

    public TarjetaAdapter(List<Tarjeta> tarjetas, OnTarjetaClickListener listener) {
        this.tarjetas = tarjetas;
        this.listener = listener;
    }

    public void actualizar(List<Tarjeta> nuevas) {
        tarjetas.clear();
        tarjetas.addAll(nuevas);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TarjetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { //Crea una nueva vista para cada elemento en la lista.
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tarjeta, parent, false);
        return new TarjetaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull TarjetaViewHolder holder, int position) {
        Tarjeta tarjeta = tarjetas.get(position);

        holder.tvNombre.setText(tarjeta.getNombre());
        // Se muestra el PAN completo sin ocultar dígitos
        holder.tvPan.setText(tarjeta.getPanFormateado());
        holder.tvSaldo.setText(Formato.dinero(tarjeta.getSaldo()));
        holder.tvExpiracion.setText(tarjeta.getExpiracion());

        int color = ContextCompat.getColor(
                holder.itemView.getContext(), COLORES[position % COLORES.length]);
        holder.card.setCardBackgroundColor(color);

        // Copiar PAN al presionar el botón o el número de tarjeta
        View.OnClickListener copiarListener = v -> {
            Context context = holder.itemView.getContext();
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("Número de tarjeta", tarjeta.getPan());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Número de tarjeta copiado", Toast.LENGTH_SHORT).show();
            }
        };

        if (holder.btnCopiar != null) {
            holder.btnCopiar.setOnClickListener(copiarListener);
        }
        holder.tvPan.setOnClickListener(copiarListener);

        holder.itemView.setOnClickListener(v -> listener.onTarjetaClick(tarjeta));
    }

    @Override
    public int getItemCount() {
        return tarjetas.size();
    }

    static class TarjetaViewHolder extends RecyclerView.ViewHolder {

        final MaterialCardView card;
        final TextView tvNombre, tvPan, tvSaldo, tvExpiracion;
        final ImageView btnCopiar;

        TarjetaViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_tarjeta);
            tvNombre = itemView.findViewById(R.id.tv_nombre_tarjeta);
            tvPan = itemView.findViewById(R.id.tv_pan_tarjeta);
            tvSaldo = itemView.findViewById(R.id.tv_saldo_tarjeta);
            tvExpiracion = itemView.findViewById(R.id.tv_expiracion_tarjeta);
            btnCopiar = itemView.findViewById(R.id.btn_copiar_pan);
        }
    }
}
