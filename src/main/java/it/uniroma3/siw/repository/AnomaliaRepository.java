package it.uniroma3.siw.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.model.TipoDiAnomalia;
import it.uniroma3.siw.model.Tratta;

public interface AnomaliaRepository extends CrudRepository<Anomalia, Long> {
    List<Anomalia> findByTipoAnomalia(TipoDiAnomalia tipo);
    List<Anomalia> findByTratta(Tratta tratta);
    List<Anomalia> findByTrattaOrderByIdDesc(Tratta tratta);        //ordinate, mostra prima le piu recenti
}
