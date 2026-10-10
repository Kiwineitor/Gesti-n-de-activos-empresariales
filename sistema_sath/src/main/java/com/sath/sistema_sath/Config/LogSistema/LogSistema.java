package com.sath.sistema_sath.Config.LogSistema;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LogSistema {
    private Long          id;
    private String        nivel;
    private String        mensaje;
    private String        logger;
    private String        usuario;
    private String        stackTrace;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fecha;
}
