package com.sath.sistema_sath.Controller.informeEquipoEspecifico;

import com.sath.sistema_sath.domain.InformeEquipoEspecifico.InformeEquipoEspecificoService;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoCreateDto;
import com.sath.sistema_sath.domain.InformeEquipoEspecifico.dto.InformeEquipoEspecificoResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/informes-equipo-especifico")
@RequiredArgsConstructor
public class InformeEquipoEspecificoController {

    private final InformeEquipoEspecificoService service;



    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public InformeEquipoEspecificoResponseDto create(@RequestBody InformeEquipoEspecificoCreateDto dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    @ResponseBody
    public InformeEquipoEspecificoResponseDto findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping
    @ResponseBody
    public List<InformeEquipoEspecificoResponseDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/lista")
    @ResponseBody
    public List<Map<String, Object>> listaInformeEquipoEspecifico() {
        return service.listaInformeEquipoEspecifico();
    }
}