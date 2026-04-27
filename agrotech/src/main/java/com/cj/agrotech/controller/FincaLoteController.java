package com.cj.agrotech.controller;

import com.cj.agrotech.domain.entity.Finca;
import com.cj.agrotech.service.FincaLoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fincas-manage")
@RequiredArgsConstructor
public class FincaLoteController {
    private final FincaLoteService fincaLoteService;

    @GetMapping("/usuario/{usuarioId}")
    public List<Finca> listarPorUsuario(@PathVariable UUID usuarioId) {
        return fincaLoteService.obtenerFincasPorUsuario(usuarioId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Finca crearFinca(@RequestBody Finca finca) {
        return fincaLoteService.registrarFinca(finca);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarFinca(@PathVariable UUID id) {
        fincaLoteService.eliminarFinca(id);
    }
}