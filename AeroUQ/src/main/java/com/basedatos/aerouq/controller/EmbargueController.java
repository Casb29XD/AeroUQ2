package com.basedatos.aerouq.controller;

import com.basedatos.aerouq.model.PuertaDeEmbarque;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class EmbargueController {

    @FXML private TextField txtBuscar;
    @FXML private TextField txtNoPuerta;
    @FXML private TextField txtEstado;

    @FXML private TableView<PuertaDeEmbarque> tableEmbargue;
    @FXML private TableColumn<PuertaDeEmbarque, Integer> colIdPuerta;
    @FXML private TableColumn<PuertaDeEmbarque, String> colNoPuerta;
    @FXML private TableColumn<PuertaDeEmbarque, String> colEstado;

    private final ObservableList<PuertaDeEmbarque> listaPuertas = FXCollections.observableArrayList();
    private int idCounter = 1;

    @FXML
    public void initialize() {
        colIdPuerta.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNoPuerta.setCellValueFactory(new PropertyValueFactory<>("numero"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tableEmbargue.setItems(listaPuertas);
    }

    @FXML
    private void handleAddPuerta() {
        String numero = txtNoPuerta.getText();
        String estado = txtEstado.getText();

        if (numero.isEmpty() || estado.isEmpty()) {
            showAlert("Campos vacíos", "Por favor, completa todos los campos.");
            return;
        }

        PuertaDeEmbarque nueva = new PuertaDeEmbarque(idCounter++, numero, estado);
        listaPuertas.add(nueva);
        limpiarCampos();
    }

    @FXML
    private void handleEditPuerta() {
        PuertaDeEmbarque seleccionada = tableEmbargue.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            showAlert("Sin selección", "Selecciona una puerta para editar.");
            return;
        }

        seleccionada.setNumeroPuerta(txtNoPuerta.getText());
        seleccionada.setEstado(txtEstado.getText());
        tableEmbargue.refresh();
        limpiarCampos();
    }

    @FXML
    private void handleDeletePuerta() {
        PuertaDeEmbarque seleccionada = tableEmbargue.getSelectionModel().getSelectedItem();
        if (seleccionada != null) {
            listaPuertas.remove(seleccionada);
            limpiarCampos();
        } else {
            showAlert("Sin selección", "Selecciona una puerta para eliminar.");
        }
    }

    @FXML
    private void handleBuscarPuerta() {
        String filtro = txtBuscar.getText().trim().toLowerCase();
        if (filtro.isEmpty()) {
            tableEmbargue.setItems(listaPuertas);
            return;
        }

        ObservableList<PuertaDeEmbarque> filtradas = FXCollections.observableArrayList();
        for (PuertaDeEmbarque puerta : listaPuertas) {
            if (String.valueOf(puerta.getIdPuerta()).equals(filtro) || puerta.getNumeroPuerta().toLowerCase().contains(filtro)) {
                filtradas.add(puerta);
            }
        }
        tableEmbargue.setItems(filtradas);
    }

    @FXML
    private void handleLimpiarBusqueda() {
        txtBuscar.clear();
        tableEmbargue.setItems(listaPuertas);
    }

    private void limpiarCampos() {
        txtNoPuerta.clear();
        txtEstado.clear();
    }

    private void showAlert(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}