package it.uniroma3.siw.controller;

import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.model.TipoDiAnomalia;
import it.uniroma3.siw.model.Video;
import it.uniroma3.siw.service.AnomaliaService;
import it.uniroma3.siw.service.VideoService;
import it.uniroma3.siw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertRestController {

    @Autowired
    private AnomaliaService anomaliaService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private UserService userService;

    // Mappa Stringa Gravità (Python) su Intero Gravità (DB: 1-5)
    private static final Map<String, Integer> SEVERITY_MAP = new HashMap<>();
    static {
        SEVERITY_MAP.put("CRITICA", 5);
        SEVERITY_MAP.put("ALTA", 4);
        SEVERITY_MAP.put("MEDIO", 3);
        SEVERITY_MAP.put("BASSO", 2);
        SEVERITY_MAP.put("UNKNOWN", 1);
    }

    // Mappa Label Stringa (Python) su Enum Java in modo robusto
    private TipoDiAnomalia mapLabelToEnum(String label) {
        String normalizedLabel = label.toUpperCase().replace(" ", "_");

        try {
            return TipoDiAnomalia.valueOf(normalizedLabel);
        } catch (IllegalArgumentException e) {
            System.err.println("WARN: Tipo anomalia non trovato per label: " + label);
            return TipoDiAnomalia.ALTRO;
        }
    }


    @PostMapping
    public ResponseEntity<String> riceviAllerta(@RequestBody AlertDtoController payload) {
        try {
            if (payload == null || payload.label == null) {
                return ResponseEntity.badRequest().body("Payload alert mancante o incompleto.");
            }

            // Mappatura Video (Logica semplificata: cerca video per ID, se non fornito, video rimane null)
            Video video = null;
            if (payload.videoId != null) {
                video = videoService.getById(payload.videoId);
            }

            // Creazione Anomalia
            Anomalia a = new Anomalia();

            // 1. Mappatura Tipo e Descrizione
            TipoDiAnomalia tipoAnomalia = mapLabelToEnum(payload.label);
            a.setTipoAnomalia(tipoAnomalia);
            a.setDescrizione("IA: " + payload.label);

            // 2. Mappatura Gravità (Stringa e Intero)
            String severity = payload.severity != null ? payload.severity.toUpperCase() : "UNKNOWN";
            a.setGravitaString(severity);
            a.setGravita(SEVERITY_MAP.getOrDefault(severity, 1));

            // 3. Mappatura Dettagli Tecnici
            a.setDettagliTecnici(payload.details);

            // 4. Mappatura Campi IA
            if (payload.conf != null) a.setConfidenza(payload.conf.floatValue());
            if (payload.frame_idx != null) a.setFrameIndex(payload.frame_idx);
            if (payload.time_s != null) a.setTimeSeconds(payload.time_s.floatValue());
            a.setSorgenteVideoIA(payload.source_video);

            // 5. Mappatura Bounding Box
            if (payload.bbox != null) {
                a.setX(payload.bbox.x);
                a.setY(payload.bbox.y);
                a.setW(payload.bbox.w);
                a.setH(payload.bbox.h);
                // Aggiungi area alla descrizione per visibilità
                if (payload.area != null) a.setDescrizione(a.getDescrizione() + " | Area:" + payload.area);
            }

            // 6. Assegnazione Entità
            a.setVideo(video);

            anomaliaService.saveFromAI(a, severity);

            return ResponseEntity.status(HttpStatus.CREATED).body("Anomalia salvata (ed eventualmente mandata su Telegram) correttamente: " + tipoAnomalia.name());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errore salvataggio anomalia: " + e.getMessage());
        }
    }
}