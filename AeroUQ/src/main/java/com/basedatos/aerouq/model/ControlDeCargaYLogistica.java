package com.basedatos.aerouq.model;

public class ControlDeCargaYLogistica {
    private int idCarga;
    private int idAerolinea;
    private double peso;
    private String tipoDeCarga;
    private String estado;

    // Constructor
    public ControlDeCargaYLogistica(int idCarga, int idAerolinea, double peso, String tipoDeCarga, String estado) {
        this.idCarga = idCarga;
        this.idAerolinea = idAerolinea;
        this.peso = peso;
        this.tipoDeCarga = tipoDeCarga;
        this.estado = estado;
    }

    // Getters y setters
    public int getIdCarga() {
        return idCarga;
    }

    public void setIdCarga(int idCarga) {
        this.idCarga = idCarga;
    }

    public int getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(int idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public String getTipoDeCarga() {
        return tipoDeCarga;
    }

    public void setTipoDeCarga(String tipoDeCarga) {
        this.tipoDeCarga = tipoDeCarga;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
