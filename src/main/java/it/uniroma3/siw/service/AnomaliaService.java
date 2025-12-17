package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.repository.AnomaliaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnomaliaService {
    @Autowired
    AnomaliaRepository anomaliaRepository;

    @Transactional
    public void save(Anomalia anomalia) {
        anomaliaRepository.save(anomalia);
    }

    @Transactional
    public Anomalia getById(Long anomaliaId) {
        return anomaliaRepository.findById(anomaliaId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Iterable<Anomalia> getAll() {
        return anomaliaRepository.findAll();
    }

}
