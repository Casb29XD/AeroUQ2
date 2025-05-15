package com.basedatos.aerouq.app;

import com.basedatos.aerouq.model.Cargo;
import com.basedatos.aerouq.repository.DatabaseRepository;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class pruebaCrud {
    public static void main(String[] args) {
//        DatabaseRepository repo = new DatabaseRepository();
//
//        try {
//            String tabla = "cargos";

//            // 1. Insertar cargo
//            Cargo nuevoCargo = new Cargo(1, "Piloto", "jnsdoiawdohi", new Date());
//            Map<String, Object> nuevoCargoMap = new HashMap<>();
//            nuevoCargoMap.put("nombreCargo", nuevoCargo.getNombre());
//            nuevoCargoMap.put("descripcion", "esta es una prueba de lo que se necesita");
//            nuevoCargoMap.put("fechaCreacion", new Date());
//
//            repo.insertar(tabla, nuevoCargoMap);
//
//            // 2. Buscar todos los cargos
//            System.out.println("\n--- Cargos después de insertar ---");
//            List<Map<String, Object>> cargos = repo.buscar(tabla);
//            for (Map<String, Object> c : cargos) {
//                System.out.println(c);
//            }
//
//            // 3. Actualizar cargo
//            Map<String, Object> datosActualizados = new HashMap<>();
//            datosActualizados.put("descripcion", "Descripción actualizada");
//
//            repo.actualizar(tabla, datosActualizados, "idCargo = ?", List.of(1));
//
//            System.out.println("\n--- Cargos después de actualizar ---");
//            cargos = repo.buscar(tabla);
//            for (Map<String, Object> c : cargos) {
//                System.out.println(c);
//            }

            // 4. Eliminar cargo
//            repo.eliminar(tabla, "idCargo = ?", List.of(1));
//
//            System.out.println("\n--- Cargos después de eliminar ---");
//            cargos = repo.buscar(tabla);
//            for (Map<String, Object> c : cargos) {
//                System.out.println(c);
//            }

//        } catch (SQLException e) {
//            System.err.println("Error SQL: " + e.getMessage());
////        }
    }

}
