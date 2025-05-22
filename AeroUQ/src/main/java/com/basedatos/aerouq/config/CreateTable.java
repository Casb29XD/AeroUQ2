    package com.basedatos.aerouq.config;


    import java.sql.Connection;
    import java.sql.PreparedStatement;
    import java.sql.SQLException;
    import java.sql.Statement;
    import java.util.List;

    public class CreateTable {
        public boolean verificarTablaExiste(String tabla) throws SQLException {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, tabla);
                var rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
                return false;
            }
        }


        public void crearTodasLasTablas() throws SQLException {
            // Lista de tablas con el orden correcto
            List<String> tablas = List.of(
                    "Aerolineas",
                    "Aeronaves",
                    "PuertasDeEmbarque",
                    "Vuelos",
                    "Pasajeros",
                    "MantenimientoAeronaves",
                    "ControlDeCargaYLogistica",
                    "UsuariosDelSistema",
                    "Cargos",
                    "Empleados",
                    "Equipajes"
            );
            for (String tabla : tablas) {
                crearTabla(tabla);
            }
        }


        public void crearTabla(String tabla) throws SQLException {
            if (verificarTablaExiste(tabla)) {
                System.out.println("⚠️ La tabla " + tabla + " ya existe.");
                return;
            }
            String sql = switch (tabla) {
                case "Vuelos" -> "CREATE TABLE Vuelos (\n" +
                        "    ID_Vuelo INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    NumeroVuelo NVARCHAR(20) NOT NULL UNIQUE,\n" +
                        "    ID_Aerolinea INT NOT NULL,\n" +
                        "    Origen NVARCHAR(100) NOT NULL,\n" +
                        "    Destino NVARCHAR(100) NOT NULL,\n" +
                        "    FechaHoraSalida DATETIME NOT NULL,\n" +
                        "    FechaHoraLlegada DATETIME NOT NULL,\n" +
                        "    EstadoVuelo NVARCHAR(20) NOT NULL,\n" +
                        "    ID_Puerta INT,\n" +
                        "    CONSTRAINT FK_Vuelos_Aerolineas FOREIGN KEY (ID_Aerolinea)\n" +
                        "        REFERENCES Aerolineas(ID_Aerolinea),\n" +
                        "    CONSTRAINT FK_Vuelos_Puertas FOREIGN KEY (ID_Puerta)\n" +
                        "        REFERENCES PuertasDeEmbarque(ID_Puerta),\n" +
                        "    CONSTRAINT CHK_EstadoVuelo CHECK (EstadoVuelo IN ('En horario', 'Retrasado', 'Cancelado')),\n" +
                        "    CONSTRAINT CHK_FechaLlegada CHECK (FechaHoraLlegada > FechaHoraSalida)\n" +
                        ")";

                case "Pasajeros" -> "CREATE TABLE Pasajeros (\n" +
                        "    ID_Pasajero INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    Nombre NVARCHAR(100) NOT NULL,\n" +
                        "    Apellido NVARCHAR(100) NOT NULL,\n" +
                        "    DocumentoIdentidad NVARCHAR(50) NOT NULL UNIQUE,\n" +
                        "    Nacionalidad NVARCHAR(100),\n" +
                        "    ID_Vuelo INT,\n" +
                        "    CONSTRAINT FK_Pasajeros_Vuelos FOREIGN KEY (ID_Vuelo)\n" +
                        "        REFERENCES Vuelos(ID_Vuelo)\n" +
                        ")";

                case "PuertasDeEmbarque" -> "CREATE TABLE PuertasDeEmbarque (\n" +
                        "    ID_Puerta INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    NumeroPuerta NVARCHAR(10) NOT NULL UNIQUE,\n" +
                        "    Estado NVARCHAR(20) NOT NULL,\n" +
                        "    CONSTRAINT CHK_EstadoPuerta CHECK (Estado IN ('Disponible', 'Ocupada', 'Mantenimiento'))\n" +
                        ")";

                case "Aerolineas" -> "CREATE TABLE Aerolineas (\n" +
                        "    ID_Aerolinea INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    Nombre NVARCHAR(100) NOT NULL UNIQUE,\n" +
                        "    Flota INT CHECK (Flota >= 0),\n" +
                        "    Contacto NVARCHAR(100)\n" +
                        ")";

                case "MantenimientoAeronaves" -> "CREATE TABLE MantenimientoAeronaves (\n" +
                        "    ID_Mantenimiento INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    ID_Aeronave INT NOT NULL,\n" +
                        "    Descripcion NVARCHAR(255) NOT NULL,\n" +
                        "    FechaMantenimiento DATE NOT NULL,\n" +
                        "    Estado NVARCHAR(20) NOT NULL,\n" +
                        "    CONSTRAINT FK_Mantenimiento_Aeronaves FOREIGN KEY (ID_Aeronave)\n" +
                        "        REFERENCES Aeronaves(ID_Aeronave)\n" +
                        ")";

                case "Aeronaves" ->"CREATE TABLE Aeronaves (\n" +
                        "    ID_Aeronave INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    ID_Aerolinea INT NOT NULL,\n" +
                        "    Modelo NVARCHAR(100),\n" +
                        "    Matricula NVARCHAR(50) UNIQUE NOT NULL,\n" +
                        "    CONSTRAINT FK_Aeronaves_Aerolineas FOREIGN KEY (ID_Aerolinea)\n" +
                        "        REFERENCES Aerolineas(ID_Aerolinea)\n" +
                        ")";

                case "Empleados" ->"CREATE TABLE Empleados (\n" +
                        "    ID_Empleado INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    DocumentoIdentidad NVARCHAR(50) NOT NULL UNIQUE,\n" +
                        "    Nombre NVARCHAR(100) NOT NULL,\n" +
                        "    Apellido NVARCHAR(100) NOT NULL,\n" +
                        "    idCargo INT NOT NULL,\n" +
                        "    CONSTRAINT FK_Empleado_Cargo FOREIGN KEY (idCargo) REFERENCES Cargos(idCargo)\n" +
                        ")";

                case "ControlDeCargaYLogistica" ->"CREATE TABLE ControlDeCargaYLogistica (\n" +
                        "    ID_Carga INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    ID_Aerolinea INT NOT NULL,\n" +
                        "    Peso DECIMAL(10,2) CHECK (Peso > 0),\n" +
                        "    TipoDeCarga NVARCHAR(50) NOT NULL,\n" +
                        "    Estado NVARCHAR(20) NOT NULL,\n" +
                        "\n" +
                        "    CONSTRAINT FK_Carga_Aerolinea FOREIGN KEY (ID_Aerolinea)\n" +
                        "        REFERENCES Aerolineas(ID_Aerolinea),\n" +
                        "\n" +
                        "    CONSTRAINT CHK_TipoDeCarga CHECK (TipoDeCarga IN ('Mercancia Peligrosa', 'Animales', 'Cerveza y Buñuelo')),\n" +
                        "    CONSTRAINT CHK_EstadoCarga CHECK (Estado IN ('En espera', 'En tránsito', 'Entregado'))\n" +
                        ");";

                case "UsuariosDelSistema" ->"CREATE TABLE UsuariosDelSistema (\n" +
                        "    ID_Usuario INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    Nombre NVARCHAR(100) NOT NULL,\n" +
                        "    Apellido NVARCHAR(100) NOT NULL,\n" +
                        "    Usuario NVARCHAR(50) NOT NULL UNIQUE,\n" +
                        "    Contraseña NVARCHAR(255) NOT NULL,  -- La contraseña se guardará cifrada.\n" +
                        "    Rol NVARCHAR(20) NOT NULL,\n" +
                        "    CONSTRAINT CHK_RolUsuario CHECK (Rol IN ('Administrador', 'Aerolínea','Cliente'))\n" +
                        ")";

                case "Equipajes" ->"CREATE TABLE Equipajes (\n" +
                        "    ID_Maleta INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    CodigoDeBarras NVARCHAR(50) NOT NULL UNIQUE,\n" +
                        "    Peso DECIMAL(10,2) CHECK (Peso > 0),\n" +
                        "    ID_Vuelo INT NOT NULL,\n" +
                        "    Estado NVARCHAR(20) NOT NULL,\n" +
                        "    ID_Pasajero INT NOT NULL,\n" +
                        "    CONSTRAINT FK_Equipaje_Vuelo FOREIGN KEY (ID_Vuelo)\n" +
                        "        REFERENCES Vuelos(ID_Vuelo),\n" +
                        "    CONSTRAINT FK_Equipaje_Pasajero FOREIGN KEY (ID_Pasajero)\n" +
                        "        REFERENCES Pasajeros(ID_Pasajero),\n" +
                        "    CONSTRAINT CHK_EstadoEquipaje CHECK (Estado IN ('En tránsito', 'Cargada', 'Extraviada'))\n" +
                        ")";
                case "Cargos" -> "CREATE TABLE Cargos (\n" +
                        "    idCargo INT PRIMARY KEY IDENTITY(1,1),\n" +
                        "    nombreCargo NVARCHAR(100) NOT NULL,\n" +
                        "    descripcion NVARCHAR(255),\n" +
                        "    fechaCreacion DATE NOT NULL\n" +
                        ")";

                default -> throw new SQLException("No hay una estructura definida para la tabla " + tabla);
            };
            try (Connection conn = DatabaseConfig.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("Tabla " + tabla + " creada correctamente.");
            }
        }

        public static void main(String[] args) {
            CreateTable createTable = new CreateTable();
            try {
                createTable.crearTodasLasTablas();
            } catch (SQLException e) {
                System.err.println("Error al crear las tablas: " + e.getMessage());
            }
        }
    }