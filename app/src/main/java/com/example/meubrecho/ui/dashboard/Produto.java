package com.example.meubrecho.ui.dashboard;

public class Produto {
    private String nome;
    private float valor;
    private float preco;
    private boolean reservado;
    private String reservadoPara;
    private boolean pago;
    private String dataRetirada;
    private byte[] imagem;

    public Produto(String nome, float valor, float preco, boolean reservado, String reservadoPara, boolean pago, String dataRetirada, byte[] imagem) {
        this.nome = nome;
        this.valor = valor;
        this.preco = preco;
        this.reservado = reservado;
        this.reservadoPara = reservadoPara;
        this.pago = pago;
        this.dataRetirada = dataRetirada;
        this.imagem = imagem;
    }

    // Getters para os atributos
    public String getNome() {
        return nome;
    }

    public float getValor() {
        return valor;
    }

    public float getPreco() {
        return preco;
    }

    public boolean isReservado() {
        return reservado;
    }

    public String getReservadoPara() {
        return reservadoPara;
    }

    public boolean isPago() {
        return pago;
    }

    public String getDataRetirada() {
        return dataRetirada;
    }

    public byte[] getImagem() {
        return imagem;
    }
}
