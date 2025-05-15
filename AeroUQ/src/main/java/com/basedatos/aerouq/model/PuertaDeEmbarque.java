package com.basedatos.aerouq.model;

public class PuertaDeEmbarque {
    private int idPuerta;
    private String numeroPuerta;
    private String estado;

    // Constructor
    public PuertaDeEmbarque(int idPuerta, String numeroPuerta, String estado) {
        this.idPuerta = idPuerta;
        this.numeroPuerta = numeroPuerta;
        this.estado = estado;
    }

    // Getters y setters
    public int getIdPuerta() {
        return idPuerta;
    }

    public void setIdPuerta(int idPuerta) {
        this.idPuerta = idPuerta;
    }

    public String getNumeroPuerta() {
        return numeroPuerta;
    }

    public void setNumeroPuerta(String numeroPuerta) {
        this.numeroPuerta = numeroPuerta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
