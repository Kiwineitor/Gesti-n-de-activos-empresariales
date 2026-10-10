package com.sath.sistema_sath.domain.solicitud.manual;

import java.time.LocalDate;
import java.util.List;

public record SolicitudManualCreateDto(
    FuncionarioManualDto funcionario,
    String descripcion,
    List<ActivoManualDto> equipos,
    LocalDate fechaSol
) {}
