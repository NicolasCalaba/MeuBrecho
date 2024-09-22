package com.example.meubrecho.ui.home;

public class Ganhos {
    private String data;
    private double investimento;
    private double renda;

    public Ganhos(String data, double investimento, double renda) {
        this.data = data;
        this.investimento = investimento;
        this.renda = renda;

    }

    public String getData() {
        return data;
    }

    public double getInvestimento() {
        return investimento;
    }

    public double getRenda() {
        return renda;
    }

}

