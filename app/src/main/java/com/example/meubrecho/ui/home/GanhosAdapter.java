package com.example.meubrecho.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meubrecho.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GanhosAdapter extends RecyclerView.Adapter<GanhosAdapter.GanhosViewHolder> {
    private List<Ganhos> ganhosList;

    public GanhosAdapter(List<Ganhos> ganhosList) {
        this.ganhosList = ganhosList;
    }

    @NonNull
    @Override
    public GanhosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ganhos, parent, false);
        return new GanhosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GanhosViewHolder holder, int position) {
        Ganhos ganhos = ganhosList.get(position);
        // Formatar a data para o formato dd/MM/yyyy
        String dataFormatada = formatarData(ganhos.getData());

        holder.textViewData.setText(dataFormatada);
        holder.textViewInvestimento.setText("Investimento: R$ " +  String.format("%.2f", ganhos.getInvestimento()));
        holder.textViewRenda.setText("Renda: R$ " + String.format("%.2f", ganhos.getRenda()));
        double lucro = ganhos.getRenda() - ganhos.getInvestimento();
        //Exibir Lucro ou Prejuízo
        if (lucro > 0){
            //Lucro
            holder.textViewLucro.setText("Lucro: R$ " + String.format("%.2f", lucro));
            holder.textViewLucro.setTextColor(Color.parseColor("#008000"));
        } else if (lucro < 0){
            //Prejuízo
            holder.textViewLucro.setText("Prejuízo: R$ " + String.format("%.2f", Math.abs(lucro)));
            holder.textViewLucro.setTextColor(Color.parseColor("#8B0000"));
        } else {
            //Neutro, caso não haja lucro ou prejuízo, apenas zero
            holder.textViewLucro.setText("Lucro: R$ " + String.format("%.2f", Math.abs(lucro)));
            holder.textViewLucro.setTextColor(Color.parseColor("#FFC000"));
        }
    }

    @Override
    public int getItemCount() {
        return ganhosList.size();
    }

    static class GanhosViewHolder extends RecyclerView.ViewHolder {
        TextView textViewData, textViewInvestimento, textViewRenda, textViewLucro;

        public GanhosViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewData = itemView.findViewById(R.id.textViewData);
            textViewInvestimento = itemView.findViewById(R.id.textViewInvestimento);
            textViewRenda = itemView.findViewById(R.id.textViewRenda);
            textViewLucro = itemView.findViewById(R.id.textViewLucro);
        }
    }
    // Função para formatar a data
    private String formatarData(String dataOriginal) {
        try {
            // Definir o formato original da data (o formato armazenado no banco de dados)
            SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            // Definir o novo formato
            SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            // Converter a data original para o novo formato
            Date data = formatoOriginal.parse(dataOriginal);
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Se der erro na conversão, retornar a data original
        return dataOriginal;
    }
}

