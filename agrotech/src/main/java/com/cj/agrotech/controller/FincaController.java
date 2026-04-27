package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.domain.entity.Usuario;
import com.cj.agrotech.dto.FincaDTO;
import com.cj.agrotech.dto.FincaRequestDTO;
import com.cj.agrotech.service.FincaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fincas")
@RequiredArgsConstructor
public class FincaController {

    private final FincaService fincaService;

    @GetMapping
    public List<FincaDTO> listar() {
        return fincaService.listarTodas().stream()
                .map(f -> new FincaDTO(f.getId(), f.getNombre(), f.getMunicipio(), f.getUsuario().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<FincaDTO> listarPorUsuario(@PathVariable UUID usuarioId) {
        return fincaService.listarPorUsuario(usuarioId).stream()
                .map(f -> new FincaDTO(f.getId(), f.getNombre(), f.getMunicipio(), f.getUsuario().getId()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public FincaDTO obtenerPorId(@PathVariable UUID id) {
        Finca f = fincaService.obtenerPorId(id);
        return new FincaDTO(f.getId(), f.getNombre(), f.getMunicipio(), f.getUsuario().getId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FincaDTO crear(@RequestBody FincaRequestDTO request) {
        Finca finca = new Finca();
        finca.setNombre(request.nombre());
        finca.setMunicipio(request.municipio());
        Usuario usuario = new Usuario();
        usuario.setId(request.usuarioId());
        finca.setUsuario(usuario);
        Finca creada = fincaService.crear(finca);
        return new FincaDTO(creada.getId(), creada.getNombre(), creada.getMunicipio(), creada.getUsuario().getId());
    }

    @PutMapping("/{id}")
    public FincaDTO actualizar(@PathVariable UUID id, @RequestBody FincaRequestDTO request) {
        Finca datos = new Finca();
        datos.setNombre(request.nombre());
        datos.setMunicipio(request.municipio());
        Finca actualizada = fincaService.actualizar(id, datos);
        return new FincaDTO(actualizada.getId(), actualizada.getNombre(), actualizada.getMunicipio(), actualizada.getUsuario().getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) {
        fincaService.eliminar(id);
    }
}
