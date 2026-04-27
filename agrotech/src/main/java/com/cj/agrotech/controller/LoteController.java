package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Lote;
import com.cj.agrotech.dto.LoteDTO;
import com.cj.agrotech.dto.LoteRequestDTO;
import com.cj.agrotech.service.LoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lotes")
@RequiredArgsConstructor
public class LoteController {

    private final LoteService loteService;

    @GetMapping
    public List<LoteDTO> listar() {
        return loteService.listarTodos().stream()
                .map(l -> new LoteDTO(l.getId(), l.getNombre(), l.getAreaHectareas(), l.getFinca().getId(), l.getCultivo().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/finca/{fincaId}")
    public List<LoteDTO> listarPorFinca(@PathVariable UUID fincaId) {
        return loteService.listarPorFinca(fincaId).stream()
                .map(l -> new LoteDTO(l.getId(), l.getNombre(), l.getAreaHectareas(), l.getFinca().getId(), l.getCultivo().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public LoteDTO obtenerPorId(@PathVariable UUID id) {
        Lote l = loteService.obtenerPorId(id);
        return new LoteDTO(l.getId(), l.getNombre(), l.getAreaHectareas(), l.getFinca().getId(), l.getCultivo().getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoteDTO crear(@RequestBody LoteRequestDTO request) {
        Lote lote = new Lote();
        lote.setNombre(request.nombre());
        lote.setAreaHectareas(request.areaHectareas());
        // Set finca and cultivo
        Lote creado = loteService.crear(lote);
        return new LoteDTO(creado.getId(), creado.getNombre(), creado.getAreaHectareas(), creado.getFinca().getId(), creado.getCultivo().getId());
    }

    @PutMapping("/{id}")
    public LoteDTO actualizar(@PathVariable UUID id, @RequestBody LoteRequestDTO request) {
        Lote datos = new Lote();
        datos.setNombre(request.nombre());
        datos.setAreaHectareas(request.areaHectareas());
        Lote actualizado = loteService.actualizar(id, datos);
        return new LoteDTO(actualizado.getId(), actualizado.getNombre(), actualizado.getAreaHectareas(), actualizado.getFinca().getId(), actualizado.getCultivo().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        loteService.eliminar(id);
    }
}
