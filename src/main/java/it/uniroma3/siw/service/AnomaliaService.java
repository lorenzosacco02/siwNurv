package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.repository.AnomaliaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnomaliaService {
    @Autowired
    private AnomaliaRepository anomaliaRepository;

    @Autowired
    private TelegramService telegramService; // Il servizio Telegram

    @Transactional
    public void save(Anomalia anomalia) {
        anomaliaRepository.save(anomalia);
    }

    /*
    *Salva l'anomalia inviata dall'IA nel database e valuta se inoltrarla nel canale Telegram
     */
    @Transactional
    public void saveFromAI(Anomalia anomalia, String severitaOriginale){
        //1. Salva l'anomalia nel DB PostgreSQL
        anomaliaRepository.save(anomalia);

        //2. Logica di filtraggio proattivo: inviamo su Telegram solo i report più urgenti
        if("CRITICA".equalsIgnoreCase(severitaOriginale) || "ALTA".equalsIgnoreCase(severitaOriginale)) {
            String tipo = (anomalia.getTipoAnomalia()!=null) ? anomalia.getTipoAnomalia().toString() : "NON SPECIFICATO";
            String dettagli = (anomalia.getDettagliTecnici() != null) ? anomalia.getDettagliTecnici() : "NESSUN DETTAGLIO EXTRA FORNITO.";
            String video = (anomalia.getSorgenteVideoIA() != null) ? anomalia.getSorgenteVideoIA() : "SCONOSCIUTO";

            //Innesca l'invio asincrono
            telegramService.inviaAlertCritico(tipo, severitaOriginale.toUpperCase(),dettagli, video);
        }
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
