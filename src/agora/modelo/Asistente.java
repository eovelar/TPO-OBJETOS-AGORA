package agora.modelo;

import java.util.Objects;

public class Asistente {

    private final int id;
    private String nombreCompleto;
    private final String documento;
    private String cargo;
    private String institucion;
    private String email;

    public Asistente(
            int id,
            String nombreCompleto,
            String documento,
            String cargo,
            String institucion,
            String email
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El id debe ser mayor que cero."
            );
        }

        String nombreValidado = validarNombre(nombreCompleto);
        String documentoValidado = validarDocumento(documento);
        String emailValidado = validarEmail(email);

        this.id = id;
        this.nombreCompleto = nombreValidado;
        this.documento = documentoValidado;
        this.cargo = limpiarTexto(cargo);
        this.institucion = limpiarTexto(institucion);
        this.email = emailValidado;
    }

    public void actualizarDatos(
            String nombreCompleto,
            String cargo,
            String institucion,
            String email
    ) {
        // valida todo antes de modificar los datos
        String nombreValidado = validarNombre(nombreCompleto);
        String emailValidado = validarEmail(email);

        this.nombreCompleto = nombreValidado;
        this.cargo = limpiarTexto(cargo);
        this.institucion = limpiarTexto(institucion);
        this.email = emailValidado;
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre completo es obligatorio."
            );
        }

        String nombreLimpio = nombre.trim()
                .replaceAll("\\s+", " ");

        if (nombreLimpio.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre no puede superar los 100 caracteres."
            );
        }

        // admite letras, tildes y separadores habituales de nombres
        if (!nombreLimpio.matches(
                "[\\p{L}\\p{M}]+(?:[ '\\-’][\\p{L}\\p{M}]+)*"
        )) {
            throw new IllegalArgumentException(
                    "El nombre solo puede contener letras, espacios, "
                            + "guiones y apóstrofes."
            );
        }

        return nombreLimpio;
    }

    private String validarDocumento(String documento) {
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException(
                    "El documento es obligatorio."
            );
        }

        String documentoLimpio = documento.trim();

        // se conserva como texto porque no se usa para hacer cálculos
        if (!documentoLimpio.matches("[0-9]+")) {
            throw new IllegalArgumentException(
                    "El documento solo puede contener números, "
                            + "sin puntos, espacios ni símbolos."
            );
        }

        return documentoLimpio;
    }

    private String validarEmail(String email) {
        String emailLimpio = limpiarTexto(email);

        // el correo es opcional
        if (emailLimpio.isEmpty()) {
            return "";
        }

        if (emailLimpio.length() > 254) {
            throw new IllegalArgumentException(
                    "El correo electrónico es demasiado largo."
            );
        }

        // comprueba un formato básico, no la existencia de la cuenta
        String formato =
                "[A-Za-z0-9_%+\\-]+"
                        + "(?:\\.[A-Za-z0-9_%+\\-]+)*"
                        + "@"
                        + "[A-Za-z0-9]"
                        + "(?:[A-Za-z0-9\\-]*[A-Za-z0-9])?"
                        + "(?:\\.[A-Za-z0-9]"
                        + "(?:[A-Za-z0-9\\-]*[A-Za-z0-9])?)+";

        if (!emailLimpio.matches(formato)) {
            throw new IllegalArgumentException(
                    "Ingresá un correo válido, "
                            + "por ejemplo: nombre@dominio.com"
            );
        }

        return emailLimpio;
    }

    private String limpiarTexto(String texto) {
        return texto == null ? "" : texto.trim();
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
        if (institucion.isBlank()) {
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