package com.cj.agrotech.config;

import com.cj.agrotech.domain.entity.*;
import com.cj.agrotech.domain.enums.EstadoDispositivo;
import com.cj.agrotech.domain.enums.Rol;
import com.cj.agrotech.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("docker")
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;
    private final FincaRepository fincaRepository;
    private final CatalogoCultivoRepository catalogoCultivoRepository;
    private final LoteRepository loteRepository;
    private final DispositivoRepository dispositivoRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        try {
            // Crear usuario demo si no existe
            Optional<Usuario> maybe = usuarioRepository.findByEmail("demo@agrotech.local");
            Usuario usuario = maybe.orElseGet(() -> {
                Usuario u = new Usuario();
                u.setNombre("Demo");
                u.setEmail("demo@agrotech.local");
                u.setRol(Rol.AGRICULTOR);
                u.setPassword(passwordEncoder.encode("demo"));
                return usuarioRepository.save(u);
            });

            // Crear finca demo
            Finca finca = fincaRepository.findByNombre("Finca Demo").orElseGet(() -> {
                Finca f = new Finca();
                f.setNombre("Finca Demo");
                f.setMunicipio("DemoVille");
                f.setLatitud(4.711);
                f.setLongitud(-74.0721);
                f.setUsuario(usuario);
                return fincaRepository.save(f);
            });

            // Crear cultivo demo
            CatalogoCultivo cultivo = catalogoCultivoRepository.findAll().stream()
                    .filter(c -> "Cultivo Demo".equals(c.getNombre()))
                    .findFirst()
                    .orElseGet(() -> {
                        CatalogoCultivo c = new CatalogoCultivo();
                        c.setNombre("Cultivo Demo");
                        c.setVariedad("Demo");
                        c.setDescripcion("Cultivo de ejemplo para demo");
                        c.setDiasCrecimiento(120);
                        c.setTempOptimaMin(18.0);
                        c.setTempOptimaMax(30.0);
                        c.setHumedadOptimaMin(50.0);
                        c.setHumedadOptimaMax(80.0);
                        return catalogoCultivoRepository.save(c);
                    });

            // Crear lote demo
            Lote lote = loteRepository.findByNombre("Lote Demo").orElseGet(() -> {
                Lote l = new Lote();
                l.setNombre("Lote Demo");
                l.setAreaHectareas(1.0);
                l.setFinca(finca);
                l.setCultivo(cultivo);
                return loteRepository.save(l);
            });

            // Crear dispositivo demo
            dispositivoRepository.findByMacAddress("DE:MO:00:00:00:01").orElseGet(() -> {
                Dispositivo d = new Dispositivo();
                d.setMacAddress("DE:MO:00:00:00:01");
                d.setNombre("ESP32-Simulador");
                d.setEstado(EstadoDispositivo.ACTIVO);
                d.setLote(lote);
                return dispositivoRepository.save(d);
            });

            log.info("DataInitializer: demo data ensured (usuario={}, finca={}, lote={})", usuario.getEmail(), finca.getNombre(), lote.getNombre());
        } catch (Exception ex) {
            log.warn("DataInitializer failed: {}", ex.getMessage(), ex);
        }
    }
}
