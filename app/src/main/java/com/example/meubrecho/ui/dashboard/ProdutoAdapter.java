package com.example.meubrecho.ui.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meubrecho.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private ArrayList<Produto> produtosList;

    public ProdutoAdapter(ArrayList<Produto> produtosList) {
        this.produtosList = produtosList;
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.produto_item, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtosList.get(position);

        // formatar valor para simular dinheiro
        String valorFormatado = "Valor Investido R$ " + String.format("%.2f", produto.getValor());
        String precoFormatado = "A Venda Por R$ " + String.format("%.2f", produto.getPreco());

        // formatar data
        String dataFormatada = formatarData(produto.getDataRetirada());

        holder.nomeTextView.setText(produto.getNome());
        holder.valorTextView.setText(valorFormatado);
        holder.precoTextView.setText(precoFormatado);
        holder.reservadoTextView.setText(produto.isReservado() ? "Está Reservado" : "Sem Reserva");
        holder.reservadoParaTextView.setText(produto.getReservadoPara());
        holder.pagoTextView.setText(produto.isPago() ? "Pago" : "Ainda não pago");
        holder.dataRetiradaTextView.setText(dataFormatada);

        // Adiciona a lógica para exibir a imagem, se estiver presente
        if (produto.getImagem() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(produto.getImagem(), 0, produto.getImagem().length);
            holder.imagemImageView.setImageBitmap(bitmap);
        } else {
            holder.imagemImageView.setImageResource(R.drawable.null_image_holder);  // Imagem padrão
        }
        // Adiciona um clique longo para editar o item
        holder.itemView.setOnLongClickListener(v -> {
            // Passa o produto para a função que abre o diálogo para edição
            if (listener != null) {
                listener.onProdutoLongClick(produto);
            }
            return true;
        });
    }

    // Adiciona uma interface para lidar com o clique longo
    public interface OnProdutoLongClickListener {
        void onProdutoLongClick(Produto produto);
    }

    private OnProdutoLongClickListener listener;

    public void setOnProdutoLongClickListener(OnProdutoLongClickListener listener) {
        this.listener = listener;
    }

    // Método para formatar a data
    private String formatarData(String dataOriginal) {
        // Defina o formato da data original (formato "yyyy-MM-dd")
        SimpleDateFormat formatoOriginal = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Defina o formato desejado ("dd/MM/yyyy")
        SimpleDateFormat formatoDesejado = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            // Converte a data original para o formato desejado
            Date data = formatoOriginal.parse(dataOriginal);
            return formatoDesejado.format(data);
        } catch (ParseException e) {
            // Se ocorrer um erro de conversão, retorna a data original
            return dataOriginal;
        }
    }

    @Override
    public int getItemCount() {
        return produtosList.size();
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView nomeTextView, valorTextView, precoTextView, reservadoTextView, reservadoParaTextView, pagoTextView, dataRetiradaTextView;
        ImageView imagemImageView;
        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            nomeTextView = itemView.findViewById(R.id.textViewData);
            valorTextView = itemView.findViewById(R.id.valor);
            precoTextView = itemView.findViewById(R.id.preco);
            reservadoTextView = itemView.findViewById(R.id.reservado);
            reservadoParaTextView = itemView.findViewById(R.id.textViewInvestimento);
            pagoTextView = itemView.findViewById(R.id.textViewRenda);
            dataRetiradaTextView = itemView.findViewById(R.id.textViewLucro);
            imagemImageView = itemView.findViewById(R.id.imageViewProduto);
        }
    }
}