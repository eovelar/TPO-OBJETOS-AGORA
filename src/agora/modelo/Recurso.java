package agora.modelo;

import java.util.Objects;

public class Recurso {

    private final int id;
    private String nombre;
    private String tipo;
    private int cantidadTotal;
    private int cantidadFueraServicio;

    // conserva el constructor que ya usa el programa
    public Recurso(
            int id,
            String nombre,
            String tipo,
            int cantidadTotal
    ) {
        this(id, nombre, tipo, cantidadTotal, 0);
    }

    public Recurso(
            int id,
            String nombre,
            String tipo,
            int cantidadTotal,
            int cantidadFueraServicio
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El id debe ser mayor que cero."
            );
        }

        validarDatos(
                nombre,
                tipo,
                cantidadTotal,
                cantidadFueraServicio
        );

        this.id = id;
        this.nombre = nombre.trim();
        this.tipo = tipo.trim();
        this.cantidadTotal = cantidadTotal;
        this.cantidadFueraServicio = cantidadFueraServicio;
    }

    public void actualizarDatos(
            String nombre,
            String tipo,
            int cantidadTotal
    ) {
        actualizarDatos(
                nombre,
                tipo,
                cantidadTotal,
                cantidadFueraServicio
        );
    }

    public void actualizarDatos(
            String nombre,
            String tipo,
            int cantidadTotal,
            int cantidadFueraServicio
    ) {
        // valida todo antes de modificar el recurso
        validarDatos(
                nombre,
                tipo,
                cantidadTotal,
                cantidadFueraServicio
        );

        this.nombre = nombre.trim();
        this.tipo = tipo.trim();
        this.cantidadTotal = cantidadTotal;
        this.cantidadFueraServicio = cantidadFueraServicio;
    }

    public void actualizarCantidadFueraServicio(int cantidad) {
        validarDatos(
                nombre,
                tipo,
                cantidadTotal,
                cantidad
        );

        cantidadFueraServicio = cantidad;
    }

    private void validarDatos(
            String nombre,
            String tipo,
            int cantidadTotal,
            int cantidadFueraServicio
    ) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del recurso es obligatorio."
            );
        }

        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException(
                    "El tipo de recurso es obligatorio."
            );
        }

        if (cantidadTotal <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad total debe ser mayor que cero."
            );
        }

        if (cantidadFueraServicio < 0) {
            throw new IllegalArgumentException(
                    "La cantidad fuera de servicio no puede ser negativa."
            );
        }

        if (cantidadFueraServicio > cantidadTotal) {
            throw new IllegalArgumentException(
                    "La cantidad fuera de servicio no puede superar "
                            + "el total del inventario."
            );
        }
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

    public int getCantidadFueraServicio() {
        return cantidadFueraServicio;
    }

    // no descuenta reservas, solo unidades que no pueden utilizarse
    public int getCantidadOperativa() {
        return cantidadTotal - cantidadFueraServicio;
    }

    @Override
    public String toString() {
        return nombre
                + " - "
                + tipo
                + " ("
                + getCantidadOperativa()
                + " operativos de "
                + cantidadTotal
                + ")";
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