package agora.modelo;

import java.util.Objects;

public class Asistente {

    private int id;
    private String nombreCompleto;
    private String documento;
    private String cargo;
    private String institucion;
    private String email;

    public Asistente(int id, String nombreCompleto, String documento,
                     String cargo, String institucion, String email) {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que cero");
        }

        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }

        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.documento = documento;
        this.cargo = cargo;
        this.institucion = institucion;
        this.email = email;
    }

    public void actualizarDatos(String nombreCompleto, String cargo,
                                String institucion, String email) {

        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        this.nombreCompleto = nombreCompleto;
        this.cargo = cargo;
        this.institucion = institucion;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getDocumento() {
        return documento;
    }

    public String getCargo() {
        return cargo;
    }

    public String getInstitucion() {
        return institucion;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        if (institucion == null || institucion.isBlank()) {
            return nombreCompleto;
        }

        return nombreCompleto + " - " + institucion;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Asistente)) {
            return false;
        }

        Asistente otro = (Asistente) objeto;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}