package com.example.meubrecho;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppDataBase extends SQLiteOpenHelper {
    public static final String DB_Name = "meubrecho.sqlite";
    public static final int DB_Version = 1;
    public String tabelaprodutos;
    public String tabelaganhos;
    SQLiteDatabase db;

    public AppDataBase(@Nullable Context context) {
        super(context, DB_Name, null, DB_Version);

        db = getReadableDatabase();
        db = getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        tabelaprodutos = "CREATE TABLE \"meusprodutos\" (\n" +
                "\t\"nome\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"valor\"\tREAL NOT NULL,\n" +
                "\t\"preco\"\tREAL NOT NULL,\n" +
                "\t\"reservado\"\tINTEGER,\n" +
                "\t\"reservadopara\"\tTEXT,\n" +
                "\t\"pago\"\tINTEGER,\n" +
                "\t\"dataretirada\"\tTEXT,\n" +
                "\t\"imagem\"\tBLOB\n" +
                ");";
        try{
            db.execSQL(tabelaprodutos);

        }catch(SQLException e){
            Log.e("DB_LOG_PRODUTOS", "onCreate:"+e.getLocalizedMessage());
        }
        tabelaganhos = "CREATE TABLE \"meusganhos\" (\n" +
                "\t\"data\"\tTEXT NOT NULL UNIQUE,\n" +
                "\t\"investimento\"\tREAL NOT NULL,\n" +
                "\t\"renda\"\tREAL NOT NULL\n" +
                ");";
        try{
            db.execSQL(tabelaganhos);

        }catch(SQLException e){
            Log.e("DB_LOG_GANHOS", "onCreate:"+e.getLocalizedMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Cursor getProdutos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM meusprodutos", null);
    }
    public void addProduto(String nome, float valor, float preco, boolean reservado, String reservadoPara, boolean pago, String dataRetirada, byte[] imagem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("valor", valor);
        values.put("preco", preco);
        values.put("reservado", reservado ? 1 : 0);
        values.put("reservadopara", reservadoPara);
        values.put("pago", pago ? 1 : 0);
        values.put("dataretirada", dataRetirada);
        values.put("imagem", imagem);
        db.insert("meusprodutos", null, values);
    }
    public void updateProduto(String nomeAntigo, String nome, float valor, float preco, boolean reservado, String reservadoPara, boolean pago, String dataRetirada, byte[] imagem) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", nome);
        values.put("valor", valor);
        values.put("preco", preco);
        values.put("reservado", reservado ? 1 : 0);
        values.put("reservadopara", reservadoPara);
        values.put("pago", pago ? 1 : 0);
        values.put("dataretirada", dataRetirada);
        values.put("imagem", imagem);
        db.update("meusprodutos", values, "nome = ?", new String[]{nomeAntigo});
    }
    public boolean produtoExiste(String nome) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT 1 FROM meusprodutos WHERE nome = ?", new String[]{nome});
        boolean existe = (cursor.getCount() > 0);
        cursor.close();
        return existe;
    }
    public void deleteProduto(String nome) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("meusprodutos", "nome = ?", new String[]{nome});
    }
    // Para ganhos
    public boolean insertGanho(String data, double investimento, double renda) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("data", data);
        contentValues.put("investimento", investimento);
        contentValues.put("renda", renda);

        long result = db.insert("meusganhos", null, contentValues);
        return result != -1; // Retorna true se a inserção foi bem-sucedida
    }
    // insere os valores do produto vendido nos nossos ganhos
    public Cursor getGanhosByDate(String data) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM meusganhos WHERE data = ?", new String[]{data});
    }

    public void updateGanhos(float valor, float preco) {
        SQLiteDatabase db = this.getWritableDatabase();

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Obter os valores atuais de investimento e renda para a data de hoje
        Cursor cursor = db.rawQuery("SELECT investimento, renda FROM meusganhos WHERE data = ?", new String[]{todayDate});

        float investimentoAtual = 0;
        float rendaAtual = 0;

        if (cursor.moveToFirst()) {
            investimentoAtual = cursor.getFloat(0);
            rendaAtual = cursor.getFloat(1);
        }
        cursor.close();

        // Atualiza os valores somando os novos valores
        ContentValues values = new ContentValues();
        values.put("investimento", investimentoAtual + valor);
        db.update("meusganhos", values, "data = ?", new String[]{todayDate});

        values.clear();
        values.put("renda", rendaAtual + preco);
        db.update("meusganhos", values, "data = ?", new String[]{todayDate});
    }
    //ordena os dados de acordo com a data mais recente no topo
    public Cursor getGanhosOrderedByDate() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM meusganhos ORDER BY data DESC", null);
    }
    // Pega os ganhos da Semana
    public Cursor getGanhosPorSemana(String dataInicio, String dataFim) {
        SQLiteDatabase db = this.getReadableDatabase();

        // A consulta SQL seleciona a soma dos ganhos e lucros entre as datas, sem erros de agrupamento
        String query = "SELECT SUM(investimento) AS totalGanhos, SUM(renda) AS totalPreco " +
                "FROM meusganhos WHERE data BETWEEN date(?) AND date(?) " +
                "ORDER BY data ASC";  // Ordena por data para garantir a ordem correta

        return db.rawQuery(query, new String[]{dataInicio, dataFim});
    }

    // Pega os ganhos do Mês
    public Cursor getGanhosPorMes(String dataInicio, String dataFim) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT SUM(investimento) AS totalGanhos, SUM(renda) AS totalPreco, data " +
                        "FROM meusganhos WHERE data BETWEEN ? AND ? GROUP BY strftime('%Y-%m', data)",
                new String[]{dataInicio, dataFim});
    }
}
