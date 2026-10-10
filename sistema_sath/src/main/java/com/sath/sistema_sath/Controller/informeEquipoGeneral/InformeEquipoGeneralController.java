package com.sath.sistema_sath.Controller.informeEquipoGeneral;


import com.sath.sistema_sath.domain.InformeEquipoGeneral.InformeEquipoGeneralService;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoGeneral.dto.InformeEquipoGeneralResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/informes-equipo-general")
@RequiredArgsConstructor
public class InformeEquipoGeneralController {

    private final InformeEquipoGeneralService service;


    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public InformeEquipoGeneralResponseDto create(@RequestBody InformeEquipoGeneralCreateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public InformeEquipoGeneralResponseDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    @ResponseBody
    public List<InformeEquipoGeneralResponseDto> findAll() {
        return service.findAll();
    }
}
