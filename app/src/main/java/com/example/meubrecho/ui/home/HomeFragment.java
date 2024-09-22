package com.example.meubrecho.ui.home;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meubrecho.AppDataBase;
import com.example.meubrecho.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private GanhosAdapter ganhosAdapter;
    private List<Ganhos> ganhosList = new ArrayList<>();
    private AppDataBase appDataBase;

    private Button buttonDia, buttonSemana, buttonMes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inicializando o RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewGanhos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inicializando o banco de dados
        appDataBase = new AppDataBase(getContext());

        // Pega o valor do dia atual
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date());

        // Verifica se esse dia já foi listado, caso contrário, cria um novo dia na tabela ganhos
        Cursor cursor = appDataBase.getGanhosByDate(currentDate);
        if (cursor != null && cursor.getCount() == 0) {
            // Se não existir um registro para a data atual, cria um novo
            appDataBase.insertGanho(currentDate, 0, 0); // Insere com valores padrão
        }
        // Carregar dados do banco de dados
        loadGanhosFromDatabase();

        // Configurar o Adapter
        ganhosAdapter = new GanhosAdapter(ganhosList);
        recyclerView.setAdapter(ganhosAdapter);

        // Botões
        buttonDia = view.findViewById(R.id.buttonDia);
        buttonSemana = view.findViewById(R.id.buttonSemana);
        buttonMes = view.findViewById(R.id.buttonMes);

        // Listeners de botões
        buttonDia.setOnClickListener(v -> mostrarGanhosDiarios());

        buttonSemana.setOnClickListener(v -> mostrarGanhosSemanais());

        buttonMes.setOnClickListener(v -> mostrarGanhosMensais());



        return view;
    }

    private void loadGanhosFromDatabase() {
        Cursor cursor = appDataBase.getGanhosOrderedByDate();
        ganhosList.clear(); // Limpa a lista antes de adicionar novos dados
        // Verificar se o cursor possui registros
        if (cursor.moveToFirst()) {
            // Verificar se as colunas estão presentes no cursor
            int indexData = cursor.getColumnIndex("data");
            int indexInvestimento = cursor.getColumnIndex("investimento");
            int indexRenda = cursor.getColumnIndex("renda");

            if (indexData != -1 && indexInvestimento != -1 && indexRenda != -1) {
                // Variaveis dos resultados
                do {
                    String data = cursor.getString(indexData);
                    double investimento = cursor.getDouble(indexInvestimento);
                    double renda = cursor.getDouble(indexRenda);

                    // Adicionando o ganho na lista
                    ganhosList.add(new Ganhos(data, investimento, renda));
                } while (cursor.moveToNext());
            }
        }
        cursor.close();
    }

    // Formas de Ordenar Ganhos
    // Método para mostrar ganhos diários
    private void mostrarGanhosDiarios() {
        loadGanhosFromDatabase(); // Carrega os ganhos do dia atual
        ganhosAdapter.notifyDataSetChanged();
    }

    // Método para mostrar ganhos semanais
    private void mostrarGanhosSemanais() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat sdfExibicao = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());  // Formato para exibição

        // Configura o calendário para o domingo da semana atual
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        ganhosList.clear(); // Limpa a lista para exibir novos dados

        // Loop para listar as últimas 32 semanas
        for (int i = 0; i < 32; i++) {

            // Define o domingo (início) da semana
            String dataInicio = sdf.format(calendar.getTime());
            String dataInicioExibicao = sdfExibicao.format(calendar.getTime());  // Formata para exibição

            // Avança para o sábado (fim) da semana
            calendar.add(Calendar.DAY_OF_WEEK, 6);  // Sábado
            String dataFim = sdf.format(calendar.getTime());
            String dataFimExibicao = sdfExibicao.format(calendar.getTime());  // Formata para exibição

            // Título para exibir o intervalo da semana em formato dd/MM/yyyy
            String tituloSemana = "Semana: " + dataInicioExibicao + " até " + dataFimExibicao;

            // Query para pegar os ganhos da semana
            Cursor cursor = appDataBase.getGanhosPorSemana(dataInicio, dataFim);

            // Verifica se há resultados
            if (cursor.moveToFirst()) {
                int indexTotalGanhos = cursor.getColumnIndex("totalGanhos");
                int indexTotalPreco = cursor.getColumnIndex("totalPreco");

                if (indexTotalGanhos != -1 && indexTotalPreco != -1) {
                    float totalGanhos = cursor.getFloat(indexTotalGanhos);
                    float totalPreco = cursor.getFloat(indexTotalPreco);

                    // Adiciona os ganhos da semana à lista com o título da semana
                    ganhosList.add(new Ganhos(tituloSemana, totalGanhos, totalPreco));
                }
            } else {
                // Se não houver ganhos para essa semana, adiciona com 0
                ganhosList.add(new Ganhos(tituloSemana, 0f, 0f));
            }

            cursor.close();

            // Retrocede para o domingo da semana anterior
            calendar.add(Calendar.DAY_OF_WEEK, -13); // Retrocede 1 semana para o próximo loop
        }

        ganhosAdapter.notifyDataSetChanged(); // Atualiza o RecyclerView
    }


    // Método para mostrar ganhos mensais
    private void mostrarGanhosMensais() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM 'de' yyyy", Locale.getDefault()); // Para formatar o mês

        // Configura o calendário para calcular os últimos meses
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        ganhosList.clear(); // Limpa a lista para exibir novos dados

        // Loop para os últimos 24 meses
        for (int i = 0; i < 24; i++) {
            String dataInicio = sdf.format(calendar.getTime()); // Início do mês

            // Vai até o fim do mês atual
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            String dataFim = sdf.format(calendar.getTime());

            // Título para exibir o nome do mês e ano
            String tituloMes = monthFormat.format(calendar.getTime());

            // Converte para ter a primeira letra maiúscula
            tituloMes = tituloMes.substring(0, 1).toUpperCase() + tituloMes.substring(1).toLowerCase();

            Cursor cursor = appDataBase.getGanhosPorMes(dataInicio, dataFim);

            if (cursor.moveToFirst()) {
                int indexTotalGanhos = cursor.getColumnIndex("totalGanhos");
                int indexTotalPreco = cursor.getColumnIndex("totalPreco");

                // Verifica se as colunas estão presentes
                if (indexTotalGanhos != -1 && indexTotalPreco != -1) {
                    float totalGanhos = cursor.getFloat(indexTotalGanhos);
                    float totalPreco = cursor.getFloat(indexTotalPreco);

                    // Adiciona os ganhos mensais à lista
                    ganhosList.add(new Ganhos(tituloMes, totalGanhos, totalPreco));
                }
            } else {
                // Se não houver ganhos para o mês, adiciona com 0
                ganhosList.add(new Ganhos(tituloMes, 0f, 0f));
            }

            cursor.close();

            // Vai para o mês anterior
            calendar.add(Calendar.MONTH, -1);
            calendar.set(Calendar.DAY_OF_MONTH, 1); // Início do mês anterior
        }

        ganhosAdapter.notifyDataSetChanged(); // Atualiza o RecyclerView
    }
}
