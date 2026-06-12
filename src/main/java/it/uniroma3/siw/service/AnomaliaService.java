package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Acquirente;
import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.model.Tratta;
import it.uniroma3.siw.model.Video;
import it.uniroma3.siw.repository.AcquirenteRepository;
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

    @Autowired
    private TrattaService trattaService;

    @Autowired
    private VideoService videoService;

    @Transactional
    public void save(Anomalia anomalia) {
        anomaliaRepository.save(anomalia);
    }

    /*
    *Salva l'anomalia inviata dall'IA nel database e valuta se inoltrarla nel canale Telegram
     */
    @Transactional
    public void saveFromAI(Anomalia anomalia, String severitaOriginale, String chatIdDalPayload){
        Tratta tratta = null;
        if(anomalia.getSorgenteVideoIA()!=null){
            tratta = trattaService.getByNomeVideo(anomalia.getSorgenteVideoIA());
            if(tratta!=null){
                Video video = videoService.getByNomeInTratta(anomalia.getSorgenteVideoIA(), tratta);
                if(video!=null){
                    anomalia.setVideo(video);
                }
            }
        }

        //2. Salva l'anomalia nel DB PostgreSQL
        anomaliaRepository.save(anomalia);

        //3. Logica di filtraggio proattivo: inviamo su Telegram solo i report più urgenti
        if("CRITICA".equalsIgnoreCase(severitaOriginale) || "ALTA".equalsIgnoreCase(severitaOriginale)) {
            String chatId= null;
            if(tratta!=null && tratta.getTelegramChatId()!=null){
                chatId = tratta.getTelegramChatId();
            }
            //if (anomalia.getAcquirente() != null && anomalia.getAcquirente().getTelegramChatId() != null) {
            if(chatId!=null && !chatId.isEmpty()){

                //String chatId = anomalia.getAcquirente().getTelegramChatId();
                //String tipo = (anomalia.getTipoAnomalia() != null) ? anomalia.getTipoAnomalia().toString() : "NON SPECIFICATO";
                //String dettagli = (anomalia.getDettagliTecnici() != null) ? anomalia.getDettagliTecnici() : "NESSUN DETTAGLIO EXTRA FORNITO.";
                //String video = (anomalia.getSorgenteVideoIA() != null) ? anomalia.getSorgenteVideoIA() : "SCONOSCIUTO";

                //Innesca l'invio asincrono
                telegramService.inviaAlertCritico(chatId, (anomalia.getTipoAnomalia()!=null ? anomalia.getTipoAnomalia().toString() : "Generic"), severitaOriginale.toUpperCase(), anomalia.getDettagliTecnici(), anomalia.getSorgenteVideoIA());
            }
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
