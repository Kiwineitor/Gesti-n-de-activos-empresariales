package com.sath.sistema_sath.Config.Exception;

/**
 * La captura el GlobalExceptionHandler — ningún Controller necesita try/catch.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
