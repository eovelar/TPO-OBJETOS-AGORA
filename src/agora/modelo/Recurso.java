package agora.modelo;

import java.util.Objects;

public class Recurso {

    private int id;
    private String nombre;
    private String tipo;
    private int cantidadTotal;

    public Recurso(int id, String nombre, String tipo, int cantidadTotal) {

        if (id <= 0) {
            throw new IllegalArgumentException("El id debe ser mayor que cero");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }

        if (cantidadTotal <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidadTotal = cantidadTotal;
    }

    public void actualizarDatos(String nombre, String tipo, int cantidadTotal) {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("El tipo es obligatorio");
        }

        if (cantidadTotal <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidadTotal = cantidadTotal;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public int getCantidadTotal() {
        return cantidadTotal;
    }

    @Override
    public String toString() {
        return nombre + " - " + tipo + " (" + cantidadTotal + ")";
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }

        if (!(objeto instanceof Recurso)) {
            return false;
        }

        Recurso otro = (Recurso) objeto;
        return id == otro.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}