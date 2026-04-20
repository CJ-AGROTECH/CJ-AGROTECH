package com.cj.agrotech.repository;

import com.cj.agrotech.domain.document.Telemetria;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.UUID;
import java.util.List;

public interface TelemetriaRepository extends MongoRepository<Telemetria, String> {
    List<Telemetria> findByDispositivoIdOrderByTimestampDesc(UUID dispositivoId);
}
