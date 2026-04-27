package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.CatalogoCultivo;
import com.cj.agrotech.service.CultivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cultivos")
@RequiredArgsConstructor
public class CultivoController {
    private final CultivoService cultivoService;

    @GetMapping
    public List<CatalogoCultivo> listarCultivos() { return cultivoService.listarCultivos(); }

    @GetMapping("/{id}")
    public CatalogoCultivo obtenerPorId(@PathVariable UUID id) { return cultivoService.obtenerPorId(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoCultivo registrarCultivo(@RequestBody CatalogoCultivo cultivo) { return cultivoService.registrarCultivo(cultivo); }

    @PutMapping("/{id}")
    public CatalogoCultivo actualizar(@PathVariable UUID id, @RequestBody CatalogoCultivo cultivo) { return cultivoService.actualizar(id, cultivo); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable UUID id) { cultivoService.eliminar(id); }
}
