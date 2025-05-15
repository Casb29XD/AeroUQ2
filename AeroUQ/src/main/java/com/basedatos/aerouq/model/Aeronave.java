package com.basedatos.aerouq.model;

public class Aeronave {
    private int idAeronave;
    private int idAerolinea;
    private String modelo;
    private String matricula;

    // Constructor
    public Aeronave(int idAeronave, int idAerolinea, String modelo, String matricula) {
        this.idAeronave = idAeronave;
        this.idAerolinea = idAerolinea;
        this.modelo = modelo;
        this.matricula = matricula;
    }

    // Getters y setters
    public int getIdAeronave() {
        return idAeronave;
    }

    public void setIdAeronave(int idAeronave) {
        this.idAeronave = idAeronave;
    }

    public int getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(int idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }
}