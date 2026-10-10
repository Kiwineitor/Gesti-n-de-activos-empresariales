package com.sath.sistema_sath.Config.Exception;

/**
 * La captura el GlobalExceptionHandler — ningún Controller necesita try/catch.
 */
public class ReglaDeNegocioException extends RuntimeException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
