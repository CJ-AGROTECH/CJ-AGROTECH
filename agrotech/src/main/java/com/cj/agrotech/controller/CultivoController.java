package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.CatalogoCultivo;
import com.cj.agrotech.service.CultivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cultivos")
@RequiredArgsConstructor
public class CultivoController {
    private final CultivoService cultivoService;

    @GetMapping
    public List<CatalogoCultivo> listarCultivos() { return cultivoService.listarCultivos(); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogoCultivo registrarCultivo(@RequestBody CatalogoCultivo cultivo) { return cultivoService.registrarCultivo(cultivo); }
}
